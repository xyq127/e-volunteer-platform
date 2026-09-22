package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.AuditLogService;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/participate")
public class ParticipationController {

    @Autowired
    ParticipationService participationService;

    @Autowired
    AuditLogService auditLogService;

    @RequestMapping(value = "/volPassRecruit", method = RequestMethod.POST)
    @ResponseBody
    public String volPassRecruit(@RequestParam(value = "participateNum") Integer participateNum,
                                 HttpServletRequest request, Authentication authentication) {
        return checkRecruit(participateNum, 1, authentication, request);
    }

    @RequestMapping(value = "/volUnPassRecruit", method = RequestMethod.POST)
    @ResponseBody
    public String volUnPassRecruit(@RequestParam(value = "participateNum") Integer participateNum,
                                   HttpServletRequest request, Authentication authentication) {
        return checkRecruit(participateNum, 2, authentication, request);
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse remove(@RequestParam(value = "participateNum") Integer participateNum,
                              HttpServletRequest request, Authentication authentication) {

        String loginId = ((User) authentication.getPrincipal()).getUsername();
        Map<Object, Object> result = participationService.removeParticipate(loginId, participateNum);
        String msg = (String) result.get("msg");
        Object ok = result.get("ok");
        if (ok == null || ((Number) ok).intValue() != 1) {
            log.warn("移除报名未完成，报名编号：{}，原因：{}", participateNum, msg);
            return ApiResponse.fail(msg);
        }
        auditLogService.record(loginId, "ROLE_ORGANIZATION", AuditActionEnum.PARTICIPATE_REMOVE,
                "报名编号 " + participateNum, msg, request.getRemoteAddr());
        return ApiResponse.success(msg);
    }

    private String checkRecruit(Integer participateNum, int isPass,
                                Authentication authentication, HttpServletRequest request) {

        Map<Object, Object> params = new HashMap<>();
        params.put("participateNum", participateNum);
        params.put("isPass", isPass);
        Object message = participationService.volCheckRecruit(params);

        String loginId = ((User) authentication.getPrincipal()).getUsername();
        auditLogService.record(loginId, "ROLE_ORGANIZATION", AuditActionEnum.PARTICIPATE_CHECK,
                "报名编号 " + participateNum, String.valueOf(message), request.getRemoteAddr());
        log.info("志愿者组织审核报名申请，报名编号：{}，结果：{}", participateNum, message);
        return (String) message;
    }
}
