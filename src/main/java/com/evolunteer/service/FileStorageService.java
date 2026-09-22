package com.evolunteer.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file);

    Resource load(String storedName);

    boolean delete(String storedName);

    String routeOf(String storedName);
}
