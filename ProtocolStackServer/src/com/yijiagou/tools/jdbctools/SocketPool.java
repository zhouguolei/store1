package com.yijiagou.tools.jdbctools;

import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zgl on 17-8-18.
 */
public class SocketPool {
    private String ip;
    private int port;
    private int initSocket;
    private int MaxSocket;
    private int MinSocket;
    private int requestSocket;
    private int createcount = 0;
    private Vector<Socket> freevector;
    private Vector<Socket> busyvector;
    private ReentrantLock lock;
    private ReentrantLock lockstatus;

//    static {
//        ip = null;
//        port = 0;
//        MinSocket = 500;
//        initSocket = 0;
//        MaxSocket = 0;
//        requestSocket = 0;
//    }


    {
        ip = null;
        port = 0;
        MinSocket = 500;
        initSocket = 0;
        MaxSocket = 0;
        requestSocket = 0;
    }

    public SocketPool(String ip, int port, int initSocket, int maxSocket, int minSocket, int requestSocket) {
        this.ip = ip;
        this.port = port;
        this.initSocket = initSocket;
        MaxSocket = maxSocket;
        MinSocket = minSocket;
        this.requestSocket = requestSocket;
        this.freevector = new Vector<>();
        this.busyvector = new Vector<>();
    }

    public SocketPool() throws Exception {

        initSocketPool(initSocket);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getInitSocket() {
        return initSocket;
    }

    public void setInitSocket(int initSocket) {
        this.initSocket = initSocket;
    }

    public int getMaxSocket() {
        return MaxSocket;
    }

    public void setMaxSocket(int maxSocket) {
        MaxSocket = maxSocket;
    }

    public int getMinSocket() {
        return MinSocket;
    }

    public void setMinSocket(int minSocket) {
        MinSocket = minSocket;
    }

    public int getRequestSocket() {
        return requestSocket;
    }

    public void setRequestSocket(int requestSocket) {
        this.requestSocket = requestSocket;
    }

    public Vector<Socket> getFreevector() {
        return freevector;
    }

    public void setFreevector(Vector<Socket> freevector) {
        this.freevector = freevector;
    }

    public Vector<Socket> getBusyvector() {
        return busyvector;
    }

    public void setBusyvector(Vector<Socket> busyvector) {
        this.busyvector = busyvector;
    }

    public void initSocketPool(int initSocket) throws Exception {
        if (MinSocket > MaxSocket) {
            throw new Exception("MimSocket can not big than MaxSocket");
        }
        if (initSocket < MaxSocket && initSocket > MinSocket) {
            for (int i = 0; i < initSocket; i++) {
                lock.lock();
                createSocket();
                lock.unlock();
            }
        } else {
            //打印日志
            throw new Exception("initSocket fail");
        }
    }

    public Socket createSocket() {
        createcount++;
        Socket socket = null;
        if ((createcount + MinSocket) <= MaxSocket) {
            try {
                socket = new Socket(ip, port);
            } catch (IOException e) {
                return null;
            }
            freevector.add(socket);

            return socket;
        }else {
            return null;
        }


    }

    public Socket findSocket() {
        Socket socket = null;
        for (int i = 0; i <= freevector.size(); i++) {
            socket = freevector.get(i);
            if (freevector.size() >= MinSocket) {
                freevector.remove(socket);
                busyvector.add(socket);
                break;
            } else {
                return null;
            }
        }
        return socket;
    }

    public Socket getSocket() {
        if (freevector.size() > MinSocket) {
            Socket socket = findSocket();
            return socket;
        } else {
            requestSocket(requestSocket);
            Socket socket = findSocket();
            return socket;
        }
    }

    public void requestSocket(int num) {
        if ((busyvector.size() + freevector.size() + num) < MaxSocket) {
            for (int i = 0; i < num; i++) {
                lock.lock();
                Socket socket = createSocket();
                lock.unlock();
                freevector.add(socket);
            }
        }
    }

    public void busyReturn(Socket socket) {
        busyvector.remove(socket);
        freevector.add(socket);
    }
}

