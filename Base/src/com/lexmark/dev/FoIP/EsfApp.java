package com.lexmark.dev.FoIP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Pete on 3/23/2017.
 */
public class EsfApp {

    public EsfApp() {
    }

    public void Send(MFP mfp) {
        String charset = "UTF-8";
        InputStream uploadFile;
        String EsfAppFileName;
        String requestURL;

        if (mfp.PrinterFamily().equals(MFP.PrtFam.PRIDE)) {
            requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appmgrservlet";
            GetLicensePage(mfp, requestURL);
            requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appinstallokservlet";
            EsfAppFileName = "/FaxOverIP_e2_mfp-1.1.8.fls";
        } else if (mfp.PrinterFamily().equals(MFP.PrtFam.HS)) {
            requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appmgrservlet";
            GetLicensePage(mfp, requestURL);
            requestURL = "/cgi-bin/direct/printer/prtappauth_fwupdate/admin/appinstallokservlet";
            EsfAppFileName = "/FaxOverIP_e3-4_mfp-1.3.0.fls";
        } else {
            System.out.println("Device Family is not Pride or HS. Cannot use eSF App.");
            throw new LexmarkFoIPException("EsfApp.send failed: wrong printer family");
        }

        System.out.print("Sending eSF App to MFP...");

        try {
            MultipartUtility multipart = new MultipartUtility(mfp, requestURL, charset, "", "");

            uploadFile = EsfApp.class.getResourceAsStream(EsfAppFileName);
            multipart.addFilePartFromStream("fileUpload", uploadFile, EsfAppFileName);

            multipart.finish();

        } catch (IOException e) {
            System.out.println("Failed");
            System.out.println("" + e);
            throw new LexmarkFoIPException("EsfApp.send failed: " + e);
        }
        System.out.println("OK");
    }

    private static String GetLicensePage(MFP mfp, String page) {
        String line;
        StringBuilder response = new StringBuilder();
        WebConnection connection = null;

        try {
            //Create connection
            connection = new WebConnection(mfp, page);
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }

            rd.close();

        } catch (Exception e) {
            throw new LexmarkFoIPException("EsfApp.GetLicensePage failed: " + e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response.toString();

    }

    public void SendLicense(MFP mfp, String LicenseFileName) {
        String charset = "UTF-8";
        String requestURL;

        System.out.print("Sending eSF App License to MFP...");

        if (mfp.vccSupported()) {
            System.out.println("Send License Failed, VCC Supported");
            throw new LexmarkFoIPException("EsfApp.SendLicense failed: VCC is Supported");
        }

        String EsfAppName = GetEsfAppName(mfp);

        if (EsfAppName.contains("Failed:")) {
            System.out.println("Send License Failed: Failed to get eSF App Name");
            throw new LexmarkFoIPException("EsfApp.SendLicense failed: Failed to get eSF App Name");
        }

        if (EsfAppName.isEmpty()) {
            System.out.println("Send License Failed: No eSF App installed");
            throw new LexmarkFoIPException("EsfApp.SendLicense failed: No eSF App installed");
        }

        InputStream is = SettingsBundle.class.getResourceAsStream(LicenseFileName);

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appservlet?SelectedAppsNames=" + EsfAppName;
        GetLicensePage(mfp, requestURL);

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applicenseupdateokservlet";

        try {
            MultipartUtility multipart = new MultipartUtility(mfp, requestURL, charset, "", "");

            multipart.addSourcePart("source");
            multipart.addFilePartFromStream2("file", is, LicenseFileName);

            java.util.List<String> response = multipart.finish();

            for (String line : response) {
                if (line.toLowerCase().contains("error")) {
                    System.out.println("Failed");
                    throw new LexmarkFoIPException("EsfApp.SendLicense failed: error response");
                }
            }
        } catch (IOException e) {
            System.out.println("Send License Failed");
            System.out.println("" + e);
            throw new LexmarkFoIPException("EsfApp.SendLicense failed: " + e);
        }

        System.out.println("OK");
    }

    private String GetEsfAppName(MFP mfp) {
        String name = "";
        String response;
        String requestURL;

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appmgrservlet";
        GetLicensePage(mfp, requestURL);

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applistservlet";
        response = GetLicensePage(mfp, requestURL);

        if (response.contains("LXKFaxOverIP"))
            name = "LXKFaxOverIP";
        else if (response.contains("FaxOverIP"))
            name = "FaxOverIP";
        else if (response.contains("Failed:"))
            name = response;

        return name;
    }
}