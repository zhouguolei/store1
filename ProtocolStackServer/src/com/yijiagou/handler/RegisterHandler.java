package com.yijiagou.handler;

import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zgl on 17-7-28.
 */
//  String string ="{\"type\":\"register\",\"username\":\"xxxxx\",\"passwd\":\"xxxxxx\",\"phone\":\"21865165\"}";
public class RegisterHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;

    public RegisterHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    private static Logger logger = Logger.getLogger(RegisterHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        if (jsonObject.get(JsonKeyword.TYPE).equals(JsonKeyword.REGIST)) {
            String username = (String) jsonObject.get(JsonKeyword.USERNAME);
            String passwd = (String) jsonObject.get(JsonKeyword.PASSWORD);
            String state = jedisLpush(username, passwd, sJedisPool);
            switch (state) {
                case "1":
                    ctx.writeAndFlush("账户存在");
                    break;
                case "2":
                    ctx.writeAndFlush("注册失败");
                case "3":
                    ctx.writeAndFlush("注册成功");
            }
            if (state.equals("1") == false && state.equals("2") == false) {
                String sql = "insert into userinfo values(?,?)";
                mysqlAdd(sql, username, passwd);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private String jedisLpush(String username, String passwd, SJedisPool sJedisPool) {
        Jedis jedis = null;
        int count = 0;
        jedis = sJedisPool.getConnection();
        lable:
        while (true) {
            try {
                if (jedis.hexists(JsonKeyword.USERS, username)) {
                    return "1";
                }
                jedis.hset(JsonKeyword.USERS, username, passwd);
                sJedisPool.putbackConnection(jedis);
                break;
            } catch (Exception e) {
                logger.error(e + "===>jedisLpush");
                if (count++ >=2) {
                    return "2";
                } else if (jedis == null) {
                    continue;
                } else {
                    sJedisPool.repairConnection(jedis);
                    continue;
                }
            }
        }
        return "3";
    }

    private int mysqlAdd(String sql, String username, String passwd) {
        int a = 0;
        int count = 0;
        while (true) {
            try {
                a= ConnPoolUtil.updata(sql, username, passwd);
                break;
            } catch (Exception e) {
                logger.error(e + "===>msqlAdd");
                if (count++>=2) {
                    return a;
                }
                continue;
            }
        }
        return a;
    }
}
