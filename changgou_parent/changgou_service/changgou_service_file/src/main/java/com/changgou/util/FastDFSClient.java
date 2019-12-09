package com.changgou.util;

import com.changgou.file.pojo.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

/**
 * 文件上传工具类
 * 实现文件上传、下载、删除等功能
 * @author Steven
 * @version 1.0
 * @description com.changgou.util
 * @date 2019-10-29
 */
public class FastDFSClient {

    //完成属性文件读取
    static {
        try {
            //1、获取配置文件路径-filePath = new ClassPathResource("fdfs_client.conf").getPath()
            String filePath = new ClassPathResource("fdfs_client.conf").getPath();
            //2、加载配置文件-ClientGlobal.init(配置文件路径)
            ClientGlobal.init(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取TrackerServer
     * @return
     */
    public static TrackerServer getTrackerServer(){
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();
            return trackerServer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建StorageClient
     * @return
     */
    public static StorageClient getStorageClient(){
        //5、创建一个StorageClient对象，直接new一个，需要两个参数TrackerServer对象、null
        StorageClient storageClient = new StorageClient(getTrackerServer(),null);
        return storageClient;
    }

    /**
     * 文件上传
     * @param fastDFSFile
     */
    public static String[] upload(FastDFSFile fastDFSFile){
        try {
            //文件扩展信息
            NameValuePair[] meta_list = new NameValuePair[1];
            meta_list[0] = new NameValuePair("author",fastDFSFile.getAuthor());
            //upload_file(上传文件流,后缀名,文件扩展信息)
            String[] uploadFile = getStorageClient().upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
            return uploadFile;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取文件信息
     * @param group_name 组名
     * @param remote_filename 文件fileId
     */
    public static FileInfo getFileInfo(String group_name,String remote_filename){
        try {
            FileInfo file_info = getStorageClient().get_file_info(group_name, remote_filename);
            return file_info;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件下载
     * @param group_name 组名
     * @param remote_filename 文件fileId
     */
    public static InputStream downloadFile(String group_name,String remote_filename){
        try {
            return new ByteArrayInputStream(getStorageClient().download_file(group_name,remote_filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    /**
     * 删除文件
     * @param group_name 组名
     * @param remote_filename 文件fileId
     */
    public static void deleteFile(String group_name,String remote_filename){
        try {
            getStorageClient().delete_file(group_name, remote_filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取StorageServer
     * @param groupName 组名
     * @return
     */
    public static StorageServer getStorageServern(String groupName){
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();

            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer, groupName);
            return storageServer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取StorageServer
     * @param groupName 组名
     * @param filename 文件id
     * @return
     */
    public static ServerInfo[] getServerInfo(String groupName,String filename){
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();

            ServerInfo[] serverInfos = trackerClient.getFetchStorages(trackerServer, groupName, filename);
            return serverInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取Tracker服务器地址与http端口
     * @return
     */
    public static String getTrackerUrl(){
        String url = "";
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();

            String hostString = trackerServer.getInetSocketAddress().getHostString();
            //拼接tracker请求Http的地址与端口
            url = "http://" + hostString + ":" + ClientGlobal.getG_tracker_http_port() + "/";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }


    public static void main(String[] args) {
        //测试简单文件上传
        /*try {
            String[] uploadFile = getStorageClient().upload_file("D:/WebWork/abc.jpg", "jpg", null);
            for (String s : uploadFile) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }*/

        //测试获取文件信息
        /*FileInfo fileInfo = getFileInfo("group1", "M00/00/00/wKjThF239qWARqXsAAJv9wZ18RI766.jpg");
        System.out.println(fileInfo);*/

        //测试文件下载
        /*try {
            //文件下载-测试
            InputStream is = downloadFile("group1", "M00/00/00/wKjThF239qWARqXsAAJv9wZ18RI766.jpg");
            //定义输出流对象
            OutputStream out = new FileOutputStream("D:/1.jpg");
            //定义缓冲区
            byte[] buff = new byte[1024];
            //读取输入流
            while ((is.read(buff) > -1)) {
                out.write(buff);
            }
            //释放资源
            out.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //测试文件删除
        //deleteFile("group1", "M00/00/00/wKjThF239qWARqXsAAJv9wZ18RI766.jpg");


        //获取storageServer服务器信息
        /*StorageServer storageServer = getStorageServern("group1");
        System.out.println("当前组服务的下标：" + storageServer.getStorePathIndex());
        System.out.println("服务器地址：" + storageServer.getInetSocketAddress());*/

        /*ServerInfo[] infos = getServerInfo("group1", "M00/00/00/wKjThF238zWAF_rkAAcHN3pW-Rw102.jpg");
        for (ServerInfo info : infos) {
            System.out.println(info.getIpAddr() + ":" + info.getPort());
        }*/


        //测试tracker的服务器地址与http端口
        System.out.println(getTrackerUrl());
    }


}
