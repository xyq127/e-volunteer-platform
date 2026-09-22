package com.evolunteer.mapper;

import com.evolunteer.entity.Participation;
import com.evolunteer.entity.ServiceRecord;
import com.evolunteer.entity.VolunteerParticipationView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名表 participate 的数据访问接口，志愿者报名与撤回报名、参加确认、志愿者组织审核与移除报名、
 * 参加确认到期自动释放均通过数据库存储过程完成。
 *
 * @Entity com.evolunteer.entity.Participation
 */
@Repository
public interface ParticipationMapper extends BaseMapper<Participation> {

    /**
     * 查询指定活动已通过报名的志愿者登录账号
     *
     * @param activityNum 活动编号
     * @return 志愿者登录账号列表
     */
    List<String> selectApprovedVolunteerAccounts(@Param("activityNum") Integer activityNum);

    /**
     * 按活动分页查询报名志愿者列表，并关联志愿者基本信息供志愿者组织审核
     *
     * @param page        分页对象
     * @param activityNum 活动编号
     * @param keyword     志愿者编号、姓名或手机号关键字，可为空
     * @return 报名记录分页结果
     */
    IPage<Participation> selectPageParticipateWithVolunteerByActivityNum(
            Page<Participation> page,
            @Param("activityNum") Integer activityNum,
            @Param("keyword") String keyword);

    /**
     * 按活动查询报名志愿者列表，并关联志愿者基本信息供志愿者组织审核
     *
     * @param activityNum 活动编号
     * @return 报名记录列表
     */
    List<Participation> selectParticipateWithVolunteerByActivityNum(Integer activityNum);

    /**
     * 按志愿者分页查询本人报名记录，关联活动信息与最近一次签到信息
     *
     * @param page         分页对象
     * @param volunteerNum 志愿者编号
     * @return 报名记录视图分页结果
     */
    IPage<VolunteerParticipationView> selectPageVolunteerParticipations(
            Page<VolunteerParticipationView> page,
            @Param("volunteerNum") Integer volunteerNum);

    /**
     * 按志愿者查询已核定服务记录，用于生成志愿服务证明
     *
     * @param volunteerNum 志愿者编号
     * @return 已核定服务记录列表
     */
    List<ServiceRecord> selectApprovedServiceRecords(@Param("volunteerNum") Integer volunteerNum);

    /**
     * 调用数据库存储过程 volunteer_apply_activity，校验信用分、报名截止时间与名额后写入报名记录
     *
     * @param map 报名参数，包含志愿者编号、活动编号与输出参数 msg、ok
     */
    void volunteer_apply_activity(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 volunteer_cancel_participate，撤回报名并在释放名额后自动递补候补志愿者
     *
     * @param map 撤回参数，包含报名编号、志愿者编号与输出参数 msg、ok
     */
    void volunteer_cancel_participate(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 volunteer_confirm_participate，确认参加或放弃参加
     *
     * @param map 确认参数，包含报名编号、志愿者编号、确认状态与输出参数 msg、ok
     */
    void volunteer_confirm_participate(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_check_volunteer，写入志愿者组织对报名申请的审核结果
     *
     * @param map 审核参数，包含报名编号、审核结果与输出参数 msg
     */
    void organization_check_volunteer(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_remove_participate，移除本组织活动名单中的报名
     *
     * @param map 移除参数，包含组织账号、报名编号与输出参数 msg、ok
     */
    void organization_remove_participate(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 participate_confirm_expire，释放未按期确认参加的报名名额
     *
     * @param map 输出参数容器，包含 releasedCount
     */
    void participate_confirm_expire(Map<Object, Object> map);

    /**
     * 统计志愿者已核定服务时长所覆盖的志愿活动数量
     *
     * @param volunteerNum 志愿者编号
     * @return 已核定服务时长的活动数量
     */
    int countApprovedActivities(@Param("volunteerNum") Integer volunteerNum);
}
