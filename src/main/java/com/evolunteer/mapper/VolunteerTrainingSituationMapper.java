package com.evolunteer.mapper;

import com.evolunteer.entity.TrainingSituationView;
import com.evolunteer.entity.VolunteerTrainingSituation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者培训情况表 volunteer_training_situation 的数据访问接口，提供按培训分页查询参加记录的志愿者信息，
 * 以及培训情况业务编号的生成能力。
 *
 * @Entity com.evolunteer.entity.VolunteerTrainingSituation
 */
@Repository
public interface VolunteerTrainingSituationMapper extends BaseMapper<VolunteerTrainingSituation> {

    /**
     * 按培训分页查询志愿者参加培训的情况，关联志愿者业务编号、姓名与联系电话
     *
     * @param page        分页对象
     * @param trainingNum 培训编号
     * @return 培训情况分页结果
     */
    IPage<TrainingSituationView> selectPageByTraining(Page<TrainingSituationView> page,
                                                      @Param("trainingNum") Integer trainingNum);

    /**
     * 统计志愿者报名指定活动的记录数，用于登记培训情况前校验志愿者是否报名过培训所属活动
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 报名记录数，未报名时返回 0
     */
    int countVolunteerParticipate(@Param("volunteerNum") Integer volunteerNum,
                                  @Param("activityNum") Integer activityNum);

    /**
     * 生成下一个培训情况业务编号的自增序号，取已有业务编号的数值部分最大值加一
     *
     * @return 自增序号，尚无培训情况记录时返回 1
     */
    Integer selectNextSituationSeq();
}
