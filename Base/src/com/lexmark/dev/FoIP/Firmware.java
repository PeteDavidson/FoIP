package com.lexmark.dev.FoIP;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.lexmark.dev.FoIP.LexmarkFirmwareUpdate.*;

/**
 * Created by Pete on 3/24/2017.
 */
public class Firmware {

    private NetworkDevice LexmarkRepository;
    private UIOutput UI;

    public Firmware() {
        String server = "ccs.lexmark.com";
        LexmarkRepository = new NetworkDevice(server);
        if (LexmarkRepository.getValidTransport()) {
            LexmarkRepository.setUseHttps(true);
            UI = UIOutputHandler.getInstance().GetHandler();
        }
        else {
            throw new LexmarkFoIPException("Could not find Lexmark FW Server " + server);
        }
    }

    public String[] GetFirmwareLink(MFP mfp, LexmarkFirmwareUpdate Level) {
        String[] Base;
        String BaseRev;
        String BaseBuildId;
        String BaseVersion;

        if (Level == LATEST_PE || Level == FORCE_LATEST_PE)
            UI.writeln("Looking for Latest PE Release of Firmware on Lexmark Server");
        else
            UI.writeln("Looking for Recommended Release of Firmware on Lexmark Server");

        UI.write("Getting BaseBuildId from Lexmark Server...");
        try {
            Base = FindBuildId(mfp, Level);
        }
        catch (Exception e) {
            UI.writelnNoTime("Failed");
            throw new LexmarkFoIPException("Could not get BaseBuildId from Lexmark Server");
        }
        UI.writelnNoTime("OK");

        BaseBuildId = Base[0];
        BaseRev = Base[1];
        BaseVersion = Base[2];


        if (Level == LATEST_PE) {
            UI.writeln("Latest PE Release is " + BaseRev + " (" + BaseVersion + ")");
        }
        else if (Level == RECOMMENDED) {
            UI.writeln("Recommended Release is " + BaseRev + " (" + BaseVersion + ")");
        }

        System.out.println("Base FW Revision: " + BaseBuildId);


        String MaterialId = "";

        UI.write("Finding Firmware File on Lexmark Server...");

        if (mfp.PrinterNPAPID() >= 0x80) {      // WC or later
            try {
                MaterialId = FindMaterialId(BaseBuildId, "COMBO_FLS");
            } catch (Exception e) {
                UI.writelnNoTime("Failed");
                UI.writeln("No MaterialId COMBO_FLS for Base = " + BaseBuildId);
                UI.write("Trying FULLNET_FLS (Engine Code will not be updated)...");
            }
        }

        if (MaterialId.equals("")) {
            try {
                MaterialId = FindMaterialId(BaseBuildId, "FULLNET_FLS");
            } catch (Exception e2) {
                UI.writelnNoTime("Failed");
                UI.writeln("No MaterialId FULLNET_FLS for Base = " + BaseBuildId);
                throw new LexmarkFoIPException("No MaterialId for Base = " + BaseBuildId);
            }
        }

        UI.writelnNoTime("OK");

        UI.writeln("https://ccs.lexmark.com/firmware/materials/"+MaterialId+"/file");

        System.out.println("Material Id: " + MaterialId);
        return(new String[]{BaseRev, "https://ccs.lexmark.com/firmware/materials/" + MaterialId + "/file"});
    }

    public void UpdateFirmware(MFP mfp, LexmarkFirmwareUpdate Level, String LocalFile) throws IOException, LexmarkFoIPException {

        if (Level == LOCAL_FILE) {
            String NewCodeLevel = "";

            UI.writeln("Updating Firmware from Local File " + LocalFile);

            UI.write("Sending Firmware to MFP...");
            try {
                NewCodeLevel = UpdateFirmwareFromLocalFile(mfp, LocalFile);
                UI.write("Waiting for MFP to Reboot...");

                mfp.WaitForMFPReboot(UI);
            }
            catch (Exception e) {
                UI.writelnNoTime("Failed");
                UI.Delay(10);
                throw e;
            }

            UI.writelnNoTime("OK");
            UI.write("Checking if Firmware Update was successful...");

            System.out.println(mfp.BaseCodeRevision() + " == " + NewCodeLevel + " ?");
            if (!mfp.BaseCodeRevision().equals(NewCodeLevel)) {
                UI.writelnNoTime("Failed");
                throw new LexmarkFoIPException("Firmware Update failed");
            }
            UI.writelnNoTime("OK");

            return;
        }
        else {
            String[] Base;
            String BaseRev;
            String BaseBuildId;
            String BaseVersion;

            if (Level == LATEST_PE || Level == FORCE_LATEST_PE)
                UI.writeln("Looking for Latest PE Release of Firmware on Lexmark Server");
            else
                UI.writeln("Looking for Recommended Release of Firmware on Lexmark Server");

            UI.write("Getting BaseBuildId from Lexmark Server...");
            try {
                Base = FindBuildId(mfp, Level);
            }
            catch (Exception e) {
                UI.writelnNoTime("Failed");
                throw new LexmarkFoIPException("Could not get BaseBuildId from Lexmark Server");
            }
            UI.writelnNoTime("OK");

            BaseBuildId = Base[0];
            BaseRev = Base[1];
            BaseVersion = Base[2];

            boolean doUpdate = false;

            if (Level == FORCE_LATEST_PE) {
                UI.writeln("Forcing Firmware Update to Latest PE Release " + BaseRev + " (" + BaseVersion + ")");
                doUpdate = true;
            }
            else if (Level == FORCE_RECOMMENDED) {
                UI.writeln("Forcing Firmware Update to Recommended Release " + BaseRev + " (" + BaseVersion + ")");
                doUpdate = true;
            }
            else {
                if (Level == LATEST_PE) {
                    UI.writeln("Latest PE Release is " + BaseRev + " (" + BaseVersion + ")");
                }
                else if (Level == RECOMMENDED) {
                    UI.writeln("Recommended Release is " + BaseRev + " (" + BaseVersion + ")");
                }
                UI.write("Checking if MFP Firmware Revision " + mfp.BaseCodeRevision() + " is up to date...");

                System.out.println("Base FW Revision: " + BaseBuildId);

                if (!FirmwareUpToDate(mfp, BaseRev)) {
                    UI.writelnNoTime("No");
                    UI.writeln("Need to Update MFP Firmware to Release " + BaseRev + " (" + BaseVersion + ")");
                    doUpdate = true;
                }
                else {
                    UI.writelnNoTime("OK");
                    doUpdate = false;
                }
            }

            if (doUpdate) {
                String MaterialId = "";

                UI.write("Finding Firmware File on Lexmark Server...");

                if(mfp.PrinterNPAPID() >= 0x80) {      // WC or later
                    try {
                        MaterialId = FindMaterialId(BaseBuildId, "COMBO_FLS");
                    } catch (Exception e) {
                        UI.writelnNoTime("Failed");
                        UI.writeln("No MaterialId COMBO_FLS for Base = " + BaseBuildId);
                        UI.write("Trying FULLNET_FLS (Engine Code will not be updated)...");
                    }
                }

                if (MaterialId.equals("")) {
                    try {
                        MaterialId = FindMaterialId(BaseBuildId, "FULLNET_FLS");
                    } catch (Exception e2) {
                        UI.writelnNoTime("Failed");
                        UI.writeln("No MaterialId FULLNET_FLS for Base = " + BaseBuildId);
                        throw new LexmarkFoIPException("No MaterialId for Base = " + BaseBuildId);
                    }
                }

                System.out.println("Material Id: " + MaterialId);

                UI.writelnNoTime("OK");
                UI.write("Sending Firmware to MFP...");
                try {
                    UpdateFirmwareWithMaterialId(mfp, MaterialId);
                    UI.write("Waiting for MFP to Reboot...");
                    mfp.WaitForMFPReboot(UI);
                } catch (Exception e) {
                    UI.writelnNoTime("Failed");
                    UI.Delay(10);
                    throw e;
                }

                UI.writelnNoTime("OK");
                UI.write("Checking if Firmware Update was successful...");

                System.out.println(mfp.BaseCodeRevision() + " == " + BaseRev + " ?");
                if (!mfp.BaseCodeRevision().equals(BaseRev)) {
                    UI.writelnNoTime("Failed");
                    throw new LexmarkFoIPException("Firmware Update failed");
                }
                UI.writelnNoTime("OK");

            }
        }
    }

    private int GetMaterialIdFileSize(String MaterialId) throws IOException, LexmarkFoIPException {
        String FileSizeString = "0";

        WebConnection connection = null;

        try {
            String line;

            //Create Lexmark Repository connection
            connection = new WebConnection(LexmarkRepository,"/firmware/materials/"+MaterialId);
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while ((line = rd.readLine()) != null) {

                if (line.contains("material id=")) {
                    String[] tokens = line.split("[<>']");
                    for (int i = 0; i < tokens.length; i++) {
                        if (tokens[i].trim().equals("length=")) {
                            FileSizeString = tokens[i + 1].trim();
                            break;
                        }
                    }
                }
            }

            is.close();

        } catch (Exception e) {
            System.out.println("Exception: " + e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Integer.parseInt(FileSizeString);
    }

    private String UpdateFirmwareFromLocalFile(MFP mfp, String FileName) throws IOException , LexmarkFoIPException {
        Socket socket;
        String CodeLevel = "";

        System.out.println("Updating FW on MFP "+mfp.IPAddr());

        int totalBytesRead = 0;

        try {
            //Create MFP connection
            socket = new Socket(mfp.IPAddr(),9100);
            DataOutputStream wr = new DataOutputStream(socket.getOutputStream());

            File fwFile = new File(FileName);
            int FileSize = (int) fwFile.length();
            System.out.println("FileSize: " + FileSize);
            InputStream is = new FileInputStream(fwFile);

            byte[]buffer = new byte[1024];
            int bytesRead;

            UI.writeNoTime(String.format("%1$2d",totalBytesRead/(FileSize/100)) + "%  Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
            int i = 0;
            boolean LookingForCodeLevel = true;
            while ((bytesRead = is.read(buffer)) != -1) {
                if (LookingForCodeLevel) {
                    String str = new String(buffer, StandardCharsets.UTF_8);
                    String[] data = str.split("RIP=\"");
                    String[] data2 = data[1].split("\"");
                    CodeLevel = data2[0];
                    LookingForCodeLevel = false;
                }
                wr.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                i++;
                if ((i % 200) == 0) {
                    UI.overwrite(String.format("%1$2d",totalBytesRead/(FileSize/100)) + "%  Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
                }
            }

            UI.overwrite("OK   Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
            UI.writelnNoTime("");

            System.out.println("Total Bytes sent to MFP: "+totalBytesRead);

            is.close();
            System.out.println("Input Stream closed.");
            wr.flush ();
            System.out.println("Data Output Stream flushed.");
            wr.close ();
            System.out.println("Data Output Stream closed.");

        } catch (Exception e) {
            UI.overwrite("Failed  Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
            UI.writelnNoTime("");
            throw e;
        }

        return CodeLevel;
    }

    private void UpdateFirmwareWithMaterialId(MFP mfp, String MaterialId) throws IOException, LexmarkFoIPException {
        Socket socket;

        System.out.println("Updating FW on MFP "+mfp.IPAddr());

        WebConnection connection = null;

        int totalBytesRead = 0;

        try {
            int FileSize = GetMaterialIdFileSize(MaterialId);

            System.out.println("FileSize: " + FileSize);

            //Create MFP connection
            socket = new Socket(mfp.IPAddr(),9100);
            DataOutputStream wr = new DataOutputStream(socket.getOutputStream());

            //Create Lexmark Repository connection
            connection = new WebConnection(LexmarkRepository,"/firmware/materials/"+MaterialId+"/file" );
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();

            byte[]buffer = new byte[1024];
            int bytesRead;

            UI.writeNoTime(String.format("%1$2d",totalBytesRead/(FileSize/100)) + "%  Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
            int i = 0;
            while ((bytesRead = is.read(buffer)) != -1) {
                wr.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                i++;
                if ((i % 200) == 0) {
                    UI.overwrite(String.format("%1$2d",totalBytesRead/(FileSize/100)) + "%  Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
                }
            }

            UI.overwrite("OK   Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
            UI.writelnNoTime("");

            System.out.println("Total Bytes sent to MFP: "+totalBytesRead);

            is.close();
            System.out.println("Input Stream closed.");
            wr.flush ();
            System.out.println("Data Output Stream flushed.");
            wr.close ();
            System.out.println("Data Output Stream closed.");

        } catch (Exception e) {
            UI.overwrite("Failed  Bytes Transferred: "+String.format("%1$9s", totalBytesRead));
            UI.writelnNoTime("");
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String [] FindBuildId(MFP mfp, LexmarkFirmwareUpdate Level) {
        if (Level == RECOMMENDED || Level == FORCE_RECOMMENDED) {
            return FindBuildIdRecommended(mfp);
        }
        else if (Level == LATEST_PE || Level == FORCE_LATEST_PE) {
            return FindBuildIdLatestPE(mfp);
        }
        return new String[]{"","",""};
    }

    /**
     * Compares two version strings.
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
     */
    public static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }

    private String[] FindBuildIdLatestPE(MFP mfp) {
        String BuildId = "";
        String FWRev = "";
        String Version = "0.0.0";

        WebConnection connection = null;
        try {
            String line;
            Integer NPAID = mfp.PrinterNPAPID();

            //Create connection
            connection = new WebConnection(LexmarkRepository,"/firmware/builds?componentType=base&compatibleNpaFamilyId="+NPAID.toString() );

            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while ((line = rd.readLine()) != null) {

                if (line.contains("build id=")) {

                    String lineFWRev = "";
                    String lineBuildId = "";
                    String lineReleaseType = "";
                    String lineVersion = "";
                    String[] tokens = line.split("[<>']");
                    for (int i=0; i<tokens.length && (lineFWRev.isEmpty() || lineBuildId.isEmpty()) || lineReleaseType.isEmpty() || lineVersion.isEmpty(); i++) {
                        if (tokens[i].trim().equals("build id=")) {
                            lineBuildId = tokens[i+1].trim();
                            i++;
                        }
                        else if (tokens[i].trim().equals("name=")) {
                            lineFWRev = tokens[i+1].trim();
                            i++;
                        }
                        else if (tokens[i].trim().equals("releaseType=")) {
                            lineReleaseType = tokens[i+1].trim();
                            i++;
                        }
                        else if (tokens[i].trim().equals("version=")) {
                            lineVersion = tokens[i+1].trim();
                            i++;
                        }
                    }

                    System.out.println("Found base FW Rev " + lineFWRev + "  Version " + lineVersion + "  Build Id " + lineBuildId);

                    // The build we are looking for must be Release Type of PE
                    if (lineReleaseType.equals("PE") /* || lineReleaseType.equals("EC")*/) {
                        if (versionCompare(lineVersion,Version) >= 0) {
                            FWRev = lineFWRev;
                            BuildId = lineBuildId;
                            Version = lineVersion;
                        }
                    }
                }
            }

            System.out.println("Latest PE Release: " + FWRev + "  Version " + Version + "  Build Id " + BuildId);

            rd.close();

        } catch (Exception e) {
            UI.writeln("Failed: " + e);
         } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return new String[]{BuildId,FWRev,Version};
    }

    public String[] FindBuildIdRecommended(MFP mfp) {
        String BuildId = "";
        String FWRev = "";
        String Version = "";
        WebConnection connection = null;
        try {
            String line;
            Integer NPAID = mfp.PrinterNPAPID();

            //Create connection
            connection = new WebConnection(LexmarkRepository,"/firmware/builds/recommended?componentType=base&compatibleNpaFamilyId="+NPAID.toString() );

            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while ((line = rd.readLine()) != null) {
                if (line.contains("recommendedBuilds")) {

                    String[] tokens = line.split("[<>']");
                    for (int i=0; i<tokens.length && (FWRev.isEmpty() || BuildId.isEmpty() || Version.isEmpty()); i++) {
                        if (tokens[i].trim().equals("build id=")) {
                            BuildId = tokens[i+1].trim();
                            i++;
                        }
                        else if (tokens[i].trim().equals("name=")) {
                            FWRev = tokens[i+1].trim();
                            i++;
                        }
                        else if (tokens[i].trim().equals("version=")) {
                            Version = tokens[i+1].trim();
                            i++;
                        }
                    }
                    System.out.println("Retrieving base FW Rev " + FWRev + "  Version " + Version + "  Build Id " + BuildId);
                }
            }

            rd.close();

        } catch (Exception e) {
            UI.writeln("Failed: " + e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return new String[]{BuildId,FWRev,Version};
    }

    private String FindMaterialId(String Base, String Type) throws IOException, LexmarkFoIPException {
        String MaterialId = "";
        WebConnection connection = null;
        String line;

        // First try COMBO_FLS
        try {
            //Create connection
            connection = new WebConnection(LexmarkRepository, "/firmware/materials?type=" + Type + "&relatedBuildId=" + Base,true,240);
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while ((line = rd.readLine()) != null) {
                if (line.contains("material id=")) {
                    String[] tokens = line.split("='");
                    if ((tokens.length > 1)) {
                        MaterialId = tokens[1];
                        tokens = MaterialId.split("'");
                        MaterialId = tokens[0];
                    }
                }
            }

            rd.close();
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (MaterialId.equals("")) {
            throw new LexmarkFoIPException("Could not find Firmware File on Lexmark Server");
        }
        return MaterialId;
    }

    private boolean FirmwareUpToDate(MFP mfp, String RecommendedFW) {
        mfp.Refresh();
        String mfpFW = mfp.BaseCodeRevision();
        System.out.println("Latest recommended code is " + RecommendedFW);
        System.out.println("MFP current code is " + mfpFW);



        if (mfp.MojaOrLaterDevice()) {
            System.out.println("(" + mfpFW.substring(0,5) + " == " + RecommendedFW.substring(0,5) + ")");
            System.out.println("(" + mfpFW + " == " + RecommendedFW + ") : " + mfpFW.compareTo(RecommendedFW));
            if (mfpFW.substring(0,5).equals(RecommendedFW.substring(0,5))) {
                if (mfpFW.compareTo(RecommendedFW) >= 0) {
                    return true;
                }
            }
        }
        else {
            System.out.println("(" + mfpFW.substring(0,2) + " == " + RecommendedFW.substring(0,2) + ")");
            System.out.println("(" + mfpFW + " == " + RecommendedFW + ") : " + mfpFW.compareTo(RecommendedFW));
            if (mfpFW.substring(0,2).equals(RecommendedFW.substring(0,2))) {
                if (mfpFW.compareTo(RecommendedFW) >= 0) {
                    return true;
                }
            }
        }

        return false;
    }
}
