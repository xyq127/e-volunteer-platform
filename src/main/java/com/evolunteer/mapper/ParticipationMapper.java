package com.evolunteer.mapper;

import com.evolunteer.entity.Participation;
import com.evolunteer.entity.ServiceRecord;
import com.evolunteer.entity.VolunteerParticipationView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名表 participate 的数据访问接口，志愿者报名与撤回报名、志愿者组织审核报名
 * 均通过数据库存储过程完成。
 *
 * @Entity com.evolunteer.entity.Participation
 */
@Repository
public interface ParticipationMapper extends BaseMapper<Participation> {

    /**
     * 按活动查询报名志愿者列表，并关联志愿者基本信息供志愿者组织审核
     *
     * @param activityNum 活动编号
     * @return 报名记录列表
     */
    List<Participation> selectParticipateWithVolunteerByActivityNum(Integer activityNum);

    /**
     * 按志愿者查询本人报名记录，关联活动信息与最近一次签到信息
     *
     * @param volunteerNum 志愿者编号
     * @return 报名记录视图列表
     */
    List<VolunteerParticipationView> selectVolunteerParticipations(@Param("volunteerNum") Integer volunteerNum);

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
     * @param map 报名参数，包含志愿者编号、活动编号与输出参数 msg
     */
    void volunteer_apply_activity(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 volunteer_cancel_participate，撤回报名并在释放名额后自动递补候补志愿者
     *
     * @param map 撤回参数，包含报名编号、志愿者编号与输出参数 msg
     */
    void volunteer_cancel_participate(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_check_volunteer，写入志愿者组织对报名申请的审核结果
     *
     * @param map 审核参数，包含报名编号、审核结果与输出参数 msg
     */
    void organization_check_volunteer(Map<Object, Object> map);

}
