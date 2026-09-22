package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.CheckinCodeView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动表 activity 的数据访问接口，提供活动申报、活动分状态分页查询、活动审核、活动状态流转、
 * 修改后重新申报与逻辑删除等数据库操作，其中活动申报、审核、状态流转与重新申报通过数据库存储过程完成。
 *
 * @Entity com.evolunteer.entity.Activity
 */
@Repository
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 调用数据库存储过程 organization_insert_activity，写入志愿者组织申报的志愿活动
     *
     * @param map 申报参数，包含组织账号、活动基本信息、技能标签、活动坐标、签到围栏半径与输出参数 msg
     */
    void organization_insert_activity(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 admin_check_activity，写入平台管理员的活动审核结果
     *
     * @param map 审核参数，包含管理员账号、活动编号、审核结果、结构化原因编码、审核意见与输出参数 msg
     */
    void admin_check_activity(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_revise_activity，将审核未通过的活动修改后重新提交审核
     *
     * @param map 重新申报参数，包含组织账号、活动编号、活动基本信息、技能标签、活动坐标与输出参数 msg
     */
    void organization_revise_activity(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 organization_change_activity_state，人工流转活动状态
     *
     * @param map 流转参数，包含组织账号、活动编号、目标状态与输出参数 msg、ok
     */
    void organization_change_activity_state(Map<Object, Object> map);

    /**
     * 调用数据库存储过程 activity_state_refresh，按活动时间自动流转活动状态
     *
     * @param map 输出参数容器，包含 startedCount、finishedCount
     */
    void activity_state_refresh(Map<Object, Object> map);

    /**
     * 按志愿者组织与活动状态分页查询活动
     *
     * @param page            分页对象
     * @param organizationNum 志愿者组织编号
     * @param activityState   活动状态
     * @param keyword         活动名称或活动编号关键字，可为空
     * @return 活动分页结果
     */
    IPage<Activity> selectPageByOrganizationAndState(Page<Activity> page,
                                                     @Param("organizationNum") Integer organizationNum,
                                                     @Param("activityState") String activityState,
                                                     @Param("keyword") String keyword);

    /**
     * 按活动状态分页查询平台内活动，供平台管理员审核使用
     *
     * @param page          分页对象
     * @param activityState 活动状态
     * @param keyword       活动名称或活动编号关键字，可为空
     * @return 活动分页结果
     */
    IPage<Activity> selectPageByState(Page<Activity> page,
                                      @Param("activityState") String activityState,
                                      @Param("keyword") String keyword);

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
     * 更新志愿活动的签到密钥，重新生成签到码即更换密钥，旧签到码立即失效
     *
     * @param activityCheckinSecret 签到密钥
     * @param activityNum           活动编号
     * @return 受影响的行数
     */
    int updateActivityCheckinSecret(@Param("activityCheckinSecret") String activityCheckinSecret,
                                    @Param("activityNum") Integer activityNum);

    /**
     * 查询活动当前生效的轮换签到码与剩余有效秒数
     *
     * @param activityNum 活动编号
     * @return 签到码信息，活动不存在时返回 null
     */
    CheckinCodeView selectCheckinCodeInfo(@Param("activityNum") Integer activityNum);

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
