package com.lexmark.dev.FoIP;

/**
 * Created by Pete on 4/11/2017.
 */
public enum LexmarkSettingsGroup {
    FAX("mfp.fax."),
    SEND("mfp.fax.send."),
    RECV("mfp.fax.receive."),
    LOGS("mfp.fax.logs."),
    VOIP("mfp.fax.voip."),
    SIP("mfp.fax.sip."),
    H323("mfp.fax.h323."),
    T38("mfp.fax.t38."),
    HTTPS("mfp.fax.httpsSettings.");

    private String value;
    LexmarkSettingsGroup(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
