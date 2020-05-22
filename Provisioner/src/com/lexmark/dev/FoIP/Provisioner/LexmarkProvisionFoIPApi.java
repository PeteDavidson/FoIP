package com.lexmark.dev.FoIP.Provisioner;

import com.lexmark.dev.FoIP.*;

import java.util.Map;

/**
 * Created by Pete on 4/5/2017.
 */
public interface LexmarkProvisionFoIPApi {
    void provision() throws LexmarkFoIPException;
    void setDevice(String IPAddr_or_FQDN, String user, String password);
    void setDevice(String IPAddr_or_FQDN);
    void addTrialLicense();
    void addProviderSpecificFoIPTestLicense();
    void addPermanentLicense(String LicenseFilename, String LicenseFilenameEsf);
    void addDefaults(LexmarkSettingsGroup... groups);
    void addSetting(LexmarkSettingsGroup group, LexmarkSettingsName name, String value);
    void addFirmwareUpdate(LexmarkFirmwareUpdate fw);
    void addFirmwareLocalFile(String LocaFile);
    void addCheckSIPRegStatus();
    void setAutoDismiss(int Seconds);
    Map<String,String> getSettingsListMap();
    String getIPAddr_or_FQDN();
    String getUser();
    String getPassword();
    String getLicenseFilename();
    String getLicenseFilenameEsf();
    void addSendTestFax(String dialString);
}
