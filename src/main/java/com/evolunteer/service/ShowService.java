package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.PortalShowView;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ShowService {

    Map<Object, Object> publish(Integer volunteerNum, Integer activityNum, String detail);

    Map<Object, Object> uploadPicture(Integer volunteerNum, Integer showNum, MultipartFile file);

    IPage<PortalShowView> pageByVolunteer(Integer volunteerNum, Integer pageNum, Integer pageSize);

    IPage<PortalShowView> pagePortal(Integer pageNum, Integer pageSize, String keyword, Integer volunteerNum);

    PortalShowView detail(Integer showNum, Integer volunteerNum);

    Map<Object, Object> like(Integer volunteerNum, Integer showNum);

    IPage<PortalShowView> pageAdmin(Integer pageNum, Integer pageSize, String keyword);

    Map<Object, Object> deleteShow(Integer showNum, String operator, String ip);
}
