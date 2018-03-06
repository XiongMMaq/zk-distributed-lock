package com.zk.lock.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by XiongMM on 2017/11/27.
 * zk管理类
 */
@Slf4j
public class ZkManager {

    public static CountDownLatch zkThreadSemphere = new CountDownLatch(1);

    private ZooKeeper zooKeeper;

    public ZkManager(String zkUrl,int timeout){
        try {
            this.zooKeeper = new ZooKeeper(zkUrl, timeout,new ZkWatcher());
            zkThreadSemphere.await();
            System.out.println(Thread.currentThread().getName() + " ZkManager zookeeper connect success ～");
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    /**
     * 关闭
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        this.zooKeeper.close();
    }

    /**
     * path是否存在
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat existsPath(String path) throws KeeperException, InterruptedException {
        return this.zooKeeper.exists(path,false);
    }

    /**
     * 创建持久根节点
     * @param path
     * @param data
     * @return
     * @throws Exception
     */
    public boolean createPersistNode( String path,byte[] data ) throws Exception {
        try {
            if(this.zooKeeper.exists(path,false) == null){
                this.zooKeeper.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                System.out.println("ZkManager create persist node for path "+path);
            }
            return true;
        } catch ( KeeperException.NodeExistsException e ) {
            return false;
        }
    }

    /**
     * 创建内存节点，自增的方式，并返回创建好的节点
     * @param path
     * @param data
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String createEphemeralNode(String path,byte[] data) throws Exception{
        String createPath = "";
        try {
            if(this.zooKeeper.exists(path,false) == null){
                createPath = this.zooKeeper.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                System.out.println("ZkManager create ephemeral node for path "+path);
            }
            return createPath;
        } catch ( KeeperException.NodeExistsException e ) {
            return "";
        }
    }

    /**
     * 获取当前路径下的子节点
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        return this.zooKeeper.getChildren(path,false);
    }

    /**
     * 获取当前节点下的数据
     * @param path
     * @param watcher
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
        byte[] b = this.zooKeeper.getData(path,watcher,new Stat());
        if(b == null){
            return null;
        }
        return new String(b);
    }

    /**
     * 设置值
     * @param path
     * @param data
     * @throws KeeperException
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    public void setData(String path, String data) throws KeeperException, InterruptedException, UnsupportedEncodingException {
       this.zooKeeper.setData(path,data.getBytes("UTF-8"),-1);//-1则忽略版本检查
    }
    /**
     * 删除节点
     * @param selfpath
     */
    public void deleteNode(String selfpath) {
        try {
            if(existsPath(selfpath) == null){
                System.out.println("要删除的节点 "+selfpath+" 已经不存在了～");
                return;
            }
            this.zooKeeper.delete(selfpath,-1);
            System.out.println("成功的删除了节点 "+selfpath+" ~");
            //当前线程就可以关闭链接了
            this.zooKeeper.close();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

