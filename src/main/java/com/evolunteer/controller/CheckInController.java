package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.CheckinCodeView;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.CheckInService;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 可信服务记录控制器：志愿者使用现场签到码并携带定位完成签到签退；
 * 志愿者组织分页查询签到轨迹、复核平台签到记录的服务时长，并为线下服务补录时长（补录由平台管理员复核）。
 */
@Slf4j
@Controller
@RequestMapping(value = "/checkin")
public class CheckInController {

    @Autowired
    CheckInService checkInService;

    @Autowired
    VolunteerService volunteerService;

    @Autowired
    AuditLogService auditLogService;

    /**
     * 志愿者签到：校验现场签到码、签到时间窗口与地理围栏
     *
     * @param activityNum    活动编号
     * @param code           现场签到码
     * @param latitude       签到地点纬度，未获取到定位时可不传
     * @param longitude      签到地点经度，未获取到定位时可不传
     * @param authentication 当前登录用户信息
     * @return 处理结果，异常轨迹的标记见 flag 字段
     */
    @RequestMapping(value = "/in", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse checkin(@RequestParam(value = "activityNum") Integer activityNum,
                               @RequestParam(value = "code", required = false) String code,
                               @RequestParam(value = "latitude", required = false) String latitude,
                               @RequestParam(value = "longitude", required = false) String longitude,
                               Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        Map<Object, Object> result = checkInService.checkin(volunteer.getVolunteerNum(), activityNum, code,
                parseCoordinate(latitude), parseCoordinate(longitude));
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            log.warn("志愿者签到未成功，志愿者编号：{}，活动编号：{}，原因：{}", volunteer.getVolunteerId(), activityNum, msg);
            return ApiResponse.fail(msg);
        }
        log.info("志愿者签到成功，志愿者编号：{}，活动编号：{}，轨迹标记：{}", volunteer.getVolunteerId(), activityNum,
                result.get("flag"));
        return ApiResponse.success(msg).add("flag", result.get("flag"));
    }

    /**
     * 志愿者签退：按签到签退时间自动核算服务时长
     *
     * @param activityNum    活动编号
     * @param latitude       签退地点纬度，未获取到定位时可不传
     * @param longitude      签退地点经度，未获取到定位时可不传
     * @param authentication 当前登录用户信息
     * @return 处理结果，本次服务时长见 duration 字段
     */
    @RequestMapping(value = "/out", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse checkout(@RequestParam(value = "activityNum") Integer activityNum,
                                @RequestParam(value = "latitude", required = false) String latitude,
                                @RequestParam(value = "longitude", required = false) String longitude,
                                Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        Map<Object, Object> result = checkInService.checkout(volunteer.getVolunteerNum(), activityNum,
                parseCoordinate(latitude), parseCoordinate(longitude));
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            return ApiResponse.fail(msg);
        }
        log.info("志愿者签退成功，志愿者编号：{}，活动编号：{}，服务时长：{} 小时", volunteer.getVolunteerId(), activityNum,
                result.get("duration"));
        return ApiResponse.success(msg).add("duration", result.get("duration"));
    }

    /**
     * 分页查询活动的服务记录，供志愿者组织复核
     *
     * @param activityNum 活动编号
     * @param page        页码
     * @param size        每页条数
     * @param state       复核状态，0 待复核、1 已确认、2 已驳回
     * @param source      记录来源，1 平台签到、2 组织补录
     * @return 服务记录分页结果
     */
    @RequestMapping(value = "/reviewList", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse reviewList(@RequestParam(value = "activityNum") Integer activityNum,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size,
                                  @RequestParam(value = "state", required = false) String state,
                                  @RequestParam(value = "source", required = false) String source) {
        return PageSupport.toResponse(checkInService.pageCheckinRecords(activityNum, state, source, page, size));
    }

    /**
     * 志愿者组织复核平台签到记录的服务时长，确认后时长计入志愿者累计服务时长
     *
     * @param checkinNum  签到编号
     * @param isPass      复核结果（1 确认时长、2 驳回）
     * @param remark      复核意见
     * @param request     请求对象
     * @param auth        当前登录用户信息
     * @return 处理结果
     */
    @RequestMapping(value = "/review", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse review(@RequestParam(value = "checkinNum") Integer checkinNum,
                              @RequestParam(value = "isPass") Integer isPass,
                              @RequestParam(value = "remark", required = false) String remark,
                              HttpServletRequest request, Authentication auth) {

        Map<Object, Object> result = checkInService.reviewCheckin(checkinNum, isPass, remark);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            log.warn("服务时长复核未完成，签到编号：{}，原因：{}", checkinNum, msg);
            return ApiResponse.fail(msg);
        }
        auditLogService.record(operator(auth), "ROLE_ORGANIZATION", AuditActionEnum.CHECKIN_REVIEW,
                "签到编号 " + checkinNum, msg, request.getRemoteAddr());
        log.info("志愿者组织完成服务时长复核，签到编号：{}，结果：{}", checkinNum, msg);
        return ApiResponse.success(msg);
    }

    /**
     * 志愿者组织为线下服务补录服务时长，补录记录提交平台管理员复核
     *
     * @param participateNum 报名编号
     * @param beginTime      服务开始时间
     * @param endTime        服务结束时间
     * @param remark         补录说明
     * @param request        请求对象
     * @param auth           当前登录用户信息
     * @return 处理结果
     */
    @RequestMapping(value = "/manual", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse manual(@RequestParam(value = "participateNum") Integer participateNum,
                              @RequestParam(value = "beginTime") String beginTime,
                              @RequestParam(value = "endTime") String endTime,
                              @RequestParam(value = "remark", required = false) String remark,
                              HttpServletRequest request, Authentication auth) {

        String loginId = operator(auth);
        Map<Object, Object> result = checkInService.recordServiceHours(loginId, participateNum,
                beginTime, endTime, remark);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            log.warn("服务时长补录未完成，报名编号：{}，原因：{}", participateNum, msg);
            return ApiResponse.fail(msg);
        }
        auditLogService.record(loginId, "ROLE_ORGANIZATION", AuditActionEnum.CHECKIN_REVIEW,
                "报名编号 " + participateNum, "补录服务时长：" + msg, request.getRemoteAddr());
        return ApiResponse.success(msg);
    }

    /**
     * 查询活动的现场签到码
     *
     * @param activityNum 活动编号
     * @return 现场签到码，尚未生成时返回 null
     */
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse getCheckinCode(@RequestParam(value = "activityNum") Integer activityNum) {
        CheckinCodeView codeInfo = checkInService.currentCheckinCode(activityNum);
        if (codeInfo == null) {
            return ApiResponse.fail("未找到对应的志愿活动，无法获取签到码");
        }
        return ApiResponse.success()
                .add("checkinCode", codeInfo.getCheckinCode())
                .add("expiresInSeconds", codeInfo.getExpiresInSeconds());
    }

    /**
     * 志愿者组织为一条服务记录生成服务确认单，交给服务对象核对确认
     *
     * @param checkinNum     签到编号
     * @param objectName     服务对象名称
     * @param objectPhone    服务对象手机号
     * @param request        请求对象
     * @param authentication 当前登录用户信息
     * @return 处理结果，确认码见 confirmCode 字段
     */
    @RequestMapping(value = "/confirmSheet", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse confirmSheet(@RequestParam(value = "checkinNum") Integer checkinNum,
                                    @RequestParam(value = "objectName") String objectName,
                                    @RequestParam(value = "objectPhone") String objectPhone,
                                    HttpServletRequest request, Authentication authentication) {

        String loginId = operator(authentication);
        Map<Object, Object> result = checkInService.issueConfirmSheet(loginId, checkinNum, objectName, objectPhone);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            log.warn("生成服务确认单未完成，签到编号：{}，原因：{}", checkinNum, msg);
            return ApiResponse.fail(msg);
        }
        auditLogService.record(loginId, "ROLE_ORGANIZATION", AuditActionEnum.SERVICE_CONFIRM_SHEET,
                "签到编号 " + checkinNum, "生成服务确认单：" + objectName, request.getRemoteAddr());
        log.info("志愿者组织生成服务确认单，签到编号：{}，服务对象：{}", checkinNum, objectName);
        return ApiResponse.success(msg).add("confirmCode", result.get("confirmCode"));
    }

    /**
     * 生成或重新生成活动的现场签到码
     *
     * @param activityNum 活动编号
     * @return 现场签到码
     */
    @RequestMapping(value = "/code", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse refreshCheckinCode(@RequestParam(value = "activityNum") Integer activityNum) {
        String checkinCode = checkInService.refreshCheckinCode(activityNum);
        if (checkinCode == null) {
            return ApiResponse.fail("未找到对应的志愿活动，无法生成签到码");
        }
        CheckinCodeView codeInfo = checkInService.currentCheckinCode(activityNum);
        log.info("志愿者组织更换签到密钥并重新生成现场签到码，活动编号：{}", activityNum);
        return ApiResponse.success()
                .add("checkinCode", checkinCode)
                .add("expiresInSeconds", codeInfo == null ? null : codeInfo.getExpiresInSeconds());
    }

    /**
     * 获取当前登录账号对应的志愿者信息
     *
     * @param authentication 当前登录用户信息
     * @return 志愿者信息，账号未关联志愿者时返回 null
     */
    private Volunteer currentVolunteer(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return volunteerService.getByLoginId(user.getUsername());
    }

    /**
     * 取当前登录账号
     */
    private String operator(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getUsername();
    }

    /**
     * 解析页面提交的定位坐标，未获取到定位时返回 null
     */
    private Double parseCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 判断存储过程返回的处理结果是否成功
     */
    private boolean isSuccess(Map<Object, Object> result) {
        Object ok = result.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
