package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Activity;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动业务接口，基于 MyBatis-Plus 提供志愿活动数据的通用增删改查能力，
 * 并面向志愿者组织与平台管理员提供分状态分页查询、活动详情查询、活动审核、活动状态流转、
 * 修改后重新申报与逻辑删除能力。
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 按志愿者组织编号与活动状态分页查询活动
     *
     * @param organizationNum 志愿者组织编号
     * @param state           活动状态（0 审核中、1 未开始、2 进行中、3 已结束、4 审核未通过）
     * @param keyword         活动名称或活动编号关键字，可为空
     * @param pageNum         页码
     * @param pageSize        每页条数
     * @return 活动分页结果
     */
    IPage<Activity> pageActivityByOrganization(Integer organizationNum, String state, String keyword,
                                               Integer pageNum, Integer pageSize);

    /**
     * 按活动状态分页查询平台内活动，供平台管理员审核使用
     *
     * @param state    活动状态
     * @param keyword  活动名称或活动编号关键字，可为空
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 活动分页结果
     */
    IPage<Activity> pageActivityByState(String state, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 按志愿者组织编号与活动状态查询活动列表
     *
     * @param num   志愿者组织编号
     * @param state 活动状态
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
     * @param map 审核参数，包含管理员账号、活动编号、审核结果、结构化原因与审核意见
     * @return 处理结果信息
     */
    Object passActByAdminIdWithStatue(Map<Object, Object> map);

    /**
     * 志愿者组织人工流转活动状态（未开始置为进行中、未开始或进行中置为已结束）
     *
     * @param loginId     志愿者组织登录账号
     * @param activityNum 活动编号
     * @param targetState 目标状态（2 进行中、3 已结束）
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> changeActivityState(String loginId, Integer activityNum, String targetState);

    /**
     * 按活动时间自动流转活动状态，供定时任务调用
     *
     * @return 处理结果，包含开始数量 startedCount 与结束数量 finishedCount
     */
    Map<Object, Object> refreshActivityStates();

    /**
     * 将审核未通过的活动修改后重新提交审核
     *
     * @param map 重新申报参数，包含组织账号、活动编号、活动基本信息、技能标签与活动坐标
     * @return 处理结果信息
     */
    Object reviseAct(Map<Object, Object> map);

    /**
     * 按活动名称精确查询活动
     *
     * @param actName 活动名称
     * @return 匹配的活动，不存在时返回 null
     */
    Activity selectAct(String actName);
}
