package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.mapper.OrganizationMapper;
import com.evolunteer.mapper.UserAccountMapper;
import com.evolunteer.service.AccountAdminService;
import com.evolunteer.service.UserAccountService;
import com.evolunteer.utils.DateTimeParser;
import com.evolunteer.utils.PageSupport;
import com.evolunteer.utils.RandomPasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AccountAdminServiceImpl implements AccountAdminService {

    private static final Integer ENABLED = 1;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public IPage<Organization> pageOrganizations(String keyword, Integer page, Integer size) {
        return organizationMapper.selectAccountPage(PageSupport.of(page, size), PageSupport.normalizeKeyword(keyword));
    }

    @Override
    public IPage<Volunteer> pageVolunteers(String keyword, Integer page, Integer size) {
        return userAccountMapper.selectVolunteerAccountPage(
                PageSupport.of(page, size), PageSupport.normalizeKeyword(keyword));
    }

    @Override
    public Map<Object, Object> createOrganization(String loginId, String organizationId, String organizationName,
                                                  String password, String establishDate, String introduction) {

        boolean generated = password == null || password.trim().isEmpty();
        String initialPassword = generated ? RandomPasswordUtil.randomPassword() : password;

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("organizationId", organizationId);
        map.put("organizationName", organizationName);
        map.put("password", passwordEncoder.encode(initialPassword));
        map.put("establishDate", toSqlDate(establishDate));
        map.put("introduction", introduction);
        userAccountMapper.admin_create_organization(map);

        if (generated) {
            map.put("password", initialPassword);
        } else {

            map.remove("password");
        }
        log.info("平台管理员开通志愿者组织账号，管理员账号：{}，组织账号：{}，处理结果：{}",
                loginId, organizationId, map.get("msg"));
        return map;
    }

    @Override
    public Map<Object, Object> setEnabled(String loginId, String accountId, Integer enabled) {

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("accountId", accountId);
        map.put("enabled", enabled);
        userAccountMapper.admin_set_account_enabled(map);

        log.info("平台管理员{}账号，管理员账号：{}，目标账号：{}，处理结果：{}",
                ENABLED.equals(enabled) ? "启用" : "停用", loginId, accountId, map.get("msg"));
        return map;
    }

    @Override
    public Map<Object, Object> resetPassword(String loginId, String accountId) {

        Map<Object, Object> result = userAccountService.resetPassword(accountId);
        log.info("平台管理员重置账号密码，管理员账号：{}，目标账号：{}，处理结果：{}",
                loginId, accountId, result.get("msg"));
        return result;
    }

    private java.sql.Date toSqlDate(String establishDate) {
        Date parsed = DateTimeParser.parse(establishDate);
        return parsed == null ? null : new java.sql.Date(parsed.getTime());
    }
}
