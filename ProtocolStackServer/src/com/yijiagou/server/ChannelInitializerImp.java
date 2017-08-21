package com.yijiagou.server;

import com.yijiagou.handler.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import com.yijiagou.tools.JedisUtils.SJedisPool;

/**
 * Created by wangwei on 17-8-19.
 */
public class ChannelInitializerImp extends ChannelInitializer<NioSocketChannel> {
    private SJedisPool sJedisPool;

    public ChannelInitializerImp(SJedisPool sJedisPool){
        this.sJedisPool = sJedisPool;
    }
    @Override
    protected void initChannel(NioSocketChannel channel) throws Exception {
        channel.pipeline().addLast(new HttpRequestDecoder());//inbound
        channel.pipeline().addLast(new HttpObjectAggregator(65536));//inbound
        channel.pipeline().addLast(new HttpResponseEncoder());//outbound
        channel.pipeline().addLast(new HttpHeadHandler());//outbound
        channel.pipeline().addLast(new HttpContentHandler());//inbound
        channel.pipeline().addLast(new RegisterHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new LoginHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new UploadHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new DownLoadHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new ShowAppStoreHandler(sJedisPool));//inbound outbound
        channel.pipeline().addLast(new AddDeviceHandler(sJedisPool));
        channel.pipeline().addLast(new GetUserDeviceHandler(sJedisPool));
    }
}
