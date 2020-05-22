package com.lexmark.dev.FoIP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LogFile {
    private int MaxLogFiles;
    private String LogDir;
    private String LogFileName;

    public LogFile(String Dir, String File, int MaxFiles) {
        initialize(Dir,File,MaxFiles);
    }

    public void initialize(String Dir, String File, int MaxFiles) {
        LogDir = Dir;
        LogFileName = File;
        MaxLogFiles = MaxFiles;
        NewLogFile();
    }

    void MoveFile(String From, String To) {
        try {
            Path FromPath = Paths.get(LogDir + File.separator + From);
            Path ToPath = Paths.get(LogDir + File.separator + To);
            if(Files.exists(FromPath)) {
                Files.deleteIfExists(ToPath);
                Files.move(FromPath, FromPath.resolveSibling(To));
            }
        }
        catch (IOException e) {
            System.out.println("LogFile: " + e);
        }
    }
    public void NewLogFile() {
        // Keep most recent log files
        for (Integer i=MaxLogFiles-1; i>1; i--) {
            MoveFile("Old" + LogFileName + (i-1) + ".txt","Old" + LogFileName + i + ".txt");
        }
        MoveFile(LogFileName + ".txt","Old" + LogFileName + "1.txt");
        try {
            Files.write(Paths.get(LogDir + File.separator + LogFileName + ".txt"), "FoIP Settings Manager Log File\n\n".getBytes(), StandardOpenOption.CREATE);
        }
        catch (IOException e) {
            System.out.println("LogFile: " + e);
        }
    }
    public void WriteLogData(String data) {
        try {
            Files.write(Paths.get(LogDir + File.separator + LogFileName + ".txt"), data.getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.out.println("LogFile: " + e);
        }
    }

    public int RemoveAllOldLogs() {
        int FilesRemoved = 0;
        File directory = new File(LogDir);
        for (File f: directory.listFiles()) {
            if (f.getName().startsWith("Old" + LogFileName)) {
                f.delete();
                FilesRemoved++;
            }
        }
        return FilesRemoved;
    }
}
