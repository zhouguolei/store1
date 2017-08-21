package com.yijiagou.tools;

import java.io.*;

/**
 * Created by wangwei on 17-8-20.
 */
public class StreamHandler {
    public static boolean streamWrite(Writer writer,String data){
        try {
            writer.write(data);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String streamRead(Reader reader){
        BufferedReader br = new BufferedReader(reader);
        try {
            String data = br.readLine();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean streamWrite(OutputStream out,byte[] bytes){
        try {
            out.write(bytes);
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void streamRead(InputStream in,byte[] bytes,int start,int end){
        try {
            in.read(bytes,start,end);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void colseOutputStream(OutputStream out){
        if(out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeInputStream(InputStream in){
        if(in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeWriter(Writer writer){
        if(writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeReader(Reader reader){
        if(reader != null){
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
