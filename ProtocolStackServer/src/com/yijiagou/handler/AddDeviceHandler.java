package com.yijiagou.handler;

import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.pojo.UserAndDevice;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zgl on 17-8-15.
 */
public class AddDeviceHandler extends ChannelHandlerAppender {
    private SJedisPool sJedisPool;
    private static Logger logger =Logger.getLogger(AddDeviceHandler.class.getName());
    public AddDeviceHandler(SJedisPool sJedisPool){
        this.sJedisPool=sJedisPool;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.ADDDEVICE)) {
            String username = (String) jsonObject.get(JsonKeyword.USERNAME);
            String deviceid = (String) jsonObject.get(JsonKeyword.DEVICEID);
            String devicetype = (String) jsonObject.get(JsonKeyword.DEVICETYPE);
            UserAndDevice userAndDevice = new UserAndDevice(username, deviceid, devicetype);
            String a = this.insertUsersDevice(userAndDevice,sJedisPool);
            switch (a) {
                case "1":
                    ctx.writeAndFlush("该设备已经绑定到用户");
                    break;
                case "2":
                    ctx.writeAndFlush("绑定成功");
                    break;
                case "3":
                    ctx.writeAndFlush("绑定失败");
            }
            if (a.equals("1") == false && a.equals("3") == false) {
                String sql = "insert into useranddevice(?,?)";
                this.insertMysql(sql, userAndDevice);
            }
        } else {
            ctx.fireChannelRead(ctx);
        }

    }

    private String insertUsersDevice(UserAndDevice userAndDevice,SJedisPool sJedisPool) {
        Jedis jedis = null;
        int count=0;
        String username=userAndDevice.getUname();
        String deviceid=userAndDevice.getDeviceid();
        String devicetype=userAndDevice.getDevicetype();

        jedis = sJedisPool.getConnection();
        while (true) {
            try {
                if(jedis.hexists(username,deviceid)){
                    return "1";
                }
                jedis.hset(username,deviceid,devicetype);
                sJedisPool.putbackConnection(jedis);
                return "2";
            } catch (Exception e) {
                logger.error(e+"===>insertUserDevice");
                if(count++>=2){
                    return "3";
                }else if (jedis==null){
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        logger.error(e1+"===>insertUserDevice:jedis is null");
                    }
                    continue ;
                }else {
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        logger.error(e1+"===>insertUserDevice:jedis is error");
                    }
                    continue ;
                }
            }
        }
    }

    private int insertMysql(String sql, UserAndDevice userAndDevice) {
        int count=0;
        int a=0;
        while (true) {
            try {
               a= ConnPoolUtil.updata(sql, userAndDevice.getUname(), userAndDevice.getDeviceid(), userAndDevice.getDevicetype());
                return a;
            } catch (Exception e) {
                logger.error(e+"insertMysql");
                if(count++>2){
                    return a;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    logger.error(e1);
                }
                continue;
            }
        }
    }
}
