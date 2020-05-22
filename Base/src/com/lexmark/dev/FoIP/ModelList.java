package com.lexmark.dev.FoIP;

import java.util.Arrays;

import static com.lexmark.dev.FoIP.LexmarkFirmwareUpdate.LATEST_PE;

public class ModelList {
    private static ModelList instance = null;
    private java.util.List<Model> Models;

    private ModelList () {
        Models = new java.util.ArrayList<>();

        /* HS */
        Models.add(new Model(false,96, new String[]{"X792","XS795","XS796","XS798"},                                 new String[]{},                                             new String[]{},                                      "Mirage",         "MR",   "C7",true, "", ""));
        Models.add(new Model(false,103, new String[]{"X925", "XS925"},                                               new String[]{},                                             new String[]{},                                      "Hickory",        "HK",   "C9",true, "", ""));
        Models.add(new Model(false,106, new String[]{"X950","X952","X954","XS950","XS955"},                          new String[]{},                                             new String[]{},                                      "Turquoise",      "TQ",   "C9",true, "", ""));
        Models.add(new Model(false,110, new String[]{"6500"},                                                        new String[]{},                                             new String[]{},                                      "Jaguar",         "JR",   "XN",true, "", ""));
        Models.add(new Model(false,111, new String[]{"X548"},                                                        new String[]{},                                             new String[]{},                                      "Viking",         "VK",   "C5",true, "", ""));
        Models.add(new Model(false,121, new String[]{"X746","X748","XS748"},                                         new String[]{},                                             new String[]{},                                      "NewYork",        "NY",   "C7",true, "", ""));

        /* WC */
        Models.add(new Model(false, 128, new String[]{"MX810","MX811","MX812"},                                       new String[]{"XM7155","XM7163","XM7170","XM7263","XM7270"}, new String[]{},                                      "Tuscany",        "TU",   "M8",true, "", ""));
        Models.add(new Model(false, 129, new String[]{},                                                              new String[]{"XM5163","XM5263"},                            new String[]{},                                      "Buckeye M",      "TU",   "M8",true, "", ""));
        Models.add(new Model(false, 130, new String[]{"MX710","MX711","MX717","MX718"},                               new String[]{"XM5170","XM5270"},                            new String[]{},                                      "Buckeye H",      "TU",   "M7",true, "", ""));
        Models.add(new Model(false, 137, new String[]{"MX310","MX317"},                                               new String[]{"XM1135","XM1140"},                            new String[]{},                                      "Sparrow L",      "SB2",  "M3",true, "", ""));
        Models.add(new Model(false, 138, new String[]{"MX410","MX417","MX510","MX511","MX517"},                       new String[]{"XM1145"},                                     new String[]{},                                      "Sparrow M/H",    "SB4",  "M4",true, "", ""));
        Models.add(new Model(false, 139, new String[]{"MX610","MX611","MX617"},                                       new String[]{"XM3150"},                                     new String[]{},                                      "Blackbeard",     "SB7",  "M6",true, "", ""));
        Models.add(new Model(false,143, new String[]{"CX310","CX317"},                                               new String[]{},                                             new String[]{},                                      "Gemini L",       "GM2",  "C3",true, "", ""));
        Models.add(new Model(false, 144, new String[]{"CX410","CX417"},                                               new String[]{"XC2130"},                                     new String[]{},                                      "Gemini M",       "GM4",  "C4",true, "", ""));
        Models.add(new Model(false, 145, new String[]{"CX510","CX517"},                                               new String[]{"XC2132"},                                     new String[]{},                                      "Gemini H",       "GM4",  "C5",true, "", ""));
        Models.add(new Model(false, 147, new String[]{"MX6500"},                                                      new String[]{},                                             new String[]{},                                      "JaguarDen",      "JD",   "XN",true, "", ""));
        Models.add(new Model(false, 158, new String[]{"MX910","MX911","MX912"},                                       new String[]{"XM9145","XM9155","XM9165"},                   new String[]{},                                      "Montego L/M/H",  "MG",   "M9",true, "", ""));

        /* YAPP */
        Models.add(new Model(false, 170, new String[]{},                                                              new String[]{"XC6152"},                                     new String[]{},                                      "Paris 7",        "CXTPP","C8",true, "", ""));
        Models.add(new Model(false, 171, new String[]{"CX825","CX827","CX860"},                                       new String[]{"XC8155","XC8160"},                            new String[]{},                                      "Palazzo 10.2",   "CXTPP","C8",true, "", ""));

        /* COATL */
        Models.add(new Model(false, 173, new String[]{"CX725","CX727"},                                               new String[]{"XC4140","XC4150"},                            new String[]{},                                      "Antlantis 7.0",  "CXTAT","C7",true, "", ""));

        /* GDW */
        Models.add(new Model(false, 176, new String[]{"MX721ade","MX721adhe","MX722ade"},                             new String[]{"XM5365","XM5370"},                            new String[]{"MB2770adwhe"},                         "Dunes 7.0",      "MXTGW","M7",true, "", ""));
        Models.add(new Model(false, 178, new String[]{"MX822ade","MX822adxe","MX826ade","MX826adxe"},                 new String[]{"XM7355","XM7370"},                            new String[]{},                                      "Wynn 10.2",      "MXTGW","M8",true, "", ""));

        /* GSM */
        Models.add(new Model(false, 181, new String[]{"MX321adn","MX321adw"},                                         new String[]{},                                             new String[]{"MB2338adw"},                           "Skyfall L 2.4",  "MXNGM","M3",true, "", ""));
        Models.add(new Model(false, 182, new String[]{"MX421ade"},                                                    new String[]{"XM1242"},                                     new String[]{"MB2442adwe"},                          "Skyfall M/H 4.3","MXTGM","M4",true, "", ""));
        Models.add(new Model(false, 183, new String[]{"MX521ade","MX521de","MX522adhe"},                              new String[]{"XM1246"},                                     new String[]{"MB2546adwe"},                          "Skyfall H+ 4.3", "MXTGM","M5",true, "", ""));
        Models.add(new Model(false, 184, new String[]{"MX622ade","MX622adhe"},                                        new String[]{"XM3250"},                                     new String[]{"MB2650adwe"},                          "Moonraker 7",    "MXTGM","M6",true, "", ""));

        /* ATLANTIC */
        Models.add(new Model(false, 191, new String[]{"CX920de","CX921de","CX923dte","CX923dxe","CX924dte","CX924dxe","CX927"}, new String[]{"XC9225","XC9235","XC9245","XC9255","XC9265"},          new String[]{},                                      "Mahi L/ML/MH/H", "CXTMH","C9",true, "", ""));

        /* ZJ */
        Models.add(new Model(false, 194, new String[]{"CX421adn"},                                                    new String[]{},                                             new String[]{"MC2325adw","MC2425adw"},               "Jupiter L 2.4",  "CXNZJ","C4",true, "", ""));
        Models.add(new Model(false, 195, new String[]{"CX522ade","CX622ade","CX622adhe"},                             new String[]{"XC2235"},                                     new String[]{"MB2535adwe"},                          "Jupiter M 4.3",  "CXTZJ","C5",true, "", ""));
        Models.add(new Model(false, 196, new String[]{"CX625ade","CX625adhe"},                                        new String[]{"XC4240"},                                     new String[]{"MC2640adwe"},                          "Jupiter H 7",    "CXTZJ","C6",true, "", ""));

        /* Bridges */
        Models.add(new Model(false,198, new String[]{},                                                              new String[]{},                                             new String[]{"MB2236adw"},                          "Goldengate",     "MXLSG","M2",true, "", ""));

        /* Fishes */
        Models.add(new Model(false,200, new String[]{"CX331adne","CX331adwe"},                                       new String[]{},                                             new String[]{"MC3326adwe"},                         "Lionfish",       "CXLBL","C3",true, "", ""));

        /* Boats */
        Models.add(new Model(false,202, new String[]{"MX331adn","MX431adn","MX431adw"},                              new String[]{},                                             new String[]{"MB3442adwe"},                         "Donzi",          "MXLBD","M3/4",true, "", ""));

        /* Fishes */
        Models.add(new Model(false,203, new String[]{},                                                              new String[]{},                                             new String[]{"MC3224adwe","MC3224dwe"},             "Lionfish L",     "CXLBL","C3",true, "", ""));

        /* Bridges */
        Models.add(new Model(false,204, new String[]{},                                                              new String[]{},                                             new String[]{"MB2236adwe"},                         "Goldengate 2.8", "MXLSG","M2",true, "", ""));

        /* Fishes */
        Models.add(new Model(false,206, new String[]{"CX431adw"},                                                    new String[]{},                                             new String[]{"MC3426adw"},                          "Needlefish",     "CXLBL","C4/5",true, "", ""));

    /*
        Models.add(new Model(false,208, new String[]{},                                                              new String[]{},                                             new String[]{},                                     "Macau",          "CX???","C?",  true, "", ""));
        Models.add(new Model(false,211, new String[]{},                                                              new String[]{},                                             new String[]{},                                     "Nile43",         "CX???","M?",  true, "", ""));
        Models.add(new Model(false,212, new String[]{},                                                              new String[]{},                                             new String[]{},                                     "Nile70",         "CX???","M?",  true, "", ""));
        Models.add(new Model(false,214, new String[]{},                                                              new String[]{},                                             new String[]{},                                     "Maui",           "CX???","C?",  true, "", ""));
    */

    }

    public static ModelList getInstance() {
        if (instance == null)
            instance = new ModelList();
        return instance;
    }

    public String[] GetFirmwareLink(Integer NPAPID, LexmarkFirmwareUpdate Level) {

        for (int i=0; i<Models.size(); i++) {
            if (Models.get(i).NPAPID.equals(NPAPID)) {
                if (Models.get(i).Link.equals("")) {
                    MFPStore mfpStore = MFPStore.getInstance();
                    Firmware fw = new Firmware();
                    MFP mfp = mfpStore.Init(NPAPID);
                    String[] link = fw.GetFirmwareLink(mfp, Level);
                    Models.get(i).Level = link[0];
                    Models.get(i).Link = link[1];
                }
                return new String[]{Models.get(i).Level, Models.get(i).Link};
            }
        }
        return new String[] {"",""};
    }

    public String GetFirmwareLevel(Integer NPAPID, LexmarkFirmwareUpdate Level) {
        for (int i=0; i<Models.size(); i++) {
            if (Models.get(i).NPAPID.equals(NPAPID)) {
                if (Models.get(i).Link.equals("")) {
                    MFPStore mfpStore = MFPStore.getInstance();
                    Firmware fw = new Firmware();
                    MFP mfp = mfpStore.Init(NPAPID);
                    String[] link = fw.FindBuildId(mfp, Level);
                    Models.get(i).Level = link[1];
                }
                return Models.get(i).Level;
            }
        }
        return "";
    }

    public String[] GetModelNames(Integer NPAPID) {

        for (int i=0; i<Models.size(); i++) {
            if (NPAPID.equals(Models.get(i).NPAPID)) {
                String[] ModelNames = new String[Models.get(i).EnterpriseModelNames.length +
                                                 Models.get(i).BSDModelNames.length +
                                                 Models.get(i).SMBModelNames.length];
                int j=0;
                for (String s: Models.get(i).EnterpriseModelNames) {
                    ModelNames[j++]=s;
                }
                for (String s: Models.get(i).BSDModelNames) {
                    ModelNames[j++]=s;
                }
                for (String s: Models.get(i).SMBModelNames) {
                    ModelNames[j++] = s;
                }
                return ModelNames;
            }
        }
        return new String[]{""};
    }

    public boolean GetUseRecommendedCode(Integer NPAPID) {
        for (int i=0; i<Models.size(); i++) {
            if (NPAPID.equals(Models.get(i).NPAPID))
                return Models.get(i).UseRecommendedCode;
        }
        return false;
    }

    public String[] GetAllModelNames() {
        int TotalModels = 0;
        for (int i=0; i<Models.size(); i++) {
            TotalModels += Models.get(i).EnterpriseModelNames.length;
            TotalModels += Models.get(i).BSDModelNames.length;
            TotalModels += Models.get(i).SMBModelNames.length;
        }
        String[] AllModelNames = new String[TotalModels];
        int j=0;
        for (int i=0; i<Models.size(); i++) {
            for (String s: Models.get(i).EnterpriseModelNames) {
                AllModelNames[j++]=s;
            }
            for (String s: Models.get(i).BSDModelNames) {
                AllModelNames[j++]=s;
            }
            for (String s: Models.get(i).SMBModelNames) {
                AllModelNames[j++]=s;
            }
        }
        Arrays.sort(AllModelNames,0,j);
        return AllModelNames;
    }

    public Integer GetNPAPIDfromId(String Id) {
        for (int i=0; i<Models.size(); i++) {
            if (Id.equals(Models.get(i).Id))
                return Models.get(i).NPAPID;
        }
        return 0;
    }

    public Integer[] GetAllNPAPIDs() {
        Integer[] Value = new Integer[Models.size()];
        for (int i=0; i<Models.size(); i++) {
            Value[i] = Models.get(i).NPAPID;
        }
        return Value;
    }

    public Integer GetNPAPIDfromModelName(String ModelName) {
        for (int i=0; i<Models.size(); i++) {
            for (String s: Models.get(i).EnterpriseModelNames) {
                if (ModelName.equals(s))
                    return Models.get(i).NPAPID;
            }
            for (String s: Models.get(i).BSDModelNames) {
                if (ModelName.equals(s))
                    return Models.get(i).NPAPID;
            }
            for (String s: Models.get(i).SMBModelNames) {
                if (ModelName.equals(s))
                    return Models.get(i).NPAPID;
            }
        }
        return 0;
    }

    public Model GetModel(Integer NPAPID) {
        for (int i=0; i<Models.size(); i++) {
            if (NPAPID.equals(Models.get(i).NPAPID))
                return Models.get(i);
        }
        return null;
    }
}
