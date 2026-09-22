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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织内容管理控制器：面向志愿者组织提供本组织申报活动的志愿培训安排维护、
 * 志愿者培训情况登记，以及志愿活动公告的发布、修改与删除能力。
 * 所有写操作的组织归属校验在业务层完成，只允许操作本组织申报活动下的培训与公告。
 */
@Slf4j
@Controller
@RequestMapping(value = "/org/content")
public class OrgContentController {

    /**
     * 时间参数解析失败的统一提示
     */
    private static final String MESSAGE_TIME_INVALID = "时间格式不正确，请按年-月-日 时:分填写";

    @Autowired
    TrainingService trainingService;

    @Autowired
    ActivityAnnouncementService activityAnnouncementService;

    /**
     * 分页查询本组织志愿活动的培训安排
     *
     * @param activityNum    活动编号，不传时查询本组织的全部培训安排
     * @param page           页码
     * @param size           每页条数
     * @param authentication 当前登录用户信息
     * @return 培训安排分页结果，含活动名称与已登记培训情况人数
     */
    @RequestMapping(value = "/trainings", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse trainings(@RequestParam(value = "activityNum", required = false) Integer activityNum,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "size", required = false) Integer size,
                                 Authentication authentication) {
        return PageSupport.toResponse(
                trainingService.pageByActivity(loginIdOf(authentication), activityNum, page, size));
    }

    /**
     * 保存志愿培训安排：培训编号为空时新增，否则修改已有培训安排
     *
     * @param trainingNum    培训编号，新增时不传
     * @param activityNum    培训所属活动编号
     * @param name           培训名称
     * @param detail         培训内容
     * @param beginTime      培训开始时间
     * @param endTime        培训结束时间
     * @param location       培训地点
     * @param checkin        签到方式
     * @param authentication 当前登录用户信息
     * @return 处理结果信息
     */
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

    /**
     * 逻辑删除志愿培训安排
     *
     * @param trainingNum    培训编号
     * @param authentication 当前登录用户信息
     * @return 处理结果信息
     */
    @RequestMapping(value = "/training/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse deleteTraining(@RequestParam(value = "trainingNum") Integer trainingNum,
                                      Authentication authentication) {
        String message = trainingService.deleteTraining(loginIdOf(authentication), trainingNum);
        return respond(message, TrainingService.MESSAGE_DELETE_SUCCESS);
    }

    /**
     * 分页查询某次培训的志愿者参加情况
     *
     * @param trainingNum    培训编号
     * @param page           页码
     * @param size           每页条数
     * @param authentication 当前登录用户信息
     * @return 志愿者培训情况分页结果，含志愿者编号、姓名与联系电话
     */
    @RequestMapping(value = "/training/situations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse situations(@RequestParam(value = "trainingNum") Integer trainingNum,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size,
                                  Authentication authentication) {
        return PageSupport.toResponse(
                trainingService.pageSituations(loginIdOf(authentication), trainingNum, page, size));
    }

    /**
     * 登记志愿者参加志愿培训的情况
     *
     * @param trainingNum    培训编号
     * @param volunteerNum   志愿者编号
     * @param beginTime      培训开始时间
     * @param endTime        培训结束时间
     * @param authentication 当前登录用户信息
     * @return 处理结果信息
     */
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

    /**
     * 分页查询本组织志愿活动的公告
     *
     * @param activityNum    活动编号，不传时查询本组织的全部活动公告
     * @param page           页码
     * @param size           每页条数
     * @param authentication 当前登录用户信息
     * @return 活动公告分页结果，含活动名称
     */
    @RequestMapping(value = "/announcements", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse announcements(@RequestParam(value = "activityNum", required = false) Integer activityNum,
                                     @RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     Authentication authentication) {
        return PageSupport.toResponse(
                activityAnnouncementService.pageByOrganization(loginIdOf(authentication), activityNum, page, size));
    }

    /**
     * 发布或修改志愿活动公告，新增成功后向该活动已通过报名的志愿者发送站内通知
     *
     * @param announcementNum 公告编号，新增时不传
     * @param activityNum     公告所属活动编号
     * @param name            公告标题
     * @param detail          公告内容
     * @param authentication  当前登录用户信息
     * @return 处理结果信息
     */
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

    /**
     * 逻辑删除志愿活动公告
     *
     * @param announcementNum 公告编号
     * @param authentication  当前登录用户信息
     * @return 处理结果信息
     */
    @RequestMapping(value = "/announcement/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse deleteAnnouncement(@RequestParam(value = "announcementNum") Integer announcementNum,
                                          Authentication authentication) {
        String message = activityAnnouncementService.deleteAnnouncement(
                loginIdOf(authentication), announcementNum);
        return respond(message, ActivityAnnouncementService.MESSAGE_DELETE_SUCCESS);
    }

    /**
     * 取当前登录志愿者组织的登录账号
     */
    private String loginIdOf(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getUsername();
    }

    /**
     * 按业务层返回的中文提示组装响应结果：与成功提示一致时视为处理成功
     */
    private ApiResponse respond(String message, String successMessage) {
        return successMessage.equals(message) ? ApiResponse.success(message) : ApiResponse.fail(message);
    }
}
