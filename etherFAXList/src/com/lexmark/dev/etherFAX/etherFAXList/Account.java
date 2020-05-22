package com.lexmark.dev.etherFAX.etherFAXList;

import java.time.format.ResolverStyle;
import java.util.ArrayList;

public class Account {
    public String AcctNumber;
    public String Name;
    public String Reseller;
    public ArrayList<Device> DeviceList = new ArrayList<>();

    public Account(String _AcctNumber, String _Name, String _Reseller, Device _Device) {
        AcctNumber = _AcctNumber;
        Name = _Name;
        Reseller = _Reseller;
        DeviceList.add(_Device);
    }

    public Account(String _AcctNumber, String _Name, String _Reseller) {
        AcctNumber = _AcctNumber;
        Name = _Name;
        Reseller = _Reseller;
    }
}
