package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.Volunteer;

import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台账号管理业务接口：面向平台管理员提供志愿者组织名单与志愿者名单查询、组织账号开通、
 * 账号启停与密码重置能力，登录账号与账号状态统一保存在统一用户表 e_user。
 */
public interface AccountAdminService {

    /**
     * 分页查询志愿者组织名单，关联登录账号状态并统计组织已申报的活动数量，
     * 已被停用的组织同样列出，便于平台管理员查看或重新启用
     *
     * @param keyword 组织登录账号或组织名称关键字，可为空
     * @param page    页码
     * @param size    每页条数
     * @return 志愿者组织分页结果
     */
    IPage<Organization> pageOrganizations(String keyword, Integer page, Integer size);

    /**
     * 分页查询志愿者名单，关联登录账号状态，已被停用的志愿者同样列出
     *
     * @param keyword 志愿者编号、姓名或联系电话关键字，可为空
     * @param page    页码
     * @param size    每页条数
     * @return 志愿者分页结果
     */
    IPage<Volunteer> pageVolunteers(String keyword, Integer page, Integer size);

    /**
     * 开通志愿者组织账号：同时写入组织信息与登录账号，
     * 未指定初始密码时由平台生成随机初始密码并在返回结果中回传
     *
     * @param loginId          操作人登录账号（平台管理员账号）
     * @param organizationId   组织登录账号
     * @param organizationName 组织名称
     * @param password         初始密码，为空时由平台生成
     * @param establishDate    成立日期文本，可为空
     * @param introduction     组织简介，可为空
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与平台生成的随机初始密码 password
     */
    Map<Object, Object> createOrganization(String loginId, String organizationId, String organizationName,
                                           String password, String establishDate, String introduction);

    /**
     * 启用或停用组织账号与志愿者账号，停用后账号无法登录且业务记录同步标记为删除，
     * 平台管理员账号不允许在账号管理中停用
     *
     * @param loginId   操作人登录账号（平台管理员账号）
     * @param accountId 目标账号
     * @param enabled   账号状态，1 启用、0 停用
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> setEnabled(String loginId, String accountId, Integer enabled);

    /**
     * 重置账号密码，重置后返回一次性的新密码
     *
     * @param loginId   操作人登录账号（平台管理员账号）
     * @param accountId 目标账号
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与重置后的新密码 password
     */
    Map<Object, Object> resetPassword(String loginId, String accountId);
}
