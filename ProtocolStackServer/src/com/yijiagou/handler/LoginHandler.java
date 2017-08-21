package com.yijiagou.handler;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import com.yijiagou.tools.JedisUtils.SJedisPool;

import java.io.UnsupportedEncodingException;
/**
 * Created by wangwei on 17-7-28.
 */
public class LoginHandler extends ChannelHandlerAdapter {
    private SJedisPool sJedisPool;

    public LoginHandler(SJedisPool sJedisPool){
        this.sJedisPool = sJedisPool;
    }

    public void channelRead(ChannelHandlerContext ctx,Object msg) throws JSONException, UnsupportedEncodingException {
        JSONObject body = (JSONObject)msg;
        String type = body.getString(JsonKeyword.TYPE);

        if(type.equalsIgnoreCase(JsonKeyword.LOGIN)){
            String username = body.getString(JsonKeyword.USERNAME);
            String passwd = body.getString(JsonKeyword.PASSWORD);
            String can = "false";
            if(canLogin(username,passwd)){
                can = "true";
            }
            ctx.writeAndFlush(can).addListener(ChannelFutureListener.CLOSE);
        }else {
            ctx.fireChannelRead(msg);
        }

    }

    private boolean canLogin(String username,String passwd){
        Jedis jedis = null;
        int count = 0;
        jedis = sJedisPool.getConnection();
        while (count < 3) {
            try{
                String passwd0 = jedis.hget(JsonKeyword.USERS,username);
                if(passwd0.equals(passwd)){
                    sJedisPool.putbackConnection(jedis);
                    return true;
                }
                sJedisPool.putbackConnection(jedis);
                return false;
            }catch(JedisConnectionException e){
                sJedisPool.repairConnection(jedis);
                System.out.println("redis connection down!");
                count ++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {

                }
            }
        }
        return false;
    }

    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();;
        ctx.close();
    }

}
