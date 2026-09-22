package com.evolunteer.mapper;

import com.evolunteer.entity.Training;
import com.evolunteer.entity.TrainingView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿培训表 training 的数据访问接口，提供志愿者组织按活动分页查询本组织活动的培训安排、
 * 志愿者查询本人已通过报名活动对应的培训安排，以及培训业务编号的生成能力。
 *
 * @Entity com.evolunteer.entity.Training
 */
@Repository
public interface TrainingMapper extends BaseMapper<Training> {

    /**
     * 按志愿者组织分页查询本组织申报活动的培训安排，可选按活动筛选
     *
     * @param page            分页对象
     * @param organizationNum 志愿者组织编号
     * @param activityNum     活动编号，为 null 时查询本组织的全部培训
     * @return 培训安排分页结果，已关联活动名称与已登记培训情况人数
     */
    IPage<TrainingView> selectPageByOrganization(Page<TrainingView> page,
                                                 @Param("organizationNum") Integer organizationNum,
                                                 @Param("activityNum") Integer activityNum);

    /**
     * 按志愿者分页查询本人已通过报名活动对应的培训安排
     *
     * @param page         分页对象
     * @param volunteerNum 志愿者编号
     * @return 培训安排分页结果，已关联活动名称与已登记培训情况人数
     */
    IPage<TrainingView> selectPageForVolunteer(Page<TrainingView> page, @Param("volunteerNum") Integer volunteerNum);

    /**
     * 生成下一个培训业务编号的自增序号，取已有培训业务编号的数值部分最大值加一
     *
     * @return 自增序号，尚无培训记录时返回 1
     */
    Integer selectNextTrainingSeq();
}
