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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 操作审计查询控制器：面向平台管理员提供关键操作审计日志的分页查询能力，
 * 支持按操作类型与操作账号过滤，并返回操作类型下拉选项供查询页面使用。
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/audit")
public class AdminAuditController {

    @Autowired
    AuditLogService auditLogService;

    /**
     * 分页查询操作审计日志
     *
     * @param page     页码
     * @param size     每页条数
     * @param action   操作类型编码，为空时查询全部类型
     * @param operator 操作账号，为空时查询全部账号
     * @return 审计日志分页结果，extend.actions 为操作类型下拉选项
     */
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

    /**
     * 组装操作类型下拉选项，供查询页面按操作类型过滤
     *
     * @return 操作类型编码与名称列表
     */
    private List<AuditActionView> actionOptions() {
        AuditActionEnum[] actions = AuditActionEnum.values();
        List<AuditActionView> options = new ArrayList<>(actions.length);
        for (AuditActionEnum action : actions) {
            options.add(new AuditActionView(action.getActionCode(), action.getActionName()));
        }
        return options;
    }
}
