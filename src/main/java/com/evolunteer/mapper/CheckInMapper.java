package com.evolunteer.mapper;

import com.evolunteer.entity.CheckIn;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务签到表 checkin 的数据访问接口，志愿者签到签退、服务时长复核与活动结算
 * 均通过数据库存储过程完成。
 *
 * @Entity com.evolunteer.entity.CheckIn
 */
@Repository
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /**
     * 调用数据库存储过程 volunteer_checkin，校验签到码、签到时间窗口与地理围栏后写入签到轨迹
     *
     * @param map 签到参数，包含志愿者编号、活动编号、签到码、经纬度，以及输出参数 msg、flag
     */
    void volunteer_checkin(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 volunteer_checkout，核算服务时长并写入签退信息
     *
     * @param map 签退参数，包含志愿者编号、活动编号、经纬度，以及输出参数 msg、duration
     */
    void volunteer_checkout(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_check_checkin，写入志愿者组织对服务时长的复核结果
     *
     * @param map 复核参数，包含签到编号、复核结果、复核意见与输出参数 msg
     */
    void organization_check_checkin(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_settle_activity，结算活动考勤并记录爽约
     *
     * @param map 结算参数，包含活动编号与输出参数 msg
     */
    void organization_settle_activity(Map<Object, Object> map);

    /**
     * 按活动查询该活动全部签到记录，并关联志愿者基本信息供志愿者组织复核
     *
     * @param activityNum 活动编号
     * @return 签到记录列表
     */
    List<CheckIn> selectCheckinListByActivityNum(@Param("activityNum") Integer activityNum);
}
