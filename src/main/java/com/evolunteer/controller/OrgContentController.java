package com.evolunteer.controller;

import com.evolunteer.entity.ActivityAnnouncement;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Training;
import com.evolunteer.service.ActivityAnnouncementService;
import com.evolunteer.service.TrainingService;
import com.evolunteer.utils.DateTimeParser;
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

import java.util.Date;

@Slf4j
@Controller
@RequestMapping(value = "/org/content")
public class OrgContentController {

    private static final String MESSAGE_TIME_INVALID = "时间格式不正确，请按年-月-日 时:分填写";

    @Autowired
    TrainingService trainingService;

    @Autowired
    ActivityAnnouncementService activityAnnouncementService;

    @RequestMapping(value = "/trainings", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse trainings(@RequestParam(value = "activityNum", required = false) Integer activityNum,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "size", required = false) Integer size,
                                 Authentication authentication) {
        return PageSupport.toResponse(
                trainingService.pageByActivity(loginIdOf(authentication), activityNum, page, size));
    }

    @RequestMapping(value = "/training/save", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse saveTraining(@RequestParam(value = "trainingNum", required = false) Integer trainingNum,
                                    @RequestParam(value = "activityNum") Integer activityNum,
                                    @RequestParam(value = "name") String name,
                                    @RequestParam(value = "detail", required = false) String detail,
                                    @RequestParam(value = "beginTime") String beginTime,
                                    @RequestParam(value = "endTime") String endTime,
                                    @RequestParam(value = "location", required = false) String location,
                                    @RequestParam(value = "checkin", required = false) String checkin,
                                    Authentication authentication) {

        Date begin;
        Date end;
        try {
            begin = DateTimeParser.parse(beginTime);
            end = DateTimeParser.parse(endTime);
        } catch (IllegalArgumentException e) {
            log.warn("培训安排时间格式不正确，开始时间：{}，结束时间：{}", beginTime, endTime);
            return ApiResponse.fail(MESSAGE_TIME_INVALID);
        }

        Training training = new Training();
        training.setTrainingNum(trainingNum);
        training.setActivityNum(activityNum);
        training.setTrainingName(name);
        training.setTrainingDetail(detail);
        training.setTrainingBegintime(begin);
        training.setTrainingEndtime(end);
        training.setTrainingLocation(location);
        training.setTrainingCheckin(checkin);

        String message = trainingService.saveTraining(loginIdOf(authentication), training);
        return respond(message, TrainingService.MESSAGE_SAVE_SUCCESS);
    }

    @RequestMapping(value = "/training/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse deleteTraining(@RequestParam(value = "trainingNum") Integer trainingNum,
                                      Authentication authentication) {
        String message = trainingService.deleteTraining(loginIdOf(authentication), trainingNum);
        return respond(message, TrainingService.MESSAGE_DELETE_SUCCESS);
    }

    @RequestMapping(value = "/training/situations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse situations(@RequestParam(value = "trainingNum") Integer trainingNum,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size,
                                  Authentication authentication) {
        return PageSupport.toResponse(
                trainingService.pageSituations(loginIdOf(authentication), trainingNum, page, size));
    }

    @RequestMapping(value = "/training/situation/save", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse saveSituation(@RequestParam(value = "trainingNum") Integer trainingNum,
                                     @RequestParam(value = "volunteerNum") Integer volunteerNum,
                                     @RequestParam(value = "beginTime") String beginTime,
                                     @RequestParam(value = "endTime") String endTime,
                                     Authentication authentication) {

        Date begin;
        Date end;
        try {
            begin = DateTimeParser.parse(beginTime);
            end = DateTimeParser.parse(endTime);
        } catch (IllegalArgumentException e) {
            log.warn("培训情况登记时间格式不正确，开始时间：{}，结束时间：{}", beginTime, endTime);
            return ApiResponse.fail(MESSAGE_TIME_INVALID);
        }

        String message = trainingService.saveSituation(
                loginIdOf(authentication), trainingNum, volunteerNum, begin, end);
        return respond(message, TrainingService.MESSAGE_SITUATION_SAVE_SUCCESS);
    }

    @RequestMapping(value = "/announcements", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse announcements(@RequestParam(value = "activityNum", required = false) Integer activityNum,
                                     @RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     Authentication authentication) {
        return PageSupport.toResponse(
                activityAnnouncementService.pageByOrganization(loginIdOf(authentication), activityNum, page, size));
    }

    @RequestMapping(value = "/announcement/save", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse saveAnnouncement(@RequestParam(value = "announcementNum", required = false) Integer announcementNum,
                                        @RequestParam(value = "activityNum") Integer activityNum,
                                        @RequestParam(value = "name") String name,
                                        @RequestParam(value = "detail") String detail,
                                        Authentication authentication) {

        ActivityAnnouncement announcement = new ActivityAnnouncement();
        announcement.setActannouncementNum(announcementNum);
        announcement.setActivityNum(activityNum);
        announcement.setActannouncementName(name);
        announcement.setActannouncementDetail(detail);

        String message = activityAnnouncementService.saveAnnouncement(loginIdOf(authentication), announcement);
        return respond(message, ActivityAnnouncementService.MESSAGE_SAVE_SUCCESS);
    }

    @RequestMapping(value = "/announcement/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse deleteAnnouncement(@RequestParam(value = "announcementNum") Integer announcementNum,
                                          Authentication authentication) {
        String message = activityAnnouncementService.deleteAnnouncement(
                loginIdOf(authentication), announcementNum);
        return respond(message, ActivityAnnouncementService.MESSAGE_DELETE_SUCCESS);
    }

    private String loginIdOf(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getUsername();
    }

    private ApiResponse respond(String message, String successMessage) {
        return successMessage.equals(message) ? ApiResponse.success(message) : ApiResponse.fail(message);
    }
}
