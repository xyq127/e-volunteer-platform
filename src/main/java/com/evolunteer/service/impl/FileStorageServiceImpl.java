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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 文件存储业务实现类：文件按“随机存储名.扩展名”保存在配置的上传目录中，
 * 仅允许白名单内的扩展名，下载时按严格的存储名规则解析路径，避免目录穿越。
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    /**
     * 允许上传的文件扩展名
     */
    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip");

    /**
     * 单个文件大小上限（字节）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 存储名格式：由字母、数字、短横线组成的主体加上扩展名
     */
    private static final Pattern STORED_NAME_PATTERN = Pattern.compile("[A-Za-z0-9-]{1,64}\\.[A-Za-z0-9]{1,8}");

    /**
     * 文件访问路径前缀
     */
    private static final String DOWNLOAD_PREFIX = "/file/download/";

    /**
     * 上传目录，部署环境可通过环境变量 EVOLUNTEER_UPLOAD_DIR 覆盖
     */
    @Value("${evolunteer.upload.dir}")
    private String uploadDir;

    /**
     * 保存上传文件
     *
     * @param file 上传文件
     * @return 文件存储名
     */
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

    /**
     * 读取存储文件
     *
     * @param storedName 文件存储名
     * @return 文件资源
     */
    @Override
    public Resource load(String storedName) {
        Path path = resolve(storedName);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件不存在或已被删除");
        }
        return new FileSystemResource(path);
    }

    /**
     * 删除存储文件
     *
     * @param storedName 文件存储名
     * @return 删除成功或文件本来就不存在时返回 true
     */
    @Override
    public boolean delete(String storedName) {
        try {
            return Files.deleteIfExists(resolve(storedName));
        } catch (IOException e) {
            log.warn("删除存储文件失败，存储名：{}", storedName, e);
            return false;
        }
    }

    /**
     * 生成文件访问路径
     *
     * @param storedName 文件存储名
     * @return 页面可直接使用的下载路径
     */
    @Override
    public String routeOf(String storedName) {
        return DOWNLOAD_PREFIX + storedName;
    }

    /**
     * 解析存储名对应的文件路径，存储名不合法时直接拒绝
     */
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

    /**
     * 取文件名扩展名，统一转为小写
     */
    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(index + 1).toLowerCase();
    }
}
