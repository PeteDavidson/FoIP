package com.lexmark.dev.FoIP.Provisioner;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Map;

import com.lexmark.dev.FoIP.*;

import static com.lexmark.dev.FoIP.LexmarkFirmwareUpdate.LATEST_PE;
import static com.lexmark.dev.FoIP.LexmarkSettingsGroup.*;
import static com.lexmark.dev.FoIP.LexmarkSettingsGroup.SIP;
import static com.lexmark.dev.FoIP.LexmarkSettingsName.*;


/**
 * Created by Pete on 4/5/2017.
 */
public class LexmarkProvisionFoIP implements LexmarkProvisionFoIPApi {
    private ArrayList<String> savedRegData = null;

    public static void main(String[] args)
    {
        System.out.println("Lexmark FoIP Provisioner 1.0");
        System.out.println("");

        //UIOutputHandler.getInstance().SetHandler(new ProvisionerUIOutput());

        UIOutput UI = UIOutputHandler.getInstance().GetHandler();

        UI.writeln("Here");

        UI.write("1234567890");
        UI.overwrite("A");
        UI.overwrite("B");
        UI.overwrite("C");
        UI.writelnNoTime("");

        UI.DelayWithCountDown("Delay 10 seconds", 10);

        UI.writeln("Lexmark FoIP Provisioner 1.0");
        UI.writelnNoTime("");

        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP();

        lexmarkFoIP.setDevice("157.184.138.31");
        lexmarkFoIP.addCheckSIPRegStatus();

        lexmarkFoIP.provision();
    }

    public LexmarkProvisionFoIP()
    {
        UIOutput UI = new UIOutput();
        UI.initialize();
        UIOutputHandler.getInstance().SetHandler(UI);
        UI.initLog(System.getProperty("user.home"),"ProvisionerLog", 5);
    }

    public LexmarkProvisionFoIP(UIOutput UI)
    {
        UIOutputHandler.getInstance().SetHandler(UI);
    }

    @Override
    public void provision() throws LexmarkFoIPException {

        if (_IPAddr_or_FQDN == null) {
            throw new LexmarkFoIPException("IPAddr or FQDN not set.");
        }

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        MFPStore mfpStore = MFPStore.getInstance();
        mfpStore.InitTrustAll();

        UIOutput UI = UIOutputHandler.getInstance().GetHandler();

        UI.writeln("Provisioning MFP for FoIP at " + _IPAddr_or_FQDN);
        UI.writelnNoTime("");

        UI.write("Finding MFP...");
        MFP mfp = mfpStore.Init(_IPAddr_or_FQDN);
        if (mfp.getValidTransport()) {
            UI.writelnNoTime("OK  (via " + (mfp.getUseHttps() ? "https" : "http") + ")");
        } else {
            UI.writelnNoTime("Failed");
            UI.end();
            throw new LexmarkFoIPException("MFP not found at " + mfp.IPAddr());
        }
        mfpStore.PrintMFPList();
        //UI.writeln("etherFAX support: " + mfp.HTTPSFaxSupported());
        //UI.writeln("Fax Transport: " + mfp.GetSettingFromWebPage("0.system.17664"));
        //UI.writeln("Redials: " + mfp.GetSettingFromWebPage("0.system.16480"));

        if (mfp.getAuthRequired()) {
            UI.writeln("Authentication required");
            // If mfpUser is not empty then we need to login to the mfp
            if (!_user.isEmpty()) {
                UI.write("Logging in to MFP...");
                mfp.setUser(_user);
                mfp.setPassword(_password);
                MFPSecurity mfpsec = new MFPSecurity(mfp);
                int rc = mfpsec.LogIn();
                if (rc != 0) {
                    UI.writelnNoTime("Failed");
                    UI.end();
                    throw new LexmarkFoIPException("Authentication failed");
                }
                UI.writelnNoTime("OK");
            } else {
                UI.write("No credentials provided");
                UI.end();
                throw new LexmarkFoIPException("Authentication required");
            }
        }

        UI.write("Retrieving data from MFP...");
        mfp.Refresh();

        if (!mfp.validData()) {
            UI.writelnNoTime("Failed");
            UI.writeln("Could not retrieve data from MFP.");
            UI.end();
            throw new LexmarkFoIPException("Could not retrieve data from MFP");
        }

        if (mfp.PrinterFamilyString().equals("SIM")) {
            UI.writelnNoTime("Failed");
            UI.writeln("MFP at IP Addr " + _IPAddr_or_FQDN + " is an unknown SIM.");
            UI.end();
            throw new LexmarkFoIPException("MFP at IP Addr " + _IPAddr_or_FQDN + " is an unknown SIM");
        }

        UI.writelnNoTime("OK");

        mfpStore.PrintMFPList();

        // Update FW if needed
        if (_firmwareUpdate != LexmarkFirmwareUpdate.NO) {
            Firmware fw;
            try {
                fw = new Firmware();
            } catch (Exception e) {
                UI.writeln(e.getMessage());
                UI.end();
                throw new LexmarkFoIPException(e.getMessage());
            }

            try {
                fw.UpdateFirmware(mfp, _firmwareUpdate, _localFile);
            } catch (Exception e) {
                UI.writeln("Are firmware updates disabled on the MFP?");
                UI.end();
                throw new LexmarkFoIPException("Firmware Update Failed: " + e);
            }
        }

        // If VCC is supported then we can send a settings bundle
        if (mfp.vccSupported()) {

            if (_checkSIPRegStatus) {
                mfp.GetSIPRegStatus();
                savedRegData = mfp.getLastRegistrationData();
                mfp.clearSIPRegistrar(UI);
            }

            try {
                SettingsBundle settingsBundle = new SettingsBundle(mfp);
                if (_settings != null) {
                    if (_LicenseFilename != null) {
                        UI.write("Sending License and Settings to MFP...");
                        settingsBundle.addLicense(_LicenseFilename);
                    } else {
                        UI.write("Sending Settings to MFP...");
                    }
                    settingsBundle.addList(_settings);
                    settingsBundle.make();
                    settingsBundle.send();
                    UI.writelnNoTime("OK");
                    settingsBundle.waitForSettingsToTakeEffect();
                } else {
                    if (_LicenseFilename != null) {
                        UI.write("Sending License to MFP...");
                        settingsBundle.addLicense(_LicenseFilename);
                        settingsBundle.make();
                        settingsBundle.send();
                        UI.writelnNoTime("OK");
                        settingsBundle.waitForSettingsToTakeEffect();
                    }
                }

            } catch (Exception e) {
                UI.writelnNoTime("Failed");
                UI.end();
                throw e;
            }
        }

        // If VCC is not supported then we send a UCF file
        else {
            if (_LicenseFilenameEsf != null) {
                EsfApp esf = new EsfApp();
                UI.write("Sending FoIP eSF App to MFP...");

                try {
                    esf.Send(mfp);
                } catch (Exception e) {
                    UI.writelnNoTime("Failed");
                    UI.end();
                    throw e;
                }

                UI.writelnNoTime("OK");

                // Wait 15 seconds for eSF App install to complete
                UI.DelayWithCountDown("Waiting 15 seconds for eSF App install to complete", 15);

                UI.write("Sending FoIP eSF App License to MFP...");

                try {
                    esf.SendLicense(mfp, _LicenseFilenameEsf);
                } catch (Exception e) {
                    UI.writelnNoTime("Failed");
                    UI.end();
                    throw e;
                }

                UI.writelnNoTime("OK");
            }

            if (_settings != null) {

                if (_checkSIPRegStatus) {
                    mfp.GetSIPRegStatus();
                    savedRegData = mfp.getLastRegistrationData();
                    mfp.clearSIPRegistrar(UI);
                }

                UI.write("Sending Settings to MFP...");
                UCF ucf = new UCF(mfp);

                try {
                    ucf.addList(_settings);
                    ucf.make();
                    ucf.send();
                } catch (Exception e) {
                    UI.writelnNoTime("Failed");
                    UI.end();
                    throw e;
                }
                UI.writelnNoTime("OK");
                UI.DelayWithCountDown("Waiting 10 seconds for settings to take effect", 10);

            }
        }

        mfpStore.PrintMFPList();

        if (_checkSIPRegStatus) {

            UI.write("Checking SIP Registration Status...");

            String SIPResult = "No SIP Registration Data";

            UI.RotatorStart();
            for (int i = 0; (i < 30) && !SIPResult.contains("200"); i++) {
                UI.Delay(1);
                UI.RotatorNext();
                if (savedRegData == null)
                    SIPResult = mfp.GetSIPRegStatus();
                else {
                    SIPResult = mfp.GetSIPRegStatusIfDifferent(savedRegData);
                }
            }

            // Wait 5 seconds for t38modem to settle
            for (int i = 0; i < 5; i++) {
                UI.Delay(1);
                UI.RotatorNext();
            }

            UI.RotatorEnd();

            if (SIPResult.equals("200 OK")) {
                UI.writelnNoTime("OK");
            } else {
                UI.writelnNoTime("Failed");
                UI.writeln("   Status: '" + SIPResult + "'");
                UI.end();
                throw new LexmarkFoIPException("SIP Registration Failed: " + SIPResult);
            }
        }

        if (_dialString != null) {
            String Result = mfp.SendAndVerifyFax(UI,_dialString);
            if (!Result.contains("OK")) {
                UI.end();
                throw new LexmarkFoIPException("Send test fax failed: " + Result);
            }
            System.out.println(Result);
        }

        UI.writeln("Lexmark FoIP Provisioning complete.");

        UI.end();
    }

    @Override
    public void setDevice(String IPAddr_or_FQDN, String user, String password) {
        _IPAddr_or_FQDN = IPAddr_or_FQDN;
        _user = user;
        _password = password;
    }

    @Override
    public void setDevice(String IPAddr_or_FQDN) {
        _IPAddr_or_FQDN = IPAddr_or_FQDN;
        _user = "";
        _password = "";
    }

    @Override
    public void addTrialLicense() {
        _LicenseFilename = "/VCC-License.lic";
        _LicenseFilenameEsf = "/eSF-License.lic";
    }

    @Override
    public void addProviderSpecificFoIPTestLicense() {
        _LicenseFilename = "/ProviderSpecificFoIPTestLicense.lic";
        _LicenseFilenameEsf = "";
    }

    @Override
    public void addPermanentLicense(String LicenseFilename, String LicenseFilenameEsf) {
        _LicenseFilename = LicenseFilename;
        _LicenseFilenameEsf = LicenseFilenameEsf;
    }

    @Override
    public void addDefaults(LexmarkSettingsGroup... groups) {
        if (_settings == null)
            _settings = new LexmarkSettings();
        _settings.addDefaults(groups);
    }

    @Override
    public void addSetting(LexmarkSettingsGroup group, LexmarkSettingsName name, String value) {
        if (_settings == null)
            _settings = new LexmarkSettings();
        if (name.inGroup(group)) {
            _settings.add(group, name, value);
        } else {
            throw new LexmarkFoIPException("Setting " + name + " not in group " + group);
        }
    }

    @Override
    public void addFirmwareUpdate(LexmarkFirmwareUpdate fw) {
        _firmwareUpdate = fw;
    }

    @Override
    public void addFirmwareLocalFile(String LocalFile) { _localFile = LocalFile; }

    @Override
    public void addCheckSIPRegStatus() {
        _checkSIPRegStatus = true;
    }

    @Override
    public void setAutoDismiss(int Seconds) {
        UIOutput box = UIOutputHandler.getInstance().GetHandler();
        box.setAutoDismiss(Seconds);
    }

    @Override
    public Map<String, String> getSettingsListMap() {
        return _settings.getListMap();
    }

    @Override
    public String getIPAddr_or_FQDN() {
        return _IPAddr_or_FQDN;
    }

    @Override
    public String getUser() {
        return _user;
    }

    @Override
    public String getPassword() {
        return _password;
    }

    @Override
    public String getLicenseFilename() {
        return _LicenseFilename;
    }

    @Override
    public String getLicenseFilenameEsf() {
        return _LicenseFilenameEsf;
    }

    @Override
    public void addSendTestFax(String dialString) {
        _dialString = dialString;
    }

    @Override
    public String toString() {
        String out = getClass().getSimpleName() + " [\n";
        out = out + pretty("IPAddr_or_FQDN", _IPAddr_or_FQDN);
        out = out + pretty("user", _user);
        out = out + pretty("password", _password);
        out = out + pretty("LicenseFilename", _LicenseFilename);
        out = out + pretty("LicenseFilenameEsf", _LicenseFilenameEsf);
        out = out + pretty("firmwareUpdate", _firmwareUpdate);
        out = out + pretty("checkSIPRegStatus", _checkSIPRegStatus);
        out = out + pretty("settings", _settings);
        out = out + "]\n";
        return out;
    }

    private String pretty(String Name, String S) {
        return Name + " = " + ((S == null) ? "[null]" : "\"" + S + "\"") + "\n";
    }

    private String pretty(String Name, LexmarkSettings S) {
        return Name + " = " + ((S == null) ? "[null]\n" : "\"" + S);
    }

    private String pretty(String Name, boolean B) {
        return Name + " = " + (B ? "true" : "false") + "\n";
    }

    private String pretty(String Name, LexmarkFirmwareUpdate F) {
        return Name + " = " + ((F == null) ? "[null]" : F) + "\n";
    }

    private String _IPAddr_or_FQDN = null;
    private String _user = null;
    private String _password = null;
    private String _LicenseFilename = null;
    private String _LicenseFilenameEsf = null;
    private LexmarkSettings _settings = null;
    private LexmarkFirmwareUpdate _firmwareUpdate = LexmarkFirmwareUpdate.NO;
    private String _localFile = "";
    private boolean _checkSIPRegStatus = false;
    private String _dialString = null;
}

