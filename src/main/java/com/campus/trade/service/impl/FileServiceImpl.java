package com.campus.trade.service.impl;

import com.campus.trade.common.BusinessException;
import com.campus.trade.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;


@Service
public class FileServiceImpl implements FileService{

    private  static final Set<String>AllOWED=Set.of("jpg","png","jpeg","gif","webp");
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file) {
        if (file==null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400,"文件大小不能超过5M");
        }
        String contentType = file.getContentType();
        if (contentType ==null || !contentType.startsWith("image/")){
            throw new BusinessException(400,"只能上传图片");
        }
        String original = file.getOriginalFilename();
        String ext="";
        if(original!=null&& original.contains(".")){
            ext= original.substring(original.lastIndexOf('.')+1).toLowerCase();
        }
        if (!AllOWED.contains(ext)) {
            throw new BusinessException(400, "文件只支持jpg/jpeg/png/gif/webp");
        }
        String filename= UUID.randomUUID().toString().replace("-", "")+"."+ext;
        try{
            Path dir=Path.of(uploadDir).toAbsolutePath().normalize();//转成绝对路径 ，清理非法字符
            Files.createDirectories(dir);//如果这个文件不存在 自动创建
            file.transferTo(dir.resolve(filename).toFile());//保存文件道文件夹里面
        }catch (IOException e){
            throw new BusinessException(500,"文件上传失败");
        }
        return  "/uploads/"+filename;
    }

}
