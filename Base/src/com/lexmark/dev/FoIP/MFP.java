package com.lexmark.dev.FoIP;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lexmark.dev.FoIP.LexmarkSettingsName.*;
import static com.lexmark.dev.FoIP.LexmarkSettingsGroup.*;
import static java.sql.Types.NULL;

/**
 * Created by Pete on 2/23/2017.
 */
public class MFP extends NetworkDevice {
    private String        _ModelName = "";
    private String        _SerialNumber = "";
    private Integer       _PrinterNPAPID = 0;
    private String        _FirmwareRevision = "";
    private String        _BaseCodeRevision = "";
    private String        _TimeDate = "";
    private String        _TB_EEC_CT = "";
    private String        _UAA = "";
    private PrtFam        _Family = PrtFam.UNKNOWN;
    private boolean       _validData = false;
    private String        _user = "";
    private String        _password = "";
    private boolean        _authRequired = false;
    private ArrayList<String> _lastRegistrationData;
    private ArrayList<String> _lastEtherFAXData;
    private boolean       _HTTPSFax;

    enum PrtFam {
        SIM,
        UNKNOWN,
        LEDO,
        HYDRA,
        PRIDE,
        HS,
        WC,
        YAPP,
        COATL,
        GDW,
        GSM,
        ATLANTIC,
        ZJ,
        BRIDGES,
        FISHES,
        BOATS,
        BBN,
    }

    static final int MAX_MFPS_IN_STORE  = 500;

    public String toString() {
        String http;
        String valid;
        String etherFAX;
        if (_useHttps) http="https"; else http="http";
        if (_validData) valid="Valid"; else valid="Invalid";
        if (_HTTPSFax) etherFAX="{etherFAX}"; else etherFAX="{}";
        return IPAddr() + "; "
                + valid + "; "
                + http + "; "
                + _ModelName + "; "
                + _SerialNumber + "; "
                + _BaseCodeRevision + "; "
                + _FirmwareRevision + "; "
                + PrinterFamilyString() + "; "
                + GetCodeName(_PrinterNPAPID) + "; "
                + _PrinterNPAPID + "; "
                + etherFAX + "; "
                + Cookies;
    }

    public Integer GetNPAPID(String Model) {
        Integer value;
        Model = Model.toUpperCase();
        HashMap<String,Integer> NPAPID_Map = new HashMap<String,Integer>();
        NPAPID_Map.put("XM71",0x80);
        NPAPID_Map.put("MX81",0x80);
        NPAPID_Map.put("XM51",0x81);
        NPAPID_Map.put("B546",0x81);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        NPAPID_Map.put("XM81",0x80);
        value = NPAPID_Map.get(Model);
        if (value != null)
            return value;
        else
            return new Integer(0);
        /*
        if (Model.startsWith("MX81",0)) return 0x80;
        else if (Model.startsWith("XM71",0)) return 0x80;
        else if (Model.startsWith("B5465",0)) return 0x81;
        else if (Model.startsWith("XM5163",0)) return 0x81;
        else if (Model.startsWith("XM5163",0)) return 0x81;
        return 0;
        */
    }

    public String GetCodeName(Integer npapid) {
        switch (npapid) {

            // LEDO
            case 0x50: return "Longbow L";
            case 0x51: return "Astros";
            case 0x54: return "Elway";
            case 0x55: return "Brees";
            case 0x56: return "Longbow M";
            case 0x57: return "Longbow H";

            // Hydra
            case 0x58: return "Montana";

            // Pride
            case 0x4E: return "Joplin";
            case 0x4F: return "Striker";
            case 0x52: return "Manning";
            case 0x53: return "Flamingo";
            case 0x59: return "Bradshaw";
            case 0x5A: return "Callisto";
            case 0x5B: return "Manning M";
            case 0x5C: return "Manning L";
            case 0x5D: return "Jib";
            case 0x5E: return "Spinnaker";
            case 0x5F: return "Montana L";

            // HS
            case 0x60: return "Mirage";
            case 0x61: return "Super Joplin";
            case 0x62: return "Cayman L";
            case 0x63: return "Cayman M";
            case 0x64: return "Cayman H";
            case 0x65: return "Cardamom";
            case 0x66: return "Clover";
            case 0x67: return "Hickory";
            case 0x68: return "Callisto L";
            case 0x69: return "Topaz";
            case 0x6A: return "Turquoise";
            case 0x6B: return "Rebel";
            case 0x6C: return "Tiger Hercules";
            case 0x6D: return "Gator";
            case 0x6E: return "Jaguar";
            case 0x6F: return "Viking";
            case 0x70: return "Inkjet???";
            case 0x71: return "Inkjet???";
            case 0x72: return "Inkjet???";
            case 0x73: return "Inkjet???";
            case 0x74: return "Inkjet???";
            case 0x75: return "Inkjet???";
            case 0x76: return "Inkjet???";
            case 0x77: return "Camaro L";
            case 0x78: return "Camaro H";
            case 0x79: return "New York H";

            // WC
            case 0x80: return "Tuscany";
            case 0x81: return "Buckeye M";
            case 0x82: return "Buckeye H";
            case 0x83: return "Denali LMHN";
            case 0x84: return "Denali LNE";
            case 0x85: return "Denali HNE";
            case 0x86: return "Denali LHHR";
            case 0x87: return "Pirate";
            case 0x88: return "MVP Cumberland";
            case 0x89: return "Sparrow L";
            case 0x8A: return "Sparrow M/H";
            case 0x8B: return "BlackBeard";
            case 0x8C: return "Voyager L";
            case 0x8D: return "Voyager M";
            case 0x8E: return "Voyager H";
            case 0x8F: return "Gemini L";
            case 0x90: return "Gemini M";
            case 0x91: return "Gemini H";
            case 0x92: return "Old Palazzo";
            case 0x93: return "Jaguar Denali LMHN";
            case 0x94: return "Jaguar Denali LNE";
            case 0x95: return "Jaguar Denali HNE";
            case 0x96: return "Jaguar Denali LHHR";
            case 0x97: return "Thunderball L";
            case 0x98: return "Thunderball ML2";
            case 0x99: return "Clapton M";
            case 0x9A: return "Clapton H";
            case 0x9B: return "Cream M";
            case 0x9C: return "Cream H";
            case 0x9D: return "Sahara";
            case 0x9E: return "Montego L/M/H";

            // YAPP
            case 0xA9: return "Yankees 4.3";
            case 0xAA: return "Paris 7";
            case 0xAB: return "Palazzo 10.2";

            // COATL
            case 0xAC: return "Cobra 4.3 SFP";
            case 0xAD: return "Atlantis 7.0 MFP";

            // GDW
            case 0xAE: return "Glacier 2.4";
            case 0xAF: return "Glacier 4.3";
            case 0xB0: return "Dunes 7.0";
            case 0xB1: return "Dunes 10.2";
            case 0xB2: return "Wynn 10.2";

            // GSM
            case 0xB3: return "Goldfinger L 2L or M1/M2/H1/H2 2.4";
            case 0xB4: return "Goldfinger H2 4.3";
            case 0xB5: return "Skyfall L 2.4";
            case 0xB6: return "Skyfall M/H 4.3";
            case 0xB7: return "Skyfall H+ 4.3";
            case 0xB8: return "Moonraker 7";

            // Atlantic
            case 0xBE: return "Swordfish L/H";
            case 0xBF: return "Mahi L/ML/MH/H";

            // ZJ
            case 0xC0: return "Zeus L/M/H1 2.4";
            case 0xC1: return "Zeus H2 4.3";
            case 0xC2: return "Jupiter L 2.4";
            case 0xC3: return "Jupiter M 4.3";
            case 0xC4: return "Jupiter H 7";

            // BRIDGES
            case 0xC5: return "Sidu";
            case 0xC6: return "Goldengate";

            // FISHES
            case 0xC7: return "Bluering";
            case 0xC8: return "Lionfish";

            // BOATS
            case 0xC9: return "Baja";
            case 0xCA: return "Donzi";

            // Another FISH
            case 0xCB: return "Lionfish L";

            // Another BRIDGE
            case 0xCC: return "Goldengate 2.8";

            // BBN
            case 0xCD: return "Big Blue";
            case 0xCE: return "Neddlefish";

            default:   return "Unknown";
        }
    }

    public String GetModelNames(Integer npapid) {
        switch (npapid) {

            // LEDO
            case 0x50: return "Longbow L";
            case 0x51: return "Astros";
            case 0x54: return "Elway";
            case 0x55: return "Brees";
            case 0x56: return "Longbow M";
            case 0x57: return "Longbow H";

            // Hydra
            case 0x58: return "Montana";

            // Pride
            case 0x4E: return "Joplin";
            case 0x4F: return "Striker";
            case 0x52: return "Manning";
            case 0x53: return "Flamingo";
            case 0x59: return "Bradshaw";
            case 0x5A: return "Callisto";
            case 0x5B: return "Manning M";
            case 0x5C: return "Manning L";
            case 0x5D: return "Jib";
            case 0x5E: return "Spinnaker";
            case 0x5F: return "Montana L";

            // HS
            case 0x60: return "Mirage";
            case 0x61: return "Super Joplin";
            case 0x62: return "Cayman L";
            case 0x63: return "Cayman M";
            case 0x64: return "Cayman H";
            case 0x65: return "Cardamom";
            case 0x66: return "Clover";
            case 0x67: return "Hickory";
            case 0x68: return "Callisto L";
            case 0x69: return "Topaz";
            case 0x6A: return "Turquoise";
            case 0x6B: return "Rebel";
            case 0x6C: return "Tiger Hercules";
            case 0x6D: return "Gator";
            case 0x6E: return "Jaguar";
            case 0x6F: return "Viking";
            case 0x70: return "Inkjet???";
            case 0x71: return "Inkjet???";
            case 0x72: return "Inkjet???";
            case 0x73: return "Inkjet???";
            case 0x74: return "Inkjet???";
            case 0x75: return "Inkjet???";
            case 0x76: return "Inkjet???";
            case 0x77: return "Camaro L";
            case 0x78: return "Camaro H";
            case 0x79: return "New York H";

            // WC
            case 0x80: return "MX81* XM71*";
            case 0x81: return "XM5163 B5465dnf";
            case 0x82: return "MX711 XM5170";
            case 0x83: return "";
            case 0x84: return "";
            case 0x85: return "";
            case 0x86: return "";
            case 0x87: return "";
            case 0x88: return "";
            case 0x89: return "MX31*";
            case 0x8A: return "MX41* MX51* XM1145";
            case 0x8B: return "MX61* XM3150";
            case 0x8C: return "";
            case 0x8D: return "";
            case 0x8E: return "";
            case 0x8F: return "CX310";
            case 0x90: return "CX410";
            case 0x91: return "CX510 XC2132";
            case 0x92: return "";
            case 0x93: return "";
            case 0x94: return "";
            case 0x95: return "";
            case 0x96: return "";
            case 0x97: return "";
            case 0x98: return "";
            case 0x99: return "";
            case 0x9A: return "";
            case 0x9B: return "";
            case 0x9C: return "";
            case 0x9D: return "";
            case 0x9E: return "MX91* XM91*";

            // YAPP
            case 0xA9: return "";
            case 0xAA: return "XC6152";
            case 0xAB: return "CX825 CX827 CX860 XC8155 XC8160";

            // COATL
            case 0xAC: return "";
            case 0xAD: return "CX725 CX727 XC4140 XC4150";

            // GDW
            case 0xAE: return "";
            case 0xAF: return "";
            case 0xB0: return "MX72* XM5370 MB2770";
            case 0xB1: return "";
            case 0xB2: return "MX82* XM7355 XM7370";

            // GSM
            case 0xB3: return "";
            case 0xB4: return "";
            case 0xB5: return "";
            case 0xB6: return "MX42* MX52* XM1242 MB2442 MB2546";
            case 0xB7: return "MX522 XM1246";
            case 0xB8: return "MX622 XM3250 MB2650";

            // Atlantic
            case 0xBE: return "";
            case 0xBF: return "CX92*";

            // ZJ
            case 0xC0: return "";
            case 0xC1: return "";
            case 0xC2: return "CX421 MC2325 MC2425";
            case 0xC3: return "CX522 CX622 XC2235 MC2535";
            case 0xC4: return "CX625 XC4240 MB2640";

            // BRIDGES
            case 0xC5: return "";
            case 0xC6: return "MB2236";

            // FISHES
            case 0xC7: return "";
            case 0xC8: return "CX331 MC3224 MC3326";

            // BOATS
            case 0xC9: return "Baja";
            case 0xCA: return "Donzi";

            // Another FISH
            case 0xCB: return "Lionfish L";

            // Another BRIDGE
            case 0xCC: return "Goldengate 2.8";

            // BBN
            case 0xCD: return "Big Blue";
            case 0xCE: return "Neddlefish";

            default:   return "Unknown";
        }
    }

    private PrtFam GetPrinterFamily(Integer npapid) {
        if (npapid == 0)
            return PrtFam.SIM;
        if (npapid < 0x4E)
            return PrtFam.UNKNOWN;
        else if (npapid == 0x4E || npapid == 0x4F || npapid == 0x52 || npapid == 0x53)
            return PrtFam.PRIDE;
        else if (npapid == 0x58)
            return PrtFam.HYDRA;
        else if (npapid < 0x58)
            return PrtFam.LEDO;
        else if (npapid < 0x60)
            return PrtFam.PRIDE;
        else if (npapid < 0x80)
            return PrtFam.HS;
        else if (npapid < 0xA9)
            return PrtFam.WC;
        else if (npapid < 0xAC)
            return PrtFam.YAPP;
        else if (npapid < 0xAE)
            return PrtFam.COATL;
        else if (npapid < 0xB3)
            return PrtFam.GDW;
        else if (npapid < 0xBE)
            return PrtFam.GSM;
        else if (npapid < 0xC0)
            return PrtFam.ATLANTIC;
        else if (npapid < 0xC5)
            return PrtFam.ZJ;
        else if (npapid < 0xC7)
            return PrtFam.BRIDGES;
        else if (npapid < 0xC9)
            return PrtFam.FISHES;
        else if (npapid < 0xCB)
            return PrtFam.BOATS;
        else if (npapid == 0xCB)
            return PrtFam.FISHES;
        else if (npapid == 0xCC)
            return PrtFam.BRIDGES;
        else if (npapid < 0xCF)
            return PrtFam.BBN;
        else
            return PrtFam.UNKNOWN;
    }

    private String GrabAfterColon(String Line) {
        String Value = "";
        Pattern p = Pattern.compile(".*: (.+)");
        Matcher m = p.matcher(Line);
        if (m.find())
            Value =  m.group(1);

        return Value;
    }

    void ClearData() {
        setValidData(false);
        Cookies.clear();
        Cookies.add("lexlang=\"0\"");
        Cookies.add("lang=en");
    }

    public void Refresh() {
        java.util.List<String> History = GetHistory();
        if ((History.size() == 0) && !_user.isEmpty()) {
            System.out.println("History size 0, need to login");
            MFPSecurity mfpsec = new MFPSecurity(this);
            int rc = mfpsec.LogIn();
            System.out.println("Login rc = " + rc);
            History = GetHistory();
        }
        for (String Line : History) {
            if (Line.contains("Model Name"))        _ModelName        = GrabAfterColon(Line);
            if (Line.contains("Serial Number"))     _SerialNumber     = GrabAfterColon(Line);
            if (Line.contains("Printer NPAP ID"))   _PrinterNPAPID    = Integer.parseInt(GrabAfterColon(Line));
            if (Line.contains("Firmware Revision")) _FirmwareRevision = GrabAfterColon(Line);
            if (Line.contains("Time/Date"))         _TimeDate         = GrabAfterColon(Line);
            if (Line.contains("TB EEC CT"))         _TB_EEC_CT        = GrabAfterColon(Line);
            if (Line.contains("@UAA"))              _UAA              = Line;
        }

        // For some reason HS models do not have the Printer NPAP ID in history,
        // so we fix it here.
        if (_PrinterNPAPID == 0) {
            if (_ModelName.contains("X925"))
                _PrinterNPAPID = 103;
            else if (_ModelName.contains("X548"))
                _PrinterNPAPID = 111;
            else if (_ModelName.contains("C925"))
                _PrinterNPAPID = 102;
            else if (_ModelName.contains("C95"))
                _PrinterNPAPID = 105;
            else if (_ModelName.contains("C746"))
                _PrinterNPAPID = 119;
            else if (_ModelName.contains("C748"))
                _PrinterNPAPID = 120;
            else if (_ModelName.contains("CS748"))
                _PrinterNPAPID = 120;
            else if (_ModelName.contains("C79"))
                _PrinterNPAPID = 90;
            else if (_ModelName.contains("X79"))
                _PrinterNPAPID = 96;
            else if (_ModelName.contains("XS79"))
                _PrinterNPAPID = 96;
            else if (_ModelName.contains("X92"))
                _PrinterNPAPID = 103;
            else if (_ModelName.contains("XS92"))
                _PrinterNPAPID = 103;
            else if (_ModelName.contains("X95"))
                _PrinterNPAPID = 106;
            else if (_ModelName.contains("X74"))
                _PrinterNPAPID = 121;
            else if (_ModelName.contains("XS748"))
                _PrinterNPAPID = 121;
            else if (_ModelName.contains("6500"))
                _PrinterNPAPID = 110;
        }

        _BaseCodeRevision = GetMFPCodeLevel();

        _Family = GetPrinterFamily(_PrinterNPAPID);

        _HTTPSFax = HTTPSFaxCheck();
    }

    public MFP(Integer npapid) {
        super("0.0.0.0");
        _PrinterNPAPID = npapid;
    }
    public MFP(String IPAddr) {
        super(IPAddr);
                //_IPAddr = IPAddr;
        //Cookies = new ArrayList<String>();
        //if (NeedToSwitchToHttps()) {
        //    setUseHttps(true);
        //}
        _lastRegistrationData = new ArrayList<>();
        _lastRegistrationData.add("No SIP Registration Data");
        _lastEtherFAXData = new ArrayList<>();
        _lastEtherFAXData.add("No etherFAX Status Data");
        if (getValidTransport())
            Refresh();
    }

    public MFP(String IPAddr, String user, String password) {
        super(IPAddr);
        _user = user;
        _password = password;
        _lastRegistrationData = new ArrayList<>();
        _lastRegistrationData.add("No SIP Registration Data");
        _lastEtherFAXData = new ArrayList<>();
        _lastEtherFAXData.add("No etherFAX Status Data");
        if (getValidTransport())
            Refresh();
    }

    public boolean validData() {
        return _validData;
    }

    public String IPAddr() {
        return _IPAddr;
    }

    public String ModelName() {
        return _ModelName;
    }

    public String SerialNumber() {
        return _SerialNumber;
    }

    public Integer PrinterNPAPID() {
        return _PrinterNPAPID;
    }

    public PrtFam PrinterFamily() {
        return _Family;
    }

    public String FirmwareRevision() {
        return _FirmwareRevision;
    }

    public String BaseCodeRevision() { return _BaseCodeRevision; }

    public String TimeDate() {
        return _TimeDate;
    }

    public String TB_EEC_CT() {
        return _TB_EEC_CT;
    }

    public String UAA() {
        return _UAA;
    }

    private boolean getValidData() { return _validData; }

    private void setValidData(boolean value) { _validData = value; }

    public boolean getAuthRequired()  { return _authRequired; }

    private void setAuthRequired() { _authRequired = true; }

    public void setUser(String user) { _user = user; }

    String getUser() { return _user; }

    public void setPassword(String password) { _password = password; }

    String getPassword() { return _password; }

    public boolean HTTPSFaxSupported() { return _HTTPSFax; }

    public boolean MojaOrLaterDevice() {
        return !(_PrinterNPAPID < 0xA9 && _PrinterNPAPID > 0x00);
    }

    public boolean VCCisSupported() {
        return _PrinterNPAPID >= 0x80;
    }

    public String GetCodeName() {
        return GetCodeName(_PrinterNPAPID);
    }

    public boolean vccSupported() {
        return _PrinterNPAPID >= 0x80;
    }

    public ArrayList<String> getLastRegistrationData() { return _lastRegistrationData; }

    public ArrayList<String> getLastHTTPSFaxData() { return _lastEtherFAXData; }

    public String PrinterFamilyString() {
        if (_Family == PrtFam.SIM) return "SIM";
        if (_Family == PrtFam.LEDO) return "LEDO";
        if (_Family == PrtFam.HYDRA) return "Hydra";
        if (_Family == PrtFam.PRIDE) return "Pride";
        if (_Family == PrtFam.HS) return "HS";
        if (_Family == PrtFam.WC) return "WC";
        if (_Family == PrtFam.YAPP) return "YaPP";
        if (_Family == PrtFam.COATL) return "CoAtl";
        if (_Family == PrtFam.GDW) return "GDW";
        if (_Family == PrtFam.GSM) return "GSM";
        if (_Family == PrtFam.ATLANTIC) return "Atlantic";
        if (_Family == PrtFam.ZJ) return "ZJ";
        if (_Family == PrtFam.BRIDGES) return "Bridges";
        if (_Family == PrtFam.FISHES) return "Fishes";
        if (_Family == PrtFam.BOATS) return "Boats";
        if (_Family == PrtFam.BBN) return "BBN";
        return "Unknown";
    }

    private ArrayList<String> GetHistory() {
        String line;
        boolean WriteData = false;
        boolean ConnectionRefused = false;
        WebConnection connection = null;
        ArrayList<String> History = new ArrayList<>();
        if (IPAddr().isEmpty()) {
            return History;
        }
        try {
            connection = new WebConnection(this,"/cgi-bin/history",getUseHttps());
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            int i=0;
            while(((line = rd.readLine()) != null) && ++i<15) {
                if (line.contains("Login Form")) {
                    setAuthRequired();
                    break;
                }
                if (WriteData) {
                    History.add(line);
                    setValidData(true);
                }
                String[] tokens = line.split("[ ]+");
                if ((tokens.length > 0) && (tokens[0].equals("History")))
                    WriteData = true;
                if (tokens.length == 0)
                    WriteData = false;
            }
            rd.close();

        } catch (Exception e) {
            setValidData(false);
            if (e.toString().contains("401")) {
                setAuthRequired();
            }
            else if (e.toString().contains("Connection refused")) {
                ConnectionRefused = true;
                System.out.println("Failed");
                System.out.print("Trying https...");
            }
            else {
                System.out.println("Failed");
                System.out.println("" + e);
            }
        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }

        // If the data in not valid and using http and we got "Connection Refused", then try https.
        if ((!getValidData()) && (!getUseHttps()) && (ConnectionRefused)) {
            setUseHttps(true);
            return GetHistory();
        }
        else {
            return History;
        }
    }

    public boolean NeedToSwitchToHttps() {
        boolean rc = false;
        WebConnection connection = null;

        if (this.IPAddr().isEmpty()) {
            return rc;
        }
        try {
            connection = new WebConnection(MFPStore.getInstance().Get(this.IPAddr()),"/",this.getUseHttps());
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            rd.close();

        } catch (Exception e) {
            if (e.toString().contains("Connection refused")) {
                rc = true;
            }
            // Windows does not return Connection refused when using http
            // when it is disabled on the device, so we have to try https
            // on connect timed out.
            else if (System.getProperty("os.name").contains("Windows")
                    && e.toString().contains("connect timed out")) {
                rc = true;
            }
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;
    }
    public String GetSIPRegStatusIfDifferent(ArrayList<String> old) {
        String result = GetSIPRegStatus();

        if (old.equals(_lastRegistrationData))
            return "No SIP Registration Data";
        else
            return result;
    }

    public String GetSIPRegStatus() {
        String line;
        String result = "No SIP Registration Data";
        ArrayList<String> outputList = new ArrayList<>();
        boolean WriteData = false;
        WebConnection connection = null;

        System.out.print("Getting SIP Registration Status from " + _IPAddr + "...");

        if (MojaOrLaterDevice()) {

            try {
                //Create connection
                connection = new WebConnection(this,"/webglue/content?c=%2FSettings%2FFax%2FAnalogFaxSetup&lang=en");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                line = rd.readLine();
                if (!line.equals("")) { // The first line should be empty
                    System.out.println("First line not empty, so trying FoIP License version");
                    connection.disconnect();
                    connection = new WebConnection(this,"/webglue/content?c=%2FSettings%2FFax%2FAnalogFaxSetupT38G711&lang=en");
                    is = connection.getInputStream();
                    rd = new BufferedReader(new InputStreamReader(is));
                }
                while((line = rd.readLine()) != null) {
                    if (WriteData) {
                        if (line.contains("<div class")) {
                            String[] tokens = line.split("[><]+");
                            line = tokens[2];
                            if (!line.equals("/div")) {
                                tokens = line.split("&#xA;");
                                result = tokens[0];
                                for (String token : tokens) {
                                    outputList.add("\t" + token);
                                }
                            }
                            WriteData = false;
                        }
                    }
                    else {
                        if (line.contains("SIP Registration Status")) {
                            WriteData = true;
                        }
                    }
                }
                if (result.equals("No SIP Registration Data"))
                    outputList.add("\t" + result);
                //WriteToResultArea("\t" + result);

                System.out.println("OK");
                for (String temp : outputList) {
                    System.out.println(temp);
                }

                rd.close();

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        else {

            try {
                //Create connection
                connection = new WebConnection(this,"/cgi-bin/dynamic/printer/config/gen/fax/fax.html");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (WriteData) {
                        if (line.contains("</textarea>")) {
                            WriteData = false;
                        }
                        else {
                            outputList.add("\t" + line);
                            //WriteToResultArea("\t" + line);
                        }
                    }
                    else {
                        if (line.contains("SIP Registration Status")) {
                             if (line.contains("</textarea>")) {
                                result = "No SIP Registration Data";
                            }
                            else {
                                WriteData = true;
                                String[] tokens = line.split("[>]+");
                                outputList.add("\t" + tokens[tokens.length-1]);
                                //WriteToResultArea("\t" + tokens[tokens.length-1]);
                                result = tokens[tokens.length-1];
                            }
                        }
                    }
                }
                if (result.equals("No SIP Registration Data"))
                    outputList.add("\t" + result);
                //WriteToResultArea("\t" + result);

                System.out.println("OK");
                for (String temp : outputList) {
                    System.out.println(temp);
                }

                rd.close();

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }

        System.out.println("");

        _lastRegistrationData = outputList;

        return result;
    }

    public String GetHTTPSFaxStatus() {
        String line;
        String result = "No etherFAX Status Data";
        ArrayList<String> outputList = new ArrayList<>();
        boolean WriteData = false;
        boolean Done = false;
        WebConnection connection = null;

        System.out.print("Getting etherFAX Status from " + _IPAddr + "...");

        if (MojaOrLaterDevice()) {

            try {
                //Create connection
                connection = new WebConnection(this,"/webglue/content?c=%2FSettings%2FFax%2FAnalogFaxSetup&lang=en");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                line = rd.readLine();
                if (!line.equals("")) { // The first line should be empty
                    System.out.println("First line not empty, so trying FoIP License version");
                    connection.disconnect();
                    connection = new WebConnection(this,"/webglue/content?c=%2FSettings%2FFax%2FAnalogFaxSetupT38G711&lang=en");
                    is = connection.getInputStream();
                    rd = new BufferedReader(new InputStreamReader(is));
                }
                while((line = rd.readLine()) != null) {
                    if (WriteData) {
                        if (line.contains("<div class")) {
                            String[] tokens = line.split("[><]+");
                            line = tokens[2];
                            if (!line.equals("/div")) {
                                tokens = line.split("&#xA;");
                                result = tokens[0];
                                for (String token : tokens) {
                                    outputList.add("\t" + token);
                                }
                            }
                            WriteData = false;
                        }
                    }
                    else {
                        if (!Done && line.contains("HTTPS Fax Status")) {
                            WriteData = true;
                            Done = true;
                        }
                    }
                }
                if (result.equals("No etherFAX Status Data"))
                    outputList.add("\t" + result);
                //WriteToResultArea("\t" + result);

                System.out.println("OK");
                for (String temp : outputList) {
                    System.out.println(temp);
                }

                rd.close();

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        else {

            try {
                //Create connection
                connection = new WebConnection(this,"/cgi-bin/dynamic/printer/config/gen/fax/fax.html");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (WriteData) {
                        if (line.contains("</textarea>")) {
                            WriteData = false;
                        }
                        else {
                            outputList.add("\t" + line);
                            //WriteToResultArea("\t" + line);
                        }
                    }
                    else {
                        if (line.contains("HTTPS Fax Status")) {
                            if (line.contains("</textarea>")) {
                                result = "No etherFAX Status Data";
                            }
                            else {
                                WriteData = true;
                                String[] tokens = line.split("[>]+");
                                outputList.add("\t" + tokens[tokens.length-1]);
                                //WriteToResultArea("\t" + tokens[tokens.length-1]);
                                result = tokens[tokens.length-1];
                            }
                        }
                    }
                }
                if (result.equals("No etherFAX Status Data"))
                    outputList.add("\t" + result);
                //WriteToResultArea("\t" + result);

                System.out.println("OK");
                for (String temp : outputList) {
                    System.out.println(temp);
                }

                rd.close();

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }

        System.out.println("");

        _lastEtherFAXData = outputList;

        return result;
    }

    private String GetMFPCodeLevel() {
        String line;
        String result = "";
        WebConnection connection = null;

        // On Moja or later devices the code level is in the History.
        // Previous devices, the FW revision in History was the network code revision.
        if (MojaOrLaterDevice()) {
            return FirmwareRevision();
        }
        else {

            try {
                //Create connection
                connection = new WebConnection(this,"/cgi-bin/dynamic/printer/config/reports/deviceinfo.html");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while(null != (line = rd.readLine())) {
                    if (line.contains(">Base<")) {
                        String[] tokens = line.split(" = {2}");
                        result = tokens[1];
                        tokens = result.split("-0");
                        result = tokens[0];
                        result = result.replace("&nbsp;"," ");
                        break;
                    }
                }

                rd.close();

            } catch (Exception e) {

                System.out.println("Failed to get device info.");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }

        return result;
    }

    public String GetLastFaxStatus() {
        String line = "";
        String Date = "?";
        String Time = "?";
        String Type = "?";
        String DialedNumber = "?";
        String Status = "?";
        boolean FoundCallLog = false;
        WebConnection connection = null;
        if (IPAddr().isEmpty()) {
            System.out.println("Must specify Tester MFP FQDN or IP Address.");
            return(Date + "|" + Time + "|" + Type + "|" + DialedNumber + "|" + Status);
        }
        try {
            while (FoundCallLog == false) {
                //Create connection
                if (MojaOrLaterDevice()) {
                    connection = new WebConnection(this, "/cgi-bin/faxcalllog");
                    connection.BasicGetSetup();
                } else {
                    connection = new WebConnection(this, "/cgi-bin/script/printer/faxcalllog");
                    connection.BasicGetSetup();
                }

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                String oldLine = "";

                while ((line = rd.readLine()) != null) {
                    if (FoundCallLog) {
                        if (MojaOrLaterDevice()) {
                            if (line.length() > 7) {
                                if ((line.substring(0, 7).equals("Summary")) && (oldLine.length() > 74)) {
                                    Date = oldLine.substring(0, 10);
                                    Time = oldLine.substring(12, 17);
                                    Type = oldLine.substring(19, 23);
                                    DialedNumber = oldLine.substring(47, 67).trim();
                                    Status = oldLine.substring(74).trim();
                                }
                                oldLine = line;
                            }
                        } else {
                            if (line.length() > 5) {
                                if ((line.substring(0, 5).equals("Total")) && (oldLine.length() > 79)) {
                                    Date = oldLine.substring(6, 16);
                                    Time = oldLine.substring(17, 22);
                                    Type = oldLine.substring(42, 46);
                                    DialedNumber = oldLine.substring(47, 67).trim();
                                    Status = oldLine.substring(79).trim();
                                }
                                oldLine = line;
                            }
                        }
                    } else {
                        if (line.contains("Fax Call Log")) {
                            FoundCallLog = true;
                        }
                    }
                }

                rd.close();
            }

        } catch (Exception e) {

            System.out.println("Failed");
            Date = "-";
            Time = "-";
            Type = "-";
            DialedNumber = "-";
            Status = "-";
            System.out.println("" + e);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
        System.out.println(Date + "|" + Time + "|" + Type + "|" + DialedNumber + "|" + Status);
        return(Date + "|" + Time + "|" + Type + "|" + DialedNumber + "|" + Status);
    }
    public String GetLastFaxStatus2() {
        String line;
        String DateTime = "?";
        String DialedNumber = "?";
        String Status = "?";
        WebConnection connection = null;
        if (IPAddr().isEmpty()) {
            System.out.println("Must specify Tester MFP FQDN or IP Address.");
            return(DateTime + "|" + DialedNumber + "|" + Status);
        }
        try {
            //Create connection
            if (MojaOrLaterDevice()) {
                connection = new WebConnection(this,"/cgi-bin/faxlog");
                connection.BasicGetSetup();
            }
            else {
                connection = new WebConnection(this,"/cgi-bin/script/printer/faxlog");
                connection.BasicGetSetup();
            }

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while((line = rd.readLine()) != null) {
                String[] tokens = line.split("[ ]+");
                if ((tokens.length > 1) && line.matches("\\d{4}-\\d{2}-\\d{2}.*"))
                    DateTime = tokens[0] + " " + tokens[1];
                if ((tokens.length > 1) && (tokens[1].equals("Answer")))
                    DialedNumber = "Answer";
                if ((tokens.length > 2) && (tokens[1].equals("Dial")))
                    DialedNumber = tokens[2];
                if ((tokens.length > 3) &&(tokens[2].equals("Status")))
                    Status = tokens[3];
            }
            rd.close();

        } catch (Exception e) {

            System.out.println("Failed");
            DateTime = "-";
            DialedNumber = "-";
            Status = "-";
            System.out.println("" + e);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
            Status = Status.replace("E-000", "OK");
            Status = Status.replace("E-901", "No Answer");
            Status = Status.replace("E-902", "No Dialtone");
            Status = Status.replace("E-903", "Busy");
            Status = Status.replace("E-904", "HW Error");
            Status = Status.replace("E-905", "Modem Timeout");
            Status = Status.replace("E-906", "Canceled");
            Status = Status.replace("E-907", "Digital Line");
            Status = Status.replace("E-908", "Line Disconnected");
        }
        return(DateTime + "|" + DialedNumber + "|" + Status);
    }

    private int SendFax(String PhoneNumber) {
        String Part1 = "\033%-12345X@PJL\n@PJL LFAX PHONENUMBER=\"";
        String Part2 = "\"\n@PJL ENTER LANGUAGE = Postscript\n" +
                "%!\n" +
                "<< /OutputDevice /FaxDevice /HWResolution [400 400] >> setpagedevice\n" +
                "300 300 moveto\n72 0 rlineto\n0 72 rlineto\n-72 0 rlineto\nclosepath\n5 setlinewidth\nstroke\n" +
                "/Times-Roman findfont\n48 scalefont\nsetfont\n" +
                "72 72 moveto\n(Lexmark Test Fax) show\nshowpage\n" +
                "%%Trailer\033%-12345X\n";
        Socket clientSocket;

        Refresh();

        try {
            clientSocket=new Socket(IPAddr(),9100);
            DataOutputStream outToMFP = new DataOutputStream(clientSocket.getOutputStream());
            outToMFP.writeBytes(Part1 + PhoneNumber + Part2);
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("Failed");
            System.out.println("" + e);
            return -1;
        }

        return 0;
    }

    public String SendAndVerifyFax(UIOutput UI, String PhoneNumber) {
        int counter = 120;
        int rc;
        if (IPAddr().isEmpty()) {
            System.out.println("Must specify FQDN or IP Address.");
            return "?|?|?|?|?";
        }
        if (PhoneNumber.isEmpty()) {
            System.out.println("Must specify Phone Number.");
            return "?|?|?|?|?1";
        }

        String savedRedials = GetSettingFromWebPage("0.system.16480");
        if (savedRedials == null)
            savedRedials = "5";
        if (0 != SendSetting("0.system.16480","0"))
            return "?|?|?|?|?";

        System.out.println("Sending a fax from " + IPAddr() + " to phone number " + PhoneNumber + "...");
        String oldStatus = GetLastFaxStatus();
        System.out.println("oldStatus = " + oldStatus);
        if (oldStatus.equals("-|-|-|-|-"))
            return "?|?|?|?|?";

        UI.write("Sending a test fax from " + IPAddr() + " to " + PhoneNumber + "...");

        rc = SendFax(PhoneNumber);

        if (rc != 0) {
            return "?|?|?|?|?";
        }

        String newStatus = GetLastFaxStatus();

        UI.RotatorStart();
        while (newStatus.equals(oldStatus) && --counter > 0) {
            UI.Delay(1);
            UI.RotatorNext();
            newStatus = GetLastFaxStatus();
            System.out.println("newStatus = " + newStatus);
        }
        UI.RotatorEnd();
        if (counter == 0)
            newStatus = "?|?|?|?|?";
        System.out.println("newStatus = " + newStatus);
        String[] resultTokens = newStatus.split("[|]");
        if (resultTokens[4].contains("OK")) {
            UI.writelnNoTime("OK");
            System.out.println("OK");
        }
        else {
            UI.writelnNoTime("Failed");
            UI.writeln(newStatus);
            System.out.println("Failed: " + resultTokens[2]);
        }

        if (0 != SendSetting("0.system.16480",savedRedials))
            return "?|?|?|?|?";

        return newStatus;
    }

    private int SendSetting(String settingName, String value) {
        int rc = 0;
        int i;
        WebConnection connection = null;
        if (IPAddr().isEmpty()) {
            System.out.println("Must specify MFP FQDN or IP Address.");
            rc = -1;
            return rc;
        }

        System.out.println("Sending setting " + WebNameToSettingName(settingName) + " with value of " + value + " to " + IPAddr() + "...");
        for (i=0;i<100 && rc!=-1;i++) {
            rc = 0;
            if (MojaOrLaterDevice()) {
                try {
                    String settingString = "data=%7B%22" + WebNameToSettingMojaName(settingName) + "%22%3A" + value + "%7D&c=" + WebNameToSettingCategory(settingName);
                    //Create connection
                    connection = new WebConnection(this,"/webglue/content");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //Send request
                    OutputStream output = connection.getOutputStream ();
                    DataOutputStream wr = new DataOutputStream (output);
                    wr.writeBytes (settingString);
                    wr.flush ();
                    wr.close ();

                    //Get Response
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    //System.out.println("Response from MFP:");
                    while((line = rd.readLine()) != null) {
                        //System.out.println(line);
                    }
                    rd.close();
                    System.out.println("OK");

                } catch (Exception e) {

                    if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                        rc = -2;
                    }
                    else {
                        System.out.println("Failed");
                        System.out.println("" + e);
                        rc = -1;
                    }

                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                if (rc == 0)
                    return rc;
            }
            else {
                try {
                    String settingString = settingName + "=" + value;
                    //Create connection
                    connection = new WebConnection(this,"/cgi-bin/dynamic/printer/config/gen/fax/fax.html");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //Send request
                    OutputStream output = connection.getOutputStream ();
                    DataOutputStream wr = new DataOutputStream (output);
                    wr.writeBytes (settingString);
                    wr.flush ();
                    wr.close ();

                    //Get Response
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    //System.out.println("Response from MFP:");
                    while((line = rd.readLine()) != null) {
                        //System.out.println(line);
                    }
                    rd.close();
                    System.out.println("OK");

                } catch (Exception e) {

                    if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                        rc = -2;
                    }
                    else {
                        System.out.println("Failed");
                        System.out.println("" + e);
                        rc = -1;
                    }

                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                if (rc == 0)
                    return rc;
            }
        }

        if (rc == -2) {
            System.out.println("Failed. Server returned HTTP response code: 408");
        }

        return rc;
    }

    public boolean HTTPSFaxCheck() {
        String line = "";
        boolean Result = false;
        WebConnection connection = null;
        if (IPAddr().isEmpty()) {
            System.out.println("Must specify MFP FQDN or IP Address.");
            return Result;
        }
        System.out.println("Checking if etherFAX is supported");

        if (MojaOrLaterDevice()) {
            try {
                //Create connection
                connection = new WebConnection(this,"/cgi-bin/faxsetup");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (line.contains("HTTPS Fax")) {
                        Result = true;
                        break;
                    }
                }
                rd.close();
                System.out.println("OK");

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        else {
            try {
                //Create connection
                connection= new WebConnection(this,"/cgi-bin/dynamic/printer/config/gen/fax/fax.html");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (line.contains("https_setting")) {
                        Result = true;
                        break;
                    }
                }
                rd.close();
                System.out.println("OK");

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        System.out.println("etherFAX Support: " + Result);
        return Result;
    }


    public String GetSettingFromWebPage(String settingName) {
        String line = "";
        String value = null;
        boolean WriteData = false;
        WebConnection connection = null;
        if (IPAddr().isEmpty()) {
            System.out.println("Must specify MFP FQDN or IP Address.");
            return null;
        }
        System.out.println("Getting Setting " + WebNameToSettingName(settingName) + " from " + IPAddr() + "...");

        if (MojaOrLaterDevice()) {
            try {
                //Create connection
                connection = new WebConnection(this,"/cgi-bin/faxsetup");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (line.contains(WebNameToSettingName(settingName).replace("\"",""))) {
                        String str[] = line.split("\\s{2,}");
                        if (str[0].equals(WebNameToSettingName(settingName)))
                            value = str[1];
                        else
                            value = str[2];
                        value = str[2];
                        break;
                    }
                }
                rd.close();
                System.out.println("OK");

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        else {
            try {
                //Create connection
                connection = new WebConnection(this,"/cgi-bin/script/printer/faxsetup");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (line.contains(WebNameToSettingName(settingName).replace("\"",""))) {
                        String str[] = line.split("\\s{2,}");
                        if (str[0].equals(WebNameToSettingName(settingName)))
                            value = str[1];
                        else
                            value = str[2];
                        value = str[2];
                        break;
                    }
                }
                rd.close();
                System.out.println("OK");

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
                /*
                //Create connection
                connection= new WebConnection(this,"/cgi-bin/dynamic/printer/config/gen/fax/fax.html");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (line.contains(settingName)) {
                        String[] parts = line.split("VALUE=");
                        if (settingName.startsWith("2")) {
                            String[] moreParts = parts[2].split(">");
                            value = moreParts[0];
                        }
                        else {
                            String[] moreParts = parts[1].split(">");
                            String[] moreMoreParts = moreParts[0].split("\"");
                            value = moreMoreParts[1];
                        }
                        break;
                    }
                }
                rd.close();
                System.out.println("OK");

            } catch (Exception e) {

                System.out.println("Failed");
                System.out.println("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
                */
            }
        }
        System.out.println("Setting " + WebNameToSettingName(settingName) + " = " + value);
        return value;
    }

    public void WaitForMFPReboot(UIOutput UI) throws LexmarkFoIPException {

        System.out.println("Waiting for MFP Reboot...");

        UI.RotatorStart();
        UI.DelayRotator(60);

        System.out.println("Checking if MFP is up...");
        UI.RotatorNext();
        long startTime = new Date().getTime()/1000;
        while (!IsUp()) {
            if ((new Date().getTime())/1000 - startTime > 420) {
                throw new LexmarkFoIPException("Waiting for MFP Reboot Timeout");
            }
            UI.RotatorNext();
            System.out.println("Checking if MFP is up...");
        }

        System.out.println("MFP is back up, waiting one minute for boot to complete...");

        UI.DelayRotator(60);

        ClearData();  // Need to clear out login data since MFP rebooted
        Refresh();

        UI.RotatorEnd();
    }


    private String WebNameToSettingName(String webName) {
        if (webName.equals("0.system.16480"))
            return "\"Automatic Redial\"";
        if (webName.equals("0.system.17724"))
            return "\"Force Fax Mode Delay\"";
        if (webName.equals("0.system.17664"))
            return "\"Fax Transport\"";
        return webName;
    }

    private String WebNameToSettingMojaName(String webName) {
        if (webName.equals("0.system.16480"))
            return "FaxSendAutomaticRedial";
        if (webName.equals("0.system.17724"))
            return "ForceFaxModeDelay";
        return webName;
    }
    private String WebNameToSettingCategory(String webName) {
        if (webName.equals("0.system.16480"))
            return "FaxSendAdminControlsPopup";
        if (webName.equals("0.system.17724"))
            return "VoIPSettings";
        return webName;
    }

    public void clearSIPRegistrar(UIOutput UI) {
        LexmarkSettings settings = new LexmarkSettings();
        settings.add(FAX, FAXMODE, "0");                   // Analog/T.38/G.711
        settings.add(VOIP, FAXTRANSPORT, "1");             // T.38
        settings.add(VOIP, PROTOCOL, "1");                 // SIP
        settings.add(SIP, REGISTRAR, "999.999.999.999");   // Bad Registrar Address

        UI.write("Clearing SIP Registration Status...");

        try {
            if (vccSupported()) {
                SettingsBundle bundle = new SettingsBundle(this);
                bundle.addList(settings);
                bundle.make();
                bundle.send();
                UI.writelnNoTime("OK");
                bundle.waitForSettingsToTakeEffect();
                UI.write("Checking for cleared SIP Registration Status...");
            } else {
                UCF ucf = new UCF(this);
                ucf.addList(settings);
                ucf.make();
                ucf.send();
            }
        } catch (Exception e) {
            UI.writelnNoTime("Failed");
            UI.end();
            throw e;
        }

        String SIPResult = "";
        UI.RotatorStart();
        for (int i=0; (i<45) && !SIPResult.contains("2 Invalid Address") && !SIPResult.contains("No SIP"); i++) {
            UI.Delay(1);
            UI.RotatorNext();
            if (i>=15)  // Wait 15 seconds for settings change to take effect
                SIPResult = GetSIPRegStatus();
        }
        UI.RotatorEnd();

        if (SIPResult.contains("2 Invalid Address") || SIPResult.contains("No SIP")) {
            UI.writelnNoTime("OK");
        } else {
            UI.writelnNoTime("Failed");
            UI.writeln("   Status: '" + SIPResult + "'");
            UI.end();
            throw new LexmarkFoIPException("Clearing SIP Registration Failed: " + SIPResult);
        }
    }

}
