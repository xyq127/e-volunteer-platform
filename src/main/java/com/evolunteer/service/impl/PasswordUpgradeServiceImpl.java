package com.evolunteer.service.impl;

import com.evolunteer.mapper.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.stereotype.Service;

@Service
public class PasswordUpgradeServiceImpl implements UserDetailsPasswordService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        userAccountMapper.updatePasswordById(user.getUsername(), newPassword);
        return User.withUsername(user.getUsername())
                .password(newPassword)
                .authorities(user.getAuthorities())
                .build();
    }
}
