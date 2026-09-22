package com.evolunteer.mapper;

import com.evolunteer.entity.Training;
import com.evolunteer.entity.TrainingView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingMapper extends BaseMapper<Training> {

    IPage<TrainingView> selectPageByOrganization(Page<TrainingView> page,
                                                 @Param("organizationNum") Integer organizationNum,
                                                 @Param("activityNum") Integer activityNum);

    IPage<TrainingView> selectPageForVolunteer(Page<TrainingView> page, @Param("volunteerNum") Integer volunteerNum);

    Integer selectNextTrainingSeq();
}
