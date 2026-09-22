package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.UserAccount;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 统一用户表 e_user 的数据访问接口，志愿者、志愿者组织与平台管理员的登录账号、登录密码与角色权限均保存在该表中。
 *
 * @Entity com.evolunteer.entity.UserAccount
 */
@Repository
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    /**
     * 按登录账号查询用户认证信息
     *
     * @param id 登录账号
     * @return 用户认证信息列表
     */
    List<UserAccount> selectByLoginId(@Param("id") String id);

    /**
     * 更新登录账号的密码密文，历史账号登录成功后由平台将弱摘要密文升级为加盐密文
     *
     * @param id       登录账号
     * @param password 升级后的密码密文
     * @return 受影响的行数
     */
    int updatePasswordById(@Param("id") String id, @Param("password") String password);

}
