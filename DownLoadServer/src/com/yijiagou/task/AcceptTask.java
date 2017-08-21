package com.yijiagou.task;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by wangwei on 17-8-4.
 */
public class AcceptTask implements Runnable {
    private ServerSocket serverSocket;
    private ExecutorService workerpool;
    private ScheduledExecutorService timepool;
    private Map<String, Socket> map;
    private Map<String,String> sessionMap;

    public AcceptTask(ServerSocket serverSocket, ExecutorService workerpool,
                      ScheduledExecutorService timepool, Map<String, Socket> map,Map<String,String> sessionMap) {
        this.serverSocket = serverSocket;
        this.workerpool = workerpool;
        this.timepool = timepool;
        this.map = map;
        this.sessionMap = sessionMap;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                this.workerpool.execute(new JudgeTask(socket, workerpool, timepool, map,sessionMap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
