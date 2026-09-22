package com.evolunteer.controller;

import com.evolunteer.service.ParticipateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名控制器：面向志愿者组织提供志愿者报名申请的审核能力。
 */
@Slf4j
@Controller
@RequestMapping(value = "/participate")
public class ParticipateController {

    @Autowired
    ParticipateService participateService;

    /**
     * 通过志愿者的报名申请
     *
     * @param participateNum 报名记录编号
     * @return 审核处理结果信息
     */
    @RequestMapping(value = "/volPassRecruit", method = RequestMethod.POST)
    @ResponseBody
    public String volPassRecruit(@RequestParam(value = "participateNum") Integer participateNum) {
        Map<Object, Object> map = new HashMap<>();
        map.put("participateNum", participateNum);
        map.put("isPass", 1);
        Object msg = participateService.volCheckRecruit(map);
        log.info("志愿者组织通过报名申请，报名编号：{}", participateNum);
        return (String) msg;
    }

    /**
     * 不通过志愿者的报名申请
     *
     * @param participateNum 报名记录编号
     * @return 审核处理结果信息
     */
    @RequestMapping(value = "/volUnPassRecruit", method = RequestMethod.POST)
    @ResponseBody
    public String volUnPassRecruit(@RequestParam(value = "participateNum") Integer participateNum) {
        Map<Object, Object> map = new HashMap<>();
        map.put("participateNum", participateNum);
        map.put("isPass", 2);
        Object msg = participateService.volCheckRecruit(map);
        log.info("志愿者组织驳回报名申请，报名编号：{}", participateNum);
        return (String) msg;
    }
}
