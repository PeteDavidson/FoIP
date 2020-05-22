package com.lexmark.dev.FoIP.LexmarkFoIPSettingsMgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
class CreateZipOfDir {

    CreateZipOfDir(String Dir, String OutZipFile) {
        try {
            FileOutputStream fout = new FileOutputStream(OutZipFile);
            ZipOutputStream zout = new ZipOutputStream(fout);
            File fileSource = new File(Dir);
            addDirectory(zout,fileSource, OutZipFile);
            zout.close();
        }
        catch (IOException e) {
            FoIPSettingsManager.getInstance().WriteToResultArea("" + e);
        }
    }

    void addDirectory(ZipOutputStream zout, File fileSource, String OutZipFile) {
        File[] files = fileSource.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].getPath().equals(OutZipFile))
                continue;
            if (files[i].isDirectory()) {
                addDirectory(zout, files[i], OutZipFile);
                continue;
            }
            try {
                byte[] buffer = new byte[1024];
                FileInputStream fin = new FileInputStream(files[i]);
                zout.putNextEntry(new ZipEntry(files[i].getName()));
                int length;
                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }
                zout.closeEntry();
                fin.close();
            }
            catch (IOException e) {
                FoIPSettingsManager.getInstance().WriteToResultArea("" + e);
            }
        }
    }
}

