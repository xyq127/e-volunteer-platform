package com.evolunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Participation;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.MatchingService;
import com.evolunteer.service.OrganizationService;
import com.evolunteer.service.ParticipationService;
import com.evolunteer.utils.MatchCalculator;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/activity")
public class ActivityController {

    @Autowired
    ActivityService activityService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    ParticipationService participationService;

    @Autowired
    MatchingService matchingService;

    @Autowired
    AuditLogService auditLogService;

    @RequestMapping(value = "/findAuditActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findAuditActivityByNumAndState(@RequestParam(value = "page", required = false) Integer page,
                                                      @RequestParam(value = "size", required = false) Integer size,
                                                      @RequestParam(value = "keyword", required = false) String keyword,
                                                      Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        return PageSupport.toResponse(activityService.pageActivityByOrganization(num, "0", keyword, page, size));
    }

    @RequestMapping(value = "/activityInfo", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse activityInfo(@RequestParam(value = "activityNum") Integer activityNum) {
        List<Activity> activityInfo = activityService.activityInfo(activityNum);
        return ApiResponse.success().add("activityInfo", activityInfo);
    }

    @RequestMapping(value = "/auditActivityDelete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse auditActivityDelete(@RequestParam(value = "activityNum") Integer activityNum,
                                           Authentication authentication) {
        activityService.deleteAct(activityNum);
        log.info("志愿者组织删除审核中的活动，活动编号：{}", activityNum);
        return findAuditActivityByNumAndState(null, null, null, authentication);
    }

    @RequestMapping(value = "/notAuditActivityDelete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse notAuditActivityDelete(@RequestParam(value = "activityNum") Integer activityNum,
                                              Authentication authentication) {
        activityService.deleteAct(activityNum);
        log.info("志愿者组织删除审核未通过的活动，活动编号：{}", activityNum);
        return findNotAuditActivityByNumAndState(null, null, null, authentication);
    }

    @RequestMapping(value = "/findNotStartActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findNotStartActivityByNumAndState(@RequestParam(value = "page", required = false) Integer page,
                                                         @RequestParam(value = "size", required = false) Integer size,
                                                         @RequestParam(value = "keyword", required = false) String keyword,
                                                         Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        return PageSupport.toResponse(activityService.pageActivityByOrganization(num, "1", keyword, page, size));
    }

    @RequestMapping(value = "/volRecruit", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse volRecruit(@RequestParam(value = "activityNum") Integer activityNum,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size,
                                  @RequestParam(value = "keyword", required = false) String keyword) {

        IPage<Participation> result = participationService.pageVolunteerList(activityNum, keyword, page, size);
        for (Participation participation : result.getRecords()) {
            MatchCalculator.MatchResult match = matchingService.matchForActivity(
                    participation.getVolunteerNum(), activityNum);
            if (match != null) {
                participation.setMatchScore(match.getScore());
                participation.setMatchReasons(String.join("、", match.getReasons()));
            }
        }
        return PageSupport.toResponse(result);
    }

    @RequestMapping(value = "/findCarryActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findCarryActivityByNumAndState(@RequestParam(value = "page", required = false) Integer page,
                                                      @RequestParam(value = "size", required = false) Integer size,
                                                      @RequestParam(value = "keyword", required = false) String keyword,
                                                      Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        return PageSupport.toResponse(activityService.pageActivityByOrganization(num, "2", keyword, page, size));
    }

    @RequestMapping(value = "/findEndActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findEndActivityByNumAndState(@RequestParam(value = "page", required = false) Integer page,
                                                    @RequestParam(value = "size", required = false) Integer size,
                                                    @RequestParam(value = "keyword", required = false) String keyword,
                                                    Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        return PageSupport.toResponse(activityService.pageActivityByOrganization(num, "3", keyword, page, size));
    }

    @RequestMapping(value = "/findNotAuditActivityByNumAndState", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse findNotAuditActivityByNumAndState(@RequestParam(value = "page", required = false) Integer page,
                                                         @RequestParam(value = "size", required = false) Integer size,
                                                         @RequestParam(value = "keyword", required = false) String keyword,
                                                         Authentication authentication) {
        Integer num = currentOrganizationNum(authentication);
        return PageSupport.toResponse(activityService.pageActivityByOrganization(num, "4", keyword, page, size));
    }

    @RequestMapping(value = "/startActivity", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse startActivity(@RequestParam(value = "activityNum") Integer activityNum,
                                     Authentication authentication, HttpServletRequest request) {
        return changeState(activityNum, "2", authentication, request);
    }

    @RequestMapping(value = "/finishActivity", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse finishActivity(@RequestParam(value = "activityNum") Integer activityNum,
                                      Authentication authentication, HttpServletRequest request) {
        return changeState(activityNum, "3", authentication, request);
    }

    private ApiResponse changeState(Integer activityNum, String targetState,
                                    Authentication authentication, HttpServletRequest request) {

        User user = (User) authentication.getPrincipal();
        Map<Object, Object> result = activityService.changeActivityState(user.getUsername(), activityNum, targetState);
        String msg = (String) result.get("msg");
        Object ok = result.get("ok");
        if (ok == null || ((Number) ok).intValue() != 1) {
            log.warn("活动状态流转未完成，活动编号：{}，原因：{}", activityNum, msg);
            return ApiResponse.fail(msg);
        }
        auditLogService.record(user.getUsername(), "ROLE_ORGANIZATION", AuditActionEnum.ACTIVITY_STATE_CHANGE,
                "活动编号 " + activityNum, msg, request.getRemoteAddr());
        log.info("志愿活动状态流转，活动编号：{}，目标状态：{}", activityNum, targetState);
        return ApiResponse.success(msg);
    }

    private Integer currentOrganizationNum(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return organizationService.getOrganizationNum(user.getUsername());
    }
}
