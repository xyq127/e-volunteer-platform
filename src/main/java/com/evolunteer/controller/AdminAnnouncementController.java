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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台管理员内容管理控制器：面向平台管理员提供通知公告的分页查询、发布、删除，
 * 公告附件的上传、删除与查询，以及志愿秀的查询与删除能力，保证门户内容真实可用。
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin")
public class AdminAnnouncementController {

    @Autowired
    AnnouncementService announcementService;

    @Autowired
    ShowService showService;

    /**
     * 分页查询通知公告，每条公告附带附件数量
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 关键字，按公告标题与公告内容模糊匹配
     * @return 通知公告分页结果
     */
    @RequestMapping(value = "/announcement/list", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse list(@RequestParam(value = "page", required = false) Integer page,
                            @RequestParam(value = "size", required = false) Integer size,
                            @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(announcementService.page(page, size, keyword));
    }

    /**
     * 发布或修改通知公告，新增时返回生成的公告编号
     *
     * @param policyannouncementNum 公告编号，新增时可不传
     * @param name                  公告标题
     * @param detail                公告内容
     * @param authentication        当前登录用户信息
     * @return 处理结果，新增公告时返回公告编号
     */
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

    /**
     * 逻辑删除通知公告，同时清理公告附件记录与物理文件
     *
     * @param policyannouncementNum 公告编号
     * @return 处理结果
     */
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

    /**
     * 上传公告附件，文件保存到服务器文件目录并写入附件表
     *
     * @param policyannouncementNum 公告编号
     * @param file                  上传文件
     * @return 处理结果
     */
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

    /**
     * 删除公告附件，同时逻辑删除附件记录并删除服务器上的物理文件
     *
     * @param policyfileNum 附件编号
     * @return 处理结果
     */
    @RequestMapping(value = "/announcement/file/delete", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse deleteFile(@RequestParam(value = "policyfileNum") Integer policyfileNum) {
        if (!announcementService.deleteFile(policyfileNum)) {
            return ApiResponse.fail("该附件不存在或已被删除");
        }
        return ApiResponse.success("附件已删除");
    }

    /**
     * 查询公告的附件列表
     *
     * @param policyannouncementNum 公告编号
     * @return 附件列表
     */
    @RequestMapping(value = "/announcement/files", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse files(@RequestParam(value = "policyannouncementNum") Integer policyannouncementNum) {
        List<PolicyFile> files = announcementService.listFiles(policyannouncementNum);
        return ApiResponse.success().add("files", files);
    }

    /**
     * 分页查询志愿秀，包含分享志愿者姓名、关联活动名称与首图路径
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 关键字，按分享内容与分享志愿者姓名模糊匹配
     * @return 志愿秀分页结果
     */
    @RequestMapping(value = "/review/shows", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse shows(@RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "size", required = false) Integer size,
                             @RequestParam(value = "keyword", required = false) String keyword) {
        return PageSupport.toResponse(showService.pageAdmin(page, size, keyword));
    }

    /**
     * 逻辑删除志愿秀，删除操作写入操作审计日志
     *
     * @param showNum        志愿秀编号
     * @param authentication 当前登录用户信息
     * @param request        当前请求，用于获取操作来源 IP
     * @return 处理结果
     */
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

    /**
     * 判断服务层返回的处理结果是否成功
     */
    private boolean isSuccess(Map<Object, Object> result) {
        Object ok = result.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
