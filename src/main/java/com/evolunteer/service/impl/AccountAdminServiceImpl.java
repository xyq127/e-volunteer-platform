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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台账号管理业务实现类：组织账号开通与账号启停通过数据库存储过程完成，保证组织信息、
 * 统一登录账号与业务表删除标记在同一事务内一致；初始密码与重置密码统一使用密码编码器加密后入库，
 * 明文密码只在开通结果中一次性回传给平台管理员。
 */
@Slf4j
@Service
public class AccountAdminServiceImpl implements AccountAdminService {

    /**
     * 账号状态：启用
     */
    private static final Integer ENABLED = 1;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询志愿者组织名单
     *
     * @param keyword 组织登录账号或组织名称关键字，可为空
     * @param page    页码
     * @param size    每页条数
     * @return 志愿者组织分页结果
     */
    @Override
    public IPage<Organization> pageOrganizations(String keyword, Integer page, Integer size) {
        return organizationMapper.selectAccountPage(PageSupport.of(page, size), PageSupport.normalizeKeyword(keyword));
    }

    /**
     * 分页查询志愿者名单
     *
     * @param keyword 志愿者编号、姓名或联系电话关键字，可为空
     * @param page    页码
     * @param size    每页条数
     * @return 志愿者分页结果
     */
    @Override
    public IPage<Volunteer> pageVolunteers(String keyword, Integer page, Integer size) {
        return userAccountMapper.selectVolunteerAccountPage(
                PageSupport.of(page, size), PageSupport.normalizeKeyword(keyword));
    }

    /**
     * 开通志愿者组织账号
     *
     * @param loginId          操作人登录账号（平台管理员账号）
     * @param organizationId   组织登录账号
     * @param organizationName 组织名称
     * @param password         初始密码，为空时由平台生成
     * @param establishDate    成立日期文本，可为空
     * @param introduction     组织简介，可为空
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与平台生成的随机初始密码 password
     */
    @Override
    public Map<Object, Object> createOrganization(String loginId, String organizationId, String organizationName,
                                                  String password, String establishDate, String introduction) {

        // 管理员未指定初始密码时由平台随机生成，生成结果在存储过程执行后回传，避免初始密码只存在于数据库中
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
            // 管理员自行指定的密码无需回传，同时避免把密码密文带到响应中
            map.remove("password");
        }
        log.info("平台管理员开通志愿者组织账号，管理员账号：{}，组织账号：{}，处理结果：{}",
                loginId, organizationId, map.get("msg"));
        return map;
    }

    /**
     * 启用或停用组织账号与志愿者账号
     *
     * @param loginId   操作人登录账号（平台管理员账号）
     * @param accountId 目标账号
     * @param enabled   账号状态，1 启用、0 停用
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
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

    /**
     * 重置账号密码
     *
     * @param loginId   操作人登录账号（平台管理员账号）
     * @param accountId 目标账号
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与重置后的新密码 password
     */
    @Override
    public Map<Object, Object> resetPassword(String loginId, String accountId) {

        Map<Object, Object> result = userAccountService.resetPassword(accountId);
        log.info("平台管理员重置账号密码，管理员账号：{}，目标账号：{}，处理结果：{}",
                loginId, accountId, result.get("msg"));
        return result;
    }

    /**
     * 把页面提交的成立日期文本解析为数据库日期
     *
     * @param establishDate 成立日期文本，支持 yyyy-MM-dd 等格式
     * @return 数据库日期，文本为空时返回 null
     */
    private java.sql.Date toSqlDate(String establishDate) {
        Date parsed = DateTimeParser.parse(establishDate);
        return parsed == null ? null : new java.sql.Date(parsed.getTime());
    }
}
