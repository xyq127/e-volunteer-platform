package com.evolunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.AuditActionView;
import com.evolunteer.entity.AuditLog;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "/admin/audit")
public class AdminAuditController {

    @Autowired
    AuditLogService auditLogService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse list(@RequestParam(value = "page", required = false) Integer page,
                            @RequestParam(value = "size", required = false) Integer size,
                            @RequestParam(value = "action", required = false) String action,
                            @RequestParam(value = "operator", required = false) String operator) {
        IPage<AuditLog> auditLogs = auditLogService.page(action, operator, page, size);
        log.info("平台管理员查询操作审计日志，操作类型：{}，操作账号：{}", action, operator);
        return PageSupport.toResponse(auditLogs).add("actions", actionOptions());
    }

    private List<AuditActionView> actionOptions() {
        AuditActionEnum[] actions = AuditActionEnum.values();
        List<AuditActionView> options = new ArrayList<>(actions.length);
        for (AuditActionEnum action : actions) {
            options.add(new AuditActionView(action.getActionCode(), action.getActionName()));
        }
        return options;
    }
}
