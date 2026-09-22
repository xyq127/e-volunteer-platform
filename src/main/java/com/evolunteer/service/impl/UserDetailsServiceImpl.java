package com.evolunteer.service.impl;

import com.evolunteer.entity.UserAccount;
import com.evolunteer.mapper.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 登录认证业务实现类：根据登录账号查询统一用户表 e_user，装配登录账号、密码与角色权限，
 * 志愿者组织与平台管理员共用同一套认证入口。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    /**
     * 按登录账号加载用户认证信息
     *
     * @param input 登录账号（志愿者组织账号或平台管理员账号）
     * @return Spring Security 用户信息，包含密码密文与角色权限
     * @throws UsernameNotFoundException 账号不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {

        List<UserAccount> userAccounts = userAccountMapper.selectByLoginId(input);
        if (userAccounts.isEmpty()) {
            throw new UsernameNotFoundException("账号不存在：" + input);
        }
        UserAccount userAccount = userAccounts.get(0);

        return new User(userAccount.getId(), userAccount.getPassword(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(userAccount.getRole()));
    }
}
