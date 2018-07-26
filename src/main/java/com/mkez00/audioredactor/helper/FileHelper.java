package com.mkez00.audioredactor.helper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.codec.binary.Base64;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class FileHelper {

    protected static final Log LOG = LogFactory.getLog(FileHelper.class);

    public static File decodeBase64AndWriteToFile(String content) throws IOException{
        byte[] byteContent = decodeBase64(content);
        return convertByteArrayToFile(byteContent);
    }

    public static File convertByteArrayToFile(byte[] byteArray) throws IOException{
        File file = java.io.File.createTempFile("random-file", ".mp3");
        FileUtils.writeByteArrayToFile(file, byteArray);
        return file;
    }

    public static String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String encodeBase64(String filePath)
            throws IOException {

        File file = new File(filePath);
        return encodeBase64(file);
    }

    public static byte[] decodeBase64(String content){
        return Base64.decodeBase64(content);
    }

    public static String encodeBase64(File file) throws IOException{
        byte[] bytes = getBytes(file);
        byte[] encoded = Base64.encodeBase64(bytes);
        String encodedString = new String(encoded);

        return encodedString;
    }

    private static byte[] getBytes(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }

    public static void deleteFiles(String... files){
        if (files!=null){
            for (String toDelete : files){
                try {
                    if (toDelete!=null && !toDelete.isEmpty()){
                        LOG.info("Deleting file at location: " + toDelete);
                        Files.delete(Paths.get(toDelete));
                    }
                } catch (IOException e){
                    LOG.warn("Problem deleting file. ", e);
                }

            }
        }
    }

    public static byte[] getBytes2(File file) {
        byte[] bFile = new byte[(int) file.length()];
        try {
            // convert file into array of bytes
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            return bFile;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSha256(File file) throws IOException{
        byte[] buffer= new byte[8192];
        int count;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file.getAbsoluteFile()));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();

            byte[] hash = digest.digest();
            return new BASE64Encoder().encode(hash);
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }
}
