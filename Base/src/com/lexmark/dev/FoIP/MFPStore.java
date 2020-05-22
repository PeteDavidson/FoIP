package com.lexmark.dev.FoIP;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Pete on 2/23/2017.
 */
public class MFPStore {
    private static MFPStore instance = null;
    private java.util.List<MFP> MFPList;

    private MFPStore() {
        MFPList = new java.util.ArrayList<>();
    }

    public static MFPStore getInstance() {
        if (instance == null)
            instance = new MFPStore();
        return instance;
    }

    public MFP Init(Integer npapid) {
        MFP newMFP = new MFP(npapid);
        if (MFPList.size() == MFP.MAX_MFPS_IN_STORE)
            MFPList.remove(0);
        MFPList.add(newMFP);
        return newMFP;
    }

    public MFP Init(String IPAddr) {
        for (MFP m : MFPList) {
            if (m.IPAddr().equals(IPAddr)) {
                m.ClearData();
                return m;
            }
        }
        MFP newMFP = new MFP(IPAddr);
        if (MFPList.size() == MFP.MAX_MFPS_IN_STORE)
            MFPList.remove(0);
        MFPList.add(newMFP);
        return newMFP;
    }

    public MFP Init(String IPAddr, String user, String password) {
        for (MFP m : MFPList) {
            if (m.IPAddr().equals(IPAddr)) {
                m.ClearData();
                return m;
            }
        }
        MFP newMFP = new MFP(IPAddr,user,password);
        if (MFPList.size() == MFP.MAX_MFPS_IN_STORE)
            MFPList.remove(0);
        MFPList.add(newMFP);
        return newMFP;
    }

    public MFP Get(String IPAddr) {
        for (MFP m : MFPList) {
            if (m.IPAddr().equals(IPAddr)) {
                if (m.validData() == false)
                    m.Refresh();
                //m.IncrementCount();
                return m;
            }
        }
        MFP newMFP = new MFP(IPAddr);
        if (MFPList.size() == MFP.MAX_MFPS_IN_STORE)
            MFPList.remove(0);
        MFPList.add(newMFP);
        newMFP.Refresh();
        return newMFP;
    }

    public int Clear() {
        int ElementsRemoved = MFPList.size();
        MFPList.clear();
        return ElementsRemoved;
    }

    public boolean getUseHttps(String IPAddr) {
        for (MFP m : MFPList) {
            if (m.IPAddr().equals(IPAddr)) {
                return m.getUseHttps();
            }
        }
        return false;
    }

    public void OutputMFPList() {
        int i = 0;
        UIOutput UI = UIOutputHandler.getInstance().GetHandler();
        UI.writeln("Known MFPs:");
        for (MFP m : MFPList) {
            //  if (m.getValidData()) {
            UI.writeln("" + m);
            i++;
        }
        // }
        UI.writeln("Total: " + i);
        UI.writelnNoTime("");
    }

    public void PrintMFPList() {
        int i = 0;
        System.out.println("Known MFPs:");
        for (MFP m : MFPList) {
            //  if (m.getValidData()) {
            System.out.println("" + m);
            i++;
        }
        // }
        System.out.println("Total: " + i);
        System.out.println("");
    }

    public void InitTrustAll() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new TrustAll() }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        }
        catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.out.println("" + e);
        }
    }
}
