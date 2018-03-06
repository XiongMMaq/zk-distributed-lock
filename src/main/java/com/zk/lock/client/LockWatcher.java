package com.zk.lock.client;

import com.zk.lock.constant.Constant;
import com.zk.lock.dis.DistributedLock;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.UnsupportedEncodingException;

import static com.zk.lock.Application.latch;


/**
 * Created by XiongMM on 2017/11/27.
 */

public class LockWatcher implements Watcher{

    private ZkManager zkManager;

    private DistributedLock distributedLock;


    public LockWatcher(DistributedLock distributedLock,ZkManager zkManager){
        this.distributedLock = distributedLock;
        this.zkManager = zkManager;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        //waitpath被删除了
        if(watchedEvent.getType() == Event.EventType.NodeDeleted && watchedEvent.getPath().equals(distributedLock.getWaitPath())){
            try {
                //检查自己是不是最小的节点了
                if(distributedLock.checkMinPath()){
                    System.out.println("前面一个节点被删除，赶紧干活～");
                    //doTemplate.doSomeThing();
                    String data = zkManager.getData(Constant.DATA_DIR,this);
                    int data1 = Integer.parseInt(data);
                    try {
                        zkManager.setData(Constant.DATA_DIR,(data1-1)+"");
                        System.out.println("当前线程："+Thread.currentThread().getName() +" 处理后的值："+(data1-1));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    distributedLock.unlock();
                    latch.countDown();
                    //System.out.println("当前线程："+Thread.currentThread().getName() +" latch的值："+latch.getCount());
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
