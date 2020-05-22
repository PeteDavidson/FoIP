package com.lexmark.dev.FoIP.FoIPLicenseSales;

import com.lexmark.dev.FoIP.Provisioner.LexmarkProvisionFoIP;
import com.lexmark.dev.FoIP.Provisioner.LexmarkProvisionFoIPApi;
import com.lexmark.dev.FoIP.Provisioner.ProvisionerUIOutput;
import com.lexmark.dev.FoIP.UIOutput;
import com.lexmark.dev.FoIP.UIOutputHandler;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Pattern;

public class FoIPLicenseSales {
    public static void main(String[] args) throws IOException {
        ArrayList<FoIPAccount> AcctList;
        AcctList = new ArrayList<>();
        int rehost = 0;
        int TotalDemo = 0;
        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP(new ProvisionerUIOutput());
        UIOutput UI = UIOutputHandler.getInstance().GetHandler();



        File file = new File("C:\\Users\\pete\\Downloads\\FOIP Sales.txt");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String st;
            int LineNum = 0;
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm");

            FoIPAccount Lowes = new FoIPAccount();
            Lowes.AccountID = "339910";
            Lowes.AccountName = "Lowe's Companies, Inc.";
            Lowes.First = df.parse("1/27/2015 00:00");
            Lowes.Last = df.parse("6/20/2019 00:00");
            Lowes.Active = 15000-1;
            for (int i=0; i<15000-1; i++) {
                Lowes.SerialNumbers.add(Integer.toString(i));
            }
            AcctList.add(Lowes);

            FoIPAccount OneMain = new FoIPAccount();
            OneMain.AccountID = "270779";
            OneMain.AccountName = "Onemain Financial, Inc.";
            OneMain.First = df.parse("3/1/2015 00:00");
            OneMain.Last = df.parse("3/1/2015 00:00");
            OneMain.Active = 1150-3;
            for (int i=0; i<1150-3; i++) {
                OneMain.SerialNumbers.add(Integer.toString(i));
            }
            AcctList.add(OneMain);

            while ((st = br.readLine()) != null) {
                int i;
                FoIPAccount Acct;
                LineNum++;
                String[] fields =st.split("\t");
                if (LineNum > 2 && fields.length>15) {
                    if (fields[13].trim().equals("DEMO")) {
                        TotalDemo++;
                        continue;
                    }
                    if (fields[14].trim().length() > 6 && fields[14].trim().substring(0,7).equals("Lexmark"))
                        continue;
                    if (fields[15].trim().length() > 6 && fields[15].trim().substring(0,7).equals("Lexmark"))
                        continue;
                    if (fields.length > 16 && Pattern.compile(Pattern.quote("REHOST"),Pattern.CASE_INSENSITIVE).matcher(fields[16]).find()) {
                        rehost++;
                        continue;
                    }
                    for (i=0; i<AcctList.size(); i++) {
                        //System.out.println(LicenseList.get(i).LicenseID + " : " + fields[5].trim());
                        if (AcctList.get(i).AccountID.equals((fields[14].trim()))) {
                            System.out.println("Found Account ID: " + fields[14].trim() + "  " + fields[15].trim());
                            break;
                        }
                    }

                    Date date = df.parse(fields[11].trim());
                    System.out.println(date);

                    if (i<AcctList.size()) {
                        Acct = AcctList.get(i);
                        if (fields[6].trim().equals("ACTIVE")) {
                            Acct.Active++;
                            if (date.before(Acct.First))
                                Acct.First = date;
                            if (date.after(Acct.Last))
                                Acct.Last = date;
                            Acct.SerialNumbers.add(fields[13].trim());

                            if (fields.length > 16 && !fields[16].trim().equals("")) {
                                PurchaseOrder newPO = new PurchaseOrder();
                                newPO.ID = fields[16].trim();
                                newPO.EntitlementUnits = Integer.parseInt(fields[4].trim());
                                newPO.Units = 1;
                                int NumPOs = Acct.POs.size();
                                for (PurchaseOrder PO : Acct.POs) {
                                    if (PO.compareTo(newPO) != 0)
                                        NumPOs--;
                                    else
                                        PO.Units++;
                                }
                                if (NumPOs == 0)
                                    Acct.POs.add(newPO);
                            }
                            else {
                                Acct.NonPOLicenses++;
                            }
                        }

                        if ((fields[6].trim().equals("RETURNED"))) {
                            if (Acct.Active > Acct.Returned)
                                Acct.Returned++;
                        }
                    }
                    else {
                        Acct = new FoIPAccount();
                        Acct.AccountID = fields[14].trim();
                        Acct.AccountName = fields[15].trim().replace("\"","");
                        Acct.First = date;
                        Acct.Last = date;
                        if (fields[6].trim().equals("ACTIVE")) {
                            Acct.SerialNumbers.add(fields[13].trim());
                            Acct.Active++;
                            if (fields.length > 16 && !fields[16].trim().equals("")) {
                                PurchaseOrder newPO = new PurchaseOrder();
                                newPO.ID = fields[16].trim();
                                newPO.EntitlementUnits = Integer.parseInt(fields[4].trim());
                                newPO.Units=1;
                                int NumPOs = Acct.POs.size();
                                for (PurchaseOrder PO : Acct.POs) {
                                    if (PO.compareTo(newPO) != 0)
                                        NumPOs--;
                                    else
                                        PO.Units++;
                                }
                                if (NumPOs == 0)
                                    Acct.POs.add(newPO);
                            }
                            else {
                                Acct.NonPOLicenses++;
                            }
                        }
                        AcctList.add(Acct);
                        System.out.println(AcctList.size());
                    }
                    System.out.println(Acct.AccountName + " " + Acct.Active + " " + Acct.Returned);
                }
                else {
                    System.out.println(">>>" + fields.length);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        int TotalActive = 0;
        int TotalReturned = 0;
        int TotalUniqueSerialNumbers = 0;

        FileWriter fw = new FileWriter("C:\\Users\\pete\\Downloads\\FoIP Sales.csv");
        fw.write("FoIP License Sales\n\n");
        fw.write("Account ID,Account Name,Active,Returned,Unique Serial Numbers, First License, Last License,POs,PO Units,PO Entitlement Units,Non-PO Units\n");

        Collections.sort(AcctList, Collections.reverseOrder());

        for (int i=0; i<AcctList.size(); i++) {
            int NumPOs = 0;
            int PO_Units = 0;
            int PO_EntitlementUnits = 0;
            FoIPAccount Acct = AcctList.get(i);
            TotalActive += Acct.Active;
            TotalReturned += Acct.Returned;
            TotalUniqueSerialNumbers += Acct.SerialNumbers.size();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            UI.writeNoTime(String.format("%-16.16s",Acct.AccountID) +
                           " " + String.format("%-40s",Acct.AccountName) +
                           " " + String.format("%5d",Acct.Active) +
                           " " + String.format("%5d",Acct.Returned) +
                           " " + String.format("%5d",(Acct.SerialNumbers.size())) +
                           "   " + df.format(Acct.First) +
                           " : " + df.format(Acct.Last));
            if (Acct.POs.size() > 0) {
                NumPOs = 0;
                PO_Units = 0;
                for (PurchaseOrder PO : Acct.POs) {
                    NumPOs++;
                    PO_Units += PO.Units;
                    PO_EntitlementUnits += PO.EntitlementUnits;
                }
                UI.writelnNoTime("  POs: " + String.format("%3d",NumPOs) + "  Units: " + String.format("%5d",PO_Units));
             }
             else {
                UI.writelnNoTime("");
            }
            fw.write("\""+Acct.AccountID+"\",\""+Acct.AccountName+"\","+Acct.Active+","+Acct.Returned+","+Acct.SerialNumbers.size()+","+df.format(Acct.First)+","+df.format(Acct.Last)+","+NumPOs+","+PO_Units+","+PO_EntitlementUnits+","+Acct.NonPOLicenses+",\n");
        }

        UI.writelnNoTime(String.format("%-16s","") +
                         " " + String.format("%-40s","") +
                         " " + String.format("%5d",TotalActive) +
                         " " + String.format("%5d",TotalReturned) +
                         " " + String.format("%5d",TotalUniqueSerialNumbers));
        UI.writelnNoTime("");

        UI.writelnNoTime("Active: " + TotalActive + "  Returned: " + TotalReturned + "  Active-Returned: " + (TotalActive-TotalReturned) + "  Total Unique Serial Numbers: " + TotalUniqueSerialNumbers + "   REHOST: " + rehost + "   Demo: " + TotalDemo);
        UI.writelnNoTime("Total Licenses Sold: " + (TotalActive - TotalReturned));

        UI.writelnNoTime("");
        UI.writelnNoTime("Done.");

        fw.close();
    }
}
