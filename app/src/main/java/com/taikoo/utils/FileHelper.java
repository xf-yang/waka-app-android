package com.taikoo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    public static void copy(File oldFile, File newFile) {

        try (InputStream in = new FileInputStream(oldFile);
             OutputStream out = new FileOutputStream(newFile)) {

            byte[] arr = new byte[1024];
            int len;
            while ((len = in.read(arr)) != -1) {
                out.write(arr, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getPrefix(File file) {
        String filename = file.getName();
        String filePrefix = filename.substring(0, filename.lastIndexOf("."));
        return filePrefix;
    }

    //读取文件扩展名，包含"."
    public static String getExt(File file) {
        String fileName = file.getName();
        String fileExt = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        return fileExt;
    }


}
