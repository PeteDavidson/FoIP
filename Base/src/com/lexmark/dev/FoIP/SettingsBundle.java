package com.lexmark.dev.FoIP;

import com.sun.xml.internal.bind.v2.model.core.RegistryInfo;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.lexmark.dev.FoIP.LexmarkSettingsGroup.*;
import static com.lexmark.dev.FoIP.LexmarkSettingsName.*;
/**
 * Created by Pete on 2/24/2017.
 */
public class SettingsBundle {

    private byte[] bundleByteArray;
    private MFP _mfp;
    private boolean _NeedToReboot;
    private UIOutput UI;

    private ArrayList<String> validSettings;

    private Map<String, String> settingsMap;
    private String _LicenseFilename = null;

    public SettingsBundle(MFP mfp) {
        _mfp = mfp;
        _NeedToReboot = false;
        UI = UIOutputHandler.getInstance().GetHandler();
        settingsMap = new HashMap<>();
        validSettings = new ArrayList<>();
        setValidSettings();
    }

    private void setValidSettings() {

        validSettings.add(FAX.getValue() + FAXNAME.getValue());
        validSettings.add(FAX.getValue() + FAXNUMBER.getValue());
        validSettings.add(FAX.getValue() + FAXID.getValue());
        validSettings.add(FAX.getValue() + FAXMODE.getValue());
        validSettings.add(VOIP.getValue() + FAXTRANSPORT.getValue());
        validSettings.add(VOIP.getValue() + PROTOCOL.getValue());
        validSettings.add(VOIP.getValue() + TRACELEVEL.getValue());
        validSettings.add(VOIP.getValue() + STUNSERVER.getValue());
        validSettings.add(VOIP.getValue() + FORCEFAXMODE.getValue());
        validSettings.add(VOIP.getValue() + FORCEFAXMODEDELAY.getValue());
        validSettings.add(SIP.getValue() + PROXY.getValue());
        validSettings.add(SIP.getValue() + REGISTRAR.getValue());
        validSettings.add(SIP.getValue() + USER.getValue());
        validSettings.add(SIP.getValue() + PASSWORD.getValue());
        validSettings.add(SIP.getValue() + CONTACT.getValue());
        validSettings.add(SIP.getValue() + REALM.getValue());
        validSettings.add(SIP.getValue() + AUTHID.getValue());
        validSettings.add(SIP.getValue() + OUTBOUNDPROXY.getValue());
        if (!_mfp.MojaOrLaterDevice()) {
            validSettings.add("mfp.fax.sip.disableIncoming");
            validSettings.add("mfp.fax.sip.dialOutDigit");
        }
        else {
            validSettings.add(SIP.getValue() + REGTRANSPORT.getValue());
            validSettings.add(SIP.getValue() + INCOMINGTRANSPORT.getValue());
            validSettings.add(SIP.getValue() + OUTGOINGTRANSPORT.getValue());
        }
        validSettings.add(H323.getValue() + GATEWAY.getValue());
        validSettings.add(H323.getValue() + GATEKEEPER.getValue());
        validSettings.add(H323.getValue() + USER.getValue());
        validSettings.add(H323.getValue() + PASSWORD.getValue());
        validSettings.add(H323.getValue() + ENABLEFASTSTART.getValue());
        validSettings.add(H323.getValue() + DISABLEH245TUNNEL.getValue());
        validSettings.add(H323.getValue() + DISABLEGATEKEEPERDISCOVERY.getValue());
        if (!_mfp.MojaOrLaterDevice()) {
            validSettings.add("mfp.fax.h323.disableIncoming");
            validSettings.add("mfp.fax.h323.dialOutDigit");
        }
        validSettings.add(T38.getValue() + INDICATORREDUNDANCY.getValue());
        validSettings.add(T38.getValue() + LOWSPEEDREDUNDANCY.getValue());
        validSettings.add(T38.getValue() + HIGHSPEEDREDUNDANCY.getValue());
        validSettings.add(T38.getValue() + UDPTLKEEPALIVEINTERVAL.getValue());
        if (_mfp.HTTPSFaxSupported()) {
            validSettings.add(HTTPS.getValue() + HTTPSPROXY.getValue());
            validSettings.add(HTTPS.getValue() + HTTPSPROXYUSER.getValue());
            validSettings.add(HTTPS.getValue() + HTTPSPROXYPASSWORD.getValue());
            validSettings.add(HTTPS.getValue() + ENABLEPEERVERIFICATION.getValue());
            validSettings.add(HTTPS.getValue() + ENCRYPTFAXSEND.getValue());
            validSettings.add(HTTPS.getValue() + ENCRYPTFAXRECEIVE.getValue());
            validSettings.add(HTTPS.getValue() + SERVICEURL.getValue());
        }
    }

    private void setSetting(String setting, String value) {
        if (!validSettings.contains(setting)) {
            System.out.println("Setting " + setting + " does not exist");
            //throw new LexmarkFoIPException("Setting " + setting + " does not exist");
        } else {
            settingsMap.put(setting, value);
        }
    }

    private void add(String setting, String value) {
        System.out.println("Adding setting " + setting + "=" + value);
        setSetting(setting, value);
    }

    public void addList(LexmarkSettings settings) {
        Map<String, String> map;
        map = settings.list();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public void addLicense(String LicenseFilename) {
        _LicenseFilename = LicenseFilename;
    }

    public void make() {
        _NeedToReboot = false;
        try {
            //final File f = new File("C:\\Users\\Pete\\Test.zip");
            //final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            final ZipOutputStream out = new ZipOutputStream(byteOut);
            ZipEntry entry = new ZipEntry("bundle.xml");
            out.putNextEntry(entry);

            byte[] data;
            String strdata;

            // Write the header
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bundle>\n<bundleInfo>\n<name> FoIP Setttings Bundle </name>\n<description>\nSettings for the FoIP Feature\n</description>\n</bundleInfo>\n";
            data = header.getBytes();
            out.write(data, 0, data.length);

            if ((_LicenseFilename != null) && !_LicenseFilename.isEmpty()) {
                // Write the license header
                strdata = "<licenses>\n\t<license src=\"" + _LicenseFilename + "\"></license>\n</licenses>\n";
                data = strdata.getBytes();
                out.write(data, 0, data.length);
            }

            if (!settingsMap.isEmpty()) {

                // Write the settings header
                strdata = "<deviceSettings>\n";
                data = strdata.getBytes();
                out.write(data, 0, data.length);

                // Write each setting
                for (String key : settingsMap.keySet()) {
                    String value = settingsMap.get(key);
                    if (key.equals("mfp.fax.voip.forceFaxModeDelay")) {
                        key = "0x453C";    // No VCC key so we use NPA ID
                    }
                    if (key.contains("mfp.fax.https")) {
                        _NeedToReboot = true;
                    }
                    else if (key.contains("mfp.fax.voip.faxTransport")) {
                        String mfpFaxTransport = _mfp.GetSettingFromWebPage("0.system.17664");
                        if (mfpFaxTransport == null || mfpFaxTransport.equals("")) {
                            _NeedToReboot = true;
                        }
                        else if (mfpFaxTransport.equals("4") || mfpFaxTransport.equals("etherFAX")) {
                            if (!value.equals(LexmarkSettingsFaxTransport.HTTPS.toString())) {
                                _NeedToReboot = true;
                            }
                        }
                        else {
                            if (value.equals(LexmarkSettingsFaxTransport.HTTPS.toString())) {
                                _NeedToReboot = true;
                            }
                        }
                    }
                    WriteXMLSetting(out, key, value);
                }

                // End settings
                strdata = "</deviceSettings>\n";
                data = strdata.getBytes();
                out.write(data, 0, data.length);
            }

            // End bundle
            strdata = "</bundle>\n";
            data = strdata.getBytes();
            out.write(data, 0, data.length);

            // Close Settings entry
            out.closeEntry();

            if ((_LicenseFilename != null) && !_LicenseFilename.isEmpty()) {
                // License file entry
                entry = new ZipEntry(_LicenseFilename);
                out.putNextEntry(entry);

                // Add License file
                String line;
                InputStream is = SettingsBundle.class.getResourceAsStream(_LicenseFilename);
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while ((line = rd.readLine()) != null) {
                    byte[] b = line.getBytes(Charset.forName("UTF-8"));
                    out.write(b, 0, b.length);
                }

                // Close License file entry
                out.closeEntry();
            }

            // Close bundle
            out.close();

            bundleByteArray = byteOut.toByteArray();
        } catch (Exception e) {
            System.out.println("Failed");
            System.out.println("" + e);
            throw new LexmarkFoIPException("SettingsBundle.make failed: " + e);
        }
    }

    public void changeMFP(MFP mfp) {
        _mfp = mfp;
    }

    public void send() {
        String charset = "UTF-8";
        String requestURL;

        if (_mfp.MojaOrLaterDevice()) {
            requestURL = "/webservices/vcc/bundles?filterLog=errorOnly";
        } else {
            requestURL = "/cgi-bin/dynamic/printer/config/gen/vcc_import_bundle.html";
        }

        try {
            MultipartUtility multipart = new MultipartUtility(_mfp, requestURL, charset, "", "");

            //multipart.addFilePart("fileUpload", uploadFile);
            multipart.addFilePartFromStream("fileUpload", new ByteArrayInputStream(bundleByteArray), "bundle.zip");

            java.util.List<String> response = multipart.finish();

            for (String line : response) {
                System.out.println(line);
            }
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("Failed");
            System.out.println("" + e);
            throw new LexmarkFoIPException("SettingsBundle Send failed: " + e);
        }
    }

    public void waitForSettingsToTakeEffect() {

        if (_NeedToReboot) {
            UI.write("Waiting for reboot after settings change...");
            try {
                _mfp.WaitForMFPReboot(UI);
            }
            catch (Exception e) {
                UI.writelnNoTime("Failed");
                UI.Delay(10);
                throw e;
            }
            UI.writelnNoTime("OK");
        }
        else {
            UI.DelayWithCountDown("Waiting 20 seconds for settings to take effect",20);
        }
    }

    public void send1() {
        int rc = -2;
        int i;
        WebConnection connection = null;
        Exception LastException = null;

        System.out.print("Sending Bundle to " + _mfp.IPAddr() + "...");
        for (i = 0; i < 100 && rc == -2; i++) {
            rc = 0;
            try {
                //Create connection
                connection = new WebConnection(_mfp, "/webservices/vcc/bundles");
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                OutputStream output = connection.getOutputStream();
                DataOutputStream wr = new DataOutputStream(output);
                //String bundleString = readFile("C:\\Users\\Pete\\Test.zip", StandardCharsets.ISO_8859_1);
                String bundleString = readByteArray(bundleByteArray, StandardCharsets.ISO_8859_1);
                wr.writeBytes(bundleString);
                wr.flush();
                wr.close();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                //WriteToResultArea("Response from MFP:");
                while ((line = rd.readLine()) != null) {
                    //WriteToResultArea(line);
                }
                rd.close();
                System.out.println("OK");

            } catch (Exception e) {

                LastException = e;
                if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                    rc = -2;
                } else {
                    System.out.println("Failed");
                    System.out.println("" + e);
                    rc = -1;
                }

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
                if (rc != 0) {
                    UI.DelayWithDots(1, 1);
                }
            }
        }

        if (rc == -2) {
            System.out.println("Failed. Server returned HTTP response code: 408");
        }

        if (rc != 0) {
            throw new LexmarkFoIPException("SettingsBundle.send failed: " + LastException);
        }
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static String readByteArray(byte[] byteArray, Charset encoding) {
        return new String(byteArray, encoding);
    }

    private void WriteXMLSetting(ZipOutputStream out, String name, int value) throws IOException {
        String strdata;
        byte[] data;
        strdata = "\t<setting name=\"" + name + "\">" + value + "</setting>\n";
        data = strdata.getBytes();
        out.write(data, 0, data.length);
    }

    private void WriteXMLSetting(ZipOutputStream out, String name, String value) throws IOException {
        String strdata;
        byte[] data;
        strdata = "\t<setting name=\"" + name + "\">" + value + "</setting>\n";
        data = strdata.getBytes();
        out.write(data, 0, data.length);
    }

}
