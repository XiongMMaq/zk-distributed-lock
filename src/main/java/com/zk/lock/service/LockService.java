package com.zk.lock.service;

import com.zk.lock.client.LockWatcher;
import com.zk.lock.client.ZkManager;
import com.zk.lock.constant.Constant;
import com.zk.lock.dis.DistributedLock;

import java.io.UnsupportedEncodingException;

import static com.zk.lock.Application.latch;


/**
 * Created by XiongMM on 2017/11/27.
 */
public class LockService {

    public void doService(){
        try {
            ZkManager zkManager = new ZkManager(Constant.ZK_URL,Constant.ZK_CONNECT_TIMEOUT);
            DistributedLock distributedLock = new DistributedLock(zkManager);
            LockWatcher lockWatcher = new LockWatcher(distributedLock,zkManager);
            distributedLock.setWatcher(lockWatcher);
            if(zkManager.existsPath(Constant.ROOT_DIR) == null){
                zkManager.createPersistNode(Constant.ROOT_DIR,null);
            }
            boolean hasLock = distributedLock.getDistributeLock();
            if(hasLock){
                String data = zkManager.getData(Constant.DATA_DIR,lockWatcher);
                int data1 = Integer.parseInt(data);
                try {
                    zkManager.setData(Constant.DATA_DIR,(data1-1)+"");
                    System.out.println("当前线程："+Thread.currentThread().getName() +" 处理后的值："+(data1-1));
                    //这个地方也得执行一次
                    latch.countDown();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                distributedLock.unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
