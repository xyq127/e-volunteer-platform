package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.AuditLog;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.mapper.AuditLogMapper;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public void record(String operator, String role, AuditActionEnum action, String target, String detail, String ip) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditOperator(operator);
            auditLog.setAuditRole(role);
            auditLog.setAuditAction(action.getActionCode());
            auditLog.setAuditTarget(truncate(target, 200));
            auditLog.setAuditDetail(truncate(detail, 500));
            auditLog.setAuditIp(ip);
            auditLog.setAuditTime(new Date());
            auditLogMapper.insert(auditLog);
        } catch (RuntimeException e) {
            log.warn("操作审计写入失败，操作账号：{}，操作类型：{}", operator, action.getActionCode(), e);
        }
    }

    @Override
    public IPage<AuditLog> page(String action, String operator, Integer pageNum, Integer pageSize) {
        Page<AuditLog> page = PageSupport.of(pageNum, pageSize);
        return auditLogMapper.selectPageByCondition(page,
                PageSupport.normalizeKeyword(action), PageSupport.normalizeKeyword(operator));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
