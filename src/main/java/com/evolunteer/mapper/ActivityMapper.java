package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.Activity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动表 activity 的数据访问接口，提供活动申报、活动查询、活动审核与逻辑删除等数据库操作，
 * 其中活动申报与活动审核通过数据库存储过程完成。
 *
 * @Entity com.evolunteer.entity.Activity
 */
/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动表 activity 的数据访问接口。
 *
 * @Entity com.evolunteer.entity.Activity
 */
@Repository
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 调用数据库存储过程 organization_insert_activity，写入门户组织申报的志愿活动
     *
     * @param map 申报参数，包含组织账号、活动基本信息与输出参数 msg
     */
    void organization_insert_activity(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 admin_check_activity，写入平台管理员的活动审核结果
     *
     * @param map 审核参数，包含管理员账号、活动编号、审核结果、审核意见与输出参数 msg
     */
    void admin_check_activity(Map<Object, Object> map);

    /**
     * 按志愿者组织编号与活动状态查询未删除的活动
     *
     * @param organizationNum   志愿者组织编号
     * @param activityState     活动状态
     * @param activityIsdeleted 删除标记，0 表示未删除
     * @return 活动列表
     */
    List<Activity> selectAllByOrganizationNumAndActivityStateAndActivityIsdeleted(
            @Param("organizationNum") Integer organizationNum,
            @Param("activityState") String activityState,
            @Param("activityIsdeleted") Integer activityIsdeleted);

    /**
     * 按活动状态查询平台内未删除的活动
     *
     * @param activityState     活动状态
     * @param activityIsdeleted 删除标记
     * @return 活动列表
     */
    List<Activity> selectAllByActivityStateAndActivityIsdeleted(
            @Param("activityState") String activityState,
            @Param("activityIsdeleted") Integer activityIsdeleted);

    /**
     * 按活动编号查询活动详情
     *
     * @param activityNum 活动编号
     * @return 活动详情列表
     */
    List<Activity> selectByActivityNum(@Param("activityNum") Integer activityNum);

    /**
     * 按活动名称精确查询活动，用于活动名称查重
     *
     * @param activityName 活动名称
     * @return 活动列表
     */
    List<Activity> selectAllByActivityName(@Param("activityName") String activityName);

    /**
     * 按活动名称精确查询活动
     *
     * @param activityName 活动名称
     * @return 活动列表
     */
    List<Activity> selectByActivityName(@Param("activityName") String activityName);

    /**
     * 逻辑删除活动
     *
     * @param activityIsdeleted 删除标记
     * @param activityNum       活动编号
     * @return 受影响的行数
     */
    int updateActivityIsdeletedByActivityNum(@Param("activityIsdeleted") Integer activityIsdeleted,
                                             @Param("activityNum") Integer activityNum);
}
