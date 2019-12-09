package com.changgou.controller;

import com.changgou.file.pojo.FastDFSFile;
import com.changgou.util.FastDFSClient;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.controller
 * @date 2019-10-29
 */
@RestController
@CrossOrigin
public class FileController {

    //文件上传
    @RequestMapping("upload")
    public Result<String> upload(MultipartFile file){
        try {
            //包装文件上传的对象
            FastDFSFile dfsFile = new FastDFSFile(
                    file.getOriginalFilename(),  //原来的文件名
                    file.getBytes(),  //字节流
                    StringUtils.getFilenameExtension(file.getOriginalFilename())  //后缀名
            );
            //文件上传
            String[] upload = FastDFSClient.upload(dfsFile);
            //拼接返回url
            //http://192.168.211.132:8080/group1/M00/00/00/wKjThF238zWAF_rkAAcHN3pW-Rw102.jpg
            //String url = "http://192.168.211.132:8080/" + upload[0] + "/" + upload[1];
            String url = FastDFSClient.getTrackerUrl() + upload[0] + "/" + upload[1];
            return new Result<String>(true, StatusCode.OK,"文件上传成功",url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result<String>(false, StatusCode.ERROR,"文件上传失败");
    }
}
