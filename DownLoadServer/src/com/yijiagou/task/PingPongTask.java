package com.yijiagou.task;

import com.yijiagou.tools.StreamHandler;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

/**
 * Created by wangwei on 17-8-4.
 */
public class PingPongTask implements Runnable {

    private String id;
    private Map<String,Socket> map;

    public PingPongTask(String id,Map<String,Socket> map) {
        this.id = id;
        this.map = map;
    }

    @Override
    public void run() {
        int j = 0;
        lable:
        while (j <= 2) {//可能断开连接
            Socket socket = map.get(id);
            if (socket != null) {
                try {
                    socket.setSoTimeout(300);
                    Writer out = new OutputStreamWriter(socket.getOutputStream());
                    Reader in = new InputStreamReader(socket.getInputStream());
                    StreamHandler.streamWrite(out, "0110|pin");
                    int i = 0;
                    while (i <= 2) {
                        String data = StreamHandler.streamRead(in);//可能连接超时
                        String[] datas = data.split("\\|");
                        if (datas[0].equals("0110") && datas[1].equals("pon")) {
                            socket.setSoTimeout(0);
                            System.out.println("状态：存活");
                            break lable;
                        }
                        i++;
                    }
                } catch (IOException e) {
                    j++;
                    this.map.put(id, null);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }else {
                j++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
