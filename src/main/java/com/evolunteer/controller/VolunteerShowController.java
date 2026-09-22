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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者志愿秀控制器：面向志愿者提供本人志愿秀分页查询、发布志愿秀、上传志愿秀图片
 * 与点赞取消点赞能力，志愿者只能发布与维护本人的志愿秀内容。
 */
@Slf4j
@Controller
@RequestMapping(value = "/volunteer/content")
public class VolunteerShowController {

    @Autowired
    VolunteerService volunteerService;

    @Autowired
    ShowService showService;

    /**
     * 分页查询本人发布的志愿秀
     *
     * @param page           页码
     * @param size           每页条数
     * @param authentication 当前登录用户信息
     * @return 志愿秀分页结果
     */
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

    /**
     * 发布志愿秀，可关联本人已通过报名的志愿活动
     *
     * @param detail         分享内容
     * @param activityNum    关联活动编号，可不传
     * @param authentication 当前登录用户信息
     * @return 处理结果，包含生成的志愿秀编号
     */
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

    /**
     * 为本人发布的志愿秀上传图片
     *
     * @param showNum        志愿秀编号
     * @param file           上传图片
     * @param authentication 当前登录用户信息
     * @return 处理结果
     */
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

    /**
     * 点赞或取消点赞志愿秀，同一志愿者对同一志愿秀只计一次
     *
     * @param showNum        志愿秀编号
     * @param authentication 当前登录用户信息
     * @return 处理结果，包含是否已点赞与最新点赞次数
     */
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
     * 判断服务层返回的处理结果是否成功
     */
    private boolean isSuccess(Map<Object, Object> result) {
        Object ok = result.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
