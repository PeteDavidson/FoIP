package com.lexmark.dev.FoIP.FWLinks;


import com.lexmark.dev.FoIP.*;
import com.lexmark.dev.FoIP.Provisioner.LexmarkProvisionFoIP;
import com.lexmark.dev.FoIP.Provisioner.LexmarkProvisionFoIPApi;
import com.lexmark.dev.FoIP.Provisioner.ProvisionerUIOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.lexmark.dev.FoIP.LexmarkFirmwareUpdate.LATEST_PE;
import static com.lexmark.dev.FoIP.LexmarkFirmwareUpdate.RECOMMENDED;
import static javafx.application.Platform.exit;

public class FWLinks {
    public static void main(String[] args) {

        final LexmarkProvisionFoIPApi lexmarkFoIP = new LexmarkProvisionFoIP(new ProvisionerUIOutput());
        UIOutput UI = UIOutputHandler.getInstance().GetHandler();
        ModelList Models = ModelList.getInstance();

        StringBuilder outstring = new StringBuilder("");
        Path path = Paths.get(System.getProperty("user.home")+File.separator+"Models.csv");
        try {
            outstring.append("Code Name,NPAP ID,Class,Enterprise Models,BSD Models,SMB Models\n");

            Integer[] NPAPIDs = Models.GetAllNPAPIDs();
            for (Integer i: NPAPIDs) {
                String ThisLine;
                Model model = Models.GetModel(i);
                System.out.print(i + ": ");
                ThisLine = model.CodeName+","+model.NPAPID+","+model.Class+",";
                for (String s: model.EnterpriseModelNames) {
                    System.out.print(s + " ");
                    ThisLine += s+" ";
                }
                ThisLine +=",";
                for (String s: model.BSDModelNames) {
                    System.out.print(s + " ");
                    ThisLine += s+" ";
                }
                ThisLine +=",";
                for (String s: model.SMBModelNames) {
                    System.out.print(s + " ");
                    ThisLine += s+" ";
                }
                ThisLine += "\n";
                System.out.println();
                outstring.append(ThisLine);
            }
            Files.write(path,outstring.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


        String[] AllModels = Models.GetAllModelNames();

        for (String s: AllModels) {
            System.out.print(s+" ");
        }
        System.out.println();


        outstring = new StringBuilder("");
        path = Paths.get(System.getProperty("user.home")+File.separator+"LatestPELinks.csv");
        try {
            outstring.append("Model,Code Level,Link,NPAPID\n");

            for (String s: AllModels) {
                UI.writeln("Processing "+s);
                Integer NPAPID=Models.GetNPAPIDfromModelName(s);
                System.out.println("NPAPID: "+NPAPID);
                String ThisLine;
                //if (true) {
                if (Models.GetUseRecommendedCode(NPAPID)) {
                    String level = Models.GetFirmwareLevel(NPAPID, RECOMMENDED);
                    ThisLine = s + "," + level + ",>>> Get Recommended FW from support.lexmark.com <<<," + NPAPID + "\n";
                }
                else {
                    String[] link = Models.GetFirmwareLink(NPAPID, LATEST_PE);
                    ThisLine = s + "," + link[0] + "," + link[1] + "," + NPAPID + "\n";
                }
                outstring.append(ThisLine);
                UI.writeln(ThisLine);
            }
            Files.write(path,outstring.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        UI.writeln("");
        UI.writeln("Done.");

    }
}
