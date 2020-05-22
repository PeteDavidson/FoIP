package com.lexmark.dev.etherFAX.etherFAXList;

import com.lexmark.dev.FoIP.ModelList;
import com.lexmark.dev.FoIP.Provisioner.LexmarkProvisionFoIP;
import com.lexmark.dev.FoIP.Provisioner.LexmarkProvisionFoIPApi;
import com.lexmark.dev.FoIP.Provisioner.ProvisionerUIOutput;
import com.lexmark.dev.FoIP.UIOutput;
import com.lexmark.dev.FoIP.UIOutputHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class etherFAXList {


    public static void main(String[] args) throws IOException {
        ArrayList<Account> AcctList;
        AcctList = new ArrayList<Account>();
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP(new ProvisionerUIOutput());
        UIOutput UI = UIOutputHandler.getInstance().GetHandler();
        File file = new File("C:\\Users\\pete\\Downloads\\etherfax_lexmark_devices.csv");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UNICODE"));
            String st;
            while ((st = br.readLine()) != null) {
                if (st.length()>4 && st.substring(0,4).equals("EFAX")) {
                    int i;
                    boolean enabled;
                    boolean eval;
                    System.out.println(st);
                    String[] fields = st.split("\t");

                    if (fields[7].trim().equals("1"))
                        enabled = true;
                    else
                        enabled = false;
                    if (fields[8].trim().equals("Yes"))
                        eval = true;
                    else
                        eval = false;
                    System.out.println(fields.length);
                    for (i = 0; i<AcctList.size(); i++) {
                        if (AcctList.get(i).AcctNumber.equals(fields[0].trim())) {
                            break;
                        }
                    }
                    if (i<AcctList.size()) {
                        AcctList.get(i).DeviceList.add(new Device(fields[3].trim(),
                                                                  fields[4].trim(),
                                                                  fields[5].trim(),
                                                                  fields[6].trim(),
                                                                  enabled,
                                                                  eval));
                    }
                    else {
                        AcctList.add(new Account(fields[0].trim(),
                                                 fields[1].trim(),
                                                 fields[2].trim(),
                                                 new Device(fields[3].trim(),
                                                            fields[4].trim(),
                                                            fields[5].trim(),
                                                            fields[6].trim(),
                                                            enabled,
                                                            eval)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int TotalPaying = 0;
        int TotalNonPaying = 0;
        int TotalEval = 0;
        int PayingAccts = 0;

        FileWriter fw = new FileWriter("C:\\Users\\pete\\Downloads\\etherFAX List.csv");
        FileWriter fw2 = new FileWriter("C:\\Users\\pete\\Downloads\\etherFAX Accts.csv");

        fw.write("List of Registered etherFAX Devices\n\n");
        fw2.write("List of Lexmark etherFAX Accounts\n\n");
        UI.writelnNoTime("Account         Customer                          Reseller                                  Units  SerialNumber      Type                           Registration Date");
        fw.write("Account         ,Customer                          ,Reseller                                  ,Units  ,SerialNumber      ,Type                           ,Registration Date\n");
        fw2.write("Account         ,Customer                          ,Reseller                                   ,Units\n");
        UI.writelnNoTime("--------------  --------------------------------  ----------------------------------------  -----  ----------------  -----------------------------  -------------------");

        for (int i=0; i<AcctList.size(); i++) {
            int NonPaying = 0;
            int Paying = 0;
            int EvalUnits = 0;
            ArrayList<Device> PayingDevices = new ArrayList<>();
            Account Acct = AcctList.get(i);
            for (int j=0; j<Acct.DeviceList.size(); j++) {
                Device dev = Acct.DeviceList.get(j);
                if (dev.Eval || dev.Enabled==false || dev.Status.equals("House") || dev.Status.equals("Evaluation") || Acct.Reseller.equals("Lexmark") || dev.RegisteredDate.equals("NULL")) {
                    NonPaying++;
                }
                else {
                    Paying++;
                    PayingDevices.add(dev);
                }
                if (dev.Eval && dev.Enabled==true && !dev.RegisteredDate.equals("NULL")) {
                    EvalUnits++;
                }
            }
            TotalPaying += Paying;
            TotalNonPaying += NonPaying;
            System.out.println(Acct.AcctNumber + " " + Acct.Name + "  " + Acct.Reseller + " " + Acct.DeviceList.size() + " Paying: " + Paying + " NonPaying: " + NonPaying);
            if (Paying > 0) {
                UI.writeNoTime(Acct.AcctNumber + "  " + String.format("%-32s",Acct.Name) + "  " + String.format("%-40s",Acct.Reseller) + "  " + String.format("%5d",Paying) + "  ");
                fw.write(Acct.AcctNumber + "  ," + String.format("\"%-32s\"",Acct.Name) + "  ," + String.format("\"%-40s\"",Acct.Reseller) + "  ," + String.format("%5d",Paying) + "  ,");
                fw2.write(Acct.AcctNumber + "  ," + String.format("\"%-32s\"",Acct.Name) + "  ," + String.format("\"%-40s\"",Acct.Reseller) + "  ," + String.format("%5d",Paying) + "\n");
                for (int k=0;k<PayingDevices.size();k++) {
                    Device dev = PayingDevices.get(k);
                    String Type = "\"" + dev.Type + "\"";
                    if (k==0) {
                        UI.writelnNoTime(dev.SerialNum + "  " + String.format("%-30s", Type) + " " + dev.RegisteredDate.substring(0, 19));
                        fw.write(dev.SerialNum + "  ," + String.format("%-30s", Type) + " ," + dev.RegisteredDate.substring(0, 19) + "\n");
                    }
                    else {
                        UI.writelnNoTime("                                                                                                   " + dev.SerialNum + "  " + String.format("%-30s", Type) + " " + dev.RegisteredDate.substring(0, 19));
                        fw.write("                  ,                                              ,             ,          ," + dev.SerialNum + "  ," + String.format("%-30s", Type) + " ," + dev.RegisteredDate.substring(0, 19) + "\n");
                    }
                }
                PayingAccts++;
            }
            if (EvalUnits > 0) {
                UI.writelnNoTime(Acct.AcctNumber + "  " + String.format("%-32s",Acct.Name) + "  " + String.format("%-40s",Acct.Reseller) + "  " + String.format("%5d",EvalUnits) + "  (Eval)");
                fw.write(Acct.AcctNumber + "  ," + String.format("\"%-32s\"",Acct.Name) + "  ," + String.format("\"%-40s\"",Acct.Reseller) + "  ," + String.format("%5d",EvalUnits) + "  ,(Eval)\n");
                TotalEval += EvalUnits;
            }
        }

        System.out.println("Total Paying: " + TotalPaying + "   Total NonPaying: " + TotalNonPaying);
        UI.writelnNoTime("");
        fw.write("\n");
        fw2.write("\n");
        UI.writelnNoTime("Total Paying Accounts: " + PayingAccts + "  Total Paying Devices: " + TotalPaying + "  Monthly Revenue: $" + TotalPaying*5 + "  Eval Units: " + TotalEval);
        fw.write("Total Paying Accounts: " + PayingAccts + "      Total Paying Devices: " + TotalPaying + "      Monthly Revenue: $" + TotalPaying*5 + "      Eval Units: " + TotalEval + "\n");
        fw2.write("Accounts: " + PayingAccts + "       Devices: " + TotalPaying + "       Monthly Revenue: $" + TotalPaying*5 + "\n");

        fw.close();
        fw2.close();
    }
}
