package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.PolicyAnnouncement;
import com.evolunteer.entity.PolicyFile;
import com.evolunteer.entity.PortalAnnouncementView;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AnnouncementService extends IService<PolicyAnnouncement> {

    IPage<PortalAnnouncementView> page(Integer pageNum, Integer pageSize, String keyword);

    Map<Object, Object> save(Integer policyannouncementNum, String name, String detail, String adminId);

    Map<Object, Object> delete(Integer policyannouncementNum);

    PolicyFile uploadFile(Integer policyannouncementNum, MultipartFile file);

    boolean deleteFile(Integer policyfileNum);

    List<PolicyFile> listFiles(Integer policyannouncementNum);
}
