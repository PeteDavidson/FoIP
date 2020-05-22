package com.lexmark.dev.FoIP;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by Pete on 2/23/2017.
 */
public class WebConnection {
    private static final int CONNECTION_TIMEOUT = 120 * 1000;  // 20 Seconds
    public static final int READ_TIMEOUT        = 120 * 1000;  // 20 Seconds
    private boolean useHttps;
    private HttpURLConnection conn;
    private HttpsURLConnection sconn;

    private void MakeWebConnection(NetworkDevice netDevice, String urlString, boolean tryHttps, int ConnTimeout, int ReadTimeout) throws IOException {
        URL url;

        useHttps = tryHttps;
        if (ConnTimeout == 0)
            ConnTimeout = CONNECTION_TIMEOUT;
        if (ReadTimeout == 0)
            ReadTimeout = READ_TIMEOUT;

        StringBuilder Cookies = new StringBuilder();
        for (int i=0; i<netDevice.Cookies.size(); i++) {
            if (i == 0)
                Cookies = new StringBuilder(netDevice.Cookies.get(i));
            else
                Cookies.append("; ").append(netDevice.Cookies.get(i));
        }

        if (useHttps) {
            url = new URL("https://"+netDevice.IPAddr()+urlString);
            sconn = (HttpsURLConnection)url.openConnection();
            sconn.setHostnameVerifier((hostname, session) -> true);
            sconn.setConnectTimeout(ConnTimeout);
            sconn.setReadTimeout(ReadTimeout);
            if (!netDevice.getCsrfToken().isEmpty()) {
                sconn.setRequestProperty("X-Csrf-Token", netDevice.getCsrfToken());
            }
            sconn.setRequestProperty("Cookie", Cookies.toString());
        }
        else {
            url = new URL("http://"+netDevice.IPAddr()+urlString);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(ConnTimeout);
            conn.setReadTimeout(ReadTimeout);
            if (!netDevice.getCsrfToken().isEmpty()) {
                conn.setRequestProperty("X-Csrf-Token", netDevice.getCsrfToken());
            }
            conn.setRequestProperty("Cookie", Cookies.toString());
        }
    }

    WebConnection(NetworkDevice netDevice, String urlString, boolean useHttps) throws IOException {
        MakeWebConnection(netDevice,urlString,useHttps,0,0);
    }


    public WebConnection(NetworkDevice netDevice, String urlString) throws IOException {
        MakeWebConnection(netDevice,urlString,netDevice.getUseHttps(),0,0);
    }

    public WebConnection(NetworkDevice netDevice, String urlString, boolean useHttps, int ConnTimeout) throws IOException {
        MakeWebConnection(netDevice,urlString,useHttps,ConnTimeout,0);
    }

    public void BasicGetSetup() throws ProtocolException {
        if (useHttps) {
            sconn.setRequestMethod("GET");
            sconn.setDoInput(true);
        }
        else {
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
        }
    }

    public InputStream getInputStream() throws IOException {
        if (useHttps) {
            return sconn.getInputStream();
        }
        else {
            return conn.getInputStream();
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (useHttps) {
            return sconn.getOutputStream();
        }
        else {
            return conn.getOutputStream();
        }
    }

    public void setRequestProperty(String key, String val) {
        if (useHttps) {
            sconn.setRequestProperty(key,val);
        }
        else {
            conn.setRequestProperty(key,val);
        }
    }

    public void setDoInput(boolean b) {
        if (useHttps) {
            sconn.setDoInput(b);
        }
        else {
            conn.setDoInput(b);
        }
    }

    public void setDoOutput(boolean b) {
        if (useHttps) {
            sconn.setDoOutput(b);
        }
        else {
            conn.setDoOutput(b);
        }
    }

    public void setRequestMethod(String val) throws ProtocolException {
        if (useHttps) {
            sconn.setRequestMethod(val);
        }
        else {
            conn.setRequestMethod(val);
        }
    }

    public void setReadTimeout(int val) {
        if (useHttps) {
            sconn.setReadTimeout(val);
        }
        else {
            conn.setReadTimeout(val);
        }
    }

    int getResponseCode() throws IOException {
        if (useHttps) {
            return sconn.getResponseCode();
        }
        else {
            return conn.getResponseCode();
        }
    }

    void setUseCaches(boolean b) {
        if (useHttps) {
            sconn.setUseCaches(b);
        }
        else {
            conn.setUseCaches(b);
        }
    }

    public void disconnect() {
        if (useHttps) {
            sconn.disconnect();
        }
        else {
            conn.disconnect();
        }
    }
}
