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

    private Volunteer currentVolunteer(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return volunteerService.getByLoginId(user.getUsername());
    }
}
