package com.lexmark.dev.FoIP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Pete on 3/27/2017.
 */
public class NetworkDevice {
    String                _IPAddr;
    public ArrayList<String> Cookies;
    boolean               _useHttps = false;
    private String        _CsrfToken = "";
    private boolean       _validTransport = false;

    NetworkDevice(String IPAddr) {
        _IPAddr = IPAddr;
        if (_IPAddr.equals("0.0.0.0")) {
            return;
        }
        Cookies = new ArrayList<>();
        System.out.println("Trying http");
        if (TryWebConnection(false)) {
            System.out.println("http ok");
            setUseHttps(false);
            setValidTransport(true);
        }
        else {
            System.out.println("Trying https");
            if (TryWebConnection(true)) {
                System.out.println("https ok");
                setUseHttps(true);
                setValidTransport(true);
            }
            else {
                System.out.println("Both http and https failed.");
                setValidTransport(false);
            }
        }
    }

    public String IPAddr() {
        return _IPAddr;
    }

    public boolean getUseHttps() {
        return _useHttps;
    }

    public void setUseHttps(boolean b) {
        _useHttps = b;
    }

    public void setCsrfToken(String token) {
        _CsrfToken = token;
    }

    String getCsrfToken() {
        return _CsrfToken;
    }

    public boolean getValidTransport() { return _validTransport; }

    private void setValidTransport(boolean value) { _validTransport = value; }

    private boolean TryWebConnection(boolean https) {
        boolean rc = false;
        WebConnection connection = null;

        if (this.IPAddr().isEmpty()) {
            return false;
        }
        try {
            connection = new WebConnection(this,"/",https);
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            rd.close();
            rc = true;
        } catch (Exception e) {
            System.out.println("TryWebConnection failed with https="+https);
            System.out.println(""+e);
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;
    }


    boolean IsUp() {
        boolean rc = true;
        WebConnection connection = null;

        try {
            connection = new WebConnection(this,"/",this.getUseHttps(),1 * 1000);
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            rd.close();

        } catch (Exception e) {
            rc = false;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;
    }
}

