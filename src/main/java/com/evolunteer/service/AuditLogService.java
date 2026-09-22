package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.AuditLog;
import com.evolunteer.enums.AuditActionEnum;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 操作审计业务接口：记录平台管理员与志愿者组织的关键操作，并提供按操作类型与操作账号的查询能力，
 * 使审核、复核、结算、账号管理与密码重置等敏感行为可追溯。
 */
public interface AuditLogService {

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
    void record(String operator, String role, AuditActionEnum action, String target, String detail, String ip);

    /**
     * 分页查询审计日志
     *
     * @param action   操作类型编码，为空时查询全部
     * @param operator 操作账号，为空时查询全部
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 审计日志分页结果
     */
    IPage<AuditLog> page(String action, String operator, Integer pageNum, Integer pageSize);
}
