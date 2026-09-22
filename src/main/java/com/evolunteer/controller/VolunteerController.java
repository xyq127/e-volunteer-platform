package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.ServiceCertificate;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.entity.VolunteerProfileView;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.enums.VolunteerSkillEnum;
import com.evolunteer.enums.VolunteerStarEnum;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.CertificateService;
import com.evolunteer.service.MatchingService;
import com.evolunteer.service.NotificationService;
import com.evolunteer.service.ParticipationService;
import com.evolunteer.service.UserAccountService;
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

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/volunteer")
public class VolunteerController {

    @Autowired
    VolunteerService volunteerService;

    @Autowired
    ParticipationService participationService;

    @Autowired
    MatchingService matchingService;

    @Autowired
    CertificateService certificateService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    AuditLogService auditLogService;

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse profile(Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return ApiResponse.success().add("profile", buildProfileView(volunteer));
    }

    @RequestMapping(value = "/profile/save", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse saveProfile(@RequestParam(value = "skills", required = false) String skills,
                                   @RequestParam(value = "latitude", required = false) String latitude,
                                   @RequestParam(value = "longitude", required = false) String longitude,
                                   Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        List<String> skillNames = new ArrayList<>();
        if (skills != null) {
            for (String skillName : skills.split(",")) {
                if (skillName.trim().isEmpty()) {
                    continue;
                }
                if (!VolunteerSkillEnum.isSupported(skillName)) {
                    return ApiResponse.fail("服务技能标签不正确：" + skillName.trim());
                }
                skillNames.add(skillName.trim());
            }
        }

        BigDecimal latitudeValue;
        BigDecimal longitudeValue;
        try {
            latitudeValue = parseCoordinate(latitude, 90);
            longitudeValue = parseCoordinate(longitude, 180);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }

        boolean saved = volunteerService.saveProfile(volunteer.getVolunteerNum(), skillNames,
                latitudeValue, longitudeValue);
        if (!saved) {
            return ApiResponse.fail("档案保存失败，请稍后重试");
        }
        log.info("志愿者保存个人档案，志愿者编号：{}，技能标签数量：{}", volunteer.getVolunteerId(), skillNames.size());
        return ApiResponse.success("档案已保存");
    }

    @RequestMapping(value = "/password/change", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse changePassword(@RequestParam(value = "oldPassword") String oldPassword,
                                      @RequestParam(value = "newPassword") String newPassword,
                                      Authentication authentication, HttpServletRequest request) {

        String loginId = operator(authentication);
        Map<Object, Object> result = userAccountService.changePassword(loginId, oldPassword, newPassword);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            return ApiResponse.fail(msg);
        }
        auditLogService.record(loginId, "ROLE_VOLUNTEER", AuditActionEnum.PASSWORD_CHANGE,
                "登录账号 " + loginId, msg, request.getRemoteAddr());
        return ApiResponse.success(msg);
    }

    @RequestMapping(value = "/plaza", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse plaza(@RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "size", required = false) Integer size,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return PageSupport.toResponse(matchingService.pageRecommendActivities(
                volunteer.getVolunteerNum(), keyword, page, size));
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse apply(@RequestParam(value = "activityNum") Integer activityNum,
                             Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        Map<Object, Object> result = participationService.applyActivity(volunteer.getVolunteerNum(), activityNum);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            log.warn("志愿者报名未成功，志愿者编号：{}，活动编号：{}，原因：{}", volunteer.getVolunteerId(), activityNum, msg);
            return ApiResponse.fail(msg);
        }
        log.info("志愿者报名志愿活动，志愿者编号：{}，活动编号：{}，结果：{}", volunteer.getVolunteerId(), activityNum, msg);
        return ApiResponse.success(msg);
    }

    @RequestMapping(value = "/myParticipations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse myParticipations(@RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "size", required = false) Integer size,
                                        Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return PageSupport.toResponse(participationService.pageVolunteerParticipations(
                volunteer.getVolunteerNum(), page, size));
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse cancel(@RequestParam(value = "participateNum") Integer participateNum,
                              Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        Map<Object, Object> result = participationService.cancelParticipate(participateNum,
                volunteer.getVolunteerNum());
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            return ApiResponse.fail(msg);
        }
        log.info("志愿者撤回报名，报名编号：{}，结果：{}", participateNum, msg);
        return ApiResponse.success(msg);
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse confirm(@RequestParam(value = "participateNum") Integer participateNum,
                               @RequestParam(value = "confirmState") Integer confirmState,
                               Authentication authentication) {

        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }

        Map<Object, Object> result = participationService.confirmParticipate(participateNum,
                volunteer.getVolunteerNum(), confirmState);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            return ApiResponse.fail(msg);
        }
        log.info("志愿者确认参加状态，报名编号：{}，确认状态：{}，结果：{}", participateNum, confirmState, msg);
        return ApiResponse.success(msg);
    }

    @RequestMapping(value = "/certificate", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse certificate(Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        ServiceCertificate certificate = certificateService.issueCertificate(volunteer.getVolunteerNum());
        if (certificate == null) {
            return ApiResponse.fail("未找到志愿者信息，无法生成服务证明");
        }
        return ApiResponse.success().add("certificate", certificate);
    }

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse notifications(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     Authentication authentication) {
        return PageSupport.toResponse(notificationService.page(operator(authentication), page, size));
    }

    @RequestMapping(value = "/notifications/unreadCount", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse unreadCount(Authentication authentication) {
        return ApiResponse.success().add("count", notificationService.unreadCount(operator(authentication)));
    }

    @RequestMapping(value = "/notifications/read", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse readNotification(@RequestParam(value = "notificationNum") Integer notificationNum,
                                        Authentication authentication) {
        boolean marked = notificationService.markRead(operator(authentication), notificationNum);
        if (!marked) {
            return ApiResponse.fail("未找到对应的通知");
        }
        return ApiResponse.success("通知已标记为已读");
    }

    @RequestMapping(value = "/notifications/readAll", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse readAllNotifications(Authentication authentication) {
        int updated = notificationService.markAllRead(operator(authentication));
        return ApiResponse.success("已标记 " + updated + " 条通知为已读");
    }

    private Volunteer currentVolunteer(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return volunteerService.getByLoginId(user.getUsername());
    }

    private String operator(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getUsername();
    }

    private VolunteerProfileView buildProfileView(Volunteer volunteer) {

        Double totalDuration = volunteer.getVolunteerTotalduration();
        VolunteerStarEnum star = VolunteerStarEnum.of(totalDuration);
        VolunteerStarEnum nextStar = VolunteerStarEnum.nextStar(totalDuration);

        VolunteerProfileView profile = new VolunteerProfileView();
        profile.setVolunteerId(volunteer.getVolunteerId());
        profile.setVolunteerName(volunteer.getVolunteerName());
        profile.setVolunteerTel(volunteer.getVolunteerTel());
        profile.setVolunteerGender(volunteer.getVolunteerGender());
        profile.setVolunteerBirth(volunteer.getVolunteerBirth());
        profile.setVolunteerCredit(volunteer.getVolunteerCredit());
        profile.setVolunteerNoshow(volunteer.getVolunteerNoshow());
        profile.setVolunteerTotalDuration(totalDuration);
        profile.setLatitude(volunteer.getVolunteerLatitude());
        profile.setLongitude(volunteer.getVolunteerLongitude());
        profile.setSkills(volunteerService.listSkillNames(volunteer.getVolunteerNum()));
        profile.setStarLevel(star.getStarName());
        profile.setNextLevelName(star.getNextStarName());
        profile.setNextLevelHours(nextStar == null ? null : nextStar.getMinHours());
        profile.setHoursToNextLevel(VolunteerStarEnum.hoursToNextStar(totalDuration));
        return profile;
    }

    private BigDecimal parseCoordinate(String value, int maxAbsolute) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        BigDecimal coordinate;
        try {
            coordinate = new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("定位坐标不正确，请重新获取当前位置");
        }
        if (coordinate.abs().compareTo(new BigDecimal(maxAbsolute)) > 0) {
            throw new IllegalArgumentException("定位坐标超出合理范围，请重新获取当前位置");
        }
        return coordinate;
    }

    private boolean isSuccess(Map<Object, Object> result) {
        Object ok = result.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
