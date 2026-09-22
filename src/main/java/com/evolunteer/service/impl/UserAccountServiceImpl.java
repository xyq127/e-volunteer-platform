package com.evolunteer.service.impl;

import com.evolunteer.entity.UserAccount;
import com.evolunteer.mapper.UserAccountMapper;
import com.evolunteer.service.UserAccountService;
import com.evolunteer.utils.RandomPasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 登录账号业务实现类：自助改密需要校验原密码，管理员重置直接生成随机初始密码，
 * 两者都通过密码编码器生成加盐密文后再写库。
 */
@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {

    /**
     * 新密码最小长度
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 新密码最大长度
     */
    private static final int MAX_PASSWORD_LENGTH = 20;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 自助修改登录密码
     *
     * @param accountId   登录账号
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    public Map<Object, Object> changePassword(String accountId, String oldPassword, String newPassword) {

        Map<Object, Object> result = new HashMap<>();
        result.put("ok", 0);

        UserAccount userAccount = userAccountMapper.selectById(accountId);
        if (userAccount == null) {
            result.put("msg", "未找到登录账号，请重新登录后再试");
            return result;
        }
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, userAccount.getPassword())) {
            result.put("msg", "原密码不正确，请重新输入");
            return result;
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH
                || newPassword.length() > MAX_PASSWORD_LENGTH) {
            result.put("msg", "新密码长度需为 " + MIN_PASSWORD_LENGTH + " 至 " + MAX_PASSWORD_LENGTH + " 位");
            return result;
        }
        if (passwordEncoder.matches(newPassword, userAccount.getPassword())) {
            result.put("msg", "新密码不能与原密码相同");
            return result;
        }
        if (newPassword.matches(".*\\s.*")) {
            result.put("msg", "新密码不能包含空格");
            return result;
        }

        userAccountMapper.updatePasswordById(accountId, passwordEncoder.encode(newPassword));
        log.info("账号修改登录密码，账号：{}", accountId);
        result.put("ok", 1);
        result.put("msg", "登录密码已修改，请使用新密码重新登录");
        return result;
    }

    /**
     * 平台管理员重置账号密码，重置后返回一次性的新密码
     *
     * @param accountId 登录账号
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与重置后的新密码 password
     */
    @Override
    public Map<Object, Object> resetPassword(String accountId) {

        Map<Object, Object> result = new HashMap<>();
        result.put("ok", 0);

        if (!exists(accountId)) {
            result.put("msg", "未找到对应的登录账号");
            return result;
        }

        String newPassword = RandomPasswordUtil.randomPassword();
        userAccountMapper.updatePasswordById(accountId, passwordEncoder.encode(newPassword));
        log.info("平台管理员重置账号密码，账号：{}", accountId);
        result.put("ok", 1);
        result.put("password", newPassword);
        result.put("msg", "密码已重置，请将新密码告知账号使用人");
        return result;
    }

    /**
     * 判断账号是否存在
     *
     * @param accountId 登录账号
     * @return 账号存在返回 true
     */
    @Override
    public boolean exists(String accountId) {
        return accountId != null && userAccountMapper.selectById(accountId) != null;
    }
}
