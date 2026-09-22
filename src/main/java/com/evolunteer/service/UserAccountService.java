package com.evolunteer.service;

import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 登录账号业务接口：提供自助修改密码与平台管理员重置密码能力，
 * 密码统一以加盐摘要保存，修改与重置都会在审计日志中留痕。
 */
public interface UserAccountService {

    /**
     * 自助修改登录密码
     *
     * @param accountId   登录账号
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> changePassword(String accountId, String oldPassword, String newPassword);

    /**
     * 平台管理员重置账号密码，重置后返回一次性的新密码
     *
     * @param accountId 登录账号
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与重置后的新密码 password
     */
    Map<Object, Object> resetPassword(String accountId);

    /**
     * 判断账号是否存在
     *
     * @param accountId 登录账号
     * @return 账号存在返回 true
     */
    boolean exists(String accountId);
}
