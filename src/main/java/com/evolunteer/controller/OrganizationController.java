package com.evolunteer.controller;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Organization;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.CheckInService;
import com.evolunteer.service.NotificationService;
import com.evolunteer.service.OrgExportService;
import com.evolunteer.service.OrganizationService;
import com.evolunteer.utils.DateTimeParser;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/org")
public class OrganizationController {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    ActivityService activityService;

    @Autowired
    CheckInService checkInService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    OrgExportService orgExportService;

    @Autowired
    AuditLogService auditLogService;

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
                        @RequestParam(value = "tags", required = false) String tags,
                        @RequestParam(value = "latitude", required = false) String latitude,
                        @RequestParam(value = "longitude", required = false) String longitude,
                        @RequestParam(value = "radius", required = false) String radius,
                        Authentication authentication) {

        Date start;
        Date end;
        Date signDdl;
        try {
            start = DateTimeParser.parse(startTime);
            end = DateTimeParser.parse(endTime);
            signDdl = DateTimeParser.parse(sign_ddl);
        } catch (IllegalArgumentException e) {
            log.warn("志愿活动申报时间格式不正确，开始时间：{}，结束时间：{}，报名截止时间：{}", startTime, endTime, sign_ddl);
            return "活动时间格式不正确，请按年-月-日 时:分重新填写";
        }

        Integer needPeople;
        try {
            needPeople = Integer.valueOf(need.trim());
            if (needPeople <= 0) {
                throw new NumberFormatException("招募人数必须大于 0");
            }
        } catch (RuntimeException e) {
            log.warn("志愿活动申报的招募人数格式不正确：{}", need);
            return "招募人数必须为大于 0 的数字，请重新填写";
        }

        BigDecimal activityLatitude;
        BigDecimal activityLongitude;
        Integer activityRadius;
        try {
            activityLatitude = parseDecimal(latitude, 90, "活动地点纬度");
            activityLongitude = parseDecimal(longitude, 180, "活动地点经度");
            activityRadius = parseRadius(radius);
        } catch (IllegalArgumentException e) {
            log.warn("志愿活动申报的签到围栏参数不正确：{}", e.getMessage());
            return e.getMessage();
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
        map.put("activitySkillNames", tags);
        map.put("activityLatitude", activityLatitude);
        map.put("activityLongitude", activityLongitude);
        map.put("activityRadius", activityRadius);
        organizationService.upAct(map);
        log.info("志愿者组织申报志愿活动，活动名称：{}", name);
        return (String) map.get("msg");
    }

    @RequestMapping(value = "/reviseAct", method = RequestMethod.POST)
    @ResponseBody
    public String reviseAct(@RequestParam(value = "activityNum") Integer activityNum,
                            @RequestParam(value = "name") String name,
                            @RequestParam(value = "content") String content,
                            @RequestParam(value = "startTime") String startTime,
                            @RequestParam(value = "endTime") String endTime,
                            @RequestParam(value = "location") String location,
                            @RequestParam(value = "affair") String affair,
                            @RequestParam(value = "need") String need,
                            @RequestParam(value = "sign_ddl") String sign_ddl,
                            @RequestParam(value = "tags", required = false) String tags,
                            @RequestParam(value = "latitude", required = false) String latitude,
                            @RequestParam(value = "longitude", required = false) String longitude,
                            @RequestParam(value = "radius", required = false) String radius,
                            Authentication authentication, HttpServletRequest request) {

        Date start;
        Date end;
        Date signDdl;
        Integer needPeople;
        BigDecimal activityLatitude;
        BigDecimal activityLongitude;
        Integer activityRadius;
        try {
            start = DateTimeParser.parse(startTime);
            end = DateTimeParser.parse(endTime);
            signDdl = DateTimeParser.parse(sign_ddl);
            needPeople = Integer.valueOf(need.trim());
            if (needPeople <= 0) {
                throw new NumberFormatException("招募人数必须大于 0");
            }
            activityLatitude = parseDecimal(latitude, 90, "活动地点纬度");
            activityLongitude = parseDecimal(longitude, 180, "活动地点经度");
            activityRadius = parseRadius(radius);
        } catch (IllegalArgumentException e) {
            log.warn("志愿活动重新申报参数不正确：{}", e.getMessage());
            return e.getMessage();
        } catch (RuntimeException e) {
            log.warn("志愿活动重新申报的招募人数格式不正确：{}", need);
            return "招募人数必须为大于 0 的数字，请重新填写";
        }

        User user = (User) authentication.getPrincipal();

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", user.getUsername());
        map.put("activityNum", activityNum);
        map.put("activityName", name);
        map.put("activityDetail", content);
        map.put("activityBegintime", start);
        map.put("activityEndtime", end);
        map.put("activityLocation", location);
        map.put("activityNeedpeople", needPeople);
        map.put("activityNotice", affair);
        map.put("activitySignddl", signDdl);
        map.put("activitySkillNames", tags);
        map.put("activityLatitude", activityLatitude);
        map.put("activityLongitude", activityLongitude);
        map.put("activityRadius", activityRadius);
        activityService.reviseAct(map);
        log.info("志愿者组织修改后重新申报志愿活动，活动编号：{}", activityNum);
        auditLogService.record(user.getUsername(), "ROLE_ORGANIZATION", AuditActionEnum.ACTIVITY_REVISE,
                "活动编号 " + activityNum, name, request.getRemoteAddr());
        return (String) map.get("msg");
    }

    @RequestMapping(value = "/settle", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse settle(@RequestParam(value = "activityNum") Integer activityNum,
                              Authentication authentication, HttpServletRequest request) {

        Map<Object, Object> result = checkInService.settleActivity(activityNum);
        String msg = (String) result.get("msg");
        Object ok = result.get("ok");
        if (ok == null || ((Number) ok).intValue() != 1) {
            log.warn("活动结算未完成，活动编号：{}，原因：{}", activityNum, msg);
            return ApiResponse.fail(msg);
        }
        String loginId = ((User) authentication.getPrincipal()).getUsername();
        auditLogService.record(loginId, "ROLE_ORGANIZATION", AuditActionEnum.ACTIVITY_SETTLE,
                "活动编号 " + activityNum, msg, request.getRemoteAddr());
        log.info("志愿者组织完成活动结算，活动编号：{}，结果：{}", activityNum, msg);
        return ApiResponse.success(msg);
    }

    @RequestMapping(value = "/export/participants", method = RequestMethod.GET)
    public void exportParticipants(@RequestParam(value = "activityNum") Integer activityNum,
                                   HttpServletResponse response) throws IOException {
        writeCsv(response, "activity-participants-" + activityNum + ".csv",
                orgExportService.exportParticipants(activityNum));
    }

    @RequestMapping(value = "/export/hours", method = RequestMethod.GET)
    public void exportServiceHours(@RequestParam(value = "activityNum") Integer activityNum,
                                   HttpServletResponse response) throws IOException {
        writeCsv(response, "activity-hours-" + activityNum + ".csv",
                orgExportService.exportServiceHours(activityNum));
    }

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse notifications(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     Authentication authentication) {
        return PageSupport.toResponse(notificationService.page(operator(authentication), page, size));
    }

    @RequestMapping(value = "/notifications/unreadCount", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse unreadCount(Authentication authentication) {
        return ApiResponse.success().add("count", notificationService.unreadCount(operator(authentication)));
    }

    @RequestMapping(value = "/notifications/read", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse readNotification(@RequestParam(value = "notificationNum") Integer notificationNum,
                                        Authentication authentication) {
        boolean marked = notificationService.markRead(operator(authentication), notificationNum);
        if (!marked) {
            return ApiResponse.fail("未找到对应的通知");
        }
        return ApiResponse.success("通知已标记为已读");
    }

    @RequestMapping(value = "/notifications/readAll", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse readAllNotifications(Authentication authentication) {
        int updated = notificationService.markAllRead(operator(authentication));
        return ApiResponse.success("已标记 " + updated + " 条通知为已读");
    }

    @RequestMapping(value = "/checkname", method = RequestMethod.GET)
    @ResponseBody
    public Boolean checkName(@RequestParam(value = "name") String name) {
        return activityService.searchActivityByName(name);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse getInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Organization organization = organizationService.getOrgInfo(user.getUsername());
        return ApiResponse.success().add("organization", organization);
    }

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

    private void writeCsv(HttpServletResponse response, String fileName, String csv) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.getWriter().write(csv);
        response.getWriter().flush();
    }

    private String operator(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getUsername();
    }

    private BigDecimal parseDecimal(String value, int maxAbsolute, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        BigDecimal decimal;
        try {
            decimal = new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "格式不正确，请重新获取定位");
        }
        if (decimal.abs().compareTo(new BigDecimal(maxAbsolute)) > 0) {
            throw new IllegalArgumentException(fieldName + "超出合理范围，请重新获取定位");
        }
        return decimal;
    }

    private Integer parseRadius(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            Integer radius = Integer.valueOf(value.trim());
            if (radius <= 0) {
                throw new NumberFormatException("签到围栏半径必须大于 0");
            }
            return radius;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("签到围栏半径必须为大于 0 的数字");
        }
    }
}
