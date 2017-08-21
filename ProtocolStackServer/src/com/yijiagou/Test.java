package com.yijiagou;

import com.yijiagou.server.PSServer;

/**
 * Created by wangwei on 17-7-28.
 */
public class Test {
    public static void main(String[] args){
        PSServer psServer = new PSServer();
        psServer.bind(8989);
        psServer.run();
    }
}
