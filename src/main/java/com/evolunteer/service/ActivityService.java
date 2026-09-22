package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Activity;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动业务接口，基于 MyBatis-Plus 提供志愿活动数据的通用增删改查能力，
 * 并面向志愿者组织与平台管理员提供分状态查询、活动详情查询、活动审核与逻辑删除能力。
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 按志愿者组织编号与活动状态查询活动列表
     *
     * @param num   志愿者组织编号
     * @param state 活动状态（0 审核中、1 未开始、2 进行中、3 已结束、4 审核未通过）
     * @return 活动列表
     */
    List<Activity> getActivityByNumAndState(Integer num, String state);

    /**
     * 按活动状态查询平台内全部活动，供平台管理员审核使用
     *
     * @param state 活动状态
     * @return 活动列表
     */
    List<Activity> getActivityByState(String state);

    /**
     * 逻辑删除活动
     *
     * @param num 活动编号
     * @return 删除成功返回 true
     */
    Boolean deleteAct(Integer num);

    /**
     * 查询指定活动的详情
     *
     * @param activityNum 活动编号
     * @return 活动详情列表
     */
    List<Activity> activityInfo(Integer activityNum);

    /**
     * 校验活动名称是否可用，供志愿者组织申报活动时查重
     *
     * @param name 活动名称
     * @return 名称未被占用返回 true
     */
    Boolean searchActivityByName(String name);

    /**
     * 写入平台管理员的活动审核结果
     *
     * @param map 审核参数，包含管理员账号、活动编号、审核结果与审核意见
     * @return 处理结果信息
     */
    Object passActByAdminIdWithStatue(Map<Object, Object> map);

    /**
     * 按活动名称精确查询活动
     *
     * @param actName 活动名称
     * @return 匹配的活动，不存在时返回 null
     */
    Activity selectAct(String actName);
}
