package com.yijiagou.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.UnsupportedEncodingException;

/**
 * Created by wangwei on 17-7-29.
 */
public class HttpContentHandler extends ChannelHandlerAdapter {//拆的剩包体

    public void channelRead(ChannelHandlerContext ctx,Object msg) throws UnsupportedEncodingException {
        FullHttpRequest request = (FullHttpRequest)msg;
        ByteBuf body = request.content();
        byte[] bytes = new byte[body.readableBytes()];
        body.readBytes(bytes);
        String content = new String(bytes,"UTF-8");
        JSONObject jsonObject = JSON.parseObject(content);
        ctx.fireChannelRead(jsonObject);
    }

    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
