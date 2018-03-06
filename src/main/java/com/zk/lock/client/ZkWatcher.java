package com.zk.lock.client;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import static com.zk.lock.client.ZkManager.zkThreadSemphere;

/**
 * Created by XiongMM on 2017/11/27.
 * zk的watcher
 */
public class ZkWatcher implements Watcher{

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            //减1
            zkThreadSemphere.countDown();
        }
    }
}
