package com.evolunteer.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 文件存储业务接口：把通知公告附件与志愿秀图片保存到服务器文件目录，
 * 存储时校验文件类型与大小并生成不重复的存储名，下载时按存储名读取文件。
 */
public interface FileStorageService {

    /**
     * 保存上传文件
     *
     * @param file 上传文件
     * @return 文件存储名
     */
    String store(MultipartFile file);

    /**
     * 读取存储文件
     *
     * @param storedName 文件存储名
     * @return 文件资源，文件不存在或名称不合法时抛出异常
     */
    Resource load(String storedName);

    /**
     * 删除存储文件
     *
     * @param storedName 文件存储名
     * @return 删除成功或文件本来就不存在时返回 true
     */
    boolean delete(String storedName);

    /**
     * 生成文件访问路径
     *
     * @param storedName 文件存储名
     * @return 页面可直接使用的下载路径
     */
    String routeOf(String storedName);
}
