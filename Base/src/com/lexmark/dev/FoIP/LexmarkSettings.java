package com.lexmark.dev.FoIP;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Pete on 4/7/2017.
 */

public class LexmarkSettings {

    public LexmarkSettings() {
        SettingsList = new HashMap<>();
    }

    Map<String,String> list() {
        return SettingsList;
    }

    public void add(LexmarkSettingsGroup group, LexmarkSettingsName name, String value) {
        SettingsList.put(group.getValue() + name.getValue(), value);
    }

    private void addDefaults() {
        addDefaults(LexmarkSettingsGroup.FAX);
        addDefaults(LexmarkSettingsGroup.SEND);
        addDefaults(LexmarkSettingsGroup.RECV);
        addDefaults(LexmarkSettingsGroup.LOGS);
        addDefaults(LexmarkSettingsGroup.VOIP);
        addDefaults(LexmarkSettingsGroup.SIP);
        addDefaults(LexmarkSettingsGroup.H323);
        addDefaults(LexmarkSettingsGroup.T38);
        addDefaults(LexmarkSettingsGroup.HTTPS);
    }

    public void addDefaults(LexmarkSettingsGroup... groups) {
        if (groups.length == 0) {
            addDefaults();
        }
        else {
            for (LexmarkSettingsGroup group : groups) {
                Map<String, String> map = defaults.get(group.getValue());
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    SettingsList.put(group.getValue() + entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public Map<String,String> getListMap() {
        return SettingsList;
    }

    @Override
    public String toString() {
        Map<String, String> map = new TreeMap<>(SettingsList);
        StringBuilder out = new StringBuilder(getClass().getSimpleName() + " [\n");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.append("   \"").append(entry.getKey()).append("\" : \"").append(entry.getValue()).append("\"\n");
        }
        out.append("]\n");
        return out.toString();
    }

    private Map<String, String> SettingsList;

    private static final Map<String, String> defaultsFAX = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.FAXID.getValue(), "0");      // FaxLexmarkSettingsName
            put(LexmarkSettingsName.FAXMODE.getValue(), "0");   // Analog
        }
    };
    private static final Map<String, String> defaultsSEND = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.DIALINGPREFIX.getValue(), "");
            put(LexmarkSettingsName.AUTOREDIAL.getValue(), "5");
            put(LexmarkSettingsName.REDIALFREQUENCY.getValue(), "3");
        }
    };

    private static final Map<String, String> defaultsRECV = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.RINGSTOANSWER.getValue(), "3");
        }
    };

    private static final Map<String, String> defaultsLOGS = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.TRANSMISSIONLOG.getValue(), "0");  // PrintLog
            put(LexmarkSettingsName.RECEIVEERRORLOG.getValue(), "1");  // PrintNever
        }
    };

    private static final Map<String, String> defaultsVOIP = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.FAXTRANSPORT.getValue(), "1");   // T.38
            put(LexmarkSettingsName.PROTOCOL.getValue(), "1");       // SIP
            put(LexmarkSettingsName.TRACELEVEL.getValue(), "0");
            put(LexmarkSettingsName.STUNSERVER.getValue(), "");
            put(LexmarkSettingsName.FORCEFAXMODE.getValue(), "0");
            put(LexmarkSettingsName.FORCEFAXMODEDELAY.getValue(), "0");
        }
    };

    private static final Map<String, String> defaultsSIP = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.PROXY.getValue(), "");
            put(LexmarkSettingsName.REGISTRAR.getValue(), "");
            put(LexmarkSettingsName.USER.getValue(), "");
            put(LexmarkSettingsName.PASSWORD.getValue(), "");
            put(LexmarkSettingsName.CONTACT.getValue(), "");
            put(LexmarkSettingsName.REALM.getValue(), "");
            put(LexmarkSettingsName.AUTHID.getValue(), "");
            put(LexmarkSettingsName.OUTBOUNDPROXY.getValue(), "");
            put(LexmarkSettingsName.REGTRANSPORT.getValue(), "0");       // UDP
            put(LexmarkSettingsName.INCOMINGTRANSPORT.getValue(), "2");  // UDP and TCP
            put(LexmarkSettingsName.OUTGOINGTRANSPORT.getValue(), "0");  // UDP

            put("disableIncoming", "0");
            put("dialOutDigit", "-1");
        }
    };

    private static final Map<String, String> defaultsH323 = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.GATEWAY.getValue(), "");
            put(LexmarkSettingsName.GATEKEEPER.getValue(), "");
            put(LexmarkSettingsName.USER.getValue(), "");
            put(LexmarkSettingsName.PASSWORD.getValue(), "");
            put(LexmarkSettingsName.ENABLEFASTSTART.getValue(), "0");
            put(LexmarkSettingsName.DISABLEH245TUNNEL.getValue(), "0");
            put(LexmarkSettingsName.DISABLEGATEKEEPERDISCOVERY.getValue(), "1");

            put("disableIncoming", "0");
            put("dialOutDigit", "-1");
        }
    };

    private static final Map<String, String> defaultsT38 = new HashMap<String, String>() {
        {
            put(LexmarkSettingsName.INDICATORREDUNDANCY.getValue(), "3");
            put(LexmarkSettingsName.LOWSPEEDREDUNDANCY.getValue(), "3");
            put(LexmarkSettingsName.HIGHSPEEDREDUNDANCY.getValue(), "1");
            put(LexmarkSettingsName.UDPTLKEEPALIVEINTERVAL.getValue(), "0");
        }
    };

    private static final Map<String, String> defaultsHTTPS = new HashMap <String, String>() {
        {
            put(LexmarkSettingsName.HTTPSPROXY.getValue(), "");
            put(LexmarkSettingsName.HTTPSPROXYUSER.getValue(), "");
            put(LexmarkSettingsName.HTTPSPROXYPASSWORD.getValue(), "");
            put(LexmarkSettingsName.ENABLEPEERVERIFICATION.getValue(), "1");
            put(LexmarkSettingsName.ENCRYPTFAXSEND.getValue(), "1");
            put(LexmarkSettingsName.ENCRYPTFAXRECEIVE.getValue(), "1");
            put(LexmarkSettingsName.SERVICEURL.getValue(), "");
        }
    };

    private static final Map<String, Map<String, String>> defaults = new HashMap<String, Map<String, String>>() {
        {
            put(LexmarkSettingsGroup.FAX.getValue(), defaultsFAX);
            put(LexmarkSettingsGroup.SEND.getValue(), defaultsSEND);
            put(LexmarkSettingsGroup.RECV.getValue(), defaultsRECV);
            put(LexmarkSettingsGroup.LOGS.getValue(), defaultsLOGS);
            put(LexmarkSettingsGroup.VOIP.getValue(), defaultsVOIP);
            put(LexmarkSettingsGroup.SIP.getValue(), defaultsSIP);
            put(LexmarkSettingsGroup.H323.getValue(), defaultsH323);
            put(LexmarkSettingsGroup.T38.getValue(), defaultsT38);
            put(LexmarkSettingsGroup.HTTPS.getValue(), defaultsHTTPS);
        }
    };
}


