package com.evolunteer.controller;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.enums.ActivityRejectReasonEnum;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.CheckInService;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @Autowired
    ActivityService activityService;

    @Autowired
    CheckInService checkInService;

    @Autowired
    AuditLogService auditLogService;

    @RequestMapping(value = "/findAuditActivityByState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findAuditActivityByState(@RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "size", required = false) Integer size,
                                                @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(activityService.pageActivityByState("0", keyword, page, size));
    }

    @RequestMapping(value = "/passAct", method = RequestMethod.POST)
    @ResponseBody
    public String passAct(@RequestParam(value = "actName") String actName,
                          @RequestParam(value = "statue") String statue,
                          @RequestParam(value = "reasonCode", required = false) String reasonCode,
                          @RequestParam(value = "remark", required = false) String remark,
                          Authentication authentication, HttpServletRequest request) {

        Activity activity = activityService.selectAct(actName);
        if (activity == null) {
            log.warn("平台管理员审核的活动不存在，活动名称：{}", actName);
            return "该志愿活动不存在，请刷新页面后重试";
        }

        if (!"1".equals(statue) && ActivityRejectReasonEnum.of(reasonCode) == null) {
            log.warn("平台管理员审核未通过但未给出原因，活动编号：{}", activity.getActivityNum());
            return "请选择审核不通过的原因";
        }

        User user = (User) authentication.getPrincipal();

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", user.getUsername());
        map.put("activityNum", activity.getActivityNum());
        map.put("isPass", statue);
        map.put("reasonCode", reasonCode);
        map.put("remark", remark);
        activityService.passActByAdminIdWithStatue(map);

        String msg = (String) map.get("msg");
        auditLogService.record(user.getUsername(), "ROLE_ADMIN", AuditActionEnum.ACTIVITY_AUDIT,
                "活动编号 " + activity.getActivityNum(), msg + "，原因：" + reasonCode, request.getRemoteAddr());
        log.info("平台管理员完成活动审核，活动编号：{}，审核结果：{}，原因：{}", activity.getActivityNum(), statue, reasonCode);
        return msg;
    }

    @RequestMapping(value = "/review/manualHours", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse manualHours(@RequestParam(value = "page", required = false) Integer page,
                                   @RequestParam(value = "size", required = false) Integer size,
                                   @RequestParam(value = "state", required = false) String state,
                                   @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(checkInService.pageManualCheckins(state, keyword, page, size));
    }

    @RequestMapping(value = "/review/anomalies", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse anomalies(@RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "size", required = false) Integer size,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(checkInService.pageAnomalies(keyword, page, size));
    }

    @RequestMapping(value = "/review/manualHours/check", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse checkManualHours(@RequestParam(value = "checkinNum") Integer checkinNum,
                                        @RequestParam(value = "isPass") Integer isPass,
                                        @RequestParam(value = "remark", required = false) String remark,
                                        Authentication authentication, HttpServletRequest request) {

        String loginId = ((User) authentication.getPrincipal()).getUsername();
        Map<Object, Object> result = checkInService.reviewByAdmin(loginId, checkinNum, isPass, remark);
        String msg = (String) result.get("msg");
        Object ok = result.get("ok");
        if (ok == null || ((Number) ok).intValue() != 1) {
            log.warn("补录服务时长复核未完成，签到编号：{}，原因：{}", checkinNum, msg);
            return ApiResponse.fail(msg);
        }
        auditLogService.record(loginId, "ROLE_ADMIN", AuditActionEnum.MANUAL_HOUR_REVIEW,
                "签到编号 " + checkinNum, msg, request.getRemoteAddr());
        log.info("平台管理员完成服务时长裁定，签到编号：{}，结果：{}", checkinNum, msg);
        return ApiResponse.success(msg);
    }
}
