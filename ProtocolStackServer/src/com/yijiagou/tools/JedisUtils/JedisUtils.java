package com.yijiagou.tools.JedisUtils;

import redis.clients.jedis.Jedis;

/**
 * Created by zgl on 17-7-29.
 */
public class JedisUtils {
    private static final String SERVER_ADDRESS = "master";
    private static final Integer SERVER_PORT = 6379;
    private static Jedis jedis;

    public static Jedis getconnect(){
        jedis = new Jedis(SERVER_ADDRESS,SERVER_PORT);
        return jedis;
    }

    public static void disconnect(){
        jedis.disconnect();
    }
}
