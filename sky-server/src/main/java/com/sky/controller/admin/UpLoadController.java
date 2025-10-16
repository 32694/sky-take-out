package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@Api(tags = "文件上传接口")
@RequestMapping("/admin/common")
public class UpLoadController {
    @Autowired
    private AliOssUtil aliyunOSSOperator;
    @PostMapping("upload")
    public Result upload(MultipartFile file) {
        log.info("文件上传开始:{}",file.getOriginalFilename());
        try {
            String url=aliyunOSSOperator.upload(file.getBytes(),file.getOriginalFilename());
            log.info("文件上传结束:{}",url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
