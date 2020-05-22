package com.lexmark.dev.FoIP;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pete on 2/24/2017.
 */
public class MFPSecurity {
    private MFP _mfp;

    public MFPSecurity(MFP mfp) {
        _mfp = mfp;
    }

    public int LogIn() {
        int rc;
        rc = LogInToMFPClassic(_mfp, _mfp.getUser(), _mfp.getPassword() );
        if (rc != 0)
            return LogInToMFPMoja(_mfp, _mfp.getUser(), _mfp.getPassword());
        else
            return rc;
    }

    private int LogInToMFPMoja(MFP mfp, String User, String Password) {
        int rc = -1;
        String line;
        String sessionId="";
        String sessionKey="";
        String sessionName="";
        WebConnection connection = null;
        if (Password.isEmpty() && User.isEmpty())
            return 0;
        //if (mfp.isEmpty()) {
        //   WriteToResultArea("Must specify MFP FQDN or IP Address.");
        //  return -1;
        //}
        //WriteToResultAreaNoCRLF("Checking MFP Family...");
        //if (!MojaOrLaterDevice(MFP)) {
        //  WriteToResultAreaNoTime("OK");
        //  WriteToResultArea("Must be Moja.");
        //  return -1;
        // }
        // WriteToResultAreaNoTime("OK");
        WriteToResultArea("");
        WriteToResultAreaNoCRLF("Using Moja method to log in to " + mfp.IPAddr() + "...");
        try {
            String encodedLogIn;

            if (!User.isEmpty() && !Password.isEmpty())
                encodedLogIn = "data=%7B%22authtype%22%3A0%2C%22authId%22%3A-1%2C%22creds%22%3A%7B%22username%22%3A%22"+User+"%22%2C%22password%22%3A%22"+Password+"%22%7D%7D";
            else if (!Password.isEmpty())
                encodedLogIn = "data=%7B%22authtype%22%3A3%2C%22authId%22%3A-1%2C%22creds%22%3A%7B%22pin%22%3A%22"+Password+"%22%7D%7D";
            else
                encodedLogIn = "data=%7B%22authtype%22%3A1%2C%22authId%22%3A-1%2C%22creds%22%3A%7B%22username%22%3A%22"+User+"%22%7D%7D";
            //Create connection
            connection = new WebConnection(mfp,"/webglue/session/create");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", "lexlang=\"0\"; autoLogin=false");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            OutputStream output = connection.getOutputStream ();
            DataOutputStream wr = new DataOutputStream (output);
            wr.writeBytes (encodedLogIn);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while((line = rd.readLine()) != null) {
                String Pairs = "\"(.*?)\":\"(.*?)\"";
                Pattern p = Pattern.compile("\\{" + Pairs + "," + Pairs + "," + Pairs + "," + Pairs + ",.*?\\}");
                Matcher m = p.matcher(line);
                m.find();
                for (int i=1;i<=8;i+=2) {
                    String key = m.group(i);
                    String value = m.group(i+1);
                    if (key.equals("sessionId")) {
                        sessionId = value;
                        mfp.Cookies.add("sessionId="+sessionId);
                    }
                    if (key.equals("sessionKey")) {
                        sessionKey = value;
                        mfp.Cookies.add("sessionKey="+sessionKey);
                        mfp.setCsrfToken(sessionKey);
                    }
                    if (key.equals("sessionName")) {
                        sessionName = value;
                        mfp.Cookies.add("sessionName="+sessionName);
                    }
                }
            }
            rd.close();
            WriteToResultAreaNoTime("OK");
            WriteToResultArea("   sessionId = \"" + sessionId + "\"  sessionKey = \"" + sessionKey + "\"  sessionName = \"" + sessionName + "\"");
            WriteToResultArea("");
            rc = 0;

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;

    }

    private int LogInToMFPClassic(MFP mfp, String User, String Password) {
        int rc = -1;
        String line;
        WebConnection connection = null;
        if (Password.isEmpty() && User.isEmpty())
            return 0;
        //if (MFP.isEmpty()) {
        //    WriteToResultArea("Must specify MFP FQDN or IP Address.");
        //    return -1;
        //}
        WriteToResultArea("");
        WriteToResultAreaNoCRLF("Using Classic method to log in to " + mfp.IPAddr() + "...");
        try {
            String encodedLogIn;

            if (!User.isEmpty() && !Password.isEmpty())
                encodedLogIn = "logon_type=username_password&goto=%2Fcgi-bin%2Fdynamic%2Fconfig%2Fsecure%2Fsecurity.html&accid=15&username="+User+"&password="+Password;
            else if (!Password.isEmpty())
                encodedLogIn = "logon_type=password_only&goto=%2Fcgi-bin%2Fdynamic%2Fconfig%2Fsecure%2Fsecurity.html&accid=1&password="+Password;
            else
                encodedLogIn = "logon_type=password_only&goto=%2Fcgi-bin%2Fdynamic%2Fconfig%2Fsecure%2Fsecurity.html&accid=15&username="+User;
            //Create connection
            connection = new WebConnection(mfp,"/cgi-bin/posttest/printer/login.html");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            OutputStream output = connection.getOutputStream ();
            DataOutputStream wr = new DataOutputStream (output);
            wr.writeBytes (encodedLogIn);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            //WriteToResultArea("Response from MFP:");
            while((line = rd.readLine()) != null) {
                //WriteToResultArea(line);
                if (line.contains("Invalid login")) {
                    connection.disconnect();
                    WriteToResultAreaNoTime("Failed");
                    WriteToResultArea(line);
                    return -1;
                }
            }

            // Save the user and password cookies
            if (!User.isEmpty())
                mfp.Cookies.add("user="+User);
            if (!Password.isEmpty())
                mfp.Cookies.add("password="+Password);

            rd.close();
            WriteToResultAreaNoTime("OK");
            rc = 0;

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;

    }


    private void WriteToResultArea(String data) {
        System.out.println(data);
    }

    private void WriteToResultAreaNoCRLF(String data) {
        System.out.print(data);
    }

    private void WriteToResultAreaNoTime(String data) {
        System.out.println(data);
    }
}
