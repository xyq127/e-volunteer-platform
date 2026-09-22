package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Organization;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织控制器：提供志愿活动申报、活动重名校验以及志愿者组织资料查询与修改能力。
 */
@Slf4j
@Controller
@RequestMapping(value = "/org")
public class OrganizationController {

    /**
     * 志愿活动申报页面使用的日期格式
     */
    private static final String DATE_PATTERN = "MM/dd/yyyy";

    @Autowired
    OrganizationService organizationService;

    @Autowired
    ActivityService activityService;

    /**
     * 志愿者组织申报志愿活动
     *
     * @param name           活动名称
     * @param content        活动内容
     * @param startTime      活动开始时间
     * @param endTime        活动结束时间
     * @param location       活动地点
     * @param affair         注意事项
     * @param need           招募人数
     * @param sign_ddl       报名截止时间
     * @param authentication 当前登录用户信息
     * @return 申报处理结果信息
     */
    @RequestMapping(value = "/upact", method = RequestMethod.POST)
    @ResponseBody
    public String upAct(@RequestParam(value = "name") String name,
                        @RequestParam(value = "content") String content,
                        @RequestParam(value = "startTime") String startTime,
                        @RequestParam(value = "endTime") String endTime,
                        @RequestParam(value = "location") String location,
                        @RequestParam(value = "affair") String affair,
                        @RequestParam(value = "need") String need,
                        @RequestParam(value = "sign_ddl") String sign_ddl,
                        Authentication authentication) {

        Date start;
        Date end;
        Date signDdl;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
            dateFormat.setLenient(false);
            start = dateFormat.parse(startTime);
            end = dateFormat.parse(endTime);
            signDdl = dateFormat.parse(sign_ddl);
        } catch (ParseException e) {
            log.warn("志愿活动申报时间格式不正确，开始时间：{}，结束时间：{}，报名截止时间：{}", startTime, endTime, sign_ddl);
            return "活动时间格式不正确，请重新选择活动时间";
        }

        Integer needPeople;
        try {
            needPeople = Integer.valueOf(need.trim());
        } catch (RuntimeException e) {
            log.warn("志愿活动申报的招募人数格式不正确：{}", need);
            return "招募人数必须为数字，请重新填写";
        }

        User user = (User) authentication.getPrincipal();

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", user.getUsername());
        map.put("activityName", name);
        map.put("activityDetail", content);
        map.put("activityBegintime", start);
        map.put("activityEndtime", end);
        map.put("activityLocation", location);
        map.put("activityNeedpeople", needPeople);
        map.put("activityNotice", affair);
        map.put("activitySignddl", signDdl);
        organizationService.upAct(map);
        log.info("志愿者组织申报志愿活动，活动名称：{}", name);
        return (String) map.get("msg");
    }

    /**
     * 校验志愿活动名称是否已被使用
     *
     * @param name 活动名称
     * @return 名称可用返回 true
     */
    @RequestMapping(value = "/checkname", method = RequestMethod.GET)
    @ResponseBody
    public Boolean checkName(@RequestParam(value = "name") String name) {
        return activityService.searchActivityByName(name);
    }

    /**
     * 查询当前登录志愿者组织的资料
     *
     * @param authentication 当前登录用户信息
     * @return 志愿者组织资料
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse getInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Organization organization = organizationService.getOrgInfo(user.getUsername());
        return ApiResponse.success().add("organization", organization);
    }

    /**
     * 修改当前登录志愿者组织的名称与简介
     *
     * @param name           志愿者组织名称
     * @param introduction   志愿者组织简介
     * @param authentication 当前登录用户信息
     * @return 处理结果
     */
    @RequestMapping(value = "/infoChange", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse infoChange(@RequestParam(value = "name") String name,
                          @RequestParam(value = "introduction") String introduction,
                          Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Organization organization = new Organization();
        organization.setOrganizationName(name);
        organization.setOrganizationIntroduction(introduction);

        if (organizationService.changeInfo(user.getUsername(), organization)) {
            log.info("志愿者组织修改组织资料，组织账号：{}", user.getUsername());
            return ApiResponse.success();
        }
        return ApiResponse.fail();
    }
}
