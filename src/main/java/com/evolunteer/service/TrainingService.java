package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Training;
import com.evolunteer.entity.TrainingSituationView;
import com.evolunteer.entity.TrainingView;

import java.util.Date;

public interface TrainingService {

    String MESSAGE_SAVE_SUCCESS = "培训信息保存成功！";

    String MESSAGE_DELETE_SUCCESS = "培训信息删除成功！";

    String MESSAGE_SITUATION_SAVE_SUCCESS = "志愿者培训情况登记成功！";

    IPage<TrainingView> pageByActivity(String loginId, Integer activityNum, Integer page, Integer size);

    String saveTraining(String loginId, Training training);

    String deleteTraining(String loginId, Integer trainingNum);

    IPage<TrainingSituationView> pageSituations(String loginId, Integer trainingNum, Integer page, Integer size);

    String saveSituation(String loginId, Integer trainingNum, Integer volunteerNum, Date begin, Date end);

    IPage<TrainingView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size);
}
