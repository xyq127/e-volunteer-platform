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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 操作审计业务实现类：审计记录属于旁路信息，写入失败只记录日志，不影响业务结果。
 */
@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    /**
     * 记录一条操作审计
     *
     * @param operator 操作账号
     * @param role     操作角色
     * @param action   操作类型
     * @param target   操作对象
     * @param detail   操作详情
     * @param ip       操作来源 IP
     */
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

    /**
     * 分页查询审计日志
     *
     * @param action   操作类型编码，为空时查询全部
     * @param operator 操作账号，为空时查询全部
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 审计日志分页结果
     */
    @Override
    public IPage<AuditLog> page(String action, String operator, Integer pageNum, Integer pageSize) {
        Page<AuditLog> page = PageSupport.of(pageNum, pageSize);
        return auditLogMapper.selectPageByCondition(page,
                PageSupport.normalizeKeyword(action), PageSupport.normalizeKeyword(operator));
    }

    /**
     * 截断超长文本，避免超出字段长度导致写入失败
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
