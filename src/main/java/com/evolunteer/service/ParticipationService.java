package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.VolunteerParticipationView;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名业务接口，基于 MyBatis-Plus 提供活动报名数据的通用增删改查能力，
 * 并提供志愿者报名、参加确认与撤回报名，志愿者组织报名审核与移除报名，
 * 以及参加确认到期自动释放名额等能力。
 */
public interface ParticipationService extends IService<Participation> {

    /**
     * 查询指定活动下已报名的志愿者列表
     *
     * @param activityNum 活动编号
     * @return 报名记录列表，已关联志愿者基本信息
     */
    List<Participation> getVolunteerList(Integer activityNum);

    /**
     * 按活动分页查询报名志愿者名单
     *
     * @param activityNum 活动编号
     * @param keyword     志愿者编号、姓名或手机号关键字，可为空
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @return 报名记录分页结果
     */
    IPage<Participation> pageVolunteerList(Integer activityNum, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 查询指定活动已通过报名的志愿者登录账号，用于向参与者发送站内通知
     *
     * @param activityNum 活动编号
     * @return 志愿者登录账号列表
     */
    List<String> approvedVolunteerAccounts(Integer activityNum);

    /**
     * 查询志愿者的报名记录，并按当前状态标记可签到、可签退、可撤回与可确认
     *
     * @param volunteerNum 志愿者编号
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 报名记录视图分页结果
     */
    IPage<VolunteerParticipationView> pageVolunteerParticipations(Integer volunteerNum,
                                                                 Integer pageNum, Integer pageSize);

    /**
     * 志愿者报名志愿活动：校验信用分、报名截止时间与名额，名额已满时进入候补队列
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> applyActivity(Integer volunteerNum, Integer activityNum);

    /**
     * 志愿者撤回报名：已通过的报名释放名额并自动递补候补队列中的第一位志愿者
     *
     * @param participateNum 报名编号
     * @param volunteerNum   志愿者编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> cancelParticipate(Integer participateNum, Integer volunteerNum);

    /**
     * 志愿者确认参加或放弃参加，放弃时释放名额并自动递补候补志愿者
     *
     * @param participateNum 报名编号
     * @param volunteerNum   志愿者编号
     * @param confirmState   确认状态（1 确认参加、2 放弃参加）
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> confirmParticipate(Integer participateNum, Integer volunteerNum, Integer confirmState);

    /**
     * 志愿者组织移除本组织活动名单中的报名
     *
     * @param loginId        志愿者组织登录账号
     * @param participateNum 报名编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> removeParticipate(String loginId, Integer participateNum);

    /**
     * 释放未按期确认参加的报名名额，供定时任务调用
     *
     * @return 处理结果，包含释放数量 releasedCount
     */
    Map<Object, Object> releaseUnconfirmedParticipations();

    /**
     * 写入志愿者组织对报名申请的审核结果
     *
     * @param map 审核参数，包含报名编号与审核结果
     * @return 处理结果信息
     */
    Object volCheckRecruit(Map<Object, Object> map);
}
