package com.zk.lock;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zk.lock.client.ZkManager;
import com.zk.lock.constant.Constant;
import com.zk.lock.service.LockService;

import java.util.concurrent.*;

/**
 * Created by XiongMM on 2017/11/27.
 * //不推荐这么使用
 * //        for(int i=0;i<THREAD_NUM;i++){
 //            final int threadId = i;
 //            new Thread(new Runnable() {
 //                @Override
 //                public void run() {
 //                    new LockService().doService(new DoTemplate() {
 //                        @Override
 //                        public void doSomeThing() {
 //                            System.out.println("线程 "+threadId+" 完成工作～");
 //                        }
 //                    });
 //                }
 //            }).start();
 //        }

 */

public class Application {

    private static final int THREAD_NUM=10;
    public static CountDownLatch latch = new CountDownLatch(THREAD_NUM);

    public static void main(String[] args) throws Exception {
        ZkManager zkManager = new ZkManager(Constant.ZK_URL,Constant.ZK_CONNECT_TIMEOUT);
        if(zkManager.existsPath(Constant.DATA_DIR) == null) {
            zkManager.createPersistNode(Constant.DATA_DIR, "10".getBytes("UTF-8"));
        }else{
            zkManager.setData(Constant.DATA_DIR, "10");
        }
        //阿里推荐使用的线程池框架方式，不建议使用Excutors框架
        ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(THREAD_NUM,200,0L,TimeUnit.MILLISECONDS
        ,new LinkedBlockingDeque<Runnable>(1024),nameThreadFactory,new ThreadPoolExecutor.AbortPolicy());
        for(int i=0;i<THREAD_NUM;i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    new LockService().doService();
                    //latch.countDown();
                }
            });
        }
        //阿里推荐的调度框架
        //ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("demo-schedule-thread").build());
        try {
            latch.await();
            System.out.println("所有线程执行结束～");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            pool.shutdown();
        }
    }
}
