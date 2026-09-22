package com.evolunteer.controller;

import com.evolunteer.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = "/file")
public class FileController {

    @Autowired
    FileStorageService fileStorageService;

    @RequestMapping(value = "/download/{storedName}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Resource> download(@PathVariable(value = "storedName") String storedName) {

        Resource resource;
        try {
            resource = fileStorageService.load(storedName);
        } catch (IllegalArgumentException e) {
            log.warn("文件下载失败，存储名：{}，原因：{}", storedName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
