package com.evolunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.AccountAdminService;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.ExportService;
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台账号管理控制器：面向平台管理员提供志愿者组织名单与志愿者名单查询、组织账号开通、
 * 账号启停、密码重置与名单导出能力，所有写操作统一记录操作审计，便于事后追溯。
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/account")
public class AdminAccountController {

    /**
     * 账号状态：启用
     */
    private static final Integer ENABLED = 1;

    /**
     * 账号状态：停用
     */
    private static final Integer DISABLED = 0;

    /**
     * 导出文件名中的日期格式
     */
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    AccountAdminService accountAdminService;

    @Autowired
    ExportService exportService;

    @Autowired
    AuditLogService auditLogService;

    /**
     * 分页查询志愿者组织名单，含已被停用的组织与组织已申报的活动数量
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 组织登录账号或组织名称关键字
     * @return 志愿者组织分页结果
     */
    @RequestMapping(value = "/organizations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse pageOrganizations(@RequestParam(value = "page", required = false) Integer page,
                                         @RequestParam(value = "size", required = false) Integer size,
                                         @RequestParam(value = "keyword", required = false) String keyword) {
        IPage<Organization> organizations = accountAdminService.pageOrganizations(keyword, page, size);
        return PageSupport.toResponse(organizations);
    }

    /**
     * 分页查询志愿者名单，含已被停用的志愿者
     *
     * @param page    页码
     * @param size    每页条数
     * @param keyword 志愿者编号、姓名或联系电话关键字
     * @return 志愿者分页结果
     */
    @RequestMapping(value = "/volunteers", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse pageVolunteers(@RequestParam(value = "page", required = false) Integer page,
                                      @RequestParam(value = "size", required = false) Integer size,
                                      @RequestParam(value = "keyword", required = false) String keyword) {
        IPage<Volunteer> volunteers = accountAdminService.pageVolunteers(keyword, page, size);
        return PageSupport.toResponse(volunteers);
    }

    /**
     * 开通志愿者组织账号，未填写初始密码时由平台生成随机初始密码并随响应返回
     *
     * @param organizationId   组织登录账号
     * @param organizationName 组织名称
     * @param password         初始密码，可为空
     * @param establishDate    成立日期
     * @param description      组织简介
     * @param authentication   当前登录用户信息
     * @param request          HTTP 请求
     * @return 开通处理结果，含平台生成的初始密码 password
     */
    @RequestMapping(value = "/organization/create", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse createOrganization(@RequestParam(value = "organizationId") String organizationId,
                                          @RequestParam(value = "organizationName") String organizationName,
                                          @RequestParam(value = "password", required = false) String password,
                                          @RequestParam(value = "establishDate", required = false) String establishDate,
                                          @RequestParam(value = "description", required = false) String description,
                                          Authentication authentication,
                                          HttpServletRequest request) {

        User user = (User) authentication.getPrincipal();
        String loginId = user.getUsername();

        Map<Object, Object> result = accountAdminService.createOrganization(
                loginId, organizationId, organizationName, password, establishDate, description);
        auditLogService.record(loginId, roleOf(authentication), AuditActionEnum.ACCOUNT_CREATE,
                organizationId, "开通组织账号：" + messageOf(result), request.getRemoteAddr());
        log.info("平台管理员开通志愿者组织账号，组织账号：{}，处理结果：{}", organizationId, result.get("msg"));

        ApiResponse response = responseOf(result);
        addPassword(response, result);
        return response;
    }

    /**
     * 启用或停用组织账号与志愿者账号，平台管理员账号不允许停用
     *
     * @param accountId      目标账号
     * @param enabled        账号状态，1 启用、0 停用
     * @param authentication 当前登录用户信息
     * @param request        HTTP 请求
     * @return 启停处理结果
     */
    @RequestMapping(value = "/enabled", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse setEnabled(@RequestParam(value = "accountId") String accountId,
                                  @RequestParam(value = "enabled") Integer enabled,
                                  Authentication authentication,
                                  HttpServletRequest request) {

        if (!ENABLED.equals(enabled) && !DISABLED.equals(enabled)) {
            return ApiResponse.fail("账号状态参数不正确，请刷新页面后重试");
        }

        User user = (User) authentication.getPrincipal();
        String loginId = user.getUsername();

        Map<Object, Object> result = accountAdminService.setEnabled(loginId, accountId, enabled);
        auditLogService.record(loginId, roleOf(authentication), AuditActionEnum.ACCOUNT_ENABLED_CHANGE,
                accountId, "账号启停：" + messageOf(result), request.getRemoteAddr());
        log.info("平台管理员{}账号，目标账号：{}，处理结果：{}",
                ENABLED.equals(enabled) ? "启用" : "停用", accountId, result.get("msg"));
        return responseOf(result);
    }

    /**
     * 重置账号密码，重置后的新密码随响应返回，由平台管理员转告账号使用人
     *
     * @param accountId      目标账号
     * @param authentication 当前登录用户信息
     * @param request        HTTP 请求
     * @return 重置处理结果，含一次性新密码 password
     */
    @RequestMapping(value = "/password/reset", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse resetPassword(@RequestParam(value = "accountId") String accountId,
                                     Authentication authentication,
                                     HttpServletRequest request) {

        User user = (User) authentication.getPrincipal();
        String loginId = user.getUsername();

        Map<Object, Object> result = accountAdminService.resetPassword(loginId, accountId);
        auditLogService.record(loginId, roleOf(authentication), AuditActionEnum.PASSWORD_RESET,
                accountId, "重置账号密码：" + messageOf(result), request.getRemoteAddr());
        log.info("平台管理员重置账号密码，目标账号：{}，处理结果：{}", accountId, result.get("msg"));

        ApiResponse response = responseOf(result);
        addPassword(response, result);
        return response;
    }

    /**
     * 导出志愿者名册
     *
     * @param keyword  志愿者编号、姓名或联系电话关键字
     * @param response HTTP 响应，CSV 内容直接写入响应流
     * @throws IOException 响应写入失败时抛出
     */
    @RequestMapping(value = "/export/volunteers", method = RequestMethod.GET)
    public void exportVolunteers(@RequestParam(value = "keyword", required = false) String keyword,
                                 HttpServletResponse response) throws IOException {
        String fileName = "volunteers-" + LocalDate.now().format(FILE_DATE_FORMAT) + ".csv";
        writeCsv(response, fileName, exportService.exportVolunteers(keyword));
        log.info("平台管理员导出志愿者名册，关键字：{}，导出文件：{}", keyword, fileName);
    }

    /**
     * 导出活动清单
     *
     * @param keyword  活动名称或活动编号关键字
     * @param response HTTP 响应，CSV 内容直接写入响应流
     * @throws IOException 响应写入失败时抛出
     */
    @RequestMapping(value = "/export/activities", method = RequestMethod.GET)
    public void exportActivities(@RequestParam(value = "keyword", required = false) String keyword,
                                 HttpServletResponse response) throws IOException {
        String fileName = "activities-" + LocalDate.now().format(FILE_DATE_FORMAT) + ".csv";
        writeCsv(response, fileName, exportService.exportActivities(keyword));
        log.info("平台管理员导出活动清单，关键字：{}，导出文件：{}", keyword, fileName);
    }

    /**
     * 把 CSV 内容作为附件写入响应，内容自带 UTF-8 字节序标记，中文不会乱码
     *
     * @param response HTTP 响应
     * @param fileName 导出文件名
     * @param csv      带 UTF-8 字节序标记的 CSV 文本
     * @throws IOException 响应写入失败时抛出
     */
    private void writeCsv(HttpServletResponse response, String fileName, String csv) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.getWriter().write(csv);
    }

    /**
     * 按服务层处理结果组装响应，处理失败时返回存储过程给出的提示信息
     *
     * @param result 服务层处理结果
     * @return 统一响应结果
     */
    private ApiResponse responseOf(Map<Object, Object> result) {
        return isOk(result) ? ApiResponse.success(messageOf(result)) : ApiResponse.fail(messageOf(result));
    }

    /**
     * 判断服务层处理结果是否成功，存储过程的 ok 出参为 1 表示处理成功
     *
     * @param result 服务层处理结果
     * @return 处理成功返回 true
     */
    private boolean isOk(Map<Object, Object> result) {
        Object ok = result == null ? null : result.get("ok");
        return ok != null && "1".equals(ok.toString());
    }

    /**
     * 把平台生成的一次性密码附加到响应：仅在处理成功时附加，失败时密码不会随响应返回
     *
     * @param response 统一响应结果
     * @param result   服务层处理结果
     */
    private void addPassword(ApiResponse response, Map<Object, Object> result) {
        if (!isOk(result)) {
            return;
        }
        Object password = result.get("password");
        if (password != null) {
            response.add("password", password);
        }
    }

    /**
     * 取处理结果中的提示信息
     *
     * @param result 服务层处理结果
     * @return 提示信息
     */
    private String messageOf(Map<Object, Object> result) {
        Object msg = result == null ? null : result.get("msg");
        return msg == null ? "操作处理失败，请稍后重试" : msg.toString();
    }

    /**
     * 取当前登录账号的角色权限，用于写入操作审计
     *
     * @param authentication 当前登录用户信息
     * @return 角色权限编码
     */
    private String roleOf(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "ROLE_ADMIN";
        }
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}
