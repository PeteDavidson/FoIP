package com.lexmark.dev.FoIP;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pete on 2/25/2017.
 */
public class UCF {
    private byte[] ucfByteArray;
    private MFP _mfp;

    private Map<String, String> settingsMap;
    private Map<String, String> vcc2ucf;

    public UCF(MFP mfp) {
        _mfp = mfp;

        settingsMap = new HashMap<>();
        vcc2ucf = new HashMap<>();

        vcc2ucf.put("mfp.fax.stationName", "mfp.fax.stationName");
        vcc2ucf.put("mfp.fax.stationNumber", "mfp.fax.stationNumber");
        vcc2ucf.put("mfp.fax.stationID", "mfp.fax.stationID");
        vcc2ucf.put("mfp.fax.mode", "mfp.fax.mode");

        vcc2ucf.put("mfp.fax.send.dialingPrefix", "mfp.fax.send.dialingPrefix");
        vcc2ucf.put("mfp.fax.send.autoRedial", "mfp.fax.send.autoRedial");
        vcc2ucf.put("mfp.fax.send.redialFrequency", "mfp.fax.send.redialFrequency");

        vcc2ucf.put("mfp.fax.receive.ringsToAnswer", "mfp.fax.receive.ringsToAnswer");

        vcc2ucf.put("mfp.fax.logs.transmissionLog", "mfp.fax.logs.transmissionLog");
        vcc2ucf.put("mfp.fax.logs.receiveErrorLog", "mfp.fax.logs.receiveErrorLog");

        vcc2ucf.put("mfp.fax.voip.faxTransport", "mfp.fax.voipSettings.faxTransport");
        vcc2ucf.put("mfp.fax.voip.protocol", "mfp.fax.voipSettings.voipProtocol");
        vcc2ucf.put("mfp.fax.voip.traceLevel", "mfp.fax.voipSettings.traceLevel");
        vcc2ucf.put("mfp.fax.voip.stunServer", "mfp.fax.voipSettings.stunServer");
        vcc2ucf.put("mfp.fax.voip.forceFaxMode", "mfp.fax.voipSettings.forceFaxMode");
        vcc2ucf.put("mfp.fax.voip.forceFaxModeDelay", "mfp.fax.voipSettings.forceFaxModeDelay");

        vcc2ucf.put("mfp.fax.sip.proxy", "mfp.fax.sipSettings.proxy");
        vcc2ucf.put("mfp.fax.sip.registrar", "mfp.fax.sipSettings.registrar");
        vcc2ucf.put("mfp.fax.sip.user", "mfp.fax.sipSettings.user");
        vcc2ucf.put("mfp.fax.sip.password", "mfp.fax.sipSettings.password");
        vcc2ucf.put("mfp.fax.sip.contact", "mfp.fax.sipSettings.contact");
        vcc2ucf.put("mfp.fax.sip.realm", "mfp.fax.sipSettings.realm");
        vcc2ucf.put("mfp.fax.sip.authID", "mfp.fax.sipSettings.authID");
        vcc2ucf.put("mfp.fax.sip.outboundProxy", "mfp.fax.sipSettings.outboundProxy");
        vcc2ucf.put("mfp.fax.sip.disableIncoming", "mfp.fax.sipSettings.disableIncomingCalls");
        vcc2ucf.put("mfp.fax.sip.dialOutDigit", "mfp.fax.sipSettings.sipDialOutDigit");

        vcc2ucf.put("mfp.fax.h323.gateway", "mfp.fax.h323Settings.gateway");
        vcc2ucf.put("mfp.fax.h323.gatekeeper", "mfp.fax.h323Settings.gatekeeper");
        vcc2ucf.put("mfp.fax.h323.user", "mfp.fax.h323Settings.user");
        vcc2ucf.put("mfp.fax.h323.password", "mfp.fax.h323Settings.password");
        vcc2ucf.put("mfp.fax.h323.enableFastStart", "mfp.fax.h323Settings.enableFastStart");
        vcc2ucf.put("mfp.fax.h323.disableH245Tunnel", "mfp.fax.h323Settings.disableH245Tunneling");
        vcc2ucf.put("mfp.fax.h323.disableGatekeeperDiscovery", "mfp.fax.h323Settings.disableGatekeeperDiscovery");
        vcc2ucf.put("mfp.fax.h323.disableIncoming", "mfp.fax.h323Settings.disableIncomingCalls");
        vcc2ucf.put("mfp.fax.h323.dialOutDigit", "mfp.fax.h323Settings.h323DialOutDigit");

        vcc2ucf.put("mfp.fax.t38.indicatorRedundancy", "mfp.fax.t38Settings.indicatorRedundancy");
        vcc2ucf.put("mfp.fax.t38.lowSpeedRedundancy", "mfp.fax.t38Settings.lowSpeedRedundancy");
        vcc2ucf.put("mfp.fax.t38.highSpeedRedundancy", "mfp.fax.t38Settings.highSpeedRedundancy");
        vcc2ucf.put("mfp.fax.t38.udptlKeepAliveInterval", "mfp.fax.t38Settings.udptlKeepAliveInterval");

        vcc2ucf.put("mfp.fax.https.proxy", "mfp.fax.httspSettings.proxy");
        vcc2ucf.put("mfp.fax.https.proxyUser", "mfp.fax.httspSettings.proxyUser");
        vcc2ucf.put("mfp.fax.https.proxyPassword", "mfp.fax.httspSettings.proxyPassword");
        vcc2ucf.put("mfp.fax.https.useHTTP", "mfp.fax.httspSettings.useHTTP");
        vcc2ucf.put("mfp.fax.https.enablePeerVerification", "mfp.fax.httspSettings.enablePeerVerification");
        vcc2ucf.put("mfp.fax.https.encryptFaxSend", "mfp.fax.httspSettings.encryptFaxSend");
        vcc2ucf.put("mfp.fax.https.encryptFaxReceive", "mfp.fax.httspSettings.encryptFaxReceive");
    }

    private void add(String setting, String value) {
        if (value != null) {
            System.out.println("Adding setting" + setting + "=" + value);
            if (!vcc2ucf.containsKey(setting)) {
                System.out.println("Setting " + setting + " does not exist");
                throw new LexmarkFoIPException("Setting " + setting + " does not exist");
            }
            settingsMap.put(setting, value);
        }
    }

    public void addList(LexmarkSettings settings) {
        Map<String, String> map;
        map = settings.list();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public void make() {
        try {
            System.out.print("UCF Creation Started...");
            //File ucfFile = new File("C:\\Users\\Pete\\Test.ucf");
            //BufferedWriter out = new BufferedWriter( new FileWriter(ucfFile));
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(byteOut));

            String ucfHeader = "// UCF File for FoIP\r\n//\r\n";
            out.write(ucfHeader);

            for (String key : settingsMap.keySet()) {
                String value = settingsMap.get(key);
                String ucfString = vcc2ucf.get(key);
                WriteUCFSetting(value, out, ucfString);
            }

            String ucfFooter = "\r\n";
            out.write(ucfFooter);

            out.close();

            ucfByteArray = byteOut.toByteArray();

            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("Failed");
            System.out.println("" + e);
            throw new LexmarkFoIPException("UCF.make failed: " + e);
        }
    }

    public void send() {
        String charset = "UTF-8";
        //File uploadFile = new File("C:\\Users\\Pete\\Test.ucf");
        String requestURL = "/cgi-bin/dynamic/printer/config/secure/importsettings.html";

        System.out.print("Checking MFP Family...");
        if (_mfp.MojaOrLaterDevice()) {
            System.out.println("OK");
            System.out.println("UCF on Moja is not supported yet.");
            throw new LexmarkFoIPException("UCF.send: UCF not supported");
        }
        System.out.println("OK");
        System.out.print("Sending UCF to MFP...");

        try {
            MultipartUtility multipart = new MultipartUtility(_mfp, requestURL, charset, "", "");

            //multipart.addFilePart("fileUpload", uploadFile);
            multipart.addFilePartFromStream("fileUpload", new ByteArrayInputStream(ucfByteArray), "Lexmark.ucf");

            java.util.List<String> response = multipart.finish();

            for (String line : response) {
            }
            System.out.println("OK");
        } catch (Exception e) {
            System.out.println("Failed");
            System.out.println("" + e);
            throw new LexmarkFoIPException("UCF.make failed: " + e);
        }
    }

    private void WriteUCFSetting(String setting, BufferedWriter out, String name) throws IOException {
        String strdata;
        strdata = name + " \"" + setting + "\"\r\n";
        out.write(strdata);
    }

}
