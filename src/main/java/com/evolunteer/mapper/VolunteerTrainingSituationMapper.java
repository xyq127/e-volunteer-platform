package com.evolunteer.mapper;

import com.evolunteer.entity.TrainingSituationView;
import com.evolunteer.entity.VolunteerTrainingSituation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerTrainingSituationMapper extends BaseMapper<VolunteerTrainingSituation> {

    IPage<TrainingSituationView> selectPageByTraining(Page<TrainingSituationView> page,
                                                      @Param("trainingNum") Integer trainingNum);

    int countVolunteerParticipate(@Param("volunteerNum") Integer volunteerNum,
                                  @Param("activityNum") Integer activityNum);

    Integer selectNextSituationSeq();
}
