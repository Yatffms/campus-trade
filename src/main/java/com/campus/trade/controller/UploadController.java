package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.service.FileService;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Resource
    private FileService fileService;

    @PostMapping("/image")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return  Result.ok(fileService.upload(file));
    }



}
