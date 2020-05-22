package com.lexmark.dev.FoIP.FoIPLicenseSales;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FoIPAccount implements Comparable<FoIPAccount> {
    public String AccountID;
    public String AccountName;
    public int Active;
    public int Returned;
    public Date First;
    public Date Last;
    public Set<String> SerialNumbers;
    public Set<PurchaseOrder> POs;
    public int NonPOLicenses;

    public FoIPAccount() {
        Active = 0;
        Returned = 0;
        NonPOLicenses = 0;
        SerialNumbers = new HashSet<>();
        POs = new HashSet<>();

    };

    @Override
    public int compareTo(FoIPAccount Acct) {
        return (this.Active < Acct.Active ? -1 :
                (this.Active == Acct.Active ? 0 : 1));
    }
}
