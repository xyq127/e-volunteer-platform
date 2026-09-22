package com.evolunteer.controller;

import com.evolunteer.service.VolunteerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 注册控制器：提供志愿者注册手机号查重以及志愿者注册能力。
 */
@Slf4j
@Controller
@RequestMapping(value = "/loginRegisterController")
public class LoginRegisterController {

    @Autowired
    VolunteerService volunteerService;

    /**
     * 校验注册手机号是否已被使用
     *
     * @param signUpTel 注册手机号
     * @return 手机号可用返回 true，已被注册返回 false
     */
    @RequestMapping(value = "/checkSignUpTel", method = RequestMethod.GET)
    @ResponseBody
    public boolean checkSignUpTel(@RequestParam(value = "signUpTel") String signUpTel) {
        return volunteerService.checkSignupTel(signUpTel) <= 0;
    }

    /**
     * 注册志愿者
     *
     * @param signUpName     志愿者姓名
     * @param signUpTel      注册手机号
     * @param signUpPassword 登录密码
     * @return 新注册志愿者的志愿者编号
     */
    @RequestMapping(value = "/signUp", method = RequestMethod.POST)
    @ResponseBody
    public String signUp(@RequestParam(value = "signUpName") String signUpName,
                         @RequestParam(value = "signUpTel") String signUpTel,
                         @RequestParam(value = "signUpPassword") String signUpPassword) {
        String volunteerId = volunteerService.signUp(signUpName, signUpTel, signUpPassword);
        if (volunteerId == null || volunteerId.isEmpty()) {
            log.warn("志愿者注册失败，手机号已被注册：{}", signUpTel);
            return "该手机号已被注册，请更换手机号后再试";
        }
        log.info("志愿者注册成功，志愿者编号：{}", volunteerId);
        return volunteerId;
    }
}
