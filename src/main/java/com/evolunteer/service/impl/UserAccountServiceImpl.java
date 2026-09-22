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

@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final int MAX_PASSWORD_LENGTH = 20;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Override
    public boolean exists(String accountId) {
        return accountId != null && userAccountMapper.selectById(accountId) != null;
    }
}
