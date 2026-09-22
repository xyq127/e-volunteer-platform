package com.evolunteer.controller;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台管理员控制器：面向平台管理员提供待审核志愿活动的查询与审核能力。
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @Autowired
    ActivityService activityService;

    /**
     * 查询平台内待审核的志愿活动
     *
     * @return 待审核的活动列表
     */
    @RequestMapping(value = "/findAuditActivityByState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findAuditActivityByState() {
        List<Activity> auditActivity = activityService.getActivityByState("0");
        return ApiResponse.success().add("auditActivity", auditActivity);
    }

    /**
     * 审核志愿活动，审核结果通过存储过程写入活动表
     *
     * @param actName        活动名称
     * @param statue         审核结果（1 审核通过、4 审核未通过）
     * @param authentication 当前登录用户信息
     * @return 审核处理结果信息
     */
    @RequestMapping(value = "/passAct", method = RequestMethod.POST)
    @ResponseBody
    public String passAct(@RequestParam(value = "actName") String actName,
                          @RequestParam(value = "statue") String statue,
                          Authentication authentication) {

        Activity activity = activityService.selectAct(actName);
        if (activity == null) {
            log.warn("平台管理员审核的活动不存在，活动名称：{}", actName);
            return "该志愿活动不存在，请刷新页面后重试";
        }

        User user = (User) authentication.getPrincipal();

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", user.getUsername());
        map.put("activityNum", activity.getActivityNum());
        map.put("isPass", statue);
        map.put("remark", "");
        activityService.passActByAdminIdWithStatue(map);
        log.info("平台管理员完成活动审核，活动编号：{}，审核结果：{}", activity.getActivityNum(), statue);
        return (String) map.get("msg");
    }
}
