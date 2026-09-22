package com.evolunteer.mapper;

import com.evolunteer.entity.CheckIn;
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
 * 志愿服务记录表 checkin 的数据访问接口，志愿者签到签退、服务时长复核、服务时长补录与补录复核、
 * 活动结算均通过数据库存储过程完成。
 *
 * @Entity com.evolunteer.entity.CheckIn
 */
@Repository
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /**
     * 调用数据库存储过程 volunteer_checkin，校验签到码、签到时间窗口与地理围栏后写入签到轨迹
     *
     * @param map 签到参数，包含志愿者编号、活动编号、签到码、经纬度，以及输出参数 msg、flag、ok
     */
    void volunteer_checkin(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 volunteer_checkout，核算服务时长并写入签退信息
     *
     * @param map 签退参数，包含志愿者编号、活动编号、经纬度，以及输出参数 msg、duration、ok
     */
    void volunteer_checkout(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_check_checkin，写入志愿者组织对平台签到记录的复核结果
     *
     * @param map 复核参数，包含签到编号、复核结果、复核意见与输出参数 msg、ok
     */
    void organization_check_checkin(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_record_service_hours，志愿者组织补录服务时长
     *
     * @param map 补录参数，包含组织账号、报名编号、服务起止时间、补录说明与输出参数 msg、ok
     */
    void organization_record_service_hours(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 admin_check_manual_checkin，平台管理员复核补录记录
     *
     * @param map 复核参数，包含管理员账号、签到编号、复核结果、复核意见与输出参数 msg、ok
     */
    void admin_check_manual_checkin(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_settle_activity，结算活动考勤并记录爽约
     *
     * @param map 结算参数，包含活动编号与输出参数 msg、ok
     */
    void organization_settle_activity(Map<Object, Object> map);

    /**
     * 按活动分页查询服务记录，并关联志愿者基本信息供志愿者组织复核
     *
     * @param page        分页对象
     * @param activityNum 活动编号
     * @param state       复核状态，0 待复核、1 已确认、2 已驳回，为空时查询全部
     * @param source      记录来源，1 平台签到、2 组织补录，为空时查询全部
     * @return 服务记录分页结果
     */
    IPage<CheckIn> selectPageCheckinByActivityNum(Page<CheckIn> page,
                                                  @Param("activityNum") Integer activityNum,
                                                  @Param("state") String state,
                                                  @Param("source") String source);

    /**
     * 按活动查询该活动全部服务记录，并关联志愿者基本信息供志愿者组织复核
     *
     * @param activityNum 活动编号
     * @return 服务记录列表
     */
    List<CheckIn> selectCheckinListByActivityNum(@Param("activityNum") Integer activityNum);

    /**
     * 分页查询志愿者组织补录的服务时长记录，供平台管理员复核
     *
     * @param page    分页对象
     * @param state   复核状态，0 待复核、1 已确认、2 已驳回，为空时查询全部
     * @param keyword 志愿者姓名、志愿者编号或活动名称关键字，可为空
     * @return 补录记录分页结果
     */
    IPage<CheckIn> selectPageManualCheckins(Page<CheckIn> page,
                                            @Param("state") String state,
                                            @Param("keyword") String keyword);
}
