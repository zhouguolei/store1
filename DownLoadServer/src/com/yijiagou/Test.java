package com.yijiagou;

import com.yijiagou.server.Server;

import java.io.IOException;

/**
 * Created by wangwei on 17-7-29.
 */
public class Test {
    public static void main(String[] args){
        try {
            Server server = new Server();
            server.bind(9999);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
