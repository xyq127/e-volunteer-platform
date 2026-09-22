package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.service.ActivityAnnouncementService;
import com.evolunteer.service.TrainingService;
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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者内容查询控制器：面向志愿者提供本人所在活动的志愿培训安排与活动公告查询能力，
 * 培训安排仅限本人已通过报名的活动，活动公告仅限本人已报名（待审核、已通过、候补）的活动。
 */
@Slf4j
@Controller
@RequestMapping(value = "/volunteer/content")
public class VolunteerTrainingController {

    @Autowired
    VolunteerService volunteerService;

    @Autowired
    TrainingService trainingService;

    @Autowired
    ActivityAnnouncementService activityAnnouncementService;

    /**
     * 分页查询本人已通过报名活动对应的志愿培训安排
     *
     * @param page           页码
     * @param size           每页条数
     * @param authentication 当前登录用户信息
     * @return 培训安排分页结果，含活动名称
     */
    @RequestMapping(value = "/trainings", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse trainings(@RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "size", required = false) Integer size,
                                 Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return PageSupport.toResponse(
                trainingService.pageForVolunteer(volunteer.getVolunteerNum(), page, size));
    }

    /**
     * 分页查询本人已报名活动的志愿活动公告
     *
     * @param page           页码
     * @param size           每页条数
     * @param authentication 当前登录用户信息
     * @return 活动公告分页结果，含活动名称
     */
    @RequestMapping(value = "/announcements", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse announcements(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return PageSupport.toResponse(
                activityAnnouncementService.pageForVolunteer(volunteer.getVolunteerNum(), page, size));
    }

    /**
     * 取当前登录志愿者
     */
    private Volunteer currentVolunteer(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return volunteerService.getByLoginId(user.getUsername());
    }
}
