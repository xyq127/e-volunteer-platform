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

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {

        List<UserAccount> userAccounts = userAccountMapper.selectByLoginId(input);
        if (userAccounts.isEmpty()) {
            throw new UsernameNotFoundException("账号不存在：" + input);
        }
        UserAccount userAccount = userAccounts.get(0);

        boolean disabled = userAccount.getEnabled() != null && userAccount.getEnabled() == 0;

        return User.withUsername(userAccount.getId())
                .password(userAccount.getPassword())
                .disabled(disabled)
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList(userAccount.getRole()))
                .build();
    }
}
