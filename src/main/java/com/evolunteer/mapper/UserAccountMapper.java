package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.entity.UserAccount;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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

    /**
     * 调用数据库存储过程 admin_create_organization，开通志愿者组织账号并写入组织信息
     *
     * @param map 开通参数，包含管理员账号、组织登录账号、组织名称、初始密码密文、成立日期、组织简介与输出参数 msg、ok
     */
    void admin_create_organization(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 admin_set_account_enabled，启用或停用组织账号与志愿者账号
     *
     * @param map 启停参数，包含管理员账号、目标账号、账号状态与输出参数 msg、ok
     */
    void admin_set_account_enabled(Map<Object, Object> map);

    /**
     * 平台管理员分页查询志愿者名单：关联统一用户表取登录账号状态，已被停用的志愿者同样列出
     *
     * @param page    分页对象
     * @param keyword 志愿者编号、姓名或联系电话关键字，可为空
     * @return 志愿者分页结果
     */
    IPage<Volunteer> selectVolunteerAccountPage(Page<Volunteer> page, @Param("keyword") String keyword);

    /**
     * 查询志愿者名单，含已被停用的志愿者，供平台管理员导出志愿者名册
     *
     * @param keyword 志愿者编号、姓名或联系电话关键字，可为空
     * @return 志愿者列表
     */
    List<Volunteer> selectVolunteerAccountList(@Param("keyword") String keyword);
}
