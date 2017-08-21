package com.yijiagou.tools.JedisUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangwei on 17-8-17.
 */
public class SJedisPool {
    String host;
    int port;
    private BlockingQueue<Jedis> workpool;
    private Map<Jedis,Boolean> work;
    private Set<Jedis> busy;
    private Jedis[] testpool;
    private int workMinNum;
    private int workMaxNum;
    private AtomicInteger count;
    private ReentrantLock lock;
    private ReentrantLock fixlock;

    public SJedisPool(int workMaxNum, int workMinNum, String host, int port) throws Exception {
        if (workMinNum > workMaxNum) {
            workMinNum = workMaxNum;
        }
        this.workpool = new ArrayBlockingQueue<Jedis>(workMaxNum);
        this.work = new ConcurrentHashMap<>();
        this.busy = new CopyOnWriteArraySet<Jedis>();
        this.testpool = new Jedis[3];
        this.workMinNum = workMinNum;
        this.workMaxNum = workMaxNum;
        this.count = new AtomicInteger(workMinNum);
        this.lock = new ReentrantLock();
        this.host = host;
        this.port = port;
        for (int i = 0; i < workMinNum; i++) {
            Jedis jedis = new Jedis(host, port);
            try {
                jedis.connect();
            }catch (JedisConnectionException e){
                System.out.println("connection open fail!");
            }
            this.workpool.put(jedis);
            this.work.put(jedis,false);
        }
        for(int i = 0;i < 3;i++){
            testpool[i] = new Jedis(host,port);
            try {
                testpool[i].connect();
            }catch (JedisConnectionException e){
                System.out.println("connection open fail!");
            }
        }
        while (true) {
            new Thread(new ConnectionFixer()).start();
        }
    }

    public Jedis getConnection() throws JedisConnectionException{
        Jedis jedis = null;
        jedis = this.workpool.poll();

        //若无可用连接尝试创建新连接
        if (jedis == null) {
            lock.lock();
            if ((this.count.get()) < workMaxNum) {
                jedis = newConnection();
            }
            lock.unlock();
            //若没办法创建新的连接(连接超过最大数)则阻塞获取连接直到获取为止
            if (jedis == null) {
                try {
                    jedis = this.workpool.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.busy.add(jedis);

        fixlock.lock();
        this.work.put(jedis,true);
        fixlock.unlock();

        return jedis;
    }

    private Jedis newConnection() {
        Jedis jedis = new Jedis(this.host,this.port);
        try {
            jedis.connect();
        }catch (JedisConnectionException e){
            e.printStackTrace();
        }
        this.count.addAndGet(1);
        return jedis;
    }

    public void putbackConnection(Jedis jedis) {
        if (busy.remove(jedis)) {
            work.put(jedis,false);
            workpool.add(jedis);
        }
    }

    //当连接坏掉的时候调用这个方法修复坏掉的连接
    public void repairConnection(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.disconnect();
            } catch (JedisConnectionException e) {
                System.out.println("connection close fail!");
            }
            try {
                jedis.connect();
            } catch (JedisConnectionException e) {
                System.out.println("connection open fail!");
            }
        }
    }

    class ConnectionFixer implements Runnable{

        @Override
        public void run() {//10秒一次存活连接判断
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int count = 0;
            for(int i = 0;i < 3;i++){
                try {
                    testpool[i].ping();//ping包发不过去会报异常
                    count++;
                }catch (JedisConnectionException e){
                    repairConnection(testpool[i]);
                }
            }
            if(count >= 2){
                for (Jedis jedis:work.keySet()){
                    fixlock.lock();
                    if(!work.get(jedis)){
                        try {
                            jedis.ping();
                        }catch (JedisConnectionException e){
                            repairConnection(jedis);
                        }
                    }
                    fixlock.unlock();
                }
            }
        }

    }

}
