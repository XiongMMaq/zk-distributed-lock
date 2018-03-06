package com.zk.lock.dis;

import com.zk.lock.client.ZkManager;
import com.zk.lock.constant.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.Collections;
import java.util.List;

/**
 * Created by XiongMM on 2017/11/27.
 * 分布式锁实现
 */

public class DistributedLock {

    private String selfpath;

    private String waitpath;

    private ZkManager zkManager;

    private String LOG_CURRENT_THREAD = Thread.currentThread().getName();

    private Watcher watcher;

    public DistributedLock(ZkManager zkManager){
        this.zkManager = zkManager;
    }

    /**
     * 获取锁
     * @return
     * @throws Exception
     */
    public boolean getDistributeLock() throws Exception {
        selfpath = zkManager.createEphemeralNode(Constant.SUB_DIR,null);
        //如果当前路径是最小路径，则获取锁
        if(checkMinPath()){
            return true;
        }
        return false;
    }

    /**
     * 判断selfpath是否为最小
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public boolean checkMinPath() throws KeeperException, InterruptedException {
        List<String> list = zkManager.getChildren(Constant.ROOT_DIR);
        Collections.sort(list);
        //返回第一次出现的下标位置
        int index = list.indexOf(selfpath.substring(Constant.ROOT_DIR.length()+1));
        switch (index){
            case -1 : {
                System.out.println(selfpath + " 节点已经不存在了～");
                return false;
            }
            case 0 : {
                System.out.println(selfpath +" 我是最小的那个节点，现在该我操作了～");
                return true;
            }
            default:{
                //不是第一个，则取出，selfpath前面的一个
                this.waitpath = Constant.ROOT_DIR + "/" + list.get(index - 1);
                System.out.println(selfpath + " 节点不是第一个，排在节点 "+waitpath+" 的后面～");
                try {
                    zkManager.getData(waitpath,this.watcher);
                    return false;
                }catch (Exception ex){
                    //如果前面的节点不存在
                    if(zkManager.existsPath(waitpath) == null){
                        System.out.println(""+waitpath+" 节点不存在～");
                        return checkMinPath();
                    }else{
                        throw ex;
                    }
                }
            }
        }
    }


    /**
     * 释放锁
     */
    public void unlock() {
        zkManager.deleteNode(this.selfpath);
    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
    }

    public String getWaitPath() {
        return waitpath;
    }


}
