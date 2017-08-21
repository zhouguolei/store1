package com.yijiagou.task;

import com.yijiagou.tools.StreamHandler;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangwei on 17-8-4.
 */
public class JudgeTask implements Runnable {
    private Socket socket;
    private ExecutorService workerpool;
    private ScheduledExecutorService timepool;
    private Map<String, Socket> map;
    private Map<String, String> sessionMap;

    public JudgeTask(Socket socket, ExecutorService workerpool,
                     ScheduledExecutorService timepool, Map<String, Socket> map, Map<String, String> sessionMap) {
        this.socket = socket;
        this.workerpool = workerpool;
        this.timepool = timepool;
        this.map = map;
        this.sessionMap = sessionMap;
    }

    public void homeDispose(String id, Socket socket) {
        //记录登录的冰箱
        this.map.put(id, socket);
        timepool.scheduleAtFixedRate(new PingPongTask(id, this.map), 1, 1, TimeUnit.MINUTES);
//        timepool.scheduleAtFixedRate(new PingPongTask(id,this.map), 1, 5, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        Writer out = null;
        Reader in = null;
        try {
            out = new OutputStreamWriter(socket.getOutputStream());
            in = new InputStreamReader(socket.getInputStream());
            String conninfo = StreamHandler.streamRead(in);//第一次收到的数据
            String[] infos = conninfo.split("\\|");//将数据拆分用于判断
            if (infos[0].equals("1111")) {//与家电的链接,与家电建立长链接
                String req = "1111|ok\n";//返回的消息
                if (infos[1] == null) {
                    req = "1111|err\n";
                } else {
                    int number = Integer.parseInt(infos[1]);
                    if (infos[2].getBytes().length != number) {
                        req = "1111|err";
                    }
                }
                boolean succe = StreamHandler.streamWrite(out, req);

                //-------------判断家电连接是否成功---------------
                if (succe) {
                    homeDispose(infos[2], socket);
                } else {
                    homeDispose(infos[2], null);
                }
                //-------------判断家电连接是否成功---------------

            } else if (infos[0].equals("0000")) {//与netty的链接,给家电发送下载命令
                String sessionId = infos[1];
                String data = sessionMap.get(sessionId);

                //-------------判断是不是上一个netty会话没有发出去的消息-------------
                if (data != null) {
                    boolean succe = StreamHandler.streamWrite(out, data);
                    if (succe) {
                        sessionMap.remove(sessionId);
                        return;
                    }
                }
                //-------------判断是不是上一个netty会话没有发出去的消息-------------

                int number = Integer.parseInt(infos[2]);
                String id = infos[3];//家电ID
                String type = infos[4];//家电类型
                String aid = infos[5];//appID
                data = "0000|ok\n";
                String path = type+"/"+aid+".py";
                int pathLen = path.length();
                Socket hsocket = this.map.get(id);
                Writer write = null;
                Reader read = null;
                try {
                    if (hsocket != null) {
                        int count = 0;
                        write = new OutputStreamWriter(hsocket.getOutputStream());
                        read = new InputStreamReader(hsocket.getInputStream());
                        while (count < 3) {
                            boolean succe = StreamHandler.streamWrite(write, "0111|lod"+"|"+pathLen+"|"+path);
                            if (!succe) {//发送消息时断开连接,重新获取socket
                                count++;
                                Thread.sleep(200);
                                continue;//产生错误就不向下执行
                            }

                            int count0 = 0;
                            while (count0 < 3){
                                String result = StreamHandler.streamRead(read);//
                                if (result != null) {
                                    String[] info = result.split("\\|");
                                    if (info[0].equals("0111") && info[1].equals("ok")) {
                                        boolean succe0 = StreamHandler.streamWrite(out, data);
                                        if (!succe0) {
                                            sessionMap.put(sessionId, data);
                                        }
                                        return;//成功则返回
                                    } else if (info[0].equals("0111") && info[1].equals("err")) {
                                        StreamHandler.streamWrite(write, "0111|lod" + "|" + pathLen + "|" + path);
                                    }
                                }
                                count0++;
                            }
                            break;
                        }
                    }
                    data = "0000|err\n";
                    boolean succe0 = StreamHandler.streamWrite(out, data);
                    if (!succe0) {
                        sessionMap.put(sessionId, data);
                    }
                } catch (InterruptedException e) {
                    //thread sleep出错
                    e.printStackTrace();
                }finally {
                    StreamHandler.closeReader(in);
                    StreamHandler.closeWriter(out);
                }
            } else {
                out.write("1001|err\n");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
