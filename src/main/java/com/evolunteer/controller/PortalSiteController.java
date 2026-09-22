package com.evolunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.CheckInService;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.PolicyFile;
import com.evolunteer.entity.PortalAnnouncementView;
import com.evolunteer.entity.PortalShowView;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.mapper.PortalMapper;
import com.evolunteer.service.ShowService;
import com.evolunteer.service.VolunteerService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/portal/site")
public class PortalSiteController {

    @Autowired
    PortalMapper portalMapper;

    @Autowired
    ShowService showService;

    @Autowired
    VolunteerService volunteerService;

    @Autowired
    CheckInService checkInService;

    @Autowired
    AuditLogService auditLogService;

    @RequestMapping(value = "/service-confirm", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse serviceConfirm(@RequestParam(value = "confirmCode") String confirmCode,
                                      @RequestParam(value = "objectPhone") String objectPhone,
                                      @RequestParam(value = "resultValue") Integer resultValue,
                                      @RequestParam(value = "objectRemark", required = false) String objectRemark,
                                      HttpServletRequest request) {

        Map<Object, Object> result = checkInService.objectConfirm(confirmCode, objectPhone, resultValue, objectRemark);
        String msg = (String) result.get("msg");
        Object ok = result.get("ok");
        if (ok == null || ((Number) ok).intValue() != 1) {
            return ApiResponse.fail(msg);
        }
        auditLogService.record("服务对象", "PUBLIC", AuditActionEnum.SERVICE_OBJECT_CONFIRM,
                "确认码 " + confirmCode, msg, request.getRemoteAddr());
        return ApiResponse.success(msg);
    }

    @RequestMapping(value = "/announcements", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse announcements(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     @RequestParam(value = "keyword", required = false) String keyword) {

        Page<PortalAnnouncementView> pageObj = PageSupport.of(page, size);
        IPage<PortalAnnouncementView> result =
                portalMapper.selectAnnouncementPage(pageObj, PageSupport.normalizeKeyword(keyword));
        fillFiles(result.getRecords());
        return PageSupport.toResponse(result);
    }

    @RequestMapping(value = "/announcement", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse announcement(@RequestParam(value = "policyannouncementNum") Integer policyannouncementNum) {

        PortalAnnouncementView announcement = portalMapper.selectAnnouncementByNum(policyannouncementNum);
        if (announcement == null) {
            return ApiResponse.fail("该通知公告不存在或已被删除");
        }
        fillFiles(Collections.singletonList(announcement));
        return ApiResponse.success().add("announcement", announcement);
    }

    @RequestMapping(value = "/activities", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse activities(@RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size,
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        Page<Activity> pageObj = PageSupport.of(page, size);
        return PageSupport.toResponse(portalMapper.selectActivityPage(pageObj, PageSupport.normalizeKeyword(keyword)));
    }

    @RequestMapping(value = "/organizations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse organizations(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     @RequestParam(value = "keyword", required = false) String keyword) {
        Page<Organization> pageObj = PageSupport.of(page, size);
        return PageSupport.toResponse(
                portalMapper.selectOrganizationPage(pageObj, PageSupport.normalizeKeyword(keyword)));
    }

    @RequestMapping(value = "/shows", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse shows(@RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "size", required = false) Integer size,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Authentication authentication) {
        return PageSupport.toResponse(
                showService.pagePortal(page, size, keyword, currentVolunteerNum(authentication)));
    }

    @RequestMapping(value = "/show", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse show(@RequestParam(value = "showNum") Integer showNum, Authentication authentication) {

        PortalShowView show = showService.detail(showNum, currentVolunteerNum(authentication));
        if (show == null) {
            return ApiResponse.fail("该志愿秀不存在或已被删除");
        }
        return ApiResponse.success().add("show", show);
    }

    private void fillFiles(List<PortalAnnouncementView> announcements) {

        if (announcements == null || announcements.isEmpty()) {
            return;
        }
        List<Integer> announcementNums = new ArrayList<>();
        for (PortalAnnouncementView announcement : announcements) {
            announcementNums.add(announcement.getPolicyannouncementNum());
        }

        Map<Integer, List<PolicyFile>> filesByAnnouncement = new HashMap<>();
        for (PolicyFile policyFile : portalMapper.selectFilesByAnnouncementNums(announcementNums)) {
            Integer announcementNum = policyFile.getPolicyannouncementNum();
            List<PolicyFile> files = filesByAnnouncement.get(announcementNum);
            if (files == null) {
                files = new ArrayList<>();
                filesByAnnouncement.put(announcementNum, files);
            }
            files.add(policyFile);
        }

        for (PortalAnnouncementView announcement : announcements) {
            List<PolicyFile> files = filesByAnnouncement.get(announcement.getPolicyannouncementNum());
            announcement.setFiles(files == null ? new ArrayList<>() : files);
            announcement.setFileCount(announcement.getFiles().size());
        }
    }

    private Integer currentVolunteerNum(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return null;
        }
        User user = (User) authentication.getPrincipal();
        Volunteer volunteer = volunteerService.getByLoginId(user.getUsername());
        return volunteer == null ? null : volunteer.getVolunteerNum();
    }
}
