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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台管理员控制器：面向平台管理员提供待审核志愿活动的分页查询与审核能力，
 * 以及志愿者组织补录服务时长的复核能力；审核不通过时必须给出结构化原因。
 */
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

    /**
     * 分页查询平台内待审核的志愿活动
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 活动名称或活动编号关键字
     * @return 待审核的活动分页结果
     */
    @RequestMapping(value = "/findAuditActivityByState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findAuditActivityByState(@RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "size", required = false) Integer size,
                                                @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(activityService.pageActivityByState("0", keyword, page, size));
    }

    /**
     * 审核志愿活动，审核结果与结构化原因通过存储过程写入活动表
     *
     * @param actName        活动名称
     * @param statue         审核结果（1 审核通过、4 审核未通过）
     * @param reasonCode     审核不通过的结构化原因编码，审核通过时可为空
     * @param remark         审核意见
     * @param authentication 当前登录用户信息
     * @param request        请求对象
     * @return 审核处理结果信息
     */
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

    /**
     * 分页查询志愿者组织补录的服务时长记录，供平台管理员复核
     *
     * @param page    页码
     * @param size    每页条数
     * @param state   复核状态，0 待复核、1 已确认、2 已驳回
     * @param keyword 志愿者姓名、志愿者编号或活动名称关键字
     * @return 补录记录分页结果
     */
    @RequestMapping(value = "/review/manualHours", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse manualHours(@RequestParam(value = "page", required = false) Integer page,
                                   @RequestParam(value = "size", required = false) Integer size,
                                   @RequestParam(value = "state", required = false) String state,
                                   @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(checkInService.pageManualCheckins(state, keyword, page, size));
    }

    /**
     * 分页查询命中时长异常规则且尚未裁定的服务记录，供平台管理员裁定
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 志愿者姓名、志愿者编号或活动名称关键字
     * @return 异常记录分页结果
     */
    @RequestMapping(value = "/review/anomalies", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse anomalies(@RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "size", required = false) Integer size,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(checkInService.pageAnomalies(keyword, page, size));
    }

    /**
     * 平台管理员复核志愿者组织补录的服务时长
     *
     * @param checkinNum     签到编号
     * @param isPass         复核结果（1 确认时长、2 驳回）
     * @param remark         复核意见
     * @param authentication 当前登录用户信息
     * @param request        请求对象
     * @return 处理结果
     */
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
