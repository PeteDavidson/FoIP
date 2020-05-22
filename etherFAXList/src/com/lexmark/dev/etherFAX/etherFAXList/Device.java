package com.lexmark.dev.etherFAX.etherFAXList;

public class Device {
    public String Status;
    public String SerialNum;
    public String Type;
    public String RegisteredDate;
    public boolean Enabled;
    public boolean Eval;

    public Device(String s1, String s2, String s3, String s4, boolean b1, boolean b2) {
        Status = s1;
        SerialNum = s2;
        Type = s3;
        RegisteredDate = s4;
        Enabled = b1;
        Eval = b2;
    }
}
