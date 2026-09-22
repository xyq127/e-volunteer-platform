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

@Slf4j
@Controller
@RequestMapping(value = "/admin/account")
public class AdminAccountController {

    private static final Integer ENABLED = 1;

    private static final Integer DISABLED = 0;

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    AccountAdminService accountAdminService;

    @Autowired
    ExportService exportService;

    @Autowired
    AuditLogService auditLogService;

    @RequestMapping(value = "/organizations", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse pageOrganizations(@RequestParam(value = "page", required = false) Integer page,
                                         @RequestParam(value = "size", required = false) Integer size,
                                         @RequestParam(value = "keyword", required = false) String keyword) {
        IPage<Organization> organizations = accountAdminService.pageOrganizations(keyword, page, size);
        return PageSupport.toResponse(organizations);
    }

    @RequestMapping(value = "/volunteers", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse pageVolunteers(@RequestParam(value = "page", required = false) Integer page,
                                      @RequestParam(value = "size", required = false) Integer size,
                                      @RequestParam(value = "keyword", required = false) String keyword) {
        IPage<Volunteer> volunteers = accountAdminService.pageVolunteers(keyword, page, size);
        return PageSupport.toResponse(volunteers);
    }

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

    @RequestMapping(value = "/export/volunteers", method = RequestMethod.GET)
    public void exportVolunteers(@RequestParam(value = "keyword", required = false) String keyword,
                                 HttpServletResponse response) throws IOException {
        String fileName = "volunteers-" + LocalDate.now().format(FILE_DATE_FORMAT) + ".csv";
        writeCsv(response, fileName, exportService.exportVolunteers(keyword));
        log.info("平台管理员导出志愿者名册，关键字：{}，导出文件：{}", keyword, fileName);
    }

    @RequestMapping(value = "/export/activities", method = RequestMethod.GET)
    public void exportActivities(@RequestParam(value = "keyword", required = false) String keyword,
                                 HttpServletResponse response) throws IOException {
        String fileName = "activities-" + LocalDate.now().format(FILE_DATE_FORMAT) + ".csv";
        writeCsv(response, fileName, exportService.exportActivities(keyword));
        log.info("平台管理员导出活动清单，关键字：{}，导出文件：{}", keyword, fileName);
    }

    private void writeCsv(HttpServletResponse response, String fileName, String csv) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.getWriter().write(csv);
    }

    private ApiResponse responseOf(Map<Object, Object> result) {
        return isOk(result) ? ApiResponse.success(messageOf(result)) : ApiResponse.fail(messageOf(result));
    }

    private boolean isOk(Map<Object, Object> result) {
        Object ok = result == null ? null : result.get("ok");
        return ok != null && "1".equals(ok.toString());
    }

    private void addPassword(ApiResponse response, Map<Object, Object> result) {
        if (!isOk(result)) {
            return;
        }
        Object password = result.get("password");
        if (password != null) {
            response.add("password", password);
        }
    }

    private String messageOf(Map<Object, Object> result) {
        Object msg = result == null ? null : result.get("msg");
        return msg == null ? "操作处理失败，请稍后重试" : msg.toString();
    }

    private String roleOf(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "ROLE_ADMIN";
        }
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}
