package com.evolunteer.service.impl;

import com.evolunteer.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final Pattern STORED_NAME_PATTERN = Pattern.compile("[A-Za-z0-9-]{1,64}\\.[A-Za-z0-9]{1,8}");

    private static final String DOWNLOAD_PREFIX = "/file/download/";

    @Value("${evolunteer.upload.dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择需要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("单个文件大小不能超过 10MB");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(originalName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("仅支持上传 jpg、png、gif、pdf、doc、docx、xls、xlsx、txt、zip 文件");
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try {
            Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            file.transferTo(directory.resolve(storedName).toFile());
        } catch (IOException e) {
            log.error("上传文件保存失败，文件名：{}", originalName, e);
            throw new IllegalStateException("文件保存失败，请稍后重试");
        }
        log.info("上传文件已保存，存储名：{}，原文件名：{}", storedName, originalName);
        return storedName;
    }

    @Override
    public Resource load(String storedName) {
        Path path = resolve(storedName);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件不存在或已被删除");
        }
        return new FileSystemResource(path);
    }

    @Override
    public boolean delete(String storedName) {
        try {
            return Files.deleteIfExists(resolve(storedName));
        } catch (IOException e) {
            log.warn("删除存储文件失败，存储名：{}", storedName, e);
            return false;
        }
    }

    @Override
    public String routeOf(String storedName) {
        return DOWNLOAD_PREFIX + storedName;
    }

    private Path resolve(String storedName) {
        if (storedName == null || !STORED_NAME_PATTERN.matcher(storedName).matches()) {
            throw new IllegalArgumentException("文件标识不合法");
        }
        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path path = directory.resolve(storedName).normalize();
        if (!path.getParent().equals(directory)) {
            throw new IllegalArgumentException("文件标识不合法");
        }
        return path;
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(index + 1).toLowerCase();
    }
}
