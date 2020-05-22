package com.lexmark.dev.FoIP;

import javax.jws.soap.SOAPBinding;

public class Model {
    public boolean UseRecommendedCode;
    public Integer NPAPID;
    public String[] EnterpriseModelNames;
    public String[] BSDModelNames;
    public String[] SMBModelNames;
    public String CodeName;
    public String Id;
    public String Class;
    public boolean IsMFP;
    public String Link;
    public String Level;

    public Model(boolean _UseRecommendedCode, Integer _NPAPID, String[] _EnterpriseModelNames, String[] _BSDModelNames, String[] _SMBModelNames, String _CodeName, String _Id, String _Class, boolean _IsMFP, String _Level, String _Link ) {
        UseRecommendedCode = _UseRecommendedCode;
        NPAPID = _NPAPID;
        EnterpriseModelNames = _EnterpriseModelNames;
        BSDModelNames = _BSDModelNames;
        SMBModelNames = _SMBModelNames;
        CodeName = _CodeName;
        Id = _Id;
        Class = _Class;
        IsMFP = _IsMFP;
        Level = _Level;
        Link = _Link;
    }
};
