package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.PolicyFile;
import com.evolunteer.service.AnnouncementService;
import com.evolunteer.service.ShowService;
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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminAnnouncementController {

    @Autowired
    AnnouncementService announcementService;

    @Autowired
    ShowService showService;

    @RequestMapping(value = "/announcement/list", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse list(@RequestParam(value = "page", required = false) Integer page,
                            @RequestParam(value = "size", required = false) Integer size,
                            @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(announcementService.page(page, size, keyword));
    }

    @RequestMapping(value = "/announcement/save", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse save(@RequestParam(value = "policyannouncementNum", required = false) Integer policyannouncementNum,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "detail", required = false) String detail,
                            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        Map<Object, Object> result = announcementService.save(policyannouncementNum, name, detail, user.getUsername());
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            log.warn("通知公告保存未成功，公告编号：{}，原因：{}", policyannouncementNum, msg);
            return ApiResponse.fail(msg);
        }
        return ApiResponse.success(msg).add("policyannouncementNum", result.get("policyannouncementNum"));
    }

    @RequestMapping(value = "/announcement/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse delete(@RequestParam(value = "policyannouncementNum") Integer policyannouncementNum) {

        Map<Object, Object> result = announcementService.delete(policyannouncementNum);
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            return ApiResponse.fail(msg);
        }
        return ApiResponse.success(msg);
    }

    @RequestMapping(value = "/announcement/file", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse uploadFile(@RequestParam(value = "policyannouncementNum") Integer policyannouncementNum,
                                  @RequestParam(value = "file") MultipartFile file) {
        try {
            PolicyFile policyFile = announcementService.uploadFile(policyannouncementNum, file);
            log.info("平台管理员上传公告附件，公告编号：{}，附件编号：{}",
                    policyannouncementNum, policyFile.getPolicyfileNum());
            return ApiResponse.success("附件已上传");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/announcement/file/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse deleteFile(@RequestParam(value = "policyfileNum") Integer policyfileNum) {
        if (!announcementService.deleteFile(policyfileNum)) {
            return ApiResponse.fail("该附件不存在或已被删除");
        }
        return ApiResponse.success("附件已删除");
    }

    @RequestMapping(value = "/announcement/files", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse files(@RequestParam(value = "policyannouncementNum") Integer policyannouncementNum) {
        List<PolicyFile> files = announcementService.listFiles(policyannouncementNum);
        return ApiResponse.success().add("files", files);
    }

    @RequestMapping(value = "/review/shows", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse shows(@RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "size", required = false) Integer size,
                             @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(showService.pageAdmin(page, size, keyword));
    }

    @RequestMapping(value = "/review/show/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse deleteShow(@RequestParam(value = "showNum") Integer showNum,
                                  Authentication authentication,
                                  HttpServletRequest request) {

        User user = (User) authentication.getPrincipal();
        Map<Object, Object> result = showService.deleteShow(showNum, user.getUsername(), request.getRemoteAddr());
        String msg = (String) result.get("msg");
        if (!isSuccess(result)) {
            return ApiResponse.fail(msg);
        }
        return ApiResponse.success(msg);
    }

    private boolean isSuccess(Map<Object, Object> result) {
        Object ok = result.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
