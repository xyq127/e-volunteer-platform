package com.evolunteer.controller;

import com.evolunteer.service.VolunteerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = "/loginRegisterController")
public class LoginRegisterController {

    @Autowired
    VolunteerService volunteerService;

    @RequestMapping(value = "/checkSignUpTel", method = RequestMethod.GET)
    @ResponseBody
    public boolean checkSignUpTel(@RequestParam(value = "signUpTel") String signUpTel) {
        return volunteerService.checkSignupTel(signUpTel) <= 0;
    }

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
