package com.lexmark.dev.FoIP;

import java.util.ArrayList;
import java.util.Collections;

import static com.lexmark.dev.FoIP.LexmarkSettingsGroup.*;

/**
 * Created by Pete on 4/11/2017.
 */
public enum LexmarkSettingsName {
    FAXNAME("stationName", FAX),
    FAXNUMBER("stationNumber", FAX),
    FAXID("stationID", FAX),                    // "0" = Name  "1" = Number
    FAXMODE("mode", FAX),                       // "0" = Analog/T.38/G.711  "2" = Fax Server
    DIALINGPREFIX("dialingPrefix", SEND),
    AUTOREDIAL("autoRedial", SEND),
    REDIALFREQUENCY("redialFrequency", SEND),
    RINGSTOANSWER("ringsToAnswer", RECV),
    TRANSMISSIONLOG("transmissionLog", LOGS),
    RECEIVEERRORLOG("receiveErrorLog", LOGS),
    FAXTRANSPORT("faxTransport", VOIP),          // "0" = Analog  "1" = T.38  "2" = G.711 "4" = etherFAX
                                                 // "5" = NativeFAX Direct "6" = VoIP Provider
    PROTOCOL("protocol", VOIP),                  // "1" = SIP  "2" = H.232
    TRACELEVEL("traceLevel", VOIP),
    STUNSERVER("stunServer", VOIP),
    FORCEFAXMODE("forceFaxMode", VOIP),
    FORCEFAXMODEDELAY("forceFaxModeDelay", VOIP),
    PROXY("proxy", SIP),
    REGISTRAR("registrar", SIP),
    USER("user", SIP, H323),
    PASSWORD("password", SIP, H323),
    CONTACT("contact", SIP),
    REALM("realm", SIP),
    AUTHID("authID", SIP),
    OUTBOUNDPROXY("outboundProxy", SIP),
    REGTRANSPORT("registrationTrasnport", SIP),  // "0" = UDP  "1" = TCP
    INCOMINGTRANSPORT("incomingTransport", SIP), // "0" = UDP  "1" = TCP  "2" = UDP and TCP
    OUTGOINGTRANSPORT("outgoingTransport", SIP), // "0" = UDP  "1" = TCP
    GATEWAY("gateway", H323),
    GATEKEEPER("gatekeeper", H323),
    ENABLEFASTSTART("enableFastStart", H323),
    DISABLEH245TUNNEL("disableH245Tunnel", H323),
    DISABLEGATEKEEPERDISCOVERY("disableGatekeeperDiscovery", H323),
    INDICATORREDUNDANCY("indicatorRedundancy", T38),
    LOWSPEEDREDUNDANCY("lowSpeedRedundancy", T38),
    HIGHSPEEDREDUNDANCY("highSpeedRedundancy", T38),
    UDPTLKEEPALIVEINTERVAL("udptlKeepAliveInterval", T38),
    HTTPSPROXY("proxy", HTTPS),
    HTTPSPROXYUSER("proxyUser", HTTPS),
    HTTPSPROXYPASSWORD("proxyPassword", HTTPS),
    ENABLEPEERVERIFICATION("enablePeerVerification", HTTPS),
    ENCRYPTFAXSEND("encryptFaxSend", HTTPS),
    ENCRYPTFAXRECEIVE("encryptFaxReceive", HTTPS),
    SERVICEURL("serviceUrl", HTTPS);


    private String value;
    private ArrayList<LexmarkSettingsGroup> groups = new ArrayList<>();

    LexmarkSettingsName(String value, LexmarkSettingsGroup... groups) {
        this.value = value;
        Collections.addAll(this.groups, groups);
    }

    public String getValue() {
        return value;
    }

    public boolean inGroup(LexmarkSettingsGroup group) {
        return this.groups.contains(group);
    }
}
