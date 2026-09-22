package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.ServiceCertificate;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.entity.VolunteerProfileView;
import com.evolunteer.enums.VolunteerSkillEnum;
import com.evolunteer.enums.VolunteerStarEnum;
import com.evolunteer.service.CertificateService;
import com.evolunteer.service.MatchingService;
import com.evolunteer.service.ParticipationService;
import com.evolunteer.service.VolunteerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者工作台控制器：面向志愿者提供个人档案维护、智能匹配活动广场、活动报名与撤回、
 * 本人报名与签到情况查询以及志愿服务证明查询能力。
 */
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

    /**
     * 查询当前登录志愿者的个人档案与成长进度
     *
     * @param authentication 当前登录用户信息
     * @return 志愿者档案
     */
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse profile(Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return ApiResponse.success().add("profile", buildProfileView(volunteer));
    }

    /**
     * 保存当前登录志愿者的服务技能标签与常住地点，供供需匹配使用
     *
     * @param skills         服务技能标签，多个标签以英文逗号分隔
     * @param latitude       常住地点纬度，未获取到定位时可不传
     * @param longitude      常住地点经度，未获取到定位时可不传
     * @param authentication 当前登录用户信息
     * @return 处理结果
     */
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

    /**
     * 查询与当前登录志愿者匹配的可报名志愿活动，按匹配度降序排列
     *
     * @param authentication 当前登录用户信息
     * @return 匹配活动列表
     */
    @RequestMapping(value = "/plaza", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse plaza(Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return ApiResponse.success().add("activities",
                matchingService.recommendActivities(volunteer.getVolunteerNum()));
    }

    /**
     * 志愿者报名志愿活动，名额已满时自动进入候补队列
     *
     * @param activityNum    活动编号
     * @param authentication 当前登录用户信息
     * @return 处理结果
     */
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

    /**
     * 查询当前登录志愿者的报名记录、签到与服务时长情况
     *
     * @param authentication 当前登录用户信息
     * @return 报名记录列表
     */
    @RequestMapping(value = "/myParticipations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse myParticipations(Authentication authentication) {
        Volunteer volunteer = currentVolunteer(authentication);
        if (volunteer == null) {
            return ApiResponse.fail("未找到志愿者信息，请重新登录后再试");
        }
        return ApiResponse.success().add("participations",
                participationService.listVolunteerParticipations(volunteer.getVolunteerNum()));
    }

    /**
     * 志愿者撤回报名，已通过的报名会释放名额并自动递补候补志愿者
     *
     * @param participateNum 报名编号
     * @param authentication 当前登录用户信息
     * @return 处理结果
     */
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

    /**
     * 查询当前登录志愿者的志愿服务证明，包含累计服务时长、星级与证明校验码
     *
     * @param authentication 当前登录用户信息
     * @return 志愿服务证明
     */
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

    /**
     * 获取当前登录账号对应的志愿者信息
     *
     * @param authentication 当前登录用户信息
     * @return 志愿者信息，账号未关联志愿者时返回 null
     */
    private Volunteer currentVolunteer(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return volunteerService.getByLoginId(user.getUsername());
    }

    /**
     * 组装志愿者档案视图：累计服务时长决定当前星级与成长进度
     */
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

    /**
     * 解析页面提交的经纬度并校验取值范围
     */
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

    /**
     * 判断存储过程返回的处理结果是否成功
     */
    private boolean isSuccess(Map<Object, Object> result) {
        Object ok = result.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
