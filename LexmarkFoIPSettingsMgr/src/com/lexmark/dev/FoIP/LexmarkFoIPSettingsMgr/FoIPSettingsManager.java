package com.lexmark.dev.FoIP.LexmarkFoIPSettingsMgr;


import com.lexmark.dev.FoIP.*;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.commons.lang.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FoIPSettingsManager extends JPanel {

    FoIPSetting settingSipProxy;
    FoIPSetting settingSipRegistrar;
    FoIPSetting settingSipUser;
    FoIPSetting settingSipPassword;
    FoIPSetting settingSipContact;
    FoIPSetting settingSipRealm;
    FoIPSetting settingSipAuthID;
    FoIPSetting settingSipOBProxy;
    FoIPSetting settingRegTransport;
    FoIPSetting settingIncomingTransport;
    FoIPSetting settingOutgoingTransport;
    FoIPSetting settingH323Gateway;
    FoIPSetting settingH323Gatekeeper;
    FoIPSetting settingH323User;
    FoIPSetting settingH323Password;
    FoIPSetting settingH323EnableFastStart;
    FoIPSetting settingH323DisableH245;
    FoIPSetting settingH323DisableGKDisc;
    FoIPSetting settingT38Indicator;
    FoIPSetting settingT38LowSpeed;
    FoIPSetting settingT38HighSpeed;
    FoIPSetting settingT38KeepAlive;
    FoIPSetting settingLicense;
    FoIPSetting settingVoipProtocol;
    FoIPSetting settingTraceLevel;
    FoIPSetting settingStunServer;
    FoIPSetting settingForceFaxMode;
    FoIPSetting settingForceFaxModeDelay;
    FoIPSetting settingFaxName;
    FoIPSetting settingFaxNumber;
    FoIPSetting settingFaxID;
    FoIPSetting settingFaxMode;
    FoIPSetting settingFaxTransport;
    FoIPSetting settingHTTPSServiceURL;
    FoIPSetting settingHTTPSProxy;
    FoIPSetting settingHTTPSProxyUser;
    FoIPSetting settingHTTPSProxyPassword;
    FoIPSetting settingHTTPSPeerVerification;
    FoIPSetting settingHTTPSEncryptFaxSend;
    FoIPSetting settingHTTPSEncryptFaxReceive;
    FoIPSetting settingAutomaticRedial;
    FoIPSetting settingRedialFrequency;
    FoIPSetting settingRingsToAnswer;
    FoIPSetting settingBundleFileName;
    FoIPSetting settingUCFFileName;
    FoIPSetting settingSettingsFileName;
    FoIPSetting settingMFPListFileName;
    FoIPSetting settingListResultFileName;
    FoIPSetting settingMFP;
    FoIPSetting settingMFPPhone;
    FoIPSetting settingMFPUser;
    FoIPSetting settingMFPPassword;
    FoIPSetting settingTesterMFP;
    FoIPSetting settingTesterPhone;
    FoIPSetting settingTesterUser;
    FoIPSetting settingTesterPassword;
    public ArrayList<FoIPSetting> arrayList = new ArrayList<FoIPSetting>();

    MFPStore mfpStore = MFPStore.getInstance();
    UIOutput UI;

    boolean CancelList = false;

    String LastT38TraceFile = "";

    String[] faxModeStrings = { "Analog/T.38", "-------", "Fax Server" };
    String[] transportStrings = { "Analog", "T.38", "G.711", "-------", "etherFAX", "NativeFAX Direct", "VoIP Provider" };
    String[] protocolStrings = { "SIP", "H.323", "SIP and H.323" };
    String[] faxIDStrings = {"Fax Name", "Fax Number" };
    String[] udptcp = {"UDP", "TCP"};
    String[] udpandtcp = {"UDP", "TCP", "UDP and TCP"};
    String[] encryption = {"Disabled", "Enabled", "Required"};

    String ProjectDirectory = System.getProperty("user.home");
    String LogFileName = "FoIPSettingsLog";
    static final int MAX_LOG_FILES = 10;

    TextTransferHandler th;

    public static final int CHARS_IN_RESULT_AREA_LIMIT = 100000;

    String Header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bundle>\n<bundleInfo>\n<name> FoIP Setttings Bundle </name>\n<description>\nSettings for the FoIP Feature\n</description>\n</bundleInfo>\n";
    String ucfHeader = "// UCF File for FoIP\r\n//\r\n";
    String ucfFooter = "\r\n";
    //String ucfHeader = "\u001b%-12345X@PJL LWRITEUCF\r\n// UCF File for FoIP\r\n//\r\n";
    //String ucfFooter = "\u001b%-12345X@PJL END DATA\r\n\u001b%-12345X\r\n";
    String helpMessage = "<!DOCTYPE html>\r\n<html><body>" +
            "<h1>FoIP Settings Manager</h1>" +
            "<body>" +
            "This app handles the creation and deployment of FoIP Settings Bundles. " +
            "The upper part of the screen contains all of the FoIP settings that may be included in a bundle. " +
            "The center part of the screen contains a list of files, IP addresses, and phone numbers that are used by the app. " +
            "The lower part of the screen is a log of the activities of the app. " +
            "The check box preceeding each setting controls which settings are to be included in the bundle. " +
            "The following describes the menu bar items: " +
            "<h2>File</h2>" +
            "<h3>Choose Project Directory</h3>" +
            "<h3>Zip Project Directory</h3>" +
            "<h3>Load Default Settings</h3>" +
            "<h3>Load Settings File</h3>" +
            "<h3>Save Settings File</h3>" +
            "<h3>Choose Settings File</h3>" +
            "<h3>Choose Bundle File</h3>" +
            "<h3>Choose UCF File</h3>" +
            "<h3>Choose MFP List File</h3>" +
            "<h3>Create Template MFP List File</h3>" +
            "<h3>Choose List Result File</h3>" +
            "<h3>Choose FoIP License File</h3>" +
            "<h3>Choose FoIP License Directory</h3>" +
            "<h3>Quit</h3>" +
            "<h2>Edit</h2>" +
            "<h3>Cut</h3>" +
            "<h3>Copy</h3>" +
            "<h3>Paste</h3>" +
            "<h2>Log</h2>" +
            "<h3>Start New Log File</h3>" +
            "<h3>Remove All Old Settings Log Files</h3>" +
            "<h2>Select</h2>" +
            "<h3>Select All</h3>" +
            "<h3>Select None</h3>" +
            "<h2>Security</h2>" +
            "<h3>Log in to MFP</h3>" +
            "<h3>Log in to Tester MFP</h3>" +
            "<h2>UCF</h2>" +
            "<h3>Create UCF File with Selected Settings</h3>" +
            "<h3>Send UCF File to MFP</h3>" +
            "<h3>Get Settings from MFP with UCF</h3>" +
            "<h2>Bundles</h2>" +
            "<h3>Create VCC Settings Bundle from Selected Settings</h3>" +
            "<h3>Create VCC Settings and License Bundle from Selected Settings</h3>" +
            "<h3>Create VCC License only Bundle</h3>" +
            "<h3>Send Bundle to MFP</h3>" +
            "<h2>eSF App</h2>" +
            "<h3>Send eSF App File to MFP</h3>" +
            "<h3>Send eSF App File to MFP via print path</h3>" +
            "<h3>Send eSF App License File to MFP</h3>" +
            "<h3>Get eSF App Info</h3>" +
            "<h2>MFP Cache</h2>" +
            "<h3>Print MFP Cache</h3>" +
            "<h3>Clear MFP Cache</h3>" +
            "<h3>IP Addr Range Sweep using http</h3>" +
            "<h3>IP Addr Range Sweep using https</h3>" +
            "<h2>Info</h2>" +
            "<h3>Get Info</h3>" +
            "<h3>Get SIP Registration Status</h3>" +
            "<h3>Get FoIP License Info</h3>" +
            "<h3>Get T.30 Log From MFP</h3>" +
            "<h3>Get T.30 Log From Tester MFP</h3>" +
            "<h3>Get All T.30 Logs From MFP</h3>" +
            "<h3>Get All T.30 Logs From Tester MFP</h3>" +
            "<h3>Get All T.30 Error Logs From MFP</h3>" +
            "<h3>Get All T.30 Error Logs From Tester MFP</h3>" +
            "<h3>Get T.38 Trace Log From MFP</h3>" +
            "<h3>Get T.38 Trace Log From Tester MFP</h3>" +
            "<h3>Run T.38 Trace Log Parser</h3>" +
            "<h3>Remove All T.30 Logs</h3>" +
            "<h3>Remove All T.38 Trace Logs</h3>" +
            "<h2>Test</h2>" +
            "<h3>Send Fax from MFP</h3>" +
            "<h3>Send Fax from Tester MFP</h3>" +
            "<h3>Get Last Fax Status from MFP</h3>" +
            "<h3>Get Last Fax Status from Tester MFP</h3>" +
            "<h2>List</h2>" +
            "<h3>Deploy Bundles</h3>" +
            "<h3>Run Fax Tests</h3>" +
            "<h3>Get SIP Registration Status</h3>" +
            "<h3>Get FoIP License Info</h3>" +
            "<h3>Deploy Bundles and Check License and Registration</h3>" +
            "<h3>Deploy Bundles and Run Fax Tests</h3>" +
            "<h3>Get Settings for MFP from List File</h3>" +
            "<h3>Copy MFP List Result File to MFP List File</h3>" +
            "<h3>Cancel Current List Operation</h3>" +
            "<h2>Help</h2>" +
            "<h3>Help</h3>" +
            "<h3>About</h3>" +
            "<h2>List File csv Headings</h2>" +
            "<h3>IP Address or FQDN</h3>" +
            "The IP Address or Fully Qualified Domain Name of the MFP under deployment." +
            "<h3>Phone Number</h3>" +
            "The phone number that is dialed on the Tester MFP to reach the MFP under deployment." +
            "<h3>License File</h3>" +
            "The file that contains the FoIP license, either *.lic or *.zip." +
            "<h3>MFP User</h3>" +
            "When using security on the MFP, this is the user." +
            "<h3>MFP Password</h3>" +
            "When using security on the MFP, this is the password or PIN." +

            "<h3>mfp.fax.stationName</h3>" +
            "Fax Name (Station Name)" +
            "<h3>mfp.fax.stationNumber</h3>" +
            "Fax Number (Station Number)" +
            "<h3>mfp.fax.stationID</h3>" +
            "Fax ID (Station ID) Chooses between Fax Name and Fax Number to be sent during transmission." +
            "<h3>mfp.fax.mode</h3>" +
            "Fax Mode -- Shoule always be set to 0 (Analog/T.38/G.711)." +
            "<h3>mfp.fax.voip.faxTransport</h3>" +
            "Fax Transport -- 0=analog, 1=T.38, 2=G.711" +
            "<h3>mfp.fax.voip.protocol</h3>" +
            "VoIP Protocol -- 1=SIP, 2=H.323" +
            "<h3>mfp.fax.voip.traceLevel</h3>" +
            "Trace Level -- Normally set to 0" +
            "<h3>mfp.fax.voip.stunServer</h3>" +
            "STUN Server" +
            "<h3>mfp.fax.voip.forceFaxMode</h3>" +
            "Force Fax Mode" +
            "<h3>mfp.fax.voip.forceFaxModeDelay</h3>" +
            "Force Fax Mode Delay" +
            "<h3>mfp.fax.sip.proxy</h3>" +
            "SIP Proxy" +
            "<h3>mfp.fax.sip.registrar</h3>" +
            "SIP Registrar" +
            "<h3>mfp.fax.sip.user</h3>" +
            "SIP User" +
            "<h3>mfp.fax.sip.password</h3>" +
            "SIP Password" +
            "<h3>mfp.fax.sip.contact</h3>" +
            "SIP Contact" +
            "<h3>mfp.fax.sip.realm</h3>" +
            "SIP Realm" +
            "<h3>mfp.fax.sip.authID</h3>" +
            "SIP Auth ID" +
            "<h3>mfp.fax.sip.outboundProxy</h3>" +
            "SIP Outbound Proxy" +
            "<h3>mfp.fax.h323.gateway</h3>" +
            "H.323 Gateway" +
            "<h3>mfp.fax.h323.gatekeeper</h3>" +
            "H.323 Gatekeeper" +
            "<h3>mfp.fax.h323.user</h3>" +
            "H.323 User" +
            "<h3>mfp.fax.h323.password</h3>" +
            "H.323 Password" +
            "<h3>mfp.fax.h323.enableFastStart</h3>" +
            "H.323 Enable Fast Start" +
            "<h3>mfp.fax.h323.disableH245Tunnel</h3>" +
            "H.323 Disable H.245 Tunneling" +
            "<h3>mfp.fax.h323.disableGatekeeperDiscovery</h3>" +
            "H.323 Disable Gatekeeper Discovery" +
            "<h3>mfp.fax.t38.indicatorRedundancy</h3>" +
            "T.38 Indicator Redundancy" +
            "<h3>mfp.fax.t38.lowSpeedRedundancy</h3>" +
            "T.38 Low Speed Redundancy" +
            "<h3>mfp.fax.t38.highSpeedRedundancy</h3>" +
            "T.38 High Speed Redundancy" +
            "<h3>mfp.fax.t38.udptlKeepAliveInterval</h3>" +
            "T.38 UDPTL Keep Alive Interval (ms)" +
            "<h3>mfp.fax.send.autoRedial</h3>" +
            "Automatic Redial (0-9)" +
            "<h3>mfp.fax.send.redialFrequency</h3>" +
            "Redial Frequency (1-200 mins)" +
            "<h3>mfp.fax.receive.ringsToAnswer</h3>" +
            "Rings to Answer (1-25)" +
            "</body></html>";

    String TemplateMFPListFile = "IP Address or FQDN,Phone Number,License File,MFP User,MFP Password,mfp.fax.sip.user,mfp.fax.sip.password,mfp.fax.sip.authID,mfp.fax.stationName,mfp.fax.stationNumber";

    /**
     * Create an Edit menu to support cut/copy/paste.
     */
    public JMenuBar createMenuBar () {

        JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem = null;
        JMenu menu;

        // File Menu
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);

        menuItem = new JMenuItem("Choose Project Directory",KeyEvent.VK_P);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setCurrentDirectory(new File(ProjectDirectory));
                int returnValue = fileChooser.showDialog(null,"Choose Directory");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    ProjectDirectory = fileChooser.getSelectedFile().getAbsolutePath();
                    File f = new File(ProjectDirectory);
                    if (f.exists()) {
                        if (!f.isDirectory()) {
                            WriteToResultArea(ProjectDirectory + " is not a directory.");
                            processingLastRequestDone();
                            return;
                        }
                    }
                    else {
                        WriteToResultAreaNoCRLF("Creating new directory " + ProjectDirectory + "...");
                        if (!f.mkdirs()) {
                            WriteToResultAreaNoTime("Failed");
                            processingLastRequestDone();
                            return;
                        }
                        WriteToResultAreaNoTime("OK");
                    }
                    UI.initLog(ProjectDirectory,LogFileName,MAX_LOG_FILES);
                    top.setTitle(" FoIP Settings Manager -- Project Diretory: " + ProjectDirectory);
                    WriteToResultArea("Project Directory changed to:  " + ProjectDirectory);
                    LoadDefaultSettings();
                    if (new File(settingSettingsFileName.getText()).exists()) {
                        LoadSettingsFile();
                    }
                    try {
                        PrintWriter pwriter = new PrintWriter(System.getProperty("user.home")+File.separator+"FoIPSettingsDefaultProject.txt","UTF-8");
                        pwriter.println(ProjectDirectory);
                        pwriter.close();
                    }
                    catch (Exception e) {
                        WriteToResultArea("Could not write FoIPSettingsDefaultProject.txt: " + e);
                    }

                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Zip Project Directory",KeyEvent.VK_D);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                CreateZipOfDir zipDir = new CreateZipOfDir(ProjectDirectory,ProjectDirectory + File.separator + "ProjectDir.zip");
                WriteToResultArea("File "+ProjectDirectory+File.separator+"ProjectDir.zip written.");
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Load Default Settings",KeyEvent.VK_D);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                LoadDefaultSettings();
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Load Settings File",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                LoadSettingsFile();
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Save Settings File",KeyEvent.VK_S);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                SaveSettingsFile();
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Choose Settings File",KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Settings file", "txt" ));
                fileChooser.setSelectedFile(new File(settingSettingsFileName.getText()));
                int returnValue = fileChooser.showDialog(null,"Choose File");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    settingSettingsFileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Choose Bundle File",KeyEvent.VK_B);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("VCC Bundle file", "zip" ));
                fileChooser.setSelectedFile(new File(settingBundleFileName.getText()));
                int returnValue = fileChooser.showDialog(null,"Choose File");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    settingBundleFileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Choose UCF File",KeyEvent.VK_U);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("UCF file", "ucf" ));
                fileChooser.setSelectedFile(new File(settingUCFFileName.getText()));
                int returnValue = fileChooser.showDialog(null,"Choose File");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    settingUCFFileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Choose MFP List File",KeyEvent.VK_M);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("MFP List file", "csv" ));
                fileChooser.setSelectedFile(new File(settingMFPListFileName.getText()));
                int returnValue = fileChooser.showDialog(null,"Choose File");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    settingMFPListFileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Create Template MFP List File",KeyEvent.VK_T);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                WriteToResultAreaNoCRLF("Writing MFPList File Template to " + settingMFPListFileName.getText() + "...");
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to overwrite file " +
                                settingMFPListFileName.getText(),
                        "Write Template MFPList File",
                        JOptionPane.OK_CANCEL_OPTION)) {

                    try {
                        PrintWriter pwriter = new PrintWriter(settingMFPListFileName.getText(),"UTF-8");
                        pwriter.println(TemplateMFPListFile);
                        pwriter.close();
                        WriteToResultAreaNoTime("OK");
                    }
                    catch (Exception e) {
                        WriteToResultAreaNoTime("Failed");
                        WriteToResultArea("Could not write Template MFPList File: " + e);
                    }
                }
                else {
                    WriteToResultAreaNoTime("Canceled");
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Choose List Result File",KeyEvent.VK_R);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("List Result file", "csv" ));
                fileChooser.setSelectedFile(new File(settingListResultFileName.getText()));
                int returnValue = fileChooser.showDialog(null,"Choose File");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    settingListResultFileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Choose FoIP License File",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("License file", "lic", "zip"));
                if (settingLicense.getText().equals(""))
                    fileChooser.setCurrentDirectory(new File(ProjectDirectory));
                else
                    fileChooser.setCurrentDirectory(new File(settingLicense.getText()));
                int returnValue = fileChooser.showDialog(null,"Choose File");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    settingLicense.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Choose FoIP License Directory",KeyEvent.VK_D);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (settingLicense.getText().equals(""))
                    fileChooser.setCurrentDirectory(new File(ProjectDirectory));
                else
                    fileChooser.setCurrentDirectory(new File(settingLicense.getText()));
                int returnValue = fileChooser.showDialog(null,"Choose Directory");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    settingLicense.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Quit",KeyEvent.VK_Q);
        menuItem.addActionListener(new QuitAction());
        menu.add(menuItem);

        menuBar.add(menu);

        // Edit Menu
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);

        menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
        menuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("Paste");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menu.add(menuItem);

        menuBar.add(menu);

        // Log Menu
        menu = new JMenu("Log");
        menu.setMnemonic(KeyEvent.VK_O);

        menuItem = new JMenuItem("Start New Log File",KeyEvent.VK_N);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // Allow for Write Log File  if (!processNewRequestOK()) return;
                StartNewLogFile();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Remove All Old Settings Log Files",KeyEvent.VK_R);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Remove All Old Settings Log Files", "Remove Settings Log Files", JOptionPane.OK_CANCEL_OPTION)) {
                            WriteToResultArea("Old Settings Files removed: " + RemoveAllFoIPSettingsLogFiles());
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // Select Menu
        menu = new JMenu("Select");
        menu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("Select All",KeyEvent.VK_A);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                SelectAll();
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Select None",KeyEvent.VK_N);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                SelectNone();
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // Select Menu
        menu = new JMenu("Security");
        menu.setMnemonic(KeyEvent.VK_C);

        menuItem = new JMenuItem("Log in to MFP",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        LogInToMFPVerify(settingMFP.getText(),settingMFPUser.getText(),settingMFPPassword.getText());
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Log in to Tester MFP",KeyEvent.VK_T);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        LogInToTesterMFPVerify(settingTesterMFP.getText(),settingTesterUser.getText(),settingTesterPassword.getText());
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // UCF Menu
        menu = new JMenu("UCF");
        menu.setMnemonic(KeyEvent.VK_U);

        menuItem = new JMenuItem("Create UCF File with Selected Settings",KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                WriteUCFFile();
                processingLastRequestDone();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Send UCF File to MFP",KeyEvent.VK_S);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        SendUCFToMFP();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get Settings from MFP with UCF",KeyEvent.VK_G);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetUCFSettingsFile();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // Bundles Menu
        menu = new JMenu("Bundles");
        menu.setMnemonic(KeyEvent.VK_B);

        menuItem = new JMenuItem("Create VCC Settings Bundle from Selected Settings",KeyEvent.VK_V);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        WriteXMLBundleFile(true,false);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Create VCC Settings and License Bundle from Selected Settings",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        WriteXMLBundleFile(true,true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Create VCC License only Bundle",KeyEvent.VK_O);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        WriteXMLBundleFile(false,true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Send Bundle to MFP",KeyEvent.VK_S);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        SendBundleToMFP();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // eSF Menu
        menu = new JMenu("eSF App");
        menu.setMnemonic(KeyEvent.VK_A);

        menuItem = new JMenuItem("Send eSF App File to MFP",KeyEvent.VK_E);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        SendEsfToMFP("");
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Send eSF App File to MFP via print path",KeyEvent.VK_S);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        SendEsfToMFPviaPrintPath();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Send eSF App License File to MFP",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        SendLicenseToMFP();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Get eSF App Info",KeyEvent.VK_I);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetEsfAppInfoFromMFP();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // Firmware Menu
        menu = new JMenu("Firmware");
        menu.setMnemonic(KeyEvent.VK_W);

        menuItem = new JMenuItem("Update MFP to Latest PE Release",KeyEvent.VK_P);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Update Firmware to Latest PE Release.", "Update Firmware", JOptionPane.OK_CANCEL_OPTION)) {
                            UpdateFirmware(LexmarkFirmwareUpdate.LATEST_PE, null);
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Update MFP to Recommended Release",KeyEvent.VK_R);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Update Firmware to Recommended Release.", "Update Firmware", JOptionPane.OK_CANCEL_OPTION)) {
                            UpdateFirmware(LexmarkFirmwareUpdate.RECOMMENDED, null);
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Force MFP to Latest PE Release",KeyEvent.VK_F);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Force Firmware to Latest PE Release.", "Update Firmware", JOptionPane.OK_CANCEL_OPTION)) {
                            UpdateFirmware(LexmarkFirmwareUpdate.FORCE_LATEST_PE, null);
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Force MFP to Recommended Release",KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Force Firmware to Recommended Release.", "Update Firmware", JOptionPane.OK_CANCEL_OPTION)) {
                            UpdateFirmware(LexmarkFirmwareUpdate.FORCE_RECOMMENDED, null);
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Update Firmware from Local File",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run()
                    {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileFilter(new FileNameExtensionFilter("Firmware File", "fls"));
                        if (settingLicense.getText().equals(""))
                            fileChooser.setCurrentDirectory(new File(ProjectDirectory));
                        else
                            fileChooser.setCurrentDirectory(new File(settingLicense.getText()));
                        int returnValue = fileChooser.showDialog(null, "Choose File");
                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Update Firmware from Local File.", "Update Firmware", JOptionPane.OK_CANCEL_OPTION)) {
                                UpdateFirmware(LexmarkFirmwareUpdate.LOCAL_FILE, fileChooser.getSelectedFile().getAbsolutePath());
                            }
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);


        // MFP Cache Menu
        menu = new JMenu("MFP Cache");
        menu.setMnemonic(KeyEvent.VK_M);

        menuItem = new JMenuItem("Print MFP Cache",KeyEvent.VK_P);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        PrintMFPCache();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Clear MFP Cache",KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Clear the MFP Cache", "Clear MFP Cache", JOptionPane.OK_CANCEL_OPTION)) {
                            ClearMFPCache();
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("IP Addr Range Sweep using http",KeyEvent.VK_H);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        Sweep(false);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("IP Addr Range Sweep using https",KeyEvent.VK_S);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        Sweep(true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // Info Menu
        menu = new JMenu("Info");
        menu.setMnemonic(KeyEvent.VK_I);

        menuItem = new JMenuItem("Get Info",KeyEvent.VK_I);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetInfo();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get SIP Registration Status",KeyEvent.VK_S);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                WriteToResultAreaNoCRLF("Getting SIP Registration Status...");
                Thread aThread = new Thread() {
                    public void run() {
                        String result = "";
                        MFP mfp = mfpStore.getInstance().Get(settingMFP.getText());
                        result = mfp.GetSIPRegStatus();
                        if (result.contains("OK"))
                            WriteToResultAreaNoTime("OK");
                        else
                            WriteToResultAreaNoTime("Failed");
                        ArrayList<String> Result = mfp.getLastRegistrationData();
                        for( String line : Result)
                            WriteToResultArea(line);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);


        menuItem = new JMenuItem("Get HTTPS Fax Status",KeyEvent.VK_E);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                WriteToResultAreaNoCRLF("Getting HTTPS Fax Status...");
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        String result = "";
                        MFP mfp = mfpStore.getInstance().Get(settingMFP.getText());
                        result = mfp.GetHTTPSFaxStatus();
                        if (result.contains("line"))  // Online or Offline
                            WriteToResultAreaNoTime("OK");
                        else
                            WriteToResultAreaNoTime("Failed");
                        ArrayList<String> Result = mfp.getLastHTTPSFaxData();
                        for( String line : Result)
                            WriteToResultArea(line);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get FoIP License Info",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetFoIPLicenseInfo();
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Get T.30 Log From MFP",KeyEvent.VK_3);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingMFP.getText(),"faxlog",true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get T.30 Log From Tester MFP",KeyEvent.VK_3);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingTesterMFP.getText(),"faxlog",true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get All T.30 Logs From MFP",KeyEvent.VK_3);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingMFP.getText(),"allfaxlogs",true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get All T.30 Logs From Tester MFP",KeyEvent.VK_3);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingTesterMFP.getText(),"allfaxlogs",true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get All T.30 Error Logs From MFP",KeyEvent.VK_3);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingMFP.getText(),"allfaxerrlogs",true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get All T.30 Error Logs From Tester MFP",KeyEvent.VK_3);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingTesterMFP.getText(),"allfaxerrlogs",true);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Get T.38 Trace Log From MFP",KeyEvent.VK_8);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingMFP.getText(),"t38log",false);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get T.38 Trace Log From Tester MFP",KeyEvent.VK_8);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingTesterMFP.getText(),"t38log",false);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Run T.38 Trace Log Parser",KeyEvent.VK_P);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileFilter(new FileFilter() {
                            public String getDescription() {
                                return "T.38 Trace Log Files";
                            }
                            public boolean accept (File f) {
                                if (f.isDirectory()) {
                                    return true;
                                } else {
                                    return f.getName().startsWith("t38trace");
                                }
                            }
                        });
                        fileChooser.setCurrentDirectory(new File(ProjectDirectory));
                        fileChooser.setSelectedFile(new File(LastT38TraceFile));
                        int returnValue = fileChooser.showDialog(null,"Choose File");
                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            JCheckBox T38  = new JCheckBox("T.38");
                            JCheckBox SIP  = new JCheckBox("SIP");
                            JCheckBox H323 = new JCheckBox("H.323");
                            JCheckBox AT   = new JCheckBox("AT");
                            JCheckBox DISP = new JCheckBox("Display");
                            String[] ParseTypes = {"summary", "detailed"};
                            T38.setSelected(true);
                            SIP.setSelected(true);
                            JComboBox ParseType = new JComboBox<String>(ParseTypes);
                            Object[] params = {"Select Options", " ", ParseType," ", T38,SIP,H323,AT, " ", DISP};
                            JOptionPane.showMessageDialog(basic,params,"T38Trace Log Parser",JOptionPane.PLAIN_MESSAGE);
                            WriteToResultArea("T38 " + T38.isSelected() + "  SIP " + SIP.isSelected() + "  H323 " + H323.isSelected() + "  AT " + AT.isSelected() + "  DISP " + DISP.isSelected());
                            RunT38TraceParser(fileChooser.getSelectedFile().getAbsolutePath(),
                                    T38.isSelected(),
                                    SIP.isSelected(),
                                    H323.isSelected(),
                                    AT.isSelected(),
                                    ParseType.getSelectedItem().toString(),
                                    DISP.isSelected());
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Get HTTPS Fax Log From MFP",KeyEvent.VK_E);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        GetLogFile(settingMFP.getText(),"HTTPSFaxLog",false);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Remove All T.30 Logs",KeyEvent.VK_R);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Remove All T.30 Logs", "Remove T.30 Logs", JOptionPane.OK_CANCEL_OPTION)) {
                            WriteToResultArea("T.30 Logs removed: " + RemoveAllT30Logs());
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Remove All T.38 Trace Logs",KeyEvent.VK_R);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Remove All T.38 Trace Logs", "Remove T.38 Trace Logs", JOptionPane.OK_CANCEL_OPTION)) {
                            WriteToResultArea("T.38 Trace Logs removed: " + RemoveAllT38TraceLogs());
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Remove All HTTPS Fax Logs",KeyEvent.VK_X);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Remove All HTTPS Fax Logs", "Remove HTTPS Fax Logs", JOptionPane.OK_CANCEL_OPTION)) {
                            WriteToResultArea("HTTPS Fax Logs removed: " + RemoveAllHTTPSFaxLogs());
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // Test Menu
        menu = new JMenu("Test");
        menu.setMnemonic(KeyEvent.VK_T);

        menuItem = new JMenuItem("Send Fax From MFP",KeyEvent.VK_F);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        MFP mfp = MFPStore.getInstance().Init(settingMFP.getText());
                        String Result = mfp.SendAndVerifyFax(UI,settingTesterPhone.getText());
                        /*
                        String Result = SendAndVerifyFax(settingMFP.getText(),settingTesterPhone.getText());
                        */
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Send Fax From Tester MFP",KeyEvent.VK_T);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        MFP mfp = MFPStore.getInstance().Init(settingTesterMFP.getText());
                        String Result = mfp.SendAndVerifyFax(UI,settingMFPPhone.getText());
                        /*
                        String Result = SendAndVerifyFax(settingTesterMFP.getText(),settingMFPPhone.getText());
                        */
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Get Last Fax Status From MFP",KeyEvent.VK_M);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        WriteToResultAreaNoTime("");
                        WriteToResultAreaNoCRLF("Getting Last Fax Status from " + settingMFP.getText() + "...");
                        MFP mfp = MFPStore.getInstance().Get(settingMFP.getText());
                        String FaxStatus = mfp.GetLastFaxStatus();
                        WriteToResultAreaNoTime("OK");
                        String[] tokens = FaxStatus.split("[|]+");
                        if (tokens[2].equals("RECV"))
                            WriteToResultArea("On " + tokens[0] + " at " + tokens[1] + " receive call from " + tokens[3] + " with status " + tokens[4]);
                        else if (tokens[1].equals("?"))
                            WriteToResultArea("Status unknown.");
                        else
                            WriteToResultArea("On " + tokens[0] + " at " + tokens[1] + " send call to " + tokens[3] + " with status " + tokens[4]);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get Last Fax Status From Tester MFP",KeyEvent.VK_G);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        WriteToResultAreaNoTime("");
                        WriteToResultAreaNoCRLF("Getting Last Fax Status from " + settingTesterMFP.getText() + "...");
                        MFP testermfp = MFPStore.getInstance().Get(settingTesterMFP.getText());
                        String FaxStatus = testermfp.GetLastFaxStatus();
                        WriteToResultAreaNoTime("OK");
                        String[] tokens = FaxStatus.split("[|]+");
                        if (tokens[2].equals("RECV"))
                            WriteToResultArea("On " + tokens[0] + " at " + tokens[1] + " receive call from " + tokens[3] + " with status " + tokens[4]);
                        else if (tokens[1].equals("?"))
                            WriteToResultArea("Status unknown.");
                        else
                            WriteToResultArea("On " + tokens[0] + " at " + tokens[1] + " send call to " + tokens[3] + " with status " + tokens[4]);
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

/*
        menuItem = new JMenuItem("Delete License from MFP",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent ae) {
                                            if (!processNewRequestOK()) return;
                                            Thread aThread = new Thread() {
                                              public void run() {
                                                DeleteLicenseOnMFP();
                                                processingLastRequestDone();
                                              }
                                            };
                                            aThread.start();
                                          }
                                        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Restore Fax Defaults on MFP",KeyEvent.VK_R);
        menuItem.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent ae) {
                                            if (!processNewRequestOK()) return;
                                            Thread aThread = new Thread() {
                                              public void run() {
                                                RestoreFaxDefaultsOnMFP();
                                                processingLastRequestDone();
                                              }
                                            };
                                            aThread.start();
                                          }
                                        });
        menu.add(menuItem);
*/

        menuBar.add(menu);


        // List Menu
        menu = new JMenu("List");
        menu.setMnemonic(KeyEvent.VK_L);


        menuItem = new JMenuItem("Deploy Bundles",KeyEvent.VK_D);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;

                Thread aThread = new Thread() {
                    public void run() {
                        if (settingLicense.getText().equals("")) {
                            if (OKtoContinueAfterDelay("Deploy Bundles without License")) {
                                ProcessMFPListDeploy();
                            }
                        }
                        else {
                            if (OKtoContinueAfterDelay("Deploy Bundles with License")) {
                                ProcessMFPListDeploy();
                            }
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Run Fax Tests",KeyEvent.VK_F);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (OKtoContinueAfterDelay("Fax Tests")) {
                            ProcessMFPListTest();
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get SIP Registration Status",KeyEvent.VK_S);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (OKtoContinueAfterDelay("SIP Registration Status")) {
                            ProcessMFPListGetSIPRegStatus();
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get FoIP License Info",KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (OKtoContinueAfterDelay("FoIP License Info")) {
                            ProcessMFPListGetFoIPLicense();
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Deploy Bundles And Check License and Registration",KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (OKtoContinueAfterDelay("Deploy And Check")) {
                            ProcessMFPListDeployAndCheck();
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Deploy Bundles And Run Fax Tests",KeyEvent.VK_A);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (OKtoContinueAfterDelay("Deploy And Test")) {
                            ProcessMFPListDeployAndTest();
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Get Settings for MFP from List File",KeyEvent.VK_G);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to get Settings from List File for MFP "+ settingMFP.getText(), "Get Settings", JOptionPane.OK_CANCEL_OPTION)) {
                            GetSettingsFromListFile(settingMFP.getText());
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

/*
        menuItem = new JMenuItem("Deploy All Bundles Then Run Fax Tests",KeyEvent.VK_T);
        menuItem.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent ae) {
                                            if (!processNewRequestOK()) return;
                                            Thread aThread = new Thread() {
                                              public void run() {
                                                if (OKtoContinueAfterDelay("Deploy Then Test")) {
                                                  ProcessMFPListDeployThenTest();
                                                }
                                                processingLastRequestDone();
                                              }
                                            };
                                            aThread.start();
                                          }
                                        });
        menu.add(menuItem);
*/

        menuItem = new JMenuItem("Copy MFP List Result File to MFP List File",KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processNewRequestOK()) return;
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(basic, "About to Copy "+
                                        settingListResultFileName.getText()+
                                        " to "+
                                        settingMFPListFileName.getText(),
                                "Copy",
                                JOptionPane.OK_CANCEL_OPTION)) {
                            copyFile(settingListResultFileName.getText(),settingMFPListFileName.getText());
                        }
                        processingLastRequestDone();
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

/*
        menuItem = new JMenuItem("Restore Fax Defaults and Delete License",KeyEvent.VK_R);
        menuItem.addActionListener(new ActionListener() {
                                          public void actionPerformed(ActionEvent ae) {
                                            if (!processNewRequestOK()) return;
                                            Thread aThread = new Thread() {
                                              public void run() {
                                                if (OKtoContinueAfterDelay("Restore Defaults and Delete License")) {
                                                  ProcessMFPListRestoreDefaultsDeleteLicense();
                                                }
                                                processingLastRequestDone();
                                              }
                                            };
                                            aThread.start();
                                          }
                                        });
        menu.add(menuItem);
*/

        menuBar.add(menu);

        menu.addSeparator();

        menuItem = new JMenuItem("Cancel Current List Operation",KeyEvent.VK_N);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!processingLastRequest()) {
                    JOptionPane.showMessageDialog(basic,"No List Operation to Cancel.","Cancel",JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                Thread aThread = new Thread() {
                    public void run() {
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(basic,
                                "About to Cancel current List operation. ",
                                "Cancel",
                                JOptionPane.YES_NO_OPTION)) {
                            CancelListOperation();
                        }
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);


        // Help Menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);

        menuItem = new JMenuItem("Help",KeyEvent.VK_H);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                Thread aThread = new Thread() {
                    public void run() {
                        try {
                            JEditorPane editorPane = new JEditorPane("text/html",helpMessage);
                            JScrollPane scrollPane = new JScrollPane(editorPane);
                            scrollPane.setPreferredSize(new Dimension(800, 700));
                            JFrame frame = new JFrame("Help");
                            frame.add(scrollPane);
                            frame.pack();
                            frame.setLocationRelativeTo(null);
                            frame.setVisible(true);
                        }
                        catch (Exception e) {
                            WriteToResultArea("" + e);
                        }
                        // JOptionPane.showMessageDialog(basic,scrollPane,"Help",JOptionPane.INFORMATION_MESSAGE);
                    }
                };
                aThread.start();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("About",KeyEvent.VK_A);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(basic,"FoIPSettingsManager version 0.96 (2019-04-03)\n\n" +
                        "For more information contact Pete Davidson at Pete@Lexmark.com","About",JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(menuItem);

        menuBar.add(menu);

        return menuBar;
    }

    private static FoIPSettingsManager instance = null;


    protected  FoIPSettingsManager() {

        CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL) );
        initUI();
    }

    public static FoIPSettingsManager getInstance() {
        if (instance == null) {
            instance = new FoIPSettingsManager();
        }
        return instance;
    }

    public boolean SettingsSelected() {
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            FoIPSetting setting = (FoIPSetting)iterator.next();
            if (setting == settingLicense)
                continue;
            if (setting.isSelected())
                return true;
        }
        return false;
    }

    public void SelectAll() {
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            FoIPSetting setting = (FoIPSetting)iterator.next();
            //if (!setting.getLabel().equals("Force Fax Mode Delay:"))
                setting.setSelected(true);
        }
        WriteToResultArea("All Settings Selected.");
    }

    public void SelectNone() {
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            FoIPSetting setting = (FoIPSetting)iterator.next();
            setting.setSelected(false);
        }
        WriteToResultArea("All Settings De-selected.");
    }

    public void SaveSettingsFile() {
        BufferedWriter writer = null;
        try {
            File settingsFile = new File(settingSettingsFileName.getText());
            writer = new BufferedWriter( new FileWriter(settingsFile));

            Iterator iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                FoIPSetting setting = (FoIPSetting)iterator.next();
                setting.writeSetting(writer);
            }

            WriteToResultArea("Settings Saved to " + settingSettingsFileName.getText() + ".");
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
        finally {
            try {
                writer.close();
            }
            catch (Exception e) {
            }
        }
    }

    public void LoadSettingsFile() {
        BufferedReader reader = null;
        String line;
        String delim = "[|]";
        try {
            File settingsFile = new File(settingSettingsFileName.getText());
            reader = new BufferedReader( new FileReader(settingsFile));

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(delim);
                String Label = tokens[0];

                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    FoIPSetting setting = (FoIPSetting)iterator.next();
                    if (setting.getLabel().equals(Label)) {
                        setting.readSetting(line);
                        break;
                    }
                }
            }

            WriteToResultArea("Settings Loaded from " + settingSettingsFileName.getText() + ".");
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
            }
        }
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public void SendEsfToMFPviaPrintPath() {
        DataOutputStream os = null;
        Socket socket = null;
        String EsfAppFileName;
        if (settingMFP.getText().isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return;
        }
        WriteToResultAreaNoCRLF("Getting MFP Family...");
        MFP mfp = mfpStore.Get(settingMFP.getText());
        if (mfp.PrinterFamilyString().equals("Pride"))
            EsfAppFileName = "FaxOverIP_e2_mfp-1.1.8.fls";
        else
            EsfAppFileName = "FaxOverIP_e3-4_mfp-1.3.0.fls";
        WriteToResultAreaNoTime("OK");

        WriteToResultAreaNoCRLF("Sending eSF App to MFP via Print Path...");
        try {
            //Create connection
            socket = new Socket(settingMFP.getText(),9100);
            DataOutputStream wr = new DataOutputStream(socket.getOutputStream());

            //Send request
            InputStream is = FoIPSettingsManager.class.getClassLoader().getResourceAsStream(EsfAppFileName);
            byte[]buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                wr.write(buffer, 0, bytesRead);
            }
            is.close();
            wr.flush ();
            wr.close ();

            WriteToResultAreaNoTime("OK");

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        } finally {

        }
    }

    public int SendEsfToMFP(String Type) {
        String charset = "UTF-8";
        //File uploadFile = new File(settingEsfApp.getText());
        InputStream uploadFile;
        String EsfAppFileName;
        String requestURL;
        if (Type.isEmpty()) {
            WriteToResultAreaNoCRLF("Checking MFP Family...");
            Type = mfpStore.Get(settingMFP.getText()).PrinterFamilyString();
            WriteToResultAreaNoTime("OK");
        }
        if (Type.equals("Pride")) {
            requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appmgrservlet";
            GetLicensePage(settingMFP.getText(),requestURL);
            requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appinstallokservlet";
            EsfAppFileName = "FaxOverIP_e2_mfp-1.1.8.fls";
        }
        else if (Type.equals("HS")) {
            requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appmgrservlet";
            GetLicensePage(settingMFP.getText(),requestURL);
            requestURL = "/cgi-bin/direct/printer/prtappauth_fwupdate/admin/appinstallokservlet";
            EsfAppFileName = "FaxOverIP_e3-4_mfp-1.3.0.fls";
        }
        else {
            WriteToResultArea("Device Family is not Pride or HS. Cannot use eSF App.");
            return -1;
        }

        if (settingMFP.getText().isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return -1;
        }
        WriteToResultAreaNoCRLF("Sending eSF App to MFP...");

        MFP mfp = MFPStore.getInstance().Get(settingMFP.getText());

        try {
            MultipartUtility multipart = new MultipartUtility(mfp, requestURL, charset, "", "");

            uploadFile = FoIPSettingsManager.class.getClassLoader().getResourceAsStream(EsfAppFileName);
            multipart.addFilePartFromStream("fileUpload", uploadFile, EsfAppFileName);

            java.util.List<String> response = multipart.finish();

            for (String line : response) {
                // WriteToResultArea(line);
            }
        } catch (IOException e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
            return -1;
        }
        WriteToResultAreaNoTime("OK");
        return 0;
    }


    public String GetLicensePage(String mfp, String page) {
        String line = "";
        String response = "";
        WebConnection connection = null;
        if (mfp.isEmpty()) {
            response = "Failed: Must specify MFP FQDN or IP Address.";
            return response;
        }
        try {
            //Create connection
            connection = new WebConnection(MFPStore.getInstance().Get(mfp),page);
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while((line = rd.readLine()) != null) {
                response += line;
            }

            rd.close();

        } catch (Exception e) {
            response = "Failed: " + e;
        }

        finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        return response;

    }

    public String GetEsfAppName(String mfp) {
        String name = "";
        String response = "";
        String requestURL;

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appmgrservlet";
        response = GetLicensePage(mfp,requestURL);

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applistservlet";
        response = GetLicensePage(mfp,requestURL);

        //WriteToResultArea(response);

        if (response.contains("LXKFaxOverIP"))
            name = "LXKFaxOverIP";
        else if (response.contains("FaxOverIP"))
            name = "FaxOverIP";
        else if (response.contains("Failed:"))
            name = response;

        return name;
    }

    public boolean GetEsfAppInfoFromMFP() {
        return GetEsfAppInfo(settingMFP.getText());
    }

    public boolean GetEsfAppInfo(String mfp) {
        String requestURL = "";
        String result = "";

        WriteToResultAreaNoCRLF("Getting MFP Family...");
        if (MojaOrLaterDevice(mfp)) {
            WriteToResultAreaNoTime("OK");
            WriteToResultArea("Get eSF App Info is not supported on Moja yet.");
            return false;
        }
        WriteToResultAreaNoTime("OK");

        WriteToResultAreaNoCRLF("Getting Fax over IP eSF App info...");
        String EsfAppName = GetEsfAppName(mfp);

        if (EsfAppName.contains("Failed:")) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea(EsfAppName);
            return false;
        }

        if (EsfAppName.isEmpty()) {
            WriteToResultAreaNoTime("OK");
            WriteToResultArea("       Not installed");
            return false;
        }

        //requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applicensestatusservlet?SelectedAppsNames=" + EsfAppName;
        //resp = GetLicensePage(mfp,requestURL);

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appservlet?SelectedAppsNames=" + EsfAppName;
        result = GetLicensePage(mfp,requestURL);

        if (result.contains("Failed:")) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea(result);
            return false;
        }

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applicenseservlet";
        result = GetLicensePage(mfp,requestURL);

        if (result.contains("Failed:")) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea(result);
            return false;
        }

        WriteToResultAreaNoTime("OK");

        String ps = "class=\"label les\"[^<]+</td>.*?<td.*?>([^<]+).*?";
        Pattern p = Pattern.compile("<title>([^<]+).*?"+ps+ps+ps+ps+ps+ps+ps);
        Matcher m = p.matcher(result);
        WriteToResultArea("\tName:\t " + EsfAppName);
        if (m.find()) {
            WriteToResultArea("\tTitle:\t " + m.group(1));
            WriteToResultArea("\tStatus:\t" + m.group(2));
            WriteToResultArea("\tSource:\t" + m.group(3));
            WriteToResultArea("\tFeature:\t" + m.group(4));
            WriteToResultArea("\tVersion:\t" + m.group(5));
            WriteToResultArea("\tHost ID:\t" + m.group(6));
            WriteToResultArea("\tServer:\t" + m.group(7));
            WriteToResultArea("\tExpiration:\t" + m.group(8));
        }

        WriteToResultAreaNoTime("");

        if (m.group(2).trim().equals("Demo"))
            return true;
        if (m.group(2).trim().equals("Licensed"))
            return true;
        if (m.group(2).trim().equals("Trial"))
            return true;

        return false;

    }

    public int UpdateFirmware(LexmarkFirmwareUpdate type, String LocalFile) {
        Firmware fw = new Firmware();
        MFP mfp = MFPStore.getInstance().Get(settingMFP.getText());
        try {
            fw.UpdateFirmware(mfp, type, LocalFile);
        } catch (Exception e) {
            WriteToResultArea("Firmware Update Failed: " + e);
            return -1;
        }
        return 0;
    }

    public int SendLicenseToMFP() {
        String charset = "UTF-8";
        String requestURL = "";
        String mfp = settingMFP.getText();
        String result = "";
        byte[] uploadBytes = new byte[2048];
        boolean useUploadBytes = false;
        String uploadFileName = "";

        if (mfp.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return -1;
        }

        WriteToResultAreaNoCRLF("Checking to make sure VCC is not supported...");
        if (VCCisSupported(mfp)) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("!!! VCC is supported. Do not use the eSF App. Use the VCC license. !!!");
            return -1;
        }
        WriteToResultAreaNoTime("OK");

        if (settingLicense.getText().isEmpty()) {
            WriteToResultArea("Must specify a FoIP License File or Diretory.");
            return -1;
        }

        String EsfAppName = GetEsfAppName(mfp);
        if (EsfAppName.contains("Failed:")) {
            WriteToResultArea(EsfAppName);
            return -1;
        }
        if (EsfAppName.isEmpty()) {
            WriteToResultArea("Install Fax over IP eSF App before deploying lincense.");
            return -1;
        }

        File uploadFile = new File(settingLicense.getText());

        File file = new File(settingLicense.getText());

        // If File is a Directory, then this is the directory of vcc license bundles.
        // The license.lic file is inside the bundle. The bundle file name starts with
        // the serial number of the machine the license was made for.
        if (file.isDirectory()) {
            WriteToResultAreaNoCRLF("Getting Serial Number of MFP...");
            final String SerialNumber = GetSerialNumber();
            if (SerialNumber.contains("Failed")) {
                WriteToResultAreaNoTime(SerialNumber);
                return -1;
            }
            else {
                WriteToResultAreaNoTime("OK  " + SerialNumber);
            }

            WriteToResultArea("Searching directory " + settingLicense.getText() + " for Serial Number " + SerialNumber + ".");
            // This searches the directory for a file that starts with the serial
            // number that we retrieved from the device and ends with ".lic"
            File[] matchingFiles = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(SerialNumber) && name.endsWith(".lic");
                }
            });
            // If length of matchingFiles is 0 then we could not find the license file
            if (matchingFiles.length == 0) {
                WriteToResultArea("Could not find license file for Serial Number " + SerialNumber);
                return -1;
            }

            uploadFile = matchingFiles[0];
            WriteToResultArea("Found " + uploadFile);

        }
        else if (settingLicense.getText().endsWith(".zip")) {
            useUploadBytes = true;
            WriteToResultAreaNoCRLF("Getting Serial Number of MFP...");
            final String SerialNumber = GetSerialNumber();
            if (SerialNumber.contains("Failed")) {
                WriteToResultAreaNoTime(SerialNumber);
                return -1;
            }
            else {
                WriteToResultAreaNoTime("OK  " + SerialNumber);
            }

            WriteToResultArea("Searching Package Builder zip file " + settingLicense.getText() + " for Serial Number " + SerialNumber + ".");

            try {
                ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
                ZipEntry entryIn = zipIn.getNextEntry();
                while (entryIn != null) {
                    if (entryIn.getName().startsWith("licenses/"+SerialNumber) && entryIn.getName().endsWith(".lic")) {
                        WriteToResultArea("Found " + entryIn.getName());
                        uploadFileName = entryIn.getName();
                        int bytesRead = 0;
                        while ((bytesRead = zipIn.read(uploadBytes)) != -1) {
                        }
                    }
                    entryIn = zipIn.getNextEntry();
                }
                zipIn.closeEntry();
            } catch (IOException e) {
                WriteToResultArea("" + e);
                return -1;
            }
            if (uploadFileName.isEmpty()) {
                WriteToResultArea("Could not find license file for Serial Number " + SerialNumber);
                return -1;
            }
        }
        else {
            WriteToResultArea("Using License file" + settingLicense.getText());
        }

        WriteToResultAreaNoCRLF("Sending License to MFP...");

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/appservlet?SelectedAppsNames=" + EsfAppName;
        GetLicensePage(mfp,requestURL);

        // referer = requestURL;
        // requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applicenseshellservlet";
        // cookie = GetLicensePage(requestURL,referer,cookie);

        // referer = requestURL;
        // requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applicenseupdateservlet";
        // cookie = GetLicensePage(requestURL,referer,cookie);

        requestURL = "/cgi-bin/direct/printer/prtappauth/admin/applicenseupdateokservlet";

        try {
            MultipartUtility multipart = new MultipartUtility(MFPStore.getInstance().Get(mfp),requestURL, charset, "", "");

            multipart.addSourcePart("source", "local");
            if (useUploadBytes)
                multipart.addFilePartAlternate2("file", uploadFileName, uploadBytes);
            else
                multipart.addFilePartAlternate("file", uploadFile);

            java.util.List<String> response = multipart.finish();

            for (String line : response) {
                if (line.toLowerCase().contains("error")) {
                    WriteToResultAreaNoTime("Failed");
                    return -1;
                }
            }
        } catch (IOException e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
            return -1;
        }

        WriteToResultAreaNoTime("OK");
        return 0;
    }


    public int SendUCFToMFP() {
        int rc = -1;
        String charset = "UTF-8";
        File uploadFile = new File(settingUCFFileName.getText());
        String requestURL = "/cgi-bin/dynamic/printer/config/secure/importsettings.html";

        if (settingMFP.getText().isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return rc;
        }

        WriteToResultAreaNoCRLF("Checking MFP Family...");
        if (MojaOrLaterDevice(settingMFP.getText())) {
            WriteToResultAreaNoTime("OK");
            WriteToResultArea("UCF on Moja is not supported yet.");
            return rc;
        }
        WriteToResultAreaNoTime("OK");
        WriteToResultAreaNoCRLF("Sending UCF to MFP...");

        try {
            MultipartUtility multipart = new MultipartUtility(MFPStore.getInstance().Get(settingMFP.getText()),requestURL, charset, "", "");

            multipart.addFilePart("fileUpload", uploadFile);

            java.util.List<String> response = multipart.finish();

            for (String line : response) {
            }
            WriteToResultAreaNoTime("OK");
            rc = 0;
        } catch (IOException e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
        }

        return rc;
    }

    public boolean GetFoIPLicenseInfo() {
        if (settingMFP.getText().isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return false;
        }
        WriteToResultAreaNoCRLF("Getting Info on License Type...");
        if (VCCisSupported(settingMFP.getText())) {
            WriteToResultAreaNoTime("OK");
            return GetVCCFoIPLicense();
        }
        else {
            WriteToResultAreaNoTime("OK");
            return GetEsfAppInfoFromMFP();
        }
    }

    public void GetVCCFoIPLicenseDetails(String mfp, String LicenseID) {
        String line = "";
        WebConnection connection = null;
        Pattern p;
        try {

            //Create connection
            connection = new WebConnection(MFPStore.getInstance().Get(mfp),"/cgi-bin/dynamic/printer/config/gen/vcc_license_details.html?id="+LicenseID);
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while((line = rd.readLine()) != null) {
                if (line.contains("Type")) {
                    p = Pattern.compile(".*?<td>(.+)</td>");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        WriteToResultArea("\tType:\t" + m.group(1));
                    }
                }
                if (line.contains("Start Date")) {
                    p = Pattern.compile(".*?<td>(.+)</td>");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        WriteToResultArea("\tStart Date:\t" + m.group(1));
                    }
                }
                if (line.contains("End Date")) {
                    p = Pattern.compile(".*?<td>(.+)</td>");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        WriteToResultArea("\tEnd Date:\t" + m.group(1));
                    }
                }
                if (line.contains("Issued")) {
                    p = Pattern.compile(".*?<td>(.+)</td>");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        WriteToResultArea("\tIssued:\t" + m.group(1));
                    }
                }
            }
            rd.close();

        } catch (Exception e) {

        }

        finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public Node GetMapFromMapById(Node map, String Id) {
        Node returnValue = null;
        Node currentNode, currentNode2;
        try {
            //WriteToResultArea("GetMapFromMapById for " + map.getNodeName());
            if (map.getNodeType() == Node.ELEMENT_NODE) {
                NodeList childNodes = map.getChildNodes();
                for (int i=0; i< childNodes.getLength(); i++) {
                    currentNode = childNodes.item(i);
                    if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                        if (currentNode.getAttributes().getNamedItem("name").getNodeValue().equals(Id)) {
                            NodeList childNodes2 = currentNode.getChildNodes();
                            for (int j=0; j< childNodes2.getLength(); j++) {
                                currentNode2 = childNodes2.item(j);
                                if (currentNode2.getNodeType() == Node.ELEMENT_NODE) {
                                    if (currentNode2.getNodeName().equals("MapElem")) {
                                        returnValue = currentNode.getChildNodes().item(j);
                                    }
                                    else {
                                        returnValue = currentNode.getChildNodes().item(j).getChildNodes().item(1);
                                    }
                                    return returnValue;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            //WriteToResultArea("" + e);
        }
        return returnValue;
    }

    public String GetValueFromMapById(Node map, String Id, String elemType) {
        String returnValue = "";
        Node currentNode2;
        NodeList childNodes = map.getChildNodes();
        try {
            //WriteToResultArea("GetValueFromMapById for " + map.getNodeName());
            if (map.getNodeType() == Node.ELEMENT_NODE) {
                NodeList childNodes2 = map.getChildNodes();
                for (int j=0; j< childNodes2.getLength(); j++) {
                    currentNode2 = childNodes2.item(j);
                    if (currentNode2.getNodeType() == Node.ELEMENT_NODE) {
                        Element elem = (Element) currentNode2;
                        if (currentNode2.getNodeType() == Node.ELEMENT_NODE) {
                            if (currentNode2.getAttributes().getNamedItem("name").getNodeValue().equals(Id)) {
                                returnValue = elem.getElementsByTagName(elemType).item(0).getChildNodes().item(0).getNodeValue();
                                return returnValue;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            //WriteToResultArea("" + e);
        }
        return returnValue;
    }

    public void WriteResult(String label, String value) {
        if (value != null && !value.isEmpty()) {
            WriteToResultArea("\t" + label + "\t" + value);
        }
    }

    public boolean GetVCCFoIPLicense() {
        String line = "";
        WebConnection connection = null;
        boolean FoundLicense = false;
        boolean ValidLicense = false;
        Pattern p;
        String LicenseID="";
        if (settingMFP.getText().isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return ValidLicense;
        }
        WriteToResultAreaNoCRLF("Getting VCC FoIP Licenses from " + settingMFP.getText() + "...");
        try {
            //Create connection
            connection = new WebConnection(MFPStore.getInstance().Get(settingMFP.getText()),"/webservices/vcc/licenses");
            connection.BasicGetSetup();

            //Get Response
            InputStream is = connection.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            Node root = doc.getDocumentElement();
            NodeList childNodes = root.getChildNodes();
            Node currentNode, currentNode2, currentNode3;
            for (int i=0; i< childNodes.getLength(); i++) {
                currentNode = childNodes.item(i);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    Node LicenseNode = GetMapFromMapById(currentNode,"license");
                    if (LicenseNode == null)
                        continue;
                    Node FeaturesNode = GetMapFromMapById(LicenseNode,"features");
                    if (FeaturesNode == null)
                        continue;
                    String LicenseName = GetValueFromMapById(FeaturesNode,"name","StringElem");
                    if (LicenseName.toLowerCase().contains("foip")      ||
                            LicenseName.toLowerCase().contains("faxoverip") ||
                            LicenseName.toLowerCase().contains("fax over ip"))  {
                        if (FoundLicense == false) {
                            WriteToResultAreaNoTime("OK");
                            FoundLicense = true;
                        }
                        if (GetValueFromMapById(currentNode,"isValid","BooleanElem").equals("true")) {
                            ValidLicense = true;
                        }
                        WriteResult("License ID:  ",GetValueFromMapById(currentNode,"id","StringElem"));
                        WriteResult("Name:        ",GetValueFromMapById(FeaturesNode,"name","StringElem"));
                        WriteResult("Legal Notice:",GetValueFromMapById(LicenseNode,"notice","StringElem"));
                        WriteResult("Valid:       ",GetValueFromMapById(currentNode,"isValid","BooleanElem"));
                        WriteResult("Reason:      ",GetValueFromMapById(currentNode,"reasonInvalid","StringElem"));
                        WriteResult("Type:        ",GetValueFromMapById(LicenseNode,"type","StringElem"));
                        WriteResult("Start Date:  ",GetValueFromMapById(LicenseNode,"startDate","StringElem"));
                        WriteResult("End Date:    ",GetValueFromMapById(LicenseNode,"endDate","StringElem"));
                        WriteResult("Issued:      ",GetValueFromMapById(LicenseNode,"issued","StringElem"));
                        WriteResult("Term Type:   ",GetValueFromMapById(LicenseNode,"termType","StringElem"));
                        WriteResult("ID:          ",GetValueFromMapById(FeaturesNode,"id","IntegerElem"));
                        WriteResult("Version:     ",GetValueFromMapById(FeaturesNode,"version","IntegerElem"));
                        //WriteResult("Days:",GetValueFromMapById(LicenseNode,"days","IntegerElem"));
                        //WriteResult("License ID:",GetValueFromMapById(LicenseNode,"id","StringElem"));
                        //WriteResult("Count:",GetValueFromMapById(FeaturesNode,"count","StringElem"));
                        WriteToResultArea("");
                    }
                }
            }

            if (FoundLicense == false) {
                WriteToResultAreaNoTime("Failed");
                WriteToResultArea("No VCC FoIP Licenses");
            }

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        }

        finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        if (FoundLicense==true && ValidLicense==false)
            WriteToResultArea("No Valid VCC FoIP Licenses.");

        return ValidLicense;

    }


    public void GetUCFSettingsFile() {
        String line = "";
        WebConnection connection = null;
        if (settingMFP.getText().isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return;
        }
        WriteToResultAreaNoCRLF("Getting MFP Family...");
        if (MojaOrLaterDevice(settingMFP.getText())) {
            WriteToResultAreaNoTime("OK");
            WriteToResultArea("Get Settings via UCF is not supported on Moja yet.");
            return;
        }
        WriteToResultAreaNoTime("OK");
        WriteToResultAreaNoCRLF("Getting Settings from " + settingMFP.getText() + " with UCF...");
        try {
            //Create connection
            connection = new WebConnection(MFPStore.getInstance().Get(settingMFP.getText()),"/cgi-bin/exportfile/printer/config/secure/settingfile.ucf");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setReadTimeout(WebConnection.READ_TIMEOUT + 20);   // Extra time for the large amount of data

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            boolean FirstTime = true;
            while((line = rd.readLine()) != null) {
                if (FirstTime) {
                    WriteToResultAreaNoTime("OK");
                    FirstTime = false;
                }
                if (!(line.equals("") || line.substring(0,2).equals("//")))
                    if (LoadSetting(line)) {
                        WriteToResultArea("   " + line);
                    }
            }
            rd.close();
            WriteToResultArea("");


        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed\r\n" + e);

        }

        finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

    }

    public String WebNameToSettingName(String webName) {
        if (webName.equals("0.system.16480"))
            return "\"Automatic Redial\"";
        if (webName.equals("0.system.17724"))
            return "\"Force Fax Mode Delay\"";
        return webName;
    }

    public String WebNameToSettingMojaName(String webName) {
        if (webName.equals("0.system.16480"))
            return "FaxSendAutomaticRedial";
        if (webName.equals("0.system.17724"))
            return "ForceFaxModeDelay";
        return webName;
    }

    public String WebNameToSettingCategory(String webName) {
        if (webName.equals("0.system.16480"))
            return "FaxSendAdminControlsPopup";
        if (webName.equals("0.system.17724"))
            return "VoIPSettings";
        return webName;
    }


    public int RunT38TraceParser(String T38TraceFile, boolean T38, boolean SIP, boolean H323, boolean AT, String ParseType, boolean DISP) {
        String ParsedFile = T38TraceFile.replace("t38log","t38log-parsed");
        String line;
        WriteToResultArea(T38TraceFile);
        String PythonParserFile = ProjectDirectory
                + File.separator
                + "p.py";
        WriteToResultAreaNoCRLF("Parsing " + T38TraceFile + "...");
        try {
            InputStream pythonT38Parser = FoIPSettingsManager.class.getResourceAsStream("/foip_t38_trace_parser.py");
            BufferedReader pythonin = new BufferedReader(new InputStreamReader(pythonT38Parser));
            PrintWriter pwriter = new PrintWriter(PythonParserFile,"ISO-8859-1");
            while((line = pythonin.readLine()) != null) {
                pwriter.println(line);
            }
            pythonin.close();
            pwriter.close();

            String POpt1;
            String POpt2;
            String POpt3;
            String POpt4;
            if (T38)  POpt1 = "--parse-t38=yes";  else POpt1 = "--parse-t38=no";
            if (SIP)  POpt2 = "--parse-sip=yes";  else POpt2 = "--parse-sip=no";
            if (H323) POpt3 = "--parse-h323=yes"; else POpt3 = "--parse-h323=no";
            if (AT)   POpt4 = "--parse-at=yes";   else POpt4 = "--parse-at=no";

            ProcessBuilder pb = new ProcessBuilder("python",PythonParserFile,POpt1,POpt2,POpt3,POpt4,ParseType,T38TraceFile);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if (DISP)
                WriteToResultAreaNoTime("OK");
            pwriter = new PrintWriter(ParsedFile,"UTF-8");
            while((line = in.readLine()) != null) {
                if (DISP) {
                    WriteToResultArea(line);
                }
                pwriter.println(line);
            }
            if (DISP) {
                WriteToResultArea("");
                WriteToResultArea("Parsing of " + T38TraceFile + " complete.");
            }
            else {
                WriteToResultAreaNoTime("OK");
            }
            pwriter.close();
            in.close();

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        } finally {
        }
        return 0;
    }

    public int RemoveAllT30Logs() {
        int FilesRemoved = 0;
        File directory = new File(ProjectDirectory);
        for (File f: directory.listFiles())
            if (f.getName().startsWith("faxlog") ||
                    f.getName().startsWith("allfaxlogs") ||
                    f.getName().startsWith("allfaxerrlogs")) {
                f.delete();
                FilesRemoved++;
            }

        return FilesRemoved;
    }

    public int RemoveAllT38TraceLogs() {
        int FilesRemoved = 0;
        File directory = new File(ProjectDirectory);
        for (File f: directory.listFiles())
            if (f.getName().startsWith("t38")) {
                f.delete();
                FilesRemoved++;
            }

        return FilesRemoved;
    }

    public int RemoveAllHTTPSFaxLogs() {
        int FilesRemoved = 0;
        File directory = new File(ProjectDirectory);
        for (File f: directory.listFiles())
            if (f.getName().startsWith("HTTPS")) {
                f.delete();
                FilesRemoved++;
            }

        return FilesRemoved;
    }

    public int RemoveAllFoIPSettingsLogFiles() {
        return UI.RemoveAllOldLogFiles();
    }

    public int GetLogFile(String IPAddr, String Log, boolean Display) {
        int rc = -1;
        String line;
        boolean FirstRead = true;
        boolean OKtoWrite = false;
        WebConnection connection = null;
        if (IPAddr.isEmpty()) {
            WriteToResultArea("Must specify FQDN or IP Address.");
            return rc;
        }
        LastT38TraceFile = Log
                + "-"
                + IPAddr
                + "-"
                + new SimpleDateFormat("yyyy.MM.dd-HHmmss").format(new Date())
                + ".txt";
        String T38TraceFile = ProjectDirectory
                + File.separator
                + LastT38TraceFile;
        String PythonParserFile = ProjectDirectory
                + File.separator
                + "p.py";
        WriteToResultAreaNoCRLF("Writing " + Log + " to " + T38TraceFile + "...");
        try {
            //Create connection
            if (MojaOrLaterDevice(IPAddr)) {
                if (Log.equals("HTTPSFaxLog"))
                    Log = "https_fax_log";
                connection = new WebConnection(MFPStore.getInstance().Get(IPAddr),"/cgi-bin/" + Log);
                connection.BasicGetSetup();
            }
            else {
                if (Log.equals("HTTPSFaxLog"))
                    Log = "etherfaxlog";
                connection = new WebConnection(MFPStore.getInstance().Get(IPAddr),"/cgi-bin/script/printer/" + Log);
                connection.BasicGetSetup();
            }

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            PrintWriter pwriter = new PrintWriter(T38TraceFile,"ISO-8859-1");
            int Lines = 0;
            while((line = rd.readLine()) != null) {
                if (FirstRead) {
                    FirstRead = false;
                    if (Display)
                        WriteToResultAreaNoTime("OK");
                }
                if (OKtoWrite) {
                    line = line.replaceAll("&gt;",">").replaceAll("&lt;","<");
                    Lines++;
                    pwriter.println(line);
                    if (Display)
                        WriteToResultArea(line);
                }
                else if (line.contains("=======")) {
                    OKtoWrite = true;
                    rc = 0;
                }
            }
            rd.close();
            pwriter.close();

            if (Display)
                WriteToResultArea("");
            else
                WriteToResultAreaNoTime("OK");

            WriteToResultArea(Lines + " lines");

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;
    }

    public boolean processingLastRequest() {
        if (processingLastReq) {
            return true;
        }
        return false;
    }

    public boolean processNewRequestOK() {
        if (processingLastRequest()) {
            JOptionPane.showMessageDialog(new JFrame(), "Busy processing last request.");
            return false;
        }
        processingLastReq = true;
        return true;
    }

    public boolean OKtoContinueAfterDelay(String Title) {
        Object[] options = {"Now","Later","Cancel"};
        int n = JOptionPane.showOptionDialog(new JFrame(),
                "Process List now or later?",
                Title,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
        // Now
        if (n==0) {
            return true;
        }

        // Later
        else if (n==1) {
            JSpinner timeSpinner = new JSpinner( new SpinnerDateModel() );
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "yyyy-MM-dd HH:mm");
            timeSpinner.setEditor(timeEditor);
            timeSpinner.setValue(new Date());
            n = JOptionPane.showOptionDialog(null,
                    timeSpinner,
                    "Select Start Time",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null);
            if (n==0) {
                // Get Time
                Date startDate = (Date)timeSpinner.getValue();
                Date now = new Date();
                long millis = startDate.getTime() - now.getTime();
                n = JOptionPane.showOptionDialog(null,
                        "List Processing to start on "+startDate.toString(),
                        Title,
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null);
                if (n==0) {
                    JFrame frame = new JFrame(Title);
                    frame.setLayout(new FlowLayout());
                    JLabel label = new JLabel("Time until List Processing Start");
                    JLabel timeUntilGo = new JLabel("           --:--:--           ");
                    JLabel startTime = new JLabel(startDate.toString());
                    frame.add(label);
                    frame.add(timeUntilGo);
                    frame.add(startTime);
                    frame.setPreferredSize(new Dimension(300,100));
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setVisible(true);
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                    df.setTimeZone(tz);
                    while (millis > 0) {
                        String timeUntilGoString;
                        String oldTimeUntilGoString = " ";
                        sleepMSeconds(100);
                        now = new Date();
                        if (millis > 48*60*60*1000) {
                            timeUntilGoString = "           " + millis/24/60/60/1000 + " Days " + df.format(new Date(millis)) + "           ";
                        }
                        else if (millis > 24*60*60*1000) {
                            timeUntilGoString = "           1 Day " + df.format(new Date(millis)) + "           ";
                        }
                        else {
                            timeUntilGoString = "              " + df.format(new Date(millis)) + "              ";
                        }
                        timeUntilGo.setText(timeUntilGoString);
                        millis = startDate.getTime() - now.getTime();
                        if (!frame.isDisplayable())
                            return false;
                    }
                    frame.setVisible(false);
                    return true;
                }
                else {
                    return false;
                }
            }
            else {  // Cancel
                return false;
            }
        }

        // Cancel
        else if (n==2) {
        }

        return false;
    }

    public void processingLastRequestDone() {
        processingLastReq = false;
    }

    public void sleepSeconds(int Seconds) {
        try {
            Thread.sleep(1000 * Seconds);
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
    }

    public void sleepMSeconds(int MSeconds) {
        try {
            Thread.sleep(MSeconds);
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
    }

    public void sleepOneSecondAndDisplayDot() {
        try {
            Thread.sleep(1000);
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
        WriteToResultAreaNoTimeNoCRLF(".");
    }

    public void delayWithMessage(int secs, String Msg) {
        int i;
        WriteToResultAreaNoCRLF(Msg+"...");
        for (i=0;i<secs;i++) {
            sleepOneSecondAndDisplayDot();
        }
        WriteToResultAreaNoTime("OK");
    }

    public void MFPListHeading(String msg) {
        WriteToResultAreaNoTime("");
        WriteToResultArea("\t>>>>>>>>>>>>>>>>>>>>  Processing MFP List  <<<<<<<<<<<<<<<<<<<<");
        WriteToResultAreaNoTime("");
        WriteToResultArea(msg);
        pbar.setLabel(msg + "..");
    }

    public void ProcessMFPListDeploy() {
        if (settingLicense.getText().equals("")) {
            MFPListHeading("Deploying Settings without License.");
        }
        else {
            MFPListHeading("Deploying Settings with License.");
        }
        ProcessMFPList(true,false,false,false,false,false);
    }
    public void ProcessMFPListTest() {

        if (settingTesterMFP.getText().equals("")) {
            WriteToResultArea("Must specify Tester MFP FQDN or IP Address.");
            return;
        }
        if (settingTesterPhone.getText().equals("")) {
            WriteToResultArea("Must specify Phone Number.");
            return;
        }
        MFPListHeading("Running Send and Receive Fax Tests.");
        ProcessMFPList(false,true,false,false,false,false);
    }
    public void ProcessMFPListDeployAndTest() {
        if (settingTesterMFP.getText().equals("")) {
            WriteToResultArea("Must specify Tester MFP FQDN or IP Address.");
            return;
        }
        if (settingTesterPhone.getText().equals("")) {
            WriteToResultArea("Must specify Phone Number.");
            return;
        }
        if (settingLicense.getText().equals("")) {
            MFPListHeading("Deploying Settings without License and Running Send and Receive Fax Tests.");
        }
        else {
            MFPListHeading("Deploying Settings with License and Running Send and Receive Fax Tests.");
        }
        ProcessMFPList(true,true,true,true,false,false);
    }
    public void ProcessMFPListDeployAndCheck() {
        if (settingLicense.getText().equals("")) {
            MFPListHeading("Deploying Settings without License and Checking License and Registration.");
        }
        else {
            MFPListHeading("Deploying Settings with License and Checking License and Registration.");
        }
        ProcessMFPList(true,false,true,true,false,false);
    }
    public void ProcessMFPListGetSIPRegStatus() {
        MFPListHeading("Getting SIP Registration Status from each MFP.");
        ProcessMFPList(false,false,true,false,false,false);
    }
    public void ProcessMFPListGetFoIPLicense() {
        MFPListHeading("Getting License Info from each MFP.");
        ProcessMFPList(false,false,false,true,false,false);
    }
    public void ProcessMFPListRestoreDefaultsDeleteLicense() {
        MFPListHeading("Restoring Fax Defaults and Deleting License on each MFP.");
        ProcessMFPList(false,false,false,false,true,true);
    }

    public void ProcessMFPList(boolean deployMFP, boolean testMFP, boolean getSIPRegStatus, boolean getFoIPLicense, boolean deleteLicense, boolean restoreFaxDefaults) {
        int rc=0;
        int mfpOK=0;
        int mfpFail=0;
        int bytesRead= 0;
        int firstLine= 0;
        boolean useLicense;
        boolean done=false;
        boolean skipBecauseResultOK = false;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String line;
        String PhoneNumber = "";
        String delim = "[,]";
        Integer i;
        String[] varNames = {""};

        CancelList = false;

        if (settingMFPListFileName.getText().equals(settingListResultFileName.getText())) {
            WriteToResultArea("MFP List File and MFP List Result File must be different.");
            return;
        }

        try {
            File listFile = new File(settingMFPListFileName.getText());
            reader = new BufferedReader( new FileReader(listFile));

            File listResultFile = new File(settingListResultFileName.getText());
            writer =  new BufferedWriter( new FileWriter(listResultFile));

            if ((line = reader.readLine()) != null) {
                firstLine = (int)line.length()+2; // +2 for CRLF
                varNames = line.split(delim);
                if (varNames.length < 1)
                    throw new IOException("Invalid MFP List File");
                // for (i=0;i<varNames.length;i++)
                //   WriteToResultArea("" + i + " " + varNames[i]);
            }
            writer.write(line+",Result\n");

            if (settingLicense.getText().equals("")) {
                useLicense = false;
            }
            else {
                useLicense = true;
            }

            pbar.setMax((int)listFile.length()-firstLine);
            pbar.updateBar(0);
            pbar.setOK(0);
            pbar.setFail(0);
            pbar.setVisible(true);

            MFP testermfp = null;
            MFP mfp = null;

            if (testMFP) {
                LogInToMFP(settingTesterMFP.getText(),settingTesterUser.getText(),settingTesterPassword.getText());
                testermfp = MFPStore.getInstance().Get(settingTesterMFP.getText());
            }

            while (!done && (line = reader.readLine()) != null) {

                if (CancelList) {
                    WriteToResultAreaNoTime("");
                    WriteToResultArea("List Operation Canceled.");
                    CancelList = false;
                    done = true;
                    continue;
                }

                String[] tokens = line.split(delim);
                bytesRead += (int)line.length() + 2;  // +2 for CRLF

                // "" means skip this line
                if (tokens.length == 0) {
                    writer.write(line+",\n");
                    pbar.updateBar(bytesRead);
                    continue;
                }

                WriteToResultAreaNoTime("");
                WriteToResultArea("Processing " + tokens[0]);
                // "End" means time to quit
                if (tokens[0].equals("End")) {
                    done = true;
                    writer.write(line+",\n");
                    pbar.updateBar(bytesRead);
                    continue;
                }
                // "#" means skip this line
                if (tokens[0].charAt(0) == '#') {
                    writer.write(line+",\n");
                    pbar.updateBar(bytesRead);
                    continue;
                }
                rc = 0;
                settingMFPPhone.setText("");
                skipBecauseResultOK = false;
                for (i=0; i<tokens.length; i++) {
                    //WriteToResultArea("" + varNames[i] + " = " + tokens[i]);
                    if (varNames[i].equals("IP Address or FQDN")) {
                        settingMFP.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("Phone Number")) {
                        settingMFPPhone.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("License File")) {
                        settingLicense.setText(tokens[i]);
                        useLicense = true;
                        if (deployMFP) {
                            WriteToResultArea("Deploying Settings with License from MFP List File.");
                            pbar.setLabel("Deploying Settings with License from MFP List File...");
                        }
                    }
                    else if (varNames[i].equals("MFP User")) {
                        settingMFPUser.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("MFP Password")) {
                        settingMFPPassword.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("Result")) {
                        if (tokens[i].equals("OK")) {
                            skipBecauseResultOK = true;
                            i=tokens.length;
                        }
                    }
                    else {
                        rc = LoadSettingVCC("" + varNames[i] + " \"" + tokens[i] + "\"");
                        if (rc != 0) {
                            WriteToResultArea("Invalid VCC Setting Name: " + varNames[i]);
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            i = tokens.length;
                            writer.write(line+",Bad VCC Setting Name: "+varNames[i]+"\n");
                        }
                    }
                }
                if (rc != 0) {
                    WriteToResultArea("Update of " + settingMFP.getText() + " failed.");
                    pbar.updateBar(bytesRead);
                    continue;
                }
                if (skipBecauseResultOK) {
                    writer.write(line+",\n");
                    pbar.updateBar(bytesRead);
                    continue;
                }

                mfp = MFPStore.getInstance().Init(settingMFP.getText());

                rc = LogInToMFP(settingMFP.getText(),settingMFPUser.getText(),settingMFPPassword.getText());
                if (rc != 0) {
                    WriteToResultArea("Could not log in to " + settingMFP.getText());
                    mfpFail++;
                    pbar.setFail(mfpFail);
                    writer.write(line+",Could not log in to MFP\n");
                    pbar.updateBar(bytesRead);
                    continue;
                }

                rc = GetInfo();

                if (rc != 0) {
                    WriteToResultArea("Could not get info from " + settingMFP.getText());
                    mfpFail++;
                    pbar.setFail(mfpFail);
                    writer.write(line+",Could not get info from MFP\n");
                    pbar.updateBar(bytesRead);
                    continue;
                }

                if (deleteLicense && restoreFaxDefaults) {
                    int deleteResult = DeleteLicenseOnMFP();
                    int restoreResult = RestoreFaxDefaultsOnMFP();
                    if (deleteResult == 0 && restoreResult == 0) {
                        mfpOK++;
                        pbar.setOK(mfpOK);
                        writer.write(line+",OK \n");
                    }
                    else {
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        if (deleteResult == 0)
                            writer.write(line+",Restore Fax Defaults Failed\n");
                        else
                            writer.write(line+",Delete License Failed\n");
                    }
                    pbar.updateBar(bytesRead);
                    continue;
                }

                if (deleteLicense) {
                    int deleteResult = DeleteLicenseOnMFP();
                    if (deleteResult == 0) {
                        mfpOK++;
                        pbar.setOK(mfpOK);
                        writer.write(line+",OK \n");
                    }
                    else {
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(line+",Delete License Failed\n");
                    }
                    pbar.updateBar(bytesRead);
                    continue;
                }

                if (restoreFaxDefaults) {
                    int restoreResult = RestoreFaxDefaultsOnMFP();
                    if (restoreResult == 0) {
                        mfpOK++;
                        pbar.setOK(mfpOK);
                        writer.write(line+",OK \n");
                    }
                    else {
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(line+",Restore Fax Defaults Failed\n");
                    }
                    pbar.updateBar(bytesRead);
                    continue;
                }

                if (deployMFP) {


                    if (!VCCisSupported(settingMFP.getText())) {
                        WriteToResultArea("MFP " + settingMFP.getText() + " does not support VCC, using UCF.");
                        if (settingLicense.getText().isEmpty()) {
                            WriteToResultArea("FoIP License not specified, skipping eSF App and License deployment.");
                        }
                        else {
                            rc = SendEsfToMFP("");
                            if (rc != 0) {
                                WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Deploy eSF App.");
                                mfpFail++;
                                pbar.setFail(mfpFail);
                                writer.write(line+",Failed to Deploy eSF App\n");
                                pbar.updateBar(bytesRead);
                                continue;
                            }
                            // Wait 15 seconds for eSF App install to complete.
                            delayWithMessage(15,"Waiting 15 seconds for eSF App install to complete.");
                            rc = SendLicenseToMFP();
                            if (rc != 0) {
                                WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Deploy eSF License.");
                                mfpFail++;
                                pbar.setFail(mfpFail);
                                writer.write(line+",Failed to Deploy eSF License\n");
                                pbar.updateBar(bytesRead);
                                continue;
                            }
                        }
                        rc = WriteUCFFile();
                        if (rc != 0) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Create UCF File.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            writer.write(line+",Failed to Create UCF File\n");
                            pbar.updateBar(bytesRead);
                            continue;
                        }
                        rc = SendUCFToMFP();
                        if (rc != 0) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed Sending Bundle to MFP.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            writer.write(line+",Failed Sending UCF File to MFP\n");
                            pbar.updateBar(bytesRead);
                            continue;
                        }
                    }

                    else {

                        rc = WriteXMLBundleFile(true,useLicense);
                        if (rc != 0) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Create Bundle File.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            writer.write(line+",Failed to Create Bundle File\n");
                            pbar.updateBar(bytesRead);
                            continue;
                        }

                        rc = SendBundleToMFP();
                        if (rc != 0) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed Sending Bundle to MFP.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            writer.write(line+",Failed Sending Bundle to MFP\n");
                            pbar.updateBar(bytesRead);
                            continue;
                        }
                    }
                    if (getFoIPLicense || getSIPRegStatus || testMFP) {
                        // Wait 15 seconds since we just deployed settings
                        delayWithMessage(15,"Waiting 15 seconds for settings to take effect.");
                    }
                }

                if (getFoIPLicense) {
                    boolean licResult = GetFoIPLicenseInfo();
                    if (!licResult) {
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(line+",Failed to get FoIP License Info\n");
                        pbar.updateBar(bytesRead);
                        continue;
                    }
                }

                if (getSIPRegStatus) {
                    String sipResult = mfp.GetSIPRegStatus();
                    UI.writeln("SIP Registration Result: " + sipResult);
                    if (sipResult.equals("200 OK")) {
                        sipResult = "OK";
                    }
                    else {
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(line+"," + sipResult + "\n");
                        pbar.updateBar(bytesRead);
                        continue;
                    }
                }

                if (testMFP) {

                    String[] resultTokens = {""};
                    String result = "";

                    // Receive a fax
                    if (settingMFPPhone.getText().equals("")) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to receive a fax. Phone Number not specified.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(line+",Failed to receive a fax. No Phone Number.\n");
                        pbar.updateBar(bytesRead);
                        continue;
                    }

                    // Skip on asterisk
                    if (!settingMFPPhone.getText().equals("*")) {

                        result = testermfp.SendAndVerifyFax(UI, settingMFPPhone.getText());
                        resultTokens = result.split("[|]");
                        if (!resultTokens[4].contains("OK")) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to receive a fax.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            writer.write(line + ",Failed to receive a fax.\n");
                            pbar.updateBar(bytesRead);
                            continue;
                        }
                    }

                    // Skip on asterisk
                    if (!settingTesterPhone.getText().equals("*")) {

                        // Send a fax

                        // Wait 10 seconds
                        UI.DelayWithCountDown("Waiting 10 seconds to avoid Busy", 10);

                        result = mfp.SendAndVerifyFax(UI, settingTesterPhone.getText());
                        resultTokens = result.split("[|]");
                        if (!resultTokens[4].contains("OK")) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to send a fax.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            writer.write(line + ",Failed to send a fax.\n");
                            pbar.updateBar(bytesRead);
                            continue;
                        }
                        WriteToResultAreaNoTime("");
                    }
                }

                WriteToResultArea("Processing of " + settingMFP.getText() + " was successful.");
                mfpOK++;
                pbar.setOK(mfpOK);
                writer.write(line+",OK\n");
                pbar.updateBar(bytesRead);

            }

            WriteToResultAreaNoTime("");
            WriteToResultArea(">>>>>>>>>>>>>>>>>>>>  MFP List Processed.  OK = " + mfpOK + "  Fail = " + mfpFail);
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
        finally {
            try {
                reader.close();
                writer.close();
                if (mfpFail > 0)
                    JOptionPane.showMessageDialog(new JFrame(), "MFP List Processing Complete.\n\nMFP failures encountered!\n\n");
                else
                    JOptionPane.showMessageDialog(new JFrame(), "MFP List Processing Complete.\n\nAll MFPs OK.\n\n");
            }
            catch (Exception e) {
            }
            pbar.setVisible(false);
        }

    }

    public void CancelListOperation() {
        CancelList = true;
        JOptionPane.showMessageDialog(basic,"List Operation will be terminated after current MFP completes.","List Operation Cancel",JOptionPane.INFORMATION_MESSAGE);
        pbar.setLabel("Canceling List Operation...");
    }


    public void ProcessMFPListDeployThenTest() {
        int rc=0;
        int mfpOK=0;
        int mfpFail=0;
        int bytesRead= 0;
        int firstLine= 0;
        boolean useLicense;
        boolean done=false;
        boolean skipBecauseResultOK = false;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String line;
        String PhoneNumbers[];
        String Licenses[];
        String Users[];
        String Passwords[];
        String FQDNs[];
        String lines[];
        String delim = "[,]";
        Integer i;
        String[] varNames = {""};
        String[] errorStrings;
        int lineNumber = 0;

        CancelList = false;

        errorStrings = new String[1000];
        PhoneNumbers = new String[1000];
        Licenses = new String[1000];
        Users = new String[1000];
        Passwords = new String[1000];
        FQDNs = new String[1000];
        lines = new String[1000];

        if (settingMFPListFileName.getText().equals(settingListResultFileName.getText())) {
            WriteToResultArea("MFP List File and MFP List Result File must be different.");
            return;
        }

        if (settingTesterMFP.getText().equals("")) {
            WriteToResultArea("Must specify Tester MFP FQDN or IP Address.");
            return;
        }
        if (settingTesterPhone.getText().equals("")) {
            WriteToResultArea("Must specify Phone Number.");
            return;
        }
        try {
            File listFile = new File(settingMFPListFileName.getText());
            reader = new BufferedReader( new FileReader(listFile));

            if ((line = reader.readLine()) != null) {
                lines[1]=line;
                firstLine = (int)line.length()+2; // +2 for CRLF
                varNames = line.split(delim);
                if (varNames.length < 1)
                    throw new IOException("Invalid MFP List File");
                // for (i=0;i<varNames.length;i++)
                //   WriteToResultArea("" + i + " " + varNames[i]);
            }
            WriteToResultAreaNoTime("");
            if (settingLicense.getText().equals("")) {
                WriteToResultArea("Processing MFP List. Bundles to be created without license.");
                pbar.setLabel("Deploying Settings Bundles created without License...");
                useLicense = false;
            }
            else {
                WriteToResultArea("Processing MFP List. Bundles to be created with license.");
                pbar.setLabel("Deploying Settings Bundles created with License...");
                useLicense = true;
            }
            pbar.setMax((int)listFile.length()-firstLine);
            pbar.updateBar(0);
            pbar.setOK(0);
            pbar.setFail(0);
            pbar.setVisible(true);
            lineNumber = 2;

            LogInToMFP(settingTesterMFP.getText(),settingTesterUser.getText(),settingTesterPassword.getText());

            while (!done && ((line = reader.readLine()) != null) && (lineNumber < lines.length)) {

                if (CancelList) {
                    WriteToResultAreaNoTime("");
                    WriteToResultArea("List Operation Canceled.");
                    done = true;
                    continue;
                }

                lines[lineNumber] = line;
                String[] tokens = line.split(delim);
                bytesRead += (int)line.length() + 2;  // +2 for CRLF

                // "" means skip this line
                if (tokens.length == 0) {
                    FQDNs[lineNumber] = "";
                    PhoneNumbers[lineNumber] = "";
                    Licenses[lineNumber] = "";
                    Users[lineNumber] = "";
                    Passwords[lineNumber] = "";
                    errorStrings[lineNumber++] = "Skipped";
                    pbar.updateBar(bytesRead);
                    continue;
                }

                WriteToResultAreaNoTime("");
                WriteToResultArea("Processing " + tokens[0]);
                // "End" means time to quit
                if (tokens[0].equals("End")) {
                    done = true;
                    FQDNs[lineNumber] = "";
                    PhoneNumbers[lineNumber] = "";
                    Licenses[lineNumber] = "";
                    Users[lineNumber] = "";
                    Passwords[lineNumber] = "";
                    errorStrings[lineNumber++] = "Skipped";
                    pbar.updateBar(bytesRead);
                    continue;
                }
                // "#" means skip this line
                if (tokens[0].charAt(0) == '#') {
                    errorStrings[lineNumber++] = "Skipped";
                    pbar.updateBar(bytesRead);
                    continue;
                }
                rc = 0;
                skipBecauseResultOK = false;
                for (i=0; i<tokens.length; i++) {
                    //WriteToResultArea("" + varNames[i] + " = " + tokens[i]);
                    if (varNames[i].equals("IP Address or FQDN")) {
                        FQDNs[lineNumber] = tokens[i];
                        settingMFP.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("Phone Number")) {
                        PhoneNumbers[lineNumber] = tokens[i];
                        settingMFPPhone.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("License File")) {
                        Licenses[lineNumber] = tokens[i];
                        settingLicense.setText(tokens[i]);
                        useLicense = true;
                        WriteToResultArea("Bundles to be created with license from MFP List File.");
                        pbar.setLabel("Deploying Settings Bundles created with License from MFP List File...");
                    }
                    else if (varNames[i].equals("MFP User")) {
                        Users[lineNumber] = tokens[i];
                        settingMFPUser.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("MFP Password")) {
                        Passwords[lineNumber] = tokens[i];
                        settingMFPPassword.setText(tokens[i]);
                    }
                    else if (varNames[i].equals("Result")) {
                        if (tokens[i].equals("OK")) {
                            skipBecauseResultOK = true;
                            i=tokens.length;
                        }
                    }
                    else {
                        rc = LoadSettingVCC("" + varNames[i] + " \"" + tokens[i] + "\"");
                        if (rc != 0) {
                            WriteToResultArea("Invalid VCC Setting Name: " + varNames[i]);
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            i = tokens.length;
                            errorStrings[lineNumber++] = "Bad VCC Setting Name: "+varNames[i];
                        }
                    }
                }
                if (rc != 0) {
                    WriteToResultArea("Update of " + settingMFP.getText() + " failed.");
                    pbar.updateBar(bytesRead);
                    continue;
                }
                if (skipBecauseResultOK) {
                    errorStrings[lineNumber++] = "Skipped";
                    pbar.updateBar(bytesRead);
                    continue;
                }

                rc = GetInfo();
                if (rc != 0) {
                    WriteToResultArea("Could not get info from " + settingMFP.getText());
                    mfpFail++;
                    pbar.setFail(mfpFail);
                    writer.write(line+",Could not get info from MFP\n");
                    pbar.updateBar(bytesRead);
                    continue;
                }

                LogInToMFP(settingMFP.getText(),settingMFPUser.getText(),settingMFPPassword.getText());

                if (!VCCisSupported(settingMFP.getText())) {
                    WriteToResultArea("MFP " + settingMFP.getText() + " does not support VCC, using UCF.");
                    if (settingLicense.getText().isEmpty()) {
                        WriteToResultArea("FoIP License not specified, skipping eSF App and License deployment.");
                    }
                    else {
                        rc = SendEsfToMFP("");
                        if (rc != 0) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Deploy eSF App.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            errorStrings[lineNumber++] = "Failed to Deploy eSF App";
                            pbar.updateBar(bytesRead);
                            continue;
                        }
                        rc = SendLicenseToMFP();
                        if (rc != 0) {
                            WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Deploy eSF License.");
                            mfpFail++;
                            pbar.setFail(mfpFail);
                            errorStrings[lineNumber++] = "Failed to Deploy eSF License";
                            pbar.updateBar(bytesRead);
                            continue;
                        }
                    }
                    rc = WriteUCFFile();
                    if (rc != 0) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Create UCF File.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        errorStrings[lineNumber++] = "Failed to Create UCF File";
                        pbar.updateBar(bytesRead);
                        continue;
                    }
                    rc = SendUCFToMFP();
                    if (rc != 0) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed Sending Bundle to MFP.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        errorStrings[lineNumber++] = "Failed Sending UCF File to MFP";
                        pbar.updateBar(bytesRead);
                        continue;
                    }
                }

                else {

                    rc = WriteXMLBundleFile(true,useLicense);
                    if (rc != 0) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to Create Bundle File.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        errorStrings[lineNumber++] = "Failed to Create Bundle File.";
                        pbar.updateBar(bytesRead);
                        continue;
                    }

                    rc = SendBundleToMFP();
                    if (rc != 0) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed Sending Bundle to MFP.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        errorStrings[lineNumber++] = "Failed Sending Bundle to MFP.";
                        pbar.updateBar(bytesRead);
                        continue;
                    }
                }

                errorStrings[lineNumber++] = "OK";
                mfpOK++;
                pbar.setOK(mfpOK);
                pbar.updateBar(bytesRead);
            }
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
            }
        }

        WriteToResultAreaNoTime("");
        WriteToResultArea("MFP List Processed.  Deployment of License and Settings:  OK = " + mfpOK + "  Fail = " + mfpFail);
        pbar.setLabel("Deployment of Settings Bundles complete.");

        if (CancelList) {
            CancelList = false;
            pbar.setVisible(false);
            return;
        }

        sleepSeconds(20);

        WriteToResultAreaNoTime("");
        WriteToResultArea("Processing MFP List. Running Send and Receive Fax Tests.");

        pbar.setVisible(false);
        pbar.setLabel("Running Send and Receive Fax Tests...");
        pbar.setMax((lineNumber-2)*2);
        pbar.updateBar(0);
        pbar.setOK(0);
        pbar.setFail(0);
        pbar.setVisible(true);
        mfpOK=0;
        mfpFail=0;
        try {
            File listResultFile = new File(settingListResultFileName.getText());
            writer =  new BufferedWriter( new FileWriter(listResultFile));

            writer.write(lines[1]+",Result\n");

            done = false;
            for (i=2;i<lineNumber && !done;i++) {

                if (CancelList) {
                    WriteToResultAreaNoTime("");
                    WriteToResultArea("List Operation Canceled.");
                    CancelList = false;
                    done = true;
                    continue;
                }

                if (errorStrings[i].equals("Skipped")) {
                    writer.write(lines[i]+",\n");
                    pbar.updateBar((i-1)*2);
                    continue;
                }
                settingMFP.setText(FQDNs[i]);
                settingMFPPhone.setText(PhoneNumbers[i]);
                settingLicense.setText(Licenses[i]);
                settingMFPUser.setText(Users[i]);
                settingMFPPassword.setText(Passwords[i]);
                WriteToResultAreaNoTime("");
                WriteToResultArea("Processing " + settingMFP.getText());

                MFP mfp = MFPStore.getInstance().Get(settingMFP.getText());
                MFP testermfp = MFPStore.getInstance().Get(settingTesterMFP.getText());

                mfp.GetSIPRegStatus();

                if (errorStrings[i].equals("OK")) {
                    String[] resultTokens = {""};
                    String result = "";

                    // Receive a fax
                    if (settingMFPPhone.getText().equals("")) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to receive a fax. Phone Number not specified.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(lines[i]+",Failed to receive a fax. No Phone Number.\n");
                        pbar.updateBar((i-1)*2);
                        continue;
                    }

                    result = testermfp.SendAndVerifyFax(UI,settingMFPPhone.getText());
                    resultTokens = result.split("[|]");
                    if (!resultTokens[2].equals("OK")) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to receive a fax.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(lines[i]+",Failed to receive a fax.\n");
                        pbar.updateBar((i-1)*2);
                        continue;
                    }
                    pbar.updateBar((i-1)*2-1);

                    delayWithMessage(10,"Waiting 10 seconds to avoid Busy");

                    // Send a fax
                    result = mfp.SendAndVerifyFax(UI,settingTesterPhone.getText());
                    resultTokens = result.split("[|]");
                    if (!resultTokens[2].equals("OK")) {
                        WriteToResultArea("Update of " + settingMFP.getText() + " failed. Failed to send a fax.");
                        mfpFail++;
                        pbar.setFail(mfpFail);
                        writer.write(lines[i]+",Failed to send a fax.\n");
                        pbar.updateBar((i-1)*2);
                        continue;
                    }

                    WriteToResultArea("Update of " + settingMFP.getText() + " was successful.");
                    mfpOK++;
                    pbar.setOK(mfpOK);
                    writer.write(lines[i]+",OK\n");
                    pbar.updateBar((i-1)*2);

                }
                else {
                    mfpFail++;
                    pbar.setFail(mfpFail);
                    pbar.updateBar((i-1)*2);
                    writer.write(lines[i]+","+errorStrings[i]+"\n");
                }
            }

            WriteToResultAreaNoTime("");
            WriteToResultArea("MFP List Processed.  OK = " + mfpOK + "  Fail = " + mfpFail);
            pbar.setLabel("Send and Receive Fax Tests complete.");
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
        finally {
            try {
                writer.close();
                if (mfpFail > 1)
                    JOptionPane.showMessageDialog(new JFrame(), "MFP List Processing Complete.\n\nMFP failures encountered!\n\n");
                else
                    JOptionPane.showMessageDialog(new JFrame(), "MFP List Processing Complete.\n\nAll MFPs OK.\n\n");
            }
            catch (Exception e) {
            }
            pbar.setVisible(false);
        }

    }

    public int copyFile(String From, String To) {
        WriteToResultAreaNoCRLF("Copy File " + From + " to " + To + "...");
        try {
            Path FromPath = Paths.get(From);
            Path ToPath = Paths.get(To);
            CopyOption[] options = new CopyOption[] {
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            };
            Files.copy(FromPath,ToPath,options);
        }
        catch (Exception e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
            return -1;
        }
        WriteToResultAreaNoTime("OK");
        return 0;
    }

    public void PrintMFPCache() {
        mfpStore.OutputMFPList();
    }

    public void Sweep(boolean useHttps) {
        String savedMFP = settingMFP.getText();
        String mfp;
        String line;
        int start;
        int end;
        String mfpFirstPart;
        WebConnection connection = null;
        boolean FoundMFP;

        String delims = "[.]";
        String[] tokens = settingMFP.getText().split(delims);
        if (tokens.length != 4) {
            WriteToResultArea("Bad IP Address Range.");
            return;
        }
        mfpFirstPart = tokens[0]+"."+tokens[1]+"."+tokens[2]+".";
        String[] tokens2 = tokens[3].split("[-]");
        if (tokens2.length != 2) {
            WriteToResultArea("Bad IP Address Range.");
            return;
        }
        start = Integer.parseInt(tokens2[0]);
        end = Integer.parseInt(tokens2[1]);

        WriteToResultArea("Starting sweep of " + settingMFP.getText());

        for (int i=start;i<=end;i++) {
            mfp = mfpFirstPart + i;
            FoundMFP = false;
            try {
                //Create connection
                connection = new WebConnection(MFPStore.getInstance().Get(mfp),"",useHttps,3000);
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    FoundMFP = true;;
                }

                rd.close();

            } catch (Exception e) {
            }

            finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
            if (FoundMFP) {
                settingMFP.setText(mfp);
                GetInfo();
            }
            else {
                WriteToResultArea("No MFP found at " + mfp);
            }
        }
        settingMFP.setText(savedMFP);
        WriteToResultArea("Sweep complete.");
    }

    public void ClearMFPCache() {
        WriteToResultAreaNoCRLF("Clearing MFP Cache...");
        int MFPsRemoved = MFPStore.getInstance().Clear();
        WriteToResultAreaNoTime("OK");
        WriteToResultArea("Total MFPs Removed: " + MFPsRemoved);
    }

    public int GetInfo() {
        int rc = -1;

        if (settingMFP.getText().isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return rc;
        }

        WriteToResultAreaNoTime("");
        WriteToResultAreaNoCRLF("Getting MFP Info from " + settingMFP.getText() + "...");

        MFP mfp = mfpStore.Get(settingMFP.getText());
        if (mfp.validData()) {
            WriteToResultAreaNoTime("OK");
            rc = 0;
            WriteToResultArea("\tModel Name:        \t" + mfp.ModelName());
            WriteToResultArea("\tSerial Number:     \t" + mfp.SerialNumber());
            WriteToResultArea("\tPrinter NPAP ID:   \t" + mfp.PrinterNPAPID());
            WriteToResultArea("\tFirmware Revision: \t" + mfp.BaseCodeRevision());
            WriteToResultArea("\tTime/Date:         \t" + mfp.TimeDate());
            WriteToResultArea("\tTB EEC CT:         \t" + mfp.TB_EEC_CT());
            WriteToResultArea("\tPrinter Family:    \t" + mfp.PrinterFamilyString());
            WriteToResultArea("\tCode Name:         \t" + mfp.GetCodeName());
            WriteToResultArea("\tVCC Support:       \t" + mfp.vccSupported());
            WriteToResultArea("\tUsing https:       \t" + mfp.getUseHttps());
            WriteToResultArea("\tHTTPS Fax Support: \t" + mfp.HTTPSFaxSupported());
            WriteToResultArea("\t" + mfp.UAA());
        }

        WriteToResultAreaNoTime("");

        return rc;
    }

    public String GetSerialNumber() {
        String SerialNumber = "Failed: No Data";
        if (settingMFP.getText().isEmpty()) {
            return SerialNumber;
        }
        MFP mfp = mfpStore.Get(settingMFP.getText());
        if (mfp.validData()) {
            SerialNumber = mfp.SerialNumber();
        }
        return SerialNumber;
    }

    public boolean VCCisSupported(String mfp) {
        return mfpStore.Get(mfp).VCCisSupported();
    }

    public boolean MojaOrLaterDevice(String mfp) {
        return mfpStore.Get(mfp).MojaOrLaterDevice();
    }


    public int LogInToMFPVerify(String MFP, String User, String Password) {
        int rc;
        if (Password.isEmpty() && User.isEmpty()) {
            WriteToResultArea("Must specify MFP User and/or MFP Password.");
            return -1;
        }
        rc = LogInToMFPClassic(MFP, User, Password);
        if (rc != 0)
            return LogInToMFPMoja(MFP, User, Password);
        else
            return rc;
    }

    public int LogInToTesterMFPVerify(String MFP, String User, String Password) {
        int rc;
        if (Password.isEmpty() && User.isEmpty()) {
            WriteToResultArea("Must specify Tester User and/or Tester Password.");
            return -1;
        }
        rc = LogInToMFPClassic(MFP, User, Password);
        if (rc != 0)
            return LogInToMFPMoja(MFP, User, Password);
        else
            return rc;
    }

    public int LogInToMFP(String MFP, String User, String Password) {
        int rc;
        rc = LogInToMFPClassic(MFP, User, Password);
        if (rc != 0)
            return LogInToMFPMoja(MFP, User, Password);
        else
            return rc;
    }

    public int LogInToMFPMoja(String MFP, String User, String Password) {
        int rc = -1;
        String line = "";
        String sessionId="";
        String sessionKey="";
        String sessionName="";
        WebConnection connection = null;
        if (Password.isEmpty() && User.isEmpty())
            return 0;
        if (MFP.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return -1;
        }
        //WriteToResultAreaNoCRLF("Checking MFP Family...");
        //if (!MojaOrLaterDevice(MFP)) {
        //  WriteToResultAreaNoTime("OK");
        //  WriteToResultArea("Must be Moja.");
        //  return -1;
        // }
        // WriteToResultAreaNoTime("OK");
        WriteToResultArea("");
        WriteToResultAreaNoCRLF("Using Moja method to log in to " + MFP + "...");
        try {
            String encodedLogIn;
            MFP mfp = MFPStore.getInstance().Init(MFP);
            if (mfp.NeedToSwitchToHttps()) {
                WriteToResultAreaNoTime("Failed");
                mfp.setUseHttps(true);
                WriteToResultAreaNoCRLF("Trying https...");
            }
            if (!User.isEmpty() && !Password.isEmpty())
                encodedLogIn = "data=%7B%22authtype%22%3A0%2C%22authId%22%3A-1%2C%22creds%22%3A%7B%22username%22%3A%22"+User+"%22%2C%22password%22%3A%22"+Password+"%22%7D%7D";
            else if (!Password.isEmpty())
                encodedLogIn = "data=%7B%22authtype%22%3A3%2C%22authId%22%3A-1%2C%22creds%22%3A%7B%22pin%22%3A%22"+Password+"%22%7D%7D";
            else
                encodedLogIn = "data=%7B%22authtype%22%3A1%2C%22authId%22%3A-1%2C%22creds%22%3A%7B%22username%22%3A%22"+User+"%22%7D%7D";
            //Create connection
            connection = new WebConnection(mfp,"/webglue/session/create");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", "lexlang=\"0\"; autoLogin=false");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            OutputStream output = connection.getOutputStream ();
            DataOutputStream wr = new DataOutputStream (output);
            wr.writeBytes (encodedLogIn);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            while((line = rd.readLine()) != null) {
                String Pairs = "\"(.*?)\":\"(.*?)\"";
                Pattern p = Pattern.compile("\\{" + Pairs + "," + Pairs + "," + Pairs + "," + Pairs + ",.*?\\}");
                Matcher m = p.matcher(line);
                m.find();
                for (int i=1;i<=8;i+=2) {
                    String key = m.group(i);
                    String value = m.group(i+1);
                    if (key.equals("sessionId")) {
                        sessionId = value;
                        mfp.Cookies.add("sessionId="+sessionId);
                    }
                    if (key.equals("sessionKey")) {
                        sessionKey = value;
                        mfp.Cookies.add("sessionKey="+sessionKey);
                        mfp.setCsrfToken(sessionKey);
                    }
                    if (key.equals("sessionName")) {
                        sessionName = value;
                        mfp.Cookies.add("sessionName="+sessionName);
                    }
                }
            }
            rd.close();
            WriteToResultAreaNoTime("OK");
            WriteToResultArea("   sessionId = \"" + sessionId + "\"  sessionKey = \"" + sessionKey + "\"  sessionName = \"" + sessionName + "\"");
            WriteToResultArea("");
            rc = 0;

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;

    }

    public int LogInToMFPClassic(String MFP, String User, String Password) {
        int rc = -1;
        String line = "";
        WebConnection connection = null;
        if (Password.isEmpty() && User.isEmpty())
            return 0;
        if (MFP.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return -1;
        }
        WriteToResultArea("");
        WriteToResultAreaNoCRLF("Using Classic method to log in to " + MFP + "...");
        try {
            String encodedLogIn;
            MFP mfp = MFPStore.getInstance().Init(MFP);
            if (mfp.NeedToSwitchToHttps()) {
                WriteToResultAreaNoTime("Failed");
                mfp.setUseHttps(true);
                WriteToResultAreaNoCRLF("Trying https...");
            }
            if (!User.isEmpty() && !Password.isEmpty())
                encodedLogIn = "logon_type=password_only&goto=%2Fcgi-bin%2Fdynamic%2Fconfig%2Fsecure%2Fsecurity.html&accid=15&username="+User+"&password="+Password;
            else if (!Password.isEmpty())
                encodedLogIn = "logon_type=password_only&goto=%2Fcgi-bin%2Fdynamic%2Fconfig%2Fsecure%2Fsecurity.html&accid=1&password="+Password;
            else
                encodedLogIn = "logon_type=password_only&goto=%2Fcgi-bin%2Fdynamic%2Fconfig%2Fsecure%2Fsecurity.html&accid=15&username="+User;
            //Create connection
            connection = new WebConnection(mfp,"/cgi-bin/posttest/printer/login.html");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            OutputStream output = connection.getOutputStream ();
            DataOutputStream wr = new DataOutputStream (output);
            wr.writeBytes (encodedLogIn);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            //WriteToResultArea("Response from MFP:");
            while((line = rd.readLine()) != null) {
                //WriteToResultArea(line);
            }

            // Save the user and password cookies
            if (!User.isEmpty())
                mfp.Cookies.add("user="+User);
            if (!Password.isEmpty())
                mfp.Cookies.add("password="+Password);

            rd.close();
            WriteToResultAreaNoTime("OK");
            rc = 0;

        } catch (Exception e) {

            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
        return rc;

    }

    public int GetSettingsFromListFile(String MFP) {
        BufferedReader reader = null;
        boolean done = false;
        String firstLine;
        String line;
        String[] varNames = {""};
        int rc;
        try {
            File listFile = new File(settingMFPListFileName.getText());
            reader = new BufferedReader( new FileReader(listFile));

            if ((line = reader.readLine()) != null) {
                varNames = line.split(",");
                if (varNames.length < 1) {
                    WriteToResultArea("Invalid MFP List File");
                    return -1;
                }
            }
            while (!done && (line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 0)
                    continue;
                if (tokens[0].equals(MFP)) {
                    WriteToResultArea("Found Settings for " + MFP + ":");
                    for (int i=0; i<tokens.length; i++) {
                        WriteToResultArea("   " + varNames[i] + " = " + tokens[i]);
                        if (varNames[i].equals("IP Address or FQDN")) {
                        }
                        else if (varNames[i].equals("Phone Number")) {
                            settingMFPPhone.setText(tokens[i]);
                        }
                        else if (varNames[i].equals("License File")) {
                            settingLicense.setText(tokens[i]);
                        }
                        else if (varNames[i].equals("MFP User")) {
                            settingMFPUser.setText(tokens[i]);
                        }
                        else if (varNames[i].equals("MFP Password")) {
                            settingMFPPassword.setText(tokens[i]);
                        }
                        else if (varNames[i].equals("Result")) {
                            i=tokens.length;
                        }
                        else {
                            rc = LoadSettingVCC("" + varNames[i] + " \"" + tokens[i] + "\"");
                            if (rc != 0) {
                                WriteToResultArea("!!!Invalid VCC Setting Name: " + varNames[i]+"!!!");
                            }
                        }
                    }
                    WriteToResultArea("");
                    return 0;
                }
            }
            reader.close();
            WriteToResultArea("Could not find " + MFP + " in MFP List File.");
        }
        catch (Exception e) {
            WriteToResultArea("" + e);
        }
        return -1;
    }

    public String GetSettingFromWebPage(String MFP, String settingName) {
        String line = "";
        String value = null;
        boolean WriteData = false;
        WebConnection connection = null;
        if (MFP.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            return value;
        }
        WriteToResultAreaNoCRLF("Getting Setting " + WebNameToSettingName(settingName) + " from " + MFP + "...");

        if (MojaOrLaterDevice(MFP)) {
            try {
                //Create connection
                connection = new WebConnection(MFPStore.getInstance().Get(MFP),"/cgi-bin/faxsetup");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (line.contains(WebNameToSettingName(settingName).replace("\"",""))) {
                        Matcher matcher = Pattern.compile("\\d+").matcher(line);
                        matcher.find();
                        value = matcher.group();
                        break;
                    }
                }
                rd.close();
                WriteToResultAreaNoTime("OK");

            } catch (Exception e) {

                WriteToResultAreaNoTime("Failed");
                WriteToResultAreaNoCRLF("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        else {
            try {
                //Create connection
                connection= new WebConnection(MFPStore.getInstance().Get(MFP),"/cgi-bin/dynamic/printer/config/gen/fax/fax.html");
                connection.BasicGetSetup();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                while((line = rd.readLine()) != null) {
                    if (line.contains(settingName)) {
                        String[] parts = line.split("VALUE=");
                        if (settingName.startsWith("2")) {
                            String[] moreParts = parts[2].split(">");
                            value = moreParts[0];
                        }
                        else {
                            String[] moreParts = parts[1].split(">");
                            String[] moreMoreParts = moreParts[0].split("\"");
                            value = moreMoreParts[1];
                        }
                        break;
                    }
                }
                rd.close();
                WriteToResultAreaNoTime("OK");

            } catch (Exception e) {

                WriteToResultAreaNoTime("Failed");
                WriteToResultAreaNoCRLF("" + e);

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
        }
        WriteToResultArea("Setting " + WebNameToSettingName(settingName) + " = " + value);
        return value;
    }


    public int SendBundleToMFP() {
        return SendBundle(settingMFP.getText());
    }

    public int SendBundle(String MFP) {
        int rc = 0;
        int i;
        WebConnection connection = null;
        if (MFP.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            rc = -1;
            return rc;
        }
        WriteToResultAreaNoCRLF("Checking if " + MFP + " supports VCC...");
        if (!VCCisSupported(MFP)) {
            WriteToResultArea("MFP does not support VCC.");
            rc = -1;
            return rc;
        }
        WriteToResultAreaNoTime("OK");

        WriteToResultAreaNoCRLF("Sending Bundle to " + MFP + "...");
        for (i=0;i<100 && rc!=-1;i++) {
            rc = 0;
            try {
                //Create connection
                connection = new WebConnection(MFPStore.getInstance().Get(MFP),"/webservices/vcc/bundles");
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                OutputStream output = connection.getOutputStream ();
                DataOutputStream wr = new DataOutputStream (output);
                String bundleString = readFile(settingBundleFileName.getText(), StandardCharsets.ISO_8859_1);
                wr.writeBytes (bundleString);
                wr.flush ();
                wr.close ();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                WriteToResultArea("Response from MFP:");
                while((line = rd.readLine()) != null) {
                    WriteToResultArea(line);
                }
                rd.close();
                WriteToResultAreaNoTime("OK");

            } catch (Exception e) {

                if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                    rc = -2;
                }
                else {
                    WriteToResultAreaNoTime("Failed");
                    WriteToResultArea("" + e);
                    rc = -1;
                }

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
                if (rc == 0)
                    return rc;
                else
                    sleepOneSecondAndDisplayDot();
            }
        }

        if (rc == -2) {
            WriteToResultAreaNoTime("Failed. Server reeturned HTTP response code: 408");
        }

        return rc;
    }


    public int SendForceFaxModeDelay() {
        return SendSettingToMFP("0.system.17724",settingForceFaxModeDelay.getText());
    }

    public int SendSettingToMFP(String settingName, String value) {
        return SendSetting(settingMFP.getText(), settingName, value);
    }

    public int SendSetting(String MFP, String settingName, String value) {
        int rc = 0;
        int i;
        WebConnection connection = null;
        if (MFP.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            rc = -1;
            return rc;
        }

        WriteToResultAreaNoCRLF("Sending setting " + WebNameToSettingName(settingName) + " with value of " + value + " to " + MFP + "...");
        for (i=0;i<100 && rc!=-1;i++) {
            rc = 0;
            if (MojaOrLaterDevice(MFP)) {
                try {
                    String settingString = "data=%7B%22" + WebNameToSettingMojaName(settingName) + "%22%3A" + value + "%7D&c=" + WebNameToSettingCategory(settingName);
                    //Create connection
                    connection = new WebConnection(MFPStore.getInstance().Get(MFP),"/webglue/content");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //Send request
                    OutputStream output = connection.getOutputStream ();
                    DataOutputStream wr = new DataOutputStream (output);
                    wr.writeBytes (settingString);
                    wr.flush ();
                    wr.close ();

                    //Get Response
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    //WriteToResultArea("Response from MFP:");
                    while((line = rd.readLine()) != null) {
                        //WriteToResultArea(line);
                    }
                    rd.close();
                    WriteToResultAreaNoTime("OK");

                } catch (Exception e) {

                    if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                        rc = -2;
                    }
                    else {
                        WriteToResultAreaNoTime("Failed");
                        WriteToResultArea("" + e);
                        rc = -1;
                    }

                } finally {

                    if(connection != null) {
                        connection.disconnect();
                    }
                    if (rc == 0)
                        return rc;
                    else
                        sleepOneSecondAndDisplayDot();
                }
            }
            else {
                try {
                    String settingString = settingName + "=" + value;
                    //Create connection
                    connection = new WebConnection(MFPStore.getInstance().Get(MFP),"/cgi-bin/dynamic/printer/config/gen/fax/fax.html");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //Send request
                    OutputStream output = connection.getOutputStream ();
                    DataOutputStream wr = new DataOutputStream (output);
                    wr.writeBytes (settingString);
                    wr.flush ();
                    wr.close ();

                    //Get Response
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    //WriteToResultArea("Response from MFP:");
                    while((line = rd.readLine()) != null) {
                        //WriteToResultArea(line);
                    }
                    rd.close();
                    WriteToResultAreaNoTime("OK");

                } catch (Exception e) {

                    if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                        rc = -2;
                    }
                    else {
                        WriteToResultAreaNoTime("Failed");
                        WriteToResultArea("" + e);
                        rc = -1;
                    }

                } finally {

                    if(connection != null) {
                        connection.disconnect();
                    }
                    if (rc == 0)
                        return rc;
                    else
                        sleepOneSecondAndDisplayDot();
                }
            }
        }

        if (rc == -2) {
            WriteToResultAreaNoTime("Failed. Server reeturned HTTP response code: 408");
        }

        return rc;
    }

    public String GetLicenseID() {
        String licenseID = "";
        BufferedReader reader = null;
        String line;
        String delim = "[\"]";
        if (settingLicense.getText().equals("")) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("!!! Must supply a FoIP VCC License File !!!");
            return licenseID;
        }
        try {
            File settingsFile = new File(settingLicense.getText());
            reader = new BufferedReader( new FileReader(settingsFile));

            while ((line = reader.readLine()) != null) {
                if (line.contains("\"id\":")) {
                    String[] tokens = line.split(delim);
                    licenseID = tokens[3];
                    break;
                }
            }
            if (licenseID.equals("")) {
                WriteToResultAreaNoTime("Failed");
            }
        }
        catch (Exception e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
            }
        }
        return licenseID;
    }


    public int DeleteLicenseOnMFP() {
        int rc = 0;
        int i;
        WebConnection connection = null;
        String MFP = settingMFP.getText();
        if (MFP.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            rc = -1;
            return rc;
        }

        WriteToResultAreaNoCRLF("Deleting License on MFP " + MFP + "...");

        String LicenseID = GetLicenseID();

        if (LicenseID.equals("")) {
            WriteToResultArea("Failed to retrieve License ID.");
            rc = -1;
            return rc;
        }

        for (i=0;i<100 && rc!=-1;i++) {
            rc = 0;
            try {
                String encodedLicenseID = "licenses=" + LicenseID + "%3B";
                //Create connection
                connection = new WebConnection(MFPStore.getInstance().Get(MFP),"/cgi-bin/dynamic/printer/config/gen/vcc_delete_licenses.html");
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                OutputStream output = connection.getOutputStream ();
                DataOutputStream wr = new DataOutputStream (output);
                wr.writeBytes (encodedLicenseID);
                wr.flush ();
                wr.close ();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                //WriteToResultArea("Response from MFP:");
                while((line = rd.readLine()) != null) {
                    //WriteToResultArea(line);
                }
                rd.close();
                WriteToResultAreaNoTime("OK");

            } catch (Exception e) {

                if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                    rc = -2;
                }
                else {
                    WriteToResultAreaNoTime("Failed");
                    WriteToResultArea("" + e);
                    rc = -1;
                }

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
                if (rc == 0)
                    return rc;
                else
                    sleepOneSecondAndDisplayDot();
            }
        }

        if (rc == -2) {
            WriteToResultAreaNoTime("Failed. Server reeturned HTTP response code: 408");
        }

        return rc;
    }


    public int RestoreFaxDefaultsOnMFP() {
        int rc = 0;
        int i;
        WebConnection connection = null;
        String MFP = settingMFP.getText();
        if (MFP.isEmpty()) {
            WriteToResultArea("Must specify MFP FQDN or IP Address.");
            rc = -1;
            return rc;
        }

        String LicenseID = GetLicenseID();

        WriteToResultAreaNoCRLF("Restoring Fax Defaults on MFP " + MFP + "...");
        for (i=0;i<100 && rc!=-1;i++) {
            rc = 0;
            try {
                //Create connection
                connection = new WebConnection(MFPStore.getInstance().Get(MFP),"/cgi-bin/dynamic/printer/config/gen/fax/faxresetfactorydefaults.html");
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                OutputStream output = connection.getOutputStream ();
                DataOutputStream wr = new DataOutputStream (output);
                wr.flush ();
                wr.close ();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                //WriteToResultArea("Response from MFP:");
                while((line = rd.readLine()) != null) {
                    //WriteToResultArea(line);
                }
                rd.close();
                WriteToResultAreaNoTime("OK");

            } catch (Exception e) {

                if (e.getMessage().startsWith("Server returned HTTP response code: 408")) {
                    rc = -2;
                }
                else {
                    WriteToResultAreaNoTime("Failed");
                    WriteToResultArea("" + e);
                    rc = -1;
                }

            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
                if (rc == 0)
                    return rc;
                else
                    sleepOneSecondAndDisplayDot();
            }
        }

        if (rc == -2) {
            WriteToResultAreaNoTime("Failed. Server reeturned HTTP response code: 408");
        }

        return rc;
    }

    public void StartNewLogFile() {
        UI.NewLogFile();
        WriteToResultArea("New Log File.");
    }

    public void WriteToResultAreaNoTime(String S) {
        UI.writelnNoTime(S);
    }

    public void WriteToResultAreaNoTimeNoCRLF(String S) {
        UI.writeNoTime(S);
    }

    public void WriteToResultArea(String S) {
        UI.writeln(S);
    }

    public void WriteToResultAreaNoCRLF(String S) {
        UI.write(S);
    }

    void WriteXMLSetting(ZipOutputStream out, String name, int value) throws IOException {
        String strdata;
        byte[] data;
        strdata = "\t<setting name=\"" + name + "\">" + value + "</setting>\n";
        data = strdata.getBytes();
        out.write(data, 0, data.length);
    }

    void WriteXMLSetting(ZipOutputStream out, String name, String value) throws IOException {
        String strdata;
        byte[] data;
        strdata = "\t<setting name=\"" + name + "\">" + value + "</setting>\n";
        data = strdata.getBytes();
        out.write(data, 0, data.length);
    }

    void WriteXMLSetting(FoIPSetting setting, ZipOutputStream out, String name, int value) throws IOException {
        String strdata;
        byte[] data;
        if (setting.isSelected()) {
            strdata = "\t<setting name=\"" + name + "\">" + value + "</setting>\n";
            data = strdata.getBytes();
            out.write(data, 0, data.length);
        }
    }

    void WriteXMLSetting(FoIPSetting setting, ZipOutputStream out, String name) throws IOException {
        String strdata;
        byte[] data;
        if (setting.isSelected()) {
            strdata = "\t<setting name=\"" + name + "\">" + StringEscapeUtils.escapeXml(setting.getText()) + "</setting>\n";
            data = strdata.getBytes();
            out.write(data, 0, data.length);
        }
    }

    public String GetLicenseFileName() {
        //    File file = new File(settingLicense.getText());
        //    if (file.isDirectory())
        return "license.lic";
        //    else
        //      return file.getName();
    }

    public int WriteXMLBundleFile(boolean WriteSettings, boolean WriteLicense) {
        int rc = 0;
        String LicenseFileName = "";
        try {
            if (WriteSettings) {
                if (WriteLicense) {
                    WriteToResultArea("Settings and License Bundle Creation");
                    WriteToResultAreaNoCRLF("Settings Portion of Bundle Creation Started...");
                }
                else {
                    WriteToResultAreaNoCRLF("Settings Bundle Creation Started...");
                }
            }
            else {
                WriteToResultArea("License Bundle Creation");
                WriteToResultAreaNoCRLF("Bundle Initialization Started...");
            }
            final File f = new File(settingBundleFileName.getText());
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
            ZipEntry entry = new ZipEntry("bundle.xml");
            out.putNextEntry(entry);

            byte[] data;
            String strdata;

            data = Header.getBytes();

            out.write(data, 0, data.length);

            if (WriteLicense) {
                if (settingLicense.getText().equals("")) {
                    WriteToResultAreaNoTime("Failed");
                    WriteToResultArea("!!! Must supply a FoIP VCC License File !!!");
                    out.closeEntry();
                    out.close();
                    rc = -1;
                    return rc;
                }
                LicenseFileName = GetLicenseFileName();
                strdata = "<licenses>\n\t<license src=\""+LicenseFileName+"\"></license>\n</licenses>\n";
                data = strdata.getBytes();
                out.write(data, 0, data.length);
            }

            if (SettingsSelected() && WriteSettings) {
                strdata = "<deviceSettings>\n";
                data = strdata.getBytes();
                out.write(data, 0, data.length);

                WriteXMLSetting(settingFaxName, out, "mfp.fax.stationName");
                WriteXMLSetting(settingFaxNumber, out, "mfp.fax.stationNumber");
                WriteXMLSetting(settingFaxID, out, "mfp.fax.stationID", settingFaxID.getSelectedIndex());
                WriteXMLSetting(settingFaxMode, out, "mfp.fax.mode", settingFaxMode.getSelectedIndex());
                WriteXMLSetting(settingFaxTransport, out, "mfp.fax.voip.faxTransport", settingFaxTransport.getSelectedIndex());
                WriteXMLSetting(settingVoipProtocol, out, "mfp.fax.voip.protocol", settingVoipProtocol.getSelectedIndex()+1);
                WriteXMLSetting(settingTraceLevel, out, "mfp.fax.voip.traceLevel");
                WriteXMLSetting(settingStunServer, out, "mfp.fax.voip.stunServer");
                WriteXMLSetting(settingForceFaxMode, out, "mfp.fax.voip.forceFaxMode",(settingForceFaxMode.getChecked() ? 1 : 0));
                WriteXMLSetting(settingForceFaxModeDelay, out, "0x453C");  // No VCC key, so we use NPA ID
                WriteXMLSetting(settingSipProxy, out, "mfp.fax.sip.proxy");
                WriteXMLSetting(settingSipRegistrar, out, "mfp.fax.sip.registrar");
                WriteXMLSetting(settingSipUser, out, "mfp.fax.sip.user");
                WriteXMLSetting(settingSipPassword, out, "mfp.fax.sip.password");
                WriteXMLSetting(settingSipContact, out, "mfp.fax.sip.contact");
                WriteXMLSetting(settingSipRealm, out, "mfp.fax.sip.realm");
                WriteXMLSetting(settingSipAuthID, out, "mfp.fax.sip.authID");
                WriteXMLSetting(settingSipOBProxy, out, "mfp.fax.sip.outboundProxy");
                if (MojaOrLaterDevice(settingMFP.getText())) {
                    WriteXMLSetting(settingRegTransport, out, "mfp.fax.sipSettings.registrationTransport", settingRegTransport.getSelectedIndex());
                    WriteXMLSetting(settingIncomingTransport, out, "mfp.fax.sipSettings.incomingCallsTransport", settingIncomingTransport.getSelectedIndex());
                    WriteXMLSetting(settingOutgoingTransport, out, "mfp.fax.sipSettings.OutgoingCallsTransport", settingOutgoingTransport.getSelectedIndex());
                }
                // These two settings removed in Moja FW3
                // WriteXMLSetting(out, "mfp.fax.sip.disableIncoming",0);
                // WriteXMLSetting(out, "mfp.fax.sip.dialOutDigit",-1);
                WriteXMLSetting(settingH323Gateway, out, "mfp.fax.h323.gateway");
                WriteXMLSetting(settingH323Gatekeeper, out, "mfp.fax.h323.gatekeeper");
                WriteXMLSetting(settingH323User, out, "mfp.fax.h323.user");
                WriteXMLSetting(settingH323User, out, "mfp.fax.h323.password");
                WriteXMLSetting(settingH323EnableFastStart, out, "mfp.fax.h323.enableFastStart",(settingH323EnableFastStart.getChecked() ? 1 : 0));
                WriteXMLSetting(settingH323DisableH245, out, "mfp.fax.h323.disableH245Tunnel",(settingH323DisableH245.getChecked() ? 1 : 0));
                WriteXMLSetting(settingH323DisableGKDisc, out, "mfp.fax.h323.disableGatekeeperDiscovery",(settingH323DisableGKDisc.getChecked() ? 1 : 0));
                // These two settings removed in Moja FW3
                // WriteXMLSetting(out, "mfp.fax.h323.disableIncoming",0);
                // WriteXMLSetting(out, "mfp.fax.h323.dialOutDigit",-1);
                WriteXMLSetting(settingT38Indicator, out, "mfp.fax.t38.indicatorRedundancy");
                WriteXMLSetting(settingT38LowSpeed, out, "mfp.fax.t38.lowSpeedRedundancy");
                WriteXMLSetting(settingT38HighSpeed, out, "mfp.fax.t38.highSpeedRedundancy");
                WriteXMLSetting(settingT38KeepAlive, out, "mfp.fax.t38.udptlKeepAliveInterval");
                WriteXMLSetting(settingAutomaticRedial, out, "mfp.fax.send.autoRedial");
                WriteXMLSetting(settingRedialFrequency, out, "mfp.fax.send.redialFrequency");
                WriteXMLSetting(settingRingsToAnswer, out, "mfp.fax.receive.RingsToAnswer");

                WriteXMLSetting(settingHTTPSServiceURL, out, "mfp.fax.httpsSettings.serviceUrl");
                WriteXMLSetting(settingHTTPSProxy, out, "mfp.fax.httpsSettings.proxy");
                WriteXMLSetting(settingHTTPSProxyUser, out, "mfp.fax.httpsSettings.proxyUser");
                WriteXMLSetting(settingHTTPSProxyPassword, out, "mfp.fax.httpsSettings.proxyPassword");
                WriteXMLSetting(settingHTTPSPeerVerification, out, "mfp.fax.httpsSettings.enablePeerVerification", (settingHTTPSPeerVerification.getChecked() ? 1 : 0));
                WriteXMLSetting(settingHTTPSEncryptFaxReceive, out, "mfp.fax.httpsSettings.encryptFaxReceive", settingHTTPSEncryptFaxReceive.getSelectedIndex());
                WriteXMLSetting(settingHTTPSEncryptFaxSend, out, "mfp.fax.httpsSettings.encryptFaxSend", settingHTTPSEncryptFaxSend.getSelectedIndex());

                strdata = "</deviceSettings>\n";
                data = strdata.getBytes();
                out.write(data, 0, data.length);
            }

            strdata = "</bundle>\n";
            data = strdata.getBytes();
            out.write(data, 0, data.length);

            out.closeEntry();

            if (WriteLicense) {
                WriteToResultAreaNoTime("OK");
                entry = new ZipEntry(LicenseFileName);
                out.putNextEntry(entry);

                File file = new File(settingLicense.getText());

                // If File is a Directory, then this is the directory of vcc license bundles.
                // The license.lic file is inside the bundle. The bundle file name starts with
                // the serial number of the machine the license was made for.
                if (file.isDirectory()) {
                    WriteToResultAreaNoCRLF("Getting Serial Number of MFP...");
                    final String SerialNumber = GetSerialNumber();
                    if (SerialNumber.contains("Failed")) {
                        WriteToResultAreaNoTime(SerialNumber);
                        return -1;
                    }
                    else {
                        WriteToResultAreaNoTime("OK  " + SerialNumber);
                    }
                    WriteToResultAreaNoCRLF("License Bundle Creation Started...");
                    // This searches the directory for a file that starts with the serial
                    // number that we retrieved from the device and ends with ".zip"
                    File[] matchingFiles = file.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.startsWith(SerialNumber) && name.endsWith(".zip");
                        }
                    });
                    // If length of matchingFiles is 0 then we could not find the bundle
                    if (matchingFiles.length == 0) {
                        WriteToResultAreaNoTime("Failed");
                        WriteToResultArea("Could not find license file for Serial Number " + SerialNumber);
                        out.closeEntry();
                        out.close();
                        return -1;
                    }

                    // Update the user that we are checking for a node locked license file
                    WriteToResultAreaNoTimeNoCRLF("For Serial Number " + SerialNumber +  " from file " + matchingFiles[0] + "...");

                    ZipInputStream zipIn = new ZipInputStream(new FileInputStream(matchingFiles[0]));
                    ZipEntry entryIn = zipIn.getNextEntry();
                    boolean NoBytesWritten = true;
                    while (entryIn != null) {
                        if (entryIn.getName().endsWith(".lic")) {
                            byte[] bytesIn = new byte[2048];
                            int bytesRead = 0;
                            while ((bytesRead = zipIn.read(bytesIn)) != -1) {
                                out.write(bytesIn, 0, bytesRead);
                                NoBytesWritten = false;
                            }
                        }
                        zipIn.closeEntry();
                        if (NoBytesWritten)
                            entryIn = zipIn.getNextEntry();
                        else
                            entryIn = null;
                    }
                    // If no bytes were written then we failed to find the license file
                    if (NoBytesWritten) {
                        WriteToResultAreaNoTime("Failed");
                        WriteToResultArea("Could not write license file for Serial Number " + SerialNumber);
                        out.closeEntry();
                        out.close();
                        return -1;
                    }

                }

                // This is the case where the license is a simple *.lic vcc license file
                // or *.zip vcc bundle
                else {
                    Path path = Paths.get(settingLicense.getText());

                    if (path.toString().toLowerCase().endsWith(".zip")) {

                        // If it is a zip file, it may be a package builder zip file with many licenses
                        // in it named with the Serial Number of the MFP. So here we get the serial number
                        // to check if that is the case. Otherwise, it could just be a bundle zip file with
                        // one .lic file.

                        // Update the user that we are checking for a node locked license file
                        WriteToResultArea("License from file " + path.toString());

                        WriteToResultAreaNoCRLF("Getting Serial Number of MFP...");
                        final String SerialNumber = GetSerialNumber();
                        if (SerialNumber.contains("Failed")) {
                            WriteToResultAreaNoTime(SerialNumber);
                            return -1;
                        }
                        else {
                            WriteToResultAreaNoTime("OK  " + SerialNumber);
                        }
                        WriteToResultAreaNoCRLF("License Bundle Creation Started...");
                        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(path.toString()));
                        ZipEntry entryIn = zipIn.getNextEntry();
                        boolean NoBytesWritten = true;
                        while (entryIn != null) {
                            if (entryIn.getName().startsWith("licenses") && entryIn.getName().contains(SerialNumber)) {
                                // Found a zip file for our serial number, so we need another ZipInputStream
                                ZipInputStream zipIn2 = new ZipInputStream(zipIn);
                                ZipEntry entryIn2 = zipIn2.getNextEntry();
                                while (entryIn2 != null) {
                                    if (entryIn2.getName().endsWith(".lic")) {
                                        byte[] bytesIn = new byte[2048];
                                        int bytesRead = 0;
                                        while ((bytesRead = zipIn2.read(bytesIn)) != -1) {
                                            out.write(bytesIn, 0, bytesRead);
                                            NoBytesWritten = false;
                                        }
                                    }
                                    zipIn2.closeEntry();
                                    if (NoBytesWritten) {
                                        entryIn2 = zipIn2.getNextEntry();
                                    }
                                    else {
                                        entryIn2 = null;
                                    }
                                }
                                entryIn = null;
                            }
                            // In this case it is just a simple license.lic file
                            else if (entryIn.getName().equals("license.lic")) {
                                byte[] bytesIn = new byte[2048];
                                int bytesRead = 0;
                                while ((bytesRead = zipIn.read(bytesIn)) != -1) {
                                    out.write(bytesIn, 0, bytesRead);
                                    NoBytesWritten = false;
                                }
                            }
                            zipIn.closeEntry();
                            if (NoBytesWritten)
                                entryIn = zipIn.getNextEntry();
                            else
                                entryIn = null;
                        }
                        // If no bytes were written then we failed to find the license file
                        if (NoBytesWritten) {
                            WriteToResultAreaNoTime("Failed");
                            WriteToResultArea("Could not get license file from " + path);
                            out.closeEntry();
                            out.close();
                            return -1;
                        }
                    }
                    else if (path.toString().toLowerCase().endsWith(".lic")) {
                        // Update the user that we are checking for a node locked license file
                        WriteToResultArea("License from file " + path.toString());
                        WriteToResultAreaNoCRLF("License Bundle Creation Started...");

                        data = Files.readAllBytes(path);
                        out.write(data, 0, data.length);
                    }
                    else {
                        WriteToResultArea("!!! FoIP License file must be *.lic or *.zip !!!");
                        out.closeEntry();
                        out.close();
                        return -1;
                    }
                }

                out.closeEntry();
            }

            out.close();
            WriteToResultAreaNoTime("OK");
        }
        catch (IOException e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
            rc = -1;
        }
        return rc;
    }


    public int WriteXMLBundleFileRedials(int Redials) {
        int rc = 0;
        try {
            WriteToResultAreaNoCRLF("Redials=" + Redials + " Bundle Creation Started...");
            final File f = new File(settingBundleFileName.getText());
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
            ZipEntry entry = new ZipEntry("bundle.xml");
            out.putNextEntry(entry);

            byte[] data;
            String strdata;

            data = Header.getBytes();

            out.write(data, 0, data.length);

            strdata = "<deviceSettings>\n";
            data = strdata.getBytes();
            out.write(data, 0, data.length);

            WriteXMLSetting(out, "mfp.fax.send.autoRedial","" + Redials);

            strdata = "</deviceSettings>\n";
            data = strdata.getBytes();
            out.write(data, 0, data.length);

            strdata = "</bundle>\n";
            data = strdata.getBytes();
            out.write(data, 0, data.length);

            out.closeEntry();

            out.close();
            WriteToResultAreaNoTime("OK");
        }
        catch (IOException e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
            rc = -1;
        }
        return rc;
    }


    void WriteUCFSetting(BufferedWriter out, String name, int value) throws IOException {
        String strdata;
        strdata = name + " \"" + value + "\"\r\n";
        out.write(strdata);
    }

    void WriteUCFSetting(BufferedWriter out, String name, String value) throws IOException {
        String strdata;
        strdata = name + " \"" + value + "\"\r\n";
        out.write(strdata);
    }

    void WriteUCFSetting(FoIPSetting setting, BufferedWriter out, String name, int value) throws IOException {
        String strdata;
        if (setting.isSelected()) {
            strdata = name + " \"" + value + "\"\r\n";
            out.write(strdata);
        }
    }

    void WriteUCFSetting(FoIPSetting setting, BufferedWriter out, String name) throws IOException {
        String strdata;
        if (setting.isSelected()) {
            strdata = name + " \"" + setting.getText() + "\"\r\n";
            out.write(strdata);
        }
    }


    public int WriteUCFFile() {
        int rc = -1;
        try {
            WriteToResultAreaNoCRLF("UCF Creation Started...");
            File ucfFile = new File(settingUCFFileName.getText());
            BufferedWriter out = new BufferedWriter( new FileWriter(ucfFile));

            String strdata;

            out.write(ucfHeader);


            if (SettingsSelected()) {
                WriteUCFSetting(settingFaxName, out, "mfp.fax.stationName");
                WriteUCFSetting(settingFaxNumber, out, "mfp.fax.stationNumber");
                WriteUCFSetting(settingFaxID, out, "mfp.fax.stationID", settingFaxID.getSelectedIndex());
                WriteUCFSetting(settingFaxMode, out, "mfp.fax.mode", settingFaxMode.getSelectedIndex());
                WriteUCFSetting(settingFaxTransport, out, "mfp.fax.voipSettings.faxTransport", settingFaxTransport.getSelectedIndex());
                WriteUCFSetting(settingVoipProtocol, out, "mfp.fax.voipSettings.voipProtocol", settingVoipProtocol.getSelectedIndex()+1);
                WriteUCFSetting(settingTraceLevel, out, "mfp.fax.voipSettings.traceLevel");
                WriteUCFSetting(settingStunServer, out, "mfp.fax.voipSettings.stunServer");
                WriteUCFSetting(settingForceFaxMode, out, "mfp.fax.voipSettings.forceFaxMode",(settingForceFaxMode.getChecked() ? 1 : 0));
                WriteUCFSetting(settingForceFaxModeDelay, out, "mfp.fax.voipSettings.forceFaxModeDelay");
                WriteUCFSetting(settingSipProxy, out, "mfp.fax.sipSettings.proxy");
                WriteUCFSetting(settingSipRegistrar, out, "mfp.fax.sipSettings.registrar");
                WriteUCFSetting(settingSipUser, out, "mfp.fax.sipSettings.user");
                WriteUCFSetting(settingSipPassword, out, "mfp.fax.sipSettings.password");
                WriteUCFSetting(settingSipContact, out, "mfp.fax.sipSettings.contact");
                WriteUCFSetting(settingSipRealm, out, "mfp.fax.sipSettings.realm");
                WriteUCFSetting(settingSipAuthID, out, "mfp.fax.sipSettings.authID");
                WriteUCFSetting(settingSipOBProxy, out, "mfp.fax.sipSettings.outboundProxy");
                WriteUCFSetting(out, "mfp.fax.sipSettings.disableIncomingCalls",0);
                WriteUCFSetting(out, "mfp.fax.sipSettings.sipDialOutDigit",-1);
                WriteUCFSetting(settingH323Gateway, out, "mfp.fax.h323Settings.gateway");
                WriteUCFSetting(settingH323Gatekeeper, out, "mfp.fax.h323Settings.gatekeeper");
                WriteUCFSetting(settingH323User, out, "mfp.fax.h323Settings.user");
                WriteUCFSetting(settingH323Password, out, "mfp.fax.h323Settings.password");
                WriteUCFSetting(settingH323EnableFastStart, out, "mfp.fax.h323Settings.enableFastStart",(settingH323EnableFastStart.getChecked() ? 1 : 0));
                WriteUCFSetting(settingH323DisableH245, out, "mfp.fax.h323Settings.disableH245Tunneling",(settingH323DisableH245.getChecked() ? 1 : 0));
                WriteUCFSetting(settingH323DisableGKDisc, out, "mfp.fax.h323Settings.disableGatekeeperDiscovery",(settingH323DisableGKDisc.getChecked() ? 1 : 0));
                WriteUCFSetting(out, "mfp.fax.h323Settings.disableIncomingCalls",0);
                WriteUCFSetting(out, "mfp.fax.h323Settings.h323DialOutDigit",-1);
                WriteUCFSetting(settingT38Indicator, out, "mfp.fax.t38Settings.indicatorRedundancy");
                WriteUCFSetting(settingT38LowSpeed, out, "mfp.fax.t38Settings.lowSpeedRedundancy");
                WriteUCFSetting(settingT38HighSpeed, out, "mfp.fax.t38Settings.highSpeedRedundancy");
                WriteUCFSetting(settingT38KeepAlive, out, "mfp.fax.t38Settings.udptlKeepAliveInterval");
                WriteUCFSetting(settingAutomaticRedial, out, "mfp.fax.send.autoRedial");
                WriteUCFSetting(settingRedialFrequency, out, "mfp.fax.send.redialFrequency");
                WriteUCFSetting(settingRingsToAnswer, out, "mfp.fax.receive.ringsToAnswer");

            }

            out.write(ucfFooter);

            out.close();
            WriteToResultAreaNoTime("OK");
            rc = 0;
        }
        catch (IOException e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
        }

        return rc;
    }

    public void WriteForceFaxModeDelayUCFFile() {
        try {
            WriteToResultAreaNoCRLF("UCF Creation for Force Fax Mode Delay Started...");
            File ucfFile = new File(settingUCFFileName.getText());
            BufferedWriter out = new BufferedWriter( new FileWriter(ucfFile));

            String strdata;

            out.write(ucfHeader);

            WriteUCFSetting(settingForceFaxModeDelay, out, "mfp.fax.voipSettings.forceFaxModeDelay");

            out.write(ucfFooter);

            out.close();
            WriteToResultAreaNoTime("OK");

        }
        catch (IOException e) {
            WriteToResultAreaNoTime("Failed");
            WriteToResultArea("" + e);
        }
    }

    public String GetDefaultSettingsFile() {
        return ProjectDirectory+File.separator+"FoIPSettings.txt";
    }

    public void LoadDefaultSettings() {

        settingFaxName.setText(""); settingFaxName.setSelected(false);
        settingFaxNumber.setText(""); settingFaxNumber.setSelected(false);
        settingFaxID.setSelectedIndex(0); settingFaxID.setSelected(false);
        settingFaxMode.setSelectedIndex(0); settingFaxMode.setSelected(true);
        settingFaxTransport.setSelectedIndex(1); settingFaxTransport.setSelected(true);
        settingVoipProtocol.setSelectedIndex(0); settingVoipProtocol.setSelected(true);
        settingTraceLevel.setText("0"); settingTraceLevel.setSelected(true);
        settingStunServer.setText(""); settingStunServer.setSelected(true);
        settingForceFaxMode.setChecked(true); settingForceFaxMode.setSelected(true);
        settingForceFaxModeDelay.setText("7"); settingForceFaxModeDelay.setSelected(true);
        settingT38Indicator.setText("3"); settingT38Indicator.setSelected(true);
        settingT38LowSpeed.setText("3"); settingT38LowSpeed.setSelected(true);
        settingT38HighSpeed.setText("1"); settingT38HighSpeed.setSelected(true);
        settingT38KeepAlive.setText("0"); settingT38KeepAlive.setSelected(true);
        settingSipProxy.setText(""); settingSipProxy.setSelected(true);
        settingSipRegistrar.setText(""); settingSipRegistrar.setSelected(true);
        settingSipUser.setText(""); settingSipUser.setSelected(true);
        settingSipPassword.setText(""); settingSipPassword.setSelected(true);
        settingSipContact.setText(""); settingSipContact.setSelected(true);
        settingSipRealm.setText(""); settingSipRealm.setSelected(true);
        settingSipAuthID.setText(""); settingSipAuthID.setSelected(true);
        settingSipOBProxy.setText(""); settingSipOBProxy.setSelected(true);
        settingRegTransport.setSelectedIndex(0); settingRegTransport.setSelected(true);
        settingIncomingTransport.setSelectedIndex(0); settingIncomingTransport.setSelected(true);
        settingOutgoingTransport.setSelectedIndex(0); settingOutgoingTransport.setSelected(true);
        settingH323Gateway.setText(""); settingH323Gateway.setSelected(true);
        settingH323Gatekeeper.setText(""); settingH323Gatekeeper.setSelected(true);
        settingH323User.setText(""); settingH323User.setSelected(true);
        settingH323Password.setText(""); settingH323Password.setSelected(true);
        settingH323EnableFastStart.setChecked(false); settingH323EnableFastStart.setSelected(true);
        settingH323DisableH245.setChecked(false); settingH323DisableH245.setSelected(true);
        settingH323DisableGKDisc.setChecked(true); settingH323DisableGKDisc.setSelected(true);
        settingAutomaticRedial.setText("5"); settingAutomaticRedial.setSelected(false);
        settingRedialFrequency.setText("3"); settingRedialFrequency.setSelected(false);
        settingRingsToAnswer.setText("3"); settingRingsToAnswer.setSelected(false);
        settingHTTPSServiceURL.setText(""); settingHTTPSServiceURL.setSelected(true);
        settingHTTPSProxy.setText(""); settingHTTPSProxy.setSelected(true);
        settingHTTPSProxyUser.setText(""); settingHTTPSProxyUser.setSelected(true);
        settingHTTPSProxyPassword.setText(""); settingHTTPSProxyPassword.setSelected(true);
        settingHTTPSPeerVerification.setChecked(true); settingHTTPSPeerVerification.setSelected(true);
        settingHTTPSEncryptFaxSend.setSelectedIndex(1); settingHTTPSEncryptFaxSend.setSelected(true);
        settingHTTPSEncryptFaxReceive.setSelectedIndex(1); settingHTTPSEncryptFaxReceive.setSelected(true);


        settingSettingsFileName.setText(GetDefaultSettingsFile());
        settingBundleFileName.setText(ProjectDirectory+File.separator+"FoIPBundle.zip");
        settingUCFFileName.setText(ProjectDirectory+File.separator+"FoIPSettings.ucf");
        settingMFPListFileName.setText(ProjectDirectory+File.separator+"MFPList.csv");
        settingListResultFileName.setText(ProjectDirectory+File.separator+"MFPListResult.csv");
        settingMFP.setText("");
        settingMFPPhone.setText("");
        settingMFPUser.setText("");
        settingMFPPassword.setText("");
        settingLicense.setText("");
        settingTesterMFP.setText("");
        settingTesterPhone.setText("");
        settingTesterUser.setText("");
        settingTesterPassword.setText("");

        WriteToResultArea("Default Settings Loaded.");

    }


    public boolean  LoadSetting(String line) {
        if (!line.substring(0,3).equals("mfp"))
            return false;
        String delim = " ";
        String[] tokens = line.split(delim,2);
        String SettingName = tokens[0];
        String SettingValue = tokens[1].substring(1,tokens[1].length()-1);
        if (SettingName.equals("mfp.fax.stationName")) settingFaxName.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.stationNumber")) settingFaxNumber.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.stationID")) settingFaxID.setSelectedIndex(Integer.parseInt(SettingValue));
        else if (SettingName.equals("mfp.fax.mode")) settingFaxMode.setSelectedIndex(Integer.parseInt(SettingValue));
        else if (SettingName.equals("mfp.fax.voipSettings.faxTransport")) settingFaxTransport.setSelectedIndex(Integer.parseInt(SettingValue));
        else if (SettingName.equals("mfp.fax.voipSettings.voipProtocol")) settingVoipProtocol.setSelectedIndex(Integer.parseInt(SettingValue)-1);
        else if (SettingName.equals("mfp.fax.voipSettings.traceLevel")) settingTraceLevel.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.voipSettings.stunServer")) settingStunServer.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.voipSettings.forceFaxMode")) settingForceFaxMode.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.voipSettings.forceFaxModeDelay")) settingForceFaxModeDelay.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.proxy")) settingSipProxy.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.registrar")) settingSipRegistrar.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.user")) settingSipUser.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.password")) settingSipPassword.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.contact")) settingSipContact.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.realm")) settingSipRealm.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.authID")) settingSipAuthID.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sipSettings.outboundProxy")) settingSipOBProxy.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323Settings.gateway")) settingH323Gateway.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323Settings.gatekeeper")) settingH323Gatekeeper.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323Settings.user")) settingH323User.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323Settings.password")) settingH323User.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323Settings.enableFastStart")) settingH323EnableFastStart.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.h323Settings.disableH245Tunneling")) settingH323DisableH245.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.h323Settings.disableGatekeeperDiscovery")) settingH323DisableGKDisc.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.t38Settings.indicatorRedundancy")) settingT38Indicator.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.t38Settings.lowSpeedRedundancy")) settingT38LowSpeed.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.t38Settings.highSpeedRedundancy")) settingT38HighSpeed.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.t38Settings.udptlKeepAliveInterval")) settingT38KeepAlive.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.send.autoRedial")) settingAutomaticRedial.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.send.redialFrequency")) settingRedialFrequency.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.receive.ringsToAnswer")) settingRingsToAnswer.setText(SettingValue);
        else return false;
        return true;
    }


    public int  LoadSettingVCC(String line) {
        if (!line.substring(0,3).equals("mfp"))
            return -1;
        String delim = " ";
        String[] tokens = line.split(delim,2);
        String SettingName = tokens[0];
        String SettingValue = tokens[1].substring(1,tokens[1].length()-1);
        if (SettingName.equals("mfp.fax.stationName")) settingFaxName.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.stationNumber")) settingFaxNumber.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.stationID")) settingFaxID.setSelectedIndex(Integer.parseInt(SettingValue));
        else if (SettingName.equals("mfp.fax.mode")) settingFaxMode.setSelectedIndex(Integer.parseInt(SettingValue));
        else if (SettingName.equals("mfp.fax.voip.faxTransport")) settingFaxTransport.setSelectedIndex(Integer.parseInt(SettingValue));
        else if (SettingName.equals("mfp.fax.voip.protocol")) settingVoipProtocol.setSelectedIndex(Integer.parseInt(SettingValue)-1);
        else if (SettingName.equals("mfp.fax.voip.traceLevel")) settingTraceLevel.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.voip.stunServer")) settingStunServer.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.voip.forceFaxMode")) settingForceFaxMode.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.voip.forceFaxModeDelay")) settingForceFaxModeDelay.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.proxy")) settingSipProxy.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.registrar")) settingSipRegistrar.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.user")) settingSipUser.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.password")) settingSipPassword.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.contact")) settingSipContact.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.realm")) settingSipRealm.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.authID")) settingSipAuthID.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.sip.outboundProxy")) settingSipOBProxy.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323.gateway")) settingH323Gateway.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323.gatekeeper")) settingH323Gatekeeper.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323.user")) settingH323User.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323.password")) settingH323User.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.h323.enableFastStart")) settingH323EnableFastStart.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.h323.disableH245Tunnel")) settingH323DisableH245.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.h323.disableGatekeeperDiscovery")) settingH323DisableGKDisc.setChecked(SettingValue.equals("true"));
        else if (SettingName.equals("mfp.fax.t38.indicatorRedundancy")) settingT38Indicator.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.t38.lowSpeedRedundancy")) settingT38LowSpeed.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.t38.highSpeedRedundancy")) settingT38HighSpeed.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.t38.udptlKeepAliveInterval")) settingT38KeepAlive.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.send.autoRedial")) settingAutomaticRedial.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.send.redialFrequency")) settingRedialFrequency.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.receive.ringsToAnswer")) settingRingsToAnswer.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.httpsSettings.serviceUrl")) settingHTTPSServiceURL.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.httpsSettings.proxy")) settingHTTPSProxy.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.httpsSettings.proxyUser")) settingHTTPSProxyUser.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.httpsSettings.proxyPassword")) settingHTTPSProxyPassword.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.httpsSettings.enablePeerVerification")) settingHTTPSPeerVerification.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.httpsSettings.encyrptFaxReceive")) settingHTTPSEncryptFaxReceive.setText(SettingValue);
        else if (SettingName.equals("mfp.fax.httpsSettings.encryptFaxSend")) settingHTTPSEncryptFaxSend.setText(SettingValue);
        else return -1;
        return 0;
    }

    JFrame top;
    JFrame top2;
    JPanel basic;
    ProgressBar pbar;
    boolean processingLastReq;
    public final void initUI() {

        int X = 0;
        int Y = 0;

        processingLastReq = false;

        top = new JFrame();
        basic = new JPanel();
        basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));
        top.add(basic);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        new FoIPSetting(settingsPanel, 0, Y++, 3, "Fax Settings");
        arrayList.add(settingFaxName            = new FoIPSetting(settingsPanel, 0, Y++, 1, "Fax Name:",              "", false));
        arrayList.add(settingFaxNumber          = new FoIPSetting(settingsPanel, 0, Y++, 1, "Fax Number:",            "", false));
        arrayList.add(settingFaxID              = new FoIPSetting(settingsPanel, 0, Y++, 1, "Fax ID:",                faxIDStrings,     0, false));
        arrayList.add(settingFaxMode            = new FoIPSetting(settingsPanel, 0, Y++, 1, "Fax Mode:",              faxModeStrings,   0, true));
        arrayList.add(settingFaxTransport       = new FoIPSetting(settingsPanel, 0, Y++, 1, "Fax Transport:",         transportStrings, 1, true));

        Y++; Y++;

        new FoIPSetting(settingsPanel, 0, Y++, 3, "HTTPS Fax Settings");
        arrayList.add(settingHTTPSServiceURL    = new FoIPSetting(settingsPanel, 0, Y++, 1, "Service URL:", "", true));
        arrayList.add(settingHTTPSProxy         = new FoIPSetting(settingsPanel, 0, Y++, 1, "HTTPS Proxy:", "", true));
        arrayList.add(settingHTTPSProxyUser     = new FoIPSetting(settingsPanel, 0, Y++, 1, "HTTPS Proxy User", "", true));
        arrayList.add(settingHTTPSProxyPassword = new FoIPSetting(settingsPanel, 0, Y++, 1, "HTTPS Proxy Password", "", true));
        arrayList.add(settingHTTPSPeerVerification = new FoIPSetting(settingsPanel, 0, Y++, 1, "Enable Peer Verification:", true, true));
        arrayList.add(settingHTTPSEncryptFaxReceive = new FoIPSetting(settingsPanel, 0, Y++, 1, "Receive Encryption:", encryption,1, true));
        arrayList.add(settingHTTPSEncryptFaxSend  = new FoIPSetting(settingsPanel, 0, Y++, 1, "Send Encryption", encryption,1, true));

        Y=0;
        new FoIPSetting(settingsPanel, 3, Y++, 3, "VoIP Settings");
        arrayList.add(settingVoipProtocol       = new FoIPSetting(settingsPanel, 3, Y++, 1, "VoIP Protocol:",         protocolStrings, 0, true));
        arrayList.add(settingTraceLevel         = new FoIPSetting(settingsPanel, 3, Y++, 1, "Trace Level:",           "0", true));
        arrayList.add(settingStunServer         = new FoIPSetting(settingsPanel, 3, Y++, 1, "STUN Server:",           "", true));
        arrayList.add(settingForceFaxMode       = new FoIPSetting(settingsPanel, 3, Y++, 1, "Force Fax Mode:",        true, true));
        arrayList.add(settingForceFaxModeDelay  = new FoIPSetting(settingsPanel, 3, Y++, 1, "Force Fax Mode Delay:",  "7", true));

        new FoIPSetting(settingsPanel, 3, Y++, 3, "SIP Settings");
        arrayList.add(settingSipProxy           = new FoIPSetting(settingsPanel, 3, Y++, 1, "Proxy:",             "", true));
        arrayList.add(settingSipRegistrar       = new FoIPSetting(settingsPanel, 3, Y++, 1, "Registrar:",         "", true));
        arrayList.add(settingSipUser            = new FoIPSetting(settingsPanel, 3, Y++, 1, "User:",              "", true));
        arrayList.add(settingSipPassword        = new FoIPSetting(settingsPanel, 3, Y++, 1, "Password:",          "", true));
        arrayList.add(settingSipAuthID          = new FoIPSetting(settingsPanel, 3, Y++, 1, "Auth ID:",           "", true));
        arrayList.add(settingRegTransport       = new FoIPSetting(settingsPanel, 3, Y++, 1, "Registration Transport:",udptcp,           0, true));
        arrayList.add(settingIncomingTransport  = new FoIPSetting(settingsPanel, 3, Y++, 1, "Incoming Transport:", udpandtcp,           0, true));
        arrayList.add(settingOutgoingTransport  = new FoIPSetting(settingsPanel, 3, Y++, 1, "Outgoing Transport:",    udptcp,           0, true));       arrayList.add(settingSipOBProxy         = new FoIPSetting(settingsPanel, 3, Y++, 1, "Outbound Proxy:",    "", true));
        arrayList.add(settingSipContact         = new FoIPSetting(settingsPanel, 3, Y++, 1, "Contact:",           "", true));
        arrayList.add(settingSipRealm           = new FoIPSetting(settingsPanel, 3, Y++, 1, "Realm:",             "", true));



        Y=0;
        new FoIPSetting(settingsPanel, 6, Y++, 3, "H.323 Settings");
        arrayList.add(settingH323Gateway        = new FoIPSetting(settingsPanel, 6, Y++, 1, "Gateway:",         "", true));
        arrayList.add(settingH323Gatekeeper     = new FoIPSetting(settingsPanel, 6, Y++, 1, "Gatekeeper:",      "", true));
        arrayList.add(settingH323User           = new FoIPSetting(settingsPanel, 6, Y++, 1, "H.323 User:",      "", true));
        arrayList.add(settingH323Password       = new FoIPSetting(settingsPanel, 6, Y++, 1, "H.323 Password:",  "", true));
        arrayList.add(settingH323EnableFastStart= new FoIPSetting(settingsPanel, 6, Y++, 1, "Enable Fast Start:", false, true));
        arrayList.add(settingH323DisableH245    = new FoIPSetting(settingsPanel, 6, Y++, 1, "Disable H.245:", false, true));
        arrayList.add(settingH323DisableGKDisc  = new FoIPSetting(settingsPanel, 6, Y++, 1, "Disable GK Disc:", true, true));

        new FoIPSetting(settingsPanel, 6, Y++, 3, "T.38 Settings");
        arrayList.add(settingT38Indicator       = new FoIPSetting(settingsPanel, 6, Y++, 1, "Ind. Redundancy:",  "3", true));
        arrayList.add(settingT38LowSpeed        = new FoIPSetting(settingsPanel, 6, Y++, 1, "LS Redundancy:",  "3", true));
        arrayList.add(settingT38HighSpeed       = new FoIPSetting(settingsPanel, 6, Y++, 1, "HS Redundancy:",  "1", true));
        arrayList.add(settingT38KeepAlive       = new FoIPSetting(settingsPanel, 6, Y++, 1, "UDPTL Keep Alive:",       "0", true));

        new FoIPSetting(settingsPanel, 6, Y++, 3, "Send/Receive Fax Settings");
        arrayList.add(settingAutomaticRedial    = new FoIPSetting(settingsPanel, 6, Y++, 1, "Automatic Redial:",  "5", false));
        arrayList.add(settingRedialFrequency    = new FoIPSetting(settingsPanel, 6, Y++, 1, "Redial Frequency:",  "3", false));
        arrayList.add(settingRingsToAnswer      = new FoIPSetting(settingsPanel, 6, Y++, 1, "Rings To Answer:",   "3", false));

        Y=24;
        new FoIPSetting(settingsPanel, 0, Y++, 9, "");
        settingSettingsFileName   = new FoIPSetting(settingsPanel, 0, Y++, 2, "Settings File Name:",    ProjectDirectory+File.separator+"FoIPSettings.txt");
        settingBundleFileName     = new FoIPSetting(settingsPanel, 0, Y++, 2, "Bundle File Name:",      ProjectDirectory+File.separator+"FoIPBundle.zip");
        settingUCFFileName        = new FoIPSetting(settingsPanel, 0, Y++, 2, "UCF File Name:",         ProjectDirectory+File.separator+"FoIPSettings.ucf");
        settingMFPListFileName    = new FoIPSetting(settingsPanel, 0, Y++, 2, "MFP List File Name:",    ProjectDirectory+File.separator+"MFPList.csv");
        settingListResultFileName = new FoIPSetting(settingsPanel, 0, Y++, 2, "List Result File Name:", ProjectDirectory+File.separator+"MFPListResult.csv");
        Y=Y-5;
        arrayList.add(settingMFP            = new FoIPSetting(settingsPanel, 3, Y++, 2, "MFP IP Addr or FQDN:",       ""));
        arrayList.add(settingMFPPhone       = new FoIPSetting(settingsPanel, 3, Y++, 2, "MFP Phone Number:",       ""));
        arrayList.add(settingMFPUser        = new FoIPSetting(settingsPanel, 3, Y++, 2, "MFP User:",  ""));
        arrayList.add(settingMFPPassword    = new FoIPSetting(settingsPanel, 3, Y++, 2, "MFP Password:",  ""));
        arrayList.add(settingLicense        = new FoIPSetting(settingsPanel, 3, Y++, 2, "FoIP License File or Dir:", ""));
        Y=Y-5;
        arrayList.add(settingTesterMFP      = new FoIPSetting(settingsPanel, 6, Y++, 1, "Tester MFP IP or FQDN:",       ""));
        arrayList.add(settingTesterPhone    = new FoIPSetting(settingsPanel, 6, Y++, 1, "Tester Phone Number:",       ""));
        arrayList.add(settingTesterUser     = new FoIPSetting(settingsPanel, 6, Y++, 1, "Tester User:",  ""));
        arrayList.add(settingTesterPassword = new FoIPSetting(settingsPanel, 6, Y++, 1, "Tester Password:",  ""));
        //arrayList.add(settingEsfApp         = new FoIPSetting(settingsPanel, 6, Y++, 1, "FoIP eSF App:",       ""));

        Y=30;

        // Result Field -- Text Field
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5,3,3,3);
        JTextArea jtaResultArea = new JTextArea(5,30);
        jtaResultArea.setEditable(false);
        jtaResultArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(jtaResultArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        c.gridx = X++; c.gridy = Y; c.ipadx = 250; c.ipady = 5; c.gridwidth=9; c.gridheight=1; c.weightx=1; c.weighty=1;
        settingsPanel.add(scroll,c);
        X = 0; Y++;

        UI = new UIOutput();
        UI.initialize(jtaResultArea,null,0,1000000);
        UIOutputHandler.getInstance().SetHandler(UI);

        // Add Settings Panel to Basic
        basic.add(settingsPanel);

        pbar = new ProgressBar();
        basic.add(pbar,c);
        pbar.setVisible(false);
        pbar.setBorder(BorderFactory.createLineBorder(Color.black));

        top.setTitle("FoIP Settings Manager -- Project Directory: " + ProjectDirectory);
        top.setSize(new Dimension(1300, 800));
        top.setResizable(true);
        top.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        top.setLocationRelativeTo(null);
        top.setJMenuBar(createMenuBar());
        top.setVisible(true);

        try {
            ProjectDirectory = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home")+File.separator+"FoIPSettingsDefaultProject.txt"))).trim();
         }
        catch (Exception e) {
            WriteToResultArea("Could not read FoIPSettingsDefaultProject.txt: " + e);
        }

        UI.initLog(ProjectDirectory, LogFileName, MAX_LOG_FILES);

        top.setTitle("FoIP Settings Manager -- Project Directory: " + ProjectDirectory);

        WriteToResultArea("ProjectDirectory: " + ProjectDirectory);
        LoadDefaultSettings();

        if (new File(settingSettingsFileName.getText()).exists()) {
            LoadSettingsFile();
        }

        //CookieManager cookieManager=new CookieManager(null,CookiePolicy.ACCEPT_NONE);
        //CookieHandler.setDefault(cookieManager);


        //JFrame testFrame = new JFrame("TestFrame");
        //testFrame.getContentPane().add(pbar, BorderLayout.CENTER);
        //testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //testFrame.pack();
        //testFrame.setVisible(true);

        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new TrustAll() }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        }
        catch (NoSuchAlgorithmException e) {
            WriteToResultArea("" + e);
        }
        catch (KeyManagementException e) {
            WriteToResultArea("" + e);
        }
    }

    private class QuitAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent event) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                FoIPSettingsManager ex = FoIPSettingsManager.getInstance();
                ex.setVisible(true);
            }
        });

    }

}
