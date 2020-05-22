package com.lexmark.dev.FoIP.Provisioner;

import com.lexmark.dev.FoIP.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static com.lexmark.dev.FoIP.LexmarkFirmwareUpdate.*;
import static com.lexmark.dev.FoIP.LexmarkSettingsName.*;
import static com.lexmark.dev.FoIP.LexmarkSettingsGroup.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.sql.Types.NULL;
import static org.junit.Assert.assertEquals;

/**
 * Created by Pete on 4/10/2017.
 */
public class LexmarkProvisionFoIPTest {

    void provision(String ip, String ext) {
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();

        lexmarkFoIP.setDevice(ip);

        lexmarkFoIP.addFirmwareUpdate(LATEST_PE);
        lexmarkFoIP.addTrialLicense();
        lexmarkFoIP.addCheckSIPRegStatus();
        lexmarkFoIP.setAutoDismiss(5);
        lexmarkFoIP.addSendTestFax("4016");

        lexmarkFoIP.addDefaults(FAX, VOIP, SIP, T38);
        lexmarkFoIP.addSetting(SIP, PROXY, "10.196.238.132");
        lexmarkFoIP.addSetting(SIP, REGISTRAR, "10.196.238.132");
        lexmarkFoIP.addSetting(SIP, USER, ext);
        lexmarkFoIP.addSetting(SIP, PASSWORD, "pas"+ext);

        lexmarkFoIP.provision();
    }
    void provision(String ip, String ext, String user, String password) {
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();

        lexmarkFoIP.setDevice(ip, user, password);

        lexmarkFoIP.addFirmwareUpdate(LATEST_PE);
        lexmarkFoIP.addTrialLicense();
        lexmarkFoIP.addCheckSIPRegStatus();
        lexmarkFoIP.setAutoDismiss(10);
        lexmarkFoIP.addSendTestFax("4016");

        lexmarkFoIP.addDefaults(FAX, VOIP, SIP, T38);
        lexmarkFoIP.addSetting(SIP, PROXY, "10.196.238.132");
        lexmarkFoIP.addSetting(SIP, REGISTRAR, "10.196.238.132");
        lexmarkFoIP.addSetting(SIP, USER, ext);
        lexmarkFoIP.addSetting(SIP, PASSWORD, "pas"+ext);

        lexmarkFoIP.provision();
    }

    @Test
    public void testDefaults() {
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();

        lexmarkFoIP.setDevice("157.184.138.30","pete","abcd1234");

        lexmarkFoIP.addDefaults();
        lexmarkFoIP.addTrialLicense();

        Map map = lexmarkFoIP.getSettingsListMap();
        assertEquals("0",map.get("mfp.fax.stationID"));
        assertEquals("0",map.get("mfp.fax.mode"));
        assertEquals("",map.get("mfp.fax.send.dialingPrefix"));
        assertEquals("5",map.get("mfp.fax.send.autoRedial"));
        assertEquals("3",map.get("mfp.fax.send.redialFrequency"));
        assertEquals("3",map.get("mfp.fax.receive.ringsToAnswer"));
        assertEquals("0",map.get("mfp.fax.logs.transmissionLog"));
        assertEquals("1",map.get("mfp.fax.logs.receiveErrorLog"));
        assertEquals("1",map.get("mfp.fax.voip.faxTransport"));
        assertEquals("1",map.get("mfp.fax.voip.protocol"));
        assertEquals("0",map.get("mfp.fax.voip.traceLevel"));
        assertEquals("",map.get("mfp.fax.voip.stunServer"));
        assertEquals("0",map.get("mfp.fax.voip.forceFaxMode"));
        assertEquals("0",map.get("mfp.fax.voip.forceFaxModeDelay"));
        assertEquals("",map.get("mfp.fax.sip.proxy"));
        assertEquals("",map.get("mfp.fax.sip.registrar"));
        assertEquals("",map.get("mfp.fax.sip.user"));
        assertEquals("",map.get("mfp.fax.sip.password"));
        assertEquals("",map.get("mfp.fax.sip.contact"));
        assertEquals("",map.get("mfp.fax.sip.realm"));
        assertEquals("",map.get("mfp.fax.sip.authID"));
        assertEquals("",map.get("mfp.fax.sip.outboundProxy"));
        assertEquals("",map.get("mfp.fax.h323.gateway"));
        assertEquals("",map.get("mfp.fax.h323.gatekeeper"));
        assertEquals("",map.get("mfp.fax.h323.user"));
        assertEquals("",map.get("mfp.fax.h323.password"));
        assertEquals("0",map.get("mfp.fax.h323.enableFastStart"));
        assertEquals("0",map.get("mfp.fax.h323.disableH245Tunnel"));
        assertEquals("1",map.get("mfp.fax.h323.disableGatekeeperDiscovery"));
        assertEquals("3",map.get("mfp.fax.t38.indicatorRedundancy"));
        assertEquals("3",map.get("mfp.fax.t38.lowSpeedRedundancy"));
        assertEquals("1",map.get("mfp.fax.t38.highSpeedRedundancy"));
        assertEquals("0",map.get("mfp.fax.t38.udptlKeepAliveInterval"));

        assertEquals("157.184.138.30", lexmarkFoIP.getIPAddr_or_FQDN());
        assertEquals("pete", lexmarkFoIP.getUser());
        assertEquals("abcd1234", lexmarkFoIP.getPassword());

        assertEquals("VCC-License.lic", lexmarkFoIP.getLicenseFilename());
        assertEquals("eSF-License.lic", lexmarkFoIP.getLicenseFilenameEsf());

    }

    @Test
    public void test4012() {
        provision("157.184.138.30","4012", "pete", "abcd1234");
    }

    @Test
    public void test4016() {
        provision("157.184.138.31","4018");
    }

    @Test
    public void test4019() {
        provision("10.196.239.69","4019");
    }

    @Test
    public void test4011() {
        provision("157.184.139.79","4011", "pete", "abcd1234");
    }

    @Test
    public void test4014() {
        provision("157.184.139.198","4014","pete", "abcd1234");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMissing() {
        thrown.expect(LexmarkFoIPException.class);
        thrown.expectMessage("MFP not found at 158.184.138.30");
        provision("158.184.138.30", "4999");
    }

    @Test
    public void testAuthReq() {
        thrown.expect(LexmarkFoIPException.class);
        thrown.expectMessage("Authentication required");
        provision("157.184.138.30", "4999");
    }

    @Test
    public void testBadAuth() {
        thrown.expect(LexmarkFoIPException.class);
        thrown.expectMessage("Authentication failed");
        provision("157.184.138.30", "4999", "pete", "abcd");
    }

    @Test
    public void testBadReg() {
        thrown.expect(LexmarkFoIPException.class);
        thrown.expectMessage("SIP Registration Failed:");
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();

        lexmarkFoIP.setDevice("157.184.138.30", "pete", "abcd1234");
        lexmarkFoIP.addTrialLicense();
        lexmarkFoIP.addCheckSIPRegStatus();
        lexmarkFoIP.setAutoDismiss(5);

        lexmarkFoIP.addDefaults(FAX, VOIP, SIP, T38);
        lexmarkFoIP.addSetting(SIP, PROXY, "10.196.238.132");
        lexmarkFoIP.addSetting(SIP, REGISTRAR, "10.196.238.133");
        lexmarkFoIP.addSetting(SIP, USER, "4012");
        lexmarkFoIP.addSetting(SIP, PASSWORD, "pas4012");

        lexmarkFoIP.provision();
    }

    @Test
    public void testPermanentLicense() {
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();
        lexmarkFoIP.addPermanentLicense("License.lic", "LicenseEsf.lic");
        System.out.println(lexmarkFoIP);
        assertEquals("License.lic", lexmarkFoIP.getLicenseFilename());
        assertEquals("LicenseEsf.lic", lexmarkFoIP.getLicenseFilenameEsf());
    }


    @Test
    public void testMX310() {
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP(new ProvisionerUIOutput());
        //final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();
        lexmarkFoIP.setDevice("157.184.139.109","pete","gators");
        lexmarkFoIP.addFirmwareUpdate(LATEST_PE);
        //lexmarkFoIP.addFirmwareUpdate(LOCAL_FILE);
        //lexmarkFoIP.addFirmwareLocalFile("C:\\Users\\Pete\\Google Drive\\etherFAX\\firmware\\MX310.dbg.fullnet.fls");
        //lexmarkFoIP.addTrialLicense();
        //lexmarkFoIP.addProviderSpecificFoIPTestLicense();
        //lexmarkFoIP.addCheckSIPRegStatus();
        lexmarkFoIP.setAutoDismiss(60);
        lexmarkFoIP.addSendTestFax("17328138442");

        //lexmarkFoIP.addDefaults(FAX, VOIP, SIP, T38);
        //lexmarkFoIP.addSetting(SIP, PROXY, "10.196.238.132");
        //lexmarkFoIP.addSetting(SIP, REGISTRAR, "10.196.238.132");
        //lexmarkFoIP.addSetting(SIP, USER, "4012");
        //lexmarkFoIP.addSetting(SIP, PASSWORD, "pas4012");

        lexmarkFoIP.provision();
    }

    @Test
    public void httpsSettings() {
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP(new ProvisionerUIOutput());
        //final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();
        lexmarkFoIP.setDevice("157.184.139.185","pete","gators");
        lexmarkFoIP.addFirmwareUpdate(LATEST_PE);
        //lexmarkFoIP.addFirmwareUpdate(LOCAL_FILE);
        //lexmarkFoIP.addFirmwareLocalFile("C:\\Users\\Pete\\Google Drive\\etherFAX\\firmware\\MX310.dbg.fullnet.fls");
        //lexmarkFoIP.addTrialLicense();
        //lexmarkFoIP.addProviderSpecificFoIPTestLicense();
        //lexmarkFoIP.addCheckSIPRegStatus();
        lexmarkFoIP.setAutoDismiss(300);
        lexmarkFoIP.addSendTestFax("17328138442");

        //lexmarkFoIP.addDefaults(FAX, VOIP, SIP, T38, HTTPS);
        //lexmarkFoIP.addSetting(HTTPS, HTTPSPROXY, "");
        //lexmarkFoIP.addSetting(HTTPS, HTTPSPROXYUSER, "");
        //lexmarkFoIP.addSetting(HTTPS, HTTPSPROXYPASSWORD, "");
        //lexmarkFoIP.addSetting(HTTPS, ENABLEPEERVERIFICATION, "1");
        //lexmarkFoIP.addSetting(HTTPS, ENCRYPTFAXSEND, "1");
        //lexmarkFoIP.addSetting(HTTPS, ENCRYPTFAXRECEIVE, "1");
        lexmarkFoIP.addSetting(VOIP, FAXTRANSPORT, LexmarkSettingsFaxTransport.HTTPS.toString());

        lexmarkFoIP.provision();
    }
    @Test
    public void testNPAIDs() {
        MFPStore mfpStore = MFPStore.getInstance();
        mfpStore.InitTrustAll();

        for (Integer i=97; i<120; i++) {
            String IP = "157.184.142." + i.toString();
            System.out.println("IP: " + IP);
            MFP mfp = mfpStore.Init(IP);
            mfpStore.PrintMFPList();
        }
    }
    @Test
    public void testMFPviaNPAPID() {
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP(new ProvisionerUIOutput());
        UIOutput UI = UIOutputHandler.getInstance().GetHandler();
        MFPStore mfpStore = MFPStore.getInstance();
        mfpStore.InitTrustAll();
        MFP mfp = mfpStore.Init(181);
        mfpStore.PrintMFPList();
        Firmware fw = new Firmware();
        //String[] fwstring = fw.FindBuildIdRecommended(mfp);
        //System.out.println(fwstring[0]+" "+fwstring[1]+" "+fwstring[2]);
        ModelList Models = ModelList.getInstance();
        String[] ModelNames = Models.GetModelNames(Models.GetNPAPIDfromId("CXTMH"));
        System.out.print("{ ");
        for (String s: ModelNames) {
            System.out.print(s+" ");
        }
        System.out.println("}");
        System.out.println(Models.GetNPAPIDfromId("SB7"));
        System.out.println(Models.GetNPAPIDfromModelName("CX927"));

        String[] AllModels = Models.GetAllModelNames();

        for (String s: AllModels) {
            System.out.print(s+" ");
        }
        System.out.println();

        String outstring = "";
        Path path = Paths.get(System.getProperty("user.home")+File.separator+"LatestPELinks.csv");
        try {
            outstring += "Model,Code Level,Link,NPAPID\n";

            for (String s: AllModels) {
                UI.writeln("Processing "+s);
                Integer NPAPID=Models.GetNPAPIDfromModelName(s);
                System.out.println("NPAPID: "+NPAPID);
                String[] link = Models.GetFirmwareLink(NPAPID,LATEST_PE);
                String ThisLine = s + "," + link[0] + "," + link[1] + "," + NPAPID + "\n";
                outstring += ThisLine;
                UI.writeln(ThisLine);
            }
            Files.write(path,outstring.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadetherFAXReport() {
        File file = new File("C:\\Users\\pete\\Downloads\\etherfax_lexmark_devices.csv");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UNICODE"));
            String st;
            while ((st = br.readLine()) != null) {
                if (st.length()>4 && st.substring(0,4).equals("EFAX")) {
                    System.out.println(st);
                    String[] fields = st.split("\t");
                    System.out.println(fields.length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
