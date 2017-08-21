package com.yijiagou.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yijiagou.pojo.JsonKeyword;
import com.yijiagou.tools.JedisUtils.JedisUtils;
import com.yijiagou.tools.JedisUtils.SJedisPool;
import com.yijiagou.tools.jdbctools.ConnPoolUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangwei on 17-7-29.
 */
public class UploadHandler extends ChannelHandlerAdapter {
    private static Logger logger = Logger.getLogger(UploadHandler.class.getName());
    private SJedisPool sJedisPool;
    private static final String IF = "if";
    private static final String ELIF = "elif";
    private static final String ELSE = "else";
    private static final String WHILE = "while";
    private static final String END = "end";
    private static final String TRUE = "True";
    private static ReentrantLock lock;

    public UploadHandler(SJedisPool sJedisPool){
        this.sJedisPool = sJedisPool;
        lock = new ReentrantLock();
    }

    private static final String retract = "    ";

    public void channelRead(ChannelHandlerContext ctx,Object msg){
        JSONObject jsonObject = (JSONObject)msg;
        try {
            String type = jsonObject.getString(JsonKeyword.TYPE);
            if(type.equals(JsonKeyword.CODE)){
                jsonToCode(jsonObject);
                mysqlAddapp(jsonObject);
                ctx.writeAndFlush("ok\n").addListener(ChannelFutureListener.CLOSE);
                System.out.println("Upload ok!");
            }else {
                ctx.fireChannelRead(msg);
            }
        } catch (JSONException e) {
            ctx.writeAndFlush("error");
            //json解析出错
            e.printStackTrace();
        }
    }

    public void jsonToCode(JSONObject jsonObject){
        try {
            JSONArray code = jsonObject.getJSONArray(JsonKeyword.CODE);
            String info = jsonObject.getString(JsonKeyword.INFO);
            String type = jsonObject.getString(JsonKeyword.TYPE);
            Step step = new Step(0);
            StringBuffer codes = new StringBuffer();
            codes.append("import power\n");
            codes.append(getBlock(step,code,""));
            int count = 0;
            Jedis jedis = sJedisPool.getConnection();
            while (count < 3) {
                try {
                    long number = 0;
                    //保证编号唯一
                    lock.lock();
                    if (jedis.exists(type)) {
                        number = jedis.zcard(type);
                    }
                    jedis.zadd(type, 1, number + "");
                    lock.unlock();



                    //----------------------CDN请求 回复-----------------------------
                    String json = this.getRequestJson(type,number,codes,info);
                    String url = "http://127.0.0.1:8080/upload";
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost httpPost;
                    httpPost = new HttpPost(url);
                    httpPost.setEntity(new StringEntity(json, "utf-8"));
                    httpPost.setHeader("Content-type", "application/json");

                    try {
                        CloseableHttpResponse response = null;
                        String body = "";

                        response = client.execute(httpPost);
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            //按指定编码转换结果实体为String类型
                            body = EntityUtils.toString(entity, "utf-8");
                        }
                        EntityUtils.consume(entity);
                        //释放链接
                        response.close();
                        System.out.println(body);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //----------------------CDN请求 回复-----------------------------


                } catch (JedisConnectionException e) {
                    count++;
                    sJedisPool.repairConnection(jedis);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {

                    }
                }
            }

        } catch (JSONException e) {
            //json解析出错
            e.printStackTrace();
        }
    }

    public StringBuffer getBlock(Step step,JSONArray code,String retract){
        StringBuffer block = new StringBuffer();
        try {
            String retract0 = this.retract;
            retract0 += retract;

            for(int i = step.getStep();i < code.size();i = step.getStep()){
                JSONObject stateBlock = code.getJSONObject(i);
                String statement = stateBlock.getString(JsonKeyword.COUNT);//可能会改
                if(statement.equals(IF) || statement.equals(ELIF)){
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" ");
                    step.addStep();

                    block.append(" power.getTime()");

                    stateBlock = code.getJSONObject(step.getStep());
                    block.append(stateBlock.getString(JsonKeyword.COUNT));//可能会改
                    block.append(" :");
                    step.addStep();//

                    block.append(this.getBlock(step,code,retract0));
                }else if(statement.equals(WHILE)){
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" "+TRUE+" :");
                    step.addStep();
                } else if(statement.equals(ELSE)){
                    block.append("\n");
                    block.append(retract);
                    block.append(statement);
                    block.append(" :");
                    step.addStep();
                    block.append(this.getBlock(step,code,retract0));
                }
                else if(statement.equals(END)){
                    step.addStep();
                    return block;
                }else{
                    block.append("\n");
                    block.append(retract);
                    block.append("power.");
                    block.append(statement);
                    block.append("()");
                    step.addStep();//
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return block;
    }

    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

    private String getRequestJson(String type,long number,StringBuffer codes,String info){
        String json = "{\n" +
                "  \"deviceType\":\""+type+"\",\n" +
                "  \"appId\":\""+number+"\",\n" +
                "  \"app\":\""+codes+"\",\n" +
                "  \"info\":\""+info+"\"\n" +
                "}";
        return json;
    }


    public int mysqlAddapp(JSONObject jsonObject){
        String appid =(String) jsonObject.get(JsonKeyword.APPID);
        String apptype=(String)jsonObject.get(JsonKeyword.DEVICETYPE);
        int a = 0;
        int count = 0;
        while (true) {
            try {
                a= ConnPoolUtil.updata(appid,apptype);
                break;
            } catch (Exception e) {
                logger.error(e + "===>mysqlAddapp");
                if (count++>=2) {
                    return a;
                }
                continue;
            }
        }
        return a;
    }

    class Step{
        private int step;

        public Step(int step){
            this.step = step;
        }

        public void addStep(){
            this.step += 1;
        }

        public int getStep() {
            return step;
        }
    }
}
