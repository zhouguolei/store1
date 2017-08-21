package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.pojo.UserAndDevice;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;

/**
 * Created by zgl on 17-8-15.
 */
public class GetUserDeviceHandler extends ChannelHandlerAppender {
    private static Logger logger = Logger.getLogger(GetUserDeviceHandler.class.getName());
    private SJedisPool sJedisPool;

    public GetUserDeviceHandler(SJedisPool sJedisPool) {
        this.sJedisPool = sJedisPool;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject jsonObject = (JSONObject) msg;
        String actiontype = (String) jsonObject.get(JsonKeyword.TYPE);
        if (actiontype.equals(JsonKeyword.GETDEVICE)) {
            String uname = (String) jsonObject.get(JsonKeyword.USERNAME);
            String devicetype = (String) jsonObject.get(JsonKeyword.DEVICETYPE);
            JSONArray jsonArray = this.getUserdevices(uname, devicetype, sJedisPool);
            ctx.writeAndFlush(jsonArray);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private JSONArray getUserdevices(String uname, String devicetype, SJedisPool sJedisPool) {
        Jedis jedis = null;
        JSONArray jsonArray = new JSONArray();
        String json = null;
        Map<String, String> device = null;
        int count = 0;
        jedis = sJedisPool.getConnection();
        while (true) {
            try {
                device = jedis.hgetAll(uname);
                break;
            } catch (JedisConnectionException e) {
                logger.error(e + "===>getUserdevices");
                if (count++ >= 3) {
                    return null;
                } else if (jedis == null) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                } else {
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
            }
        }
        for (String str : device.keySet()) {
            if (device.get(str).equals(devicetype)) {
                UserAndDevice userAndDevice = new UserAndDevice(str);
                json = JSON.toJSONString(userAndDevice);
                jsonArray.add(json);
            }
        }

        return jsonArray;

    }
}
