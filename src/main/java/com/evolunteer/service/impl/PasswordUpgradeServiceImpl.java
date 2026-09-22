package com.evolunteer.service.impl;

import com.evolunteer.mapper.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.stereotype.Service;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 登录密码升级实现类：历史账号使用 MD5 摘要存储密码，登录校验通过后由本实现将密文改写为加盐摘要，
 * 在用户无感知的前提下逐步替换弱摘要存储。
 */
@Service
public class PasswordUpgradeServiceImpl implements UserDetailsPasswordService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    /**
     * 将指定账号的密码密文升级为加盐摘要
     *
     * @param user        当前登录用户信息
     * @param newPassword 升级后的密码密文
     * @return 使用新密文构建的用户信息
     */
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        userAccountMapper.updatePasswordById(user.getUsername(), newPassword);
        return User.withUsername(user.getUsername())
                .password(newPassword)
                .authorities(user.getAuthorities())
                .build();
    }
}
