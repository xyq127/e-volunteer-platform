package com.evolunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ApiResponse;
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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 公众门户数据控制器：为无需登录的门户页面提供通知公告、志愿活动、志愿者组织与志愿秀广场的
 * 只读分页查询，全部数据取自平台真实业务表；志愿秀详情按浏览量累加，已登录志愿者可看到本人点赞状态。
 */
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

    /**
     * 分页查询门户通知公告，每条公告附带附件列表
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 关键字，按公告标题与公告内容模糊匹配
     * @return 通知公告分页结果
     */
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

    /**
     * 查询门户公告详情，包含公告附件列表
     *
     * @param policyannouncementNum 公告编号
     * @return 公告详情
     */
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

    /**
     * 分页查询门户公开志愿活动，仅展示未开始与进行中且未删除的活动
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 关键字，按活动名称与活动地点模糊匹配
     * @return 志愿活动分页结果
     */
    @RequestMapping(value = "/activities", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse activities(@RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size,
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        Page<Activity> pageObj = PageSupport.of(page, size);
        return PageSupport.toResponse(portalMapper.selectActivityPage(pageObj, PageSupport.normalizeKeyword(keyword)));
    }

    /**
     * 分页查询门户志愿者组织
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 关键字，按组织名称与组织简介模糊匹配
     * @return 志愿者组织分页结果
     */
    @RequestMapping(value = "/organizations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse organizations(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     @RequestParam(value = "keyword", required = false) String keyword) {
        Page<Organization> pageObj = PageSupport.of(page, size);
        return PageSupport.toResponse(
                portalMapper.selectOrganizationPage(pageObj, PageSupport.normalizeKeyword(keyword)));
    }

    /**
     * 分页查询门户志愿秀，附带分享志愿者姓名、关联活动名称与首图路径
     *
     * @param page           页码
     * @param size           每页条数
     * @param keyword        关键字，按分享内容与分享志愿者姓名模糊匹配
     * @param authentication 当前登录用户信息，未登录时为空
     * @return 志愿秀分页结果
     */
    @RequestMapping(value = "/shows", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse shows(@RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "size", required = false) Integer size,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Authentication authentication) {
        return PageSupport.toResponse(
                showService.pagePortal(page, size, keyword, currentVolunteerNum(authentication)));
    }

    /**
     * 查询门户志愿秀详情，包含图片列表，未登录也可浏览
     *
     * @param showNum        志愿秀编号
     * @param authentication 当前登录用户信息，未登录时为空
     * @return 志愿秀详情
     */
    @RequestMapping(value = "/show", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse show(@RequestParam(value = "showNum") Integer showNum, Authentication authentication) {

        PortalShowView show = showService.detail(showNum, currentVolunteerNum(authentication));
        if (show == null) {
            return ApiResponse.fail("该志愿秀不存在或已被删除");
        }
        return ApiResponse.success().add("show", show);
    }

    /**
     * 为公告列表装配附件：按公告编号一次性查询附件并按公告归并，避免逐条查询附件
     */
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

    /**
     * 取当前登录志愿者的志愿者编号，未登录或非志愿者账号时返回 null
     *
     * @param authentication 当前登录用户信息
     * @return 志愿者编号，未登录时返回 null
     */
    private Integer currentVolunteerNum(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return null;
        }
        User user = (User) authentication.getPrincipal();
        Volunteer volunteer = volunteerService.getByLoginId(user.getUsername());
        return volunteer == null ? null : volunteer.getVolunteerNum();
    }
}
