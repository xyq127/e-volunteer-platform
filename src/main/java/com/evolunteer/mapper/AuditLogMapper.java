package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.AuditLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 操作审计表 audit_log 的数据访问接口，提供关键操作的写入与按条件分页查询能力。
 *
 * @Entity com.evolunteer.entity.AuditLog
 */
@Repository
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 按操作类型与操作账号分页查询审计日志
     *
     * @param page     分页对象
     * @param action   操作类型编码，为空时查询全部类型
     * @param operator 操作账号，为空时查询全部账号
     * @return 审计日志分页结果
     */
    IPage<AuditLog> selectPageByCondition(Page<AuditLog> page,
                                          @Param("action") String action,
                                          @Param("operator") String operator);
}
