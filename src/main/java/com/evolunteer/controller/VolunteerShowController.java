package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.service.ShowService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/volunteer/content")
public class VolunteerShowController {

    @Autowired
    VolunteerService volunteerService;

    @Autowired
    ShowService showService;

    @RequestMapping(value = "/shows", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse shows(@RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "size", required = false) Integer size,
                             Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return PageSupport.toResponse(showService.pageByVolunteer(volunteer.getVolunteerNum(), page, size));
    }

    @RequestMapping(value = "/show/publish", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse publish(@RequestParam(value = "detail", required = false) String detail,
                               @RequestParam(value = "activityNum", required = false) Integer activityNum,
                               Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        Map<Object, Object> result = showService.publish(volunteer.getVolunteerNum(), activityNum, detail);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            log.warn("志愿者发布志愿秀未成功，志愿者编号：{}，原因：{}", volunteer.getVolunteerId(), msg);
            return ApiResponse.fail(msg);
        }
        return ApiResponse.success(msg).add("showNum", result.get("showNum"));
    }

    @RequestMapping(value = "/show/picture", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse picture(@RequestParam(value = "showNum") Integer showNum,
                               @RequestParam(value = "file") MultipartFile file,
                               Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        try {
            Map<Object, Object> result = showService.uploadPicture(volunteer.getVolunteerNum(), showNum, file);
            String msg = (String) result.get("msg");
            if (!isSuccess(result)) {
                return ApiResponse.fail(msg);
            }
            return ApiResponse.success(msg);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/show/like", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse like(@RequestParam(value = "showNum") Integer showNum,
                            Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        Map<Object, Object> result = showService.like(volunteer.getVolunteerNum(), showNum);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            return ApiResponse.fail(msg);
        }
        return ApiResponse.success(msg)
                .add("liked", result.get("liked"))
                .add("likeCount", result.get("likeCount"));
    }

    private Volunteer currentVolunteer(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return volunteerService.getByLoginId(user.getUsername());
    }

    private boolean isSuccess(Map<Object, Object> result) {
        Object ok = result.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
