package com.evolunteer.controller;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Participation;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.OrganizationService;
import com.evolunteer.service.ParticipationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动控制器：面向志愿者组织提供志愿活动的分状态查询、活动详情查询、活动删除以及活动报名人员查询等功能。
 */
@Slf4j
@Controller
@RequestMapping(value = "/activity")
public class ActivityController {

    @Autowired
    ActivityService activityService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    ParticipationService participationService;

    /**
     * 查询当前登录组织审核中的志愿活动
     *
     * @param authentication 当前登录用户信息
     * @return 审核中的活动列表
     */
    @RequestMapping(value = "/findAuditActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findAuditActivityByNumAndState(Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        List<Activity> auditActivity = activityService.getActivityByNumAndState(num, "0");
        return ApiResponse.success().add("auditActivity", auditActivity);
    }

    /**
     * 查询活动的详细信息
     *
     * @param activityNum 活动编号
     * @return 活动详情
     */
    @RequestMapping(value = "/activityInfo", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse activityInfo(@RequestParam(value = "activityNum") Integer activityNum) {
        List<Activity> activityInfo = activityService.activityInfo(activityNum);
        return ApiResponse.success().add("activityInfo", activityInfo);
    }

    /**
     * 删除审核中的志愿活动
     *
     * @param activityNum    活动编号
     * @param authentication 当前登录用户信息
     * @return 删除后的审核中活动列表
     */
    @RequestMapping(value = "/auditActivityDelete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse auditActivityDelete(@RequestParam(value = "activityNum") Integer activityNum,
                                   Authentication authentication) {
        activityService.deleteAct(activityNum);
        log.info("志愿者组织删除审核中的活动，活动编号：{}", activityNum);
        return findAuditActivityByNumAndState(authentication);
    }

    /**
     * 删除审核未通过的志愿活动
     *
     * @param activityNum    活动编号
     * @param authentication 当前登录用户信息
     * @return 删除后的审核未通过活动列表
     */
    @RequestMapping(value = "/notAuditActivityDelete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse notAuditActivityDelete(@RequestParam(value = "activityNum") Integer activityNum,
                                      Authentication authentication) {
        activityService.deleteAct(activityNum);
        log.info("志愿者组织删除审核未通过的活动，活动编号：{}", activityNum);
        return findNotAuditActivityByNumAndState(authentication);
    }

    /**
     * 查询当前登录组织未开始的志愿活动
     *
     * @param authentication 当前登录用户信息
     * @return 未开始的活动列表
     */
    @RequestMapping(value = "/findNotStartActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findNotStartActivityByNumAndState(Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        List<Activity> notStartActivity = activityService.getActivityByNumAndState(num, "1");
        return ApiResponse.success().add("auditActivity", notStartActivity);
    }

    /**
     * 查询指定活动下已报名的志愿者
     *
     * @param activityNum 活动编号
     * @return 报名志愿者列表
     */
    @RequestMapping(value = "/volRecruit", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse volRecruit(@RequestParam(value = "activityNum") Integer activityNum) {
        List<Participation> volunteerList = participationService.getVolunteerList(activityNum);
        return ApiResponse.success().add("volunteerList", volunteerList);
    }

    /**
     * 查询当前登录组织进行中的志愿活动
     *
     * @param authentication 当前登录用户信息
     * @return 进行中的活动列表
     */
    @RequestMapping(value = "/findCarryActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findCarryActivityByNumAndState(Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        List<Activity> carryActivity = activityService.getActivityByNumAndState(num, "2");
        return ApiResponse.success().add("carryActivity", carryActivity);
    }

    /**
     * 查询当前登录组织已结束的志愿活动
     *
     * @param authentication 当前登录用户信息
     * @return 已结束的活动列表
     */
    @RequestMapping(value = "/findEndActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findEndActivityByNumAndState(Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        List<Activity> endActivity = activityService.getActivityByNumAndState(num, "3");
        return ApiResponse.success().add("endActivity", endActivity);
    }

    /**
     * 查询当前登录组织审核未通过的志愿活动
     *
     * @param authentication 当前登录用户信息
     * @return 审核未通过的活动列表
     */
    @RequestMapping(value = "/findNotAuditActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findNotAuditActivityByNumAndState(Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        List<Activity> notAuditActivity = activityService.getActivityByNumAndState(num, "4");
        return ApiResponse.success().add("notAuditActivity", notAuditActivity);
    }

    /**
     * 获取当前登录用户所属志愿者组织的组织编号
     *
     * @param authentication 当前登录用户信息
     * @return 志愿者组织编号，未匹配到组织时返回 null
     */
    private Integer currentOrganizationNum(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return organizationService.getOrganizationNum(user.getUsername());
    }
}
