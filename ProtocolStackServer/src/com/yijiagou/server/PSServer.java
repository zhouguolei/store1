package com.yijiagou.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import com.yijiagou.tools.JedisUtils.SJedisPool;

/**
 * Created by wangwei on 17-7-28.
 */
public class PSServer {
    private int port;
    private SJedisPool sJedisPool;

    public PSServer(){
        try {
            this.sJedisPool = new SJedisPool(200,100,"127.0.0.1",6379);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PSServer bind(int port){
        this.port = port;
        return this;
    }

    public void run(){
        EventLoopGroup bossgroup = new NioEventLoopGroup();
        EventLoopGroup workgroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();

            server.group(bossgroup, workgroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializerImp(sJedisPool));

            ChannelFuture future = server.bind(port).sync();

            future.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            bossgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }
    }

}
