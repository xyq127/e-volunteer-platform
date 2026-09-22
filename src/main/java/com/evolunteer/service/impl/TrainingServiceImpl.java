package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.Training;
import com.evolunteer.entity.TrainingSituationView;
import com.evolunteer.entity.TrainingView;
import com.evolunteer.entity.VolunteerTrainingSituation;
import com.evolunteer.mapper.TrainingMapper;
import com.evolunteer.mapper.VolunteerTrainingSituationMapper;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.OrganizationService;
import com.evolunteer.service.TrainingService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    private static final String TRAINING_ID_FORMAT = "tra_%05d";

    private static final String SITUATION_ID_FORMAT = "vts_%05d";

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private VolunteerTrainingSituationMapper volunteerTrainingSituationMapper;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ActivityService activityService;

    @Override
    public IPage<TrainingView> pageByActivity(String loginId, Integer activityNum, Integer page, Integer size) {
        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            log.warn("未找到志愿者组织信息，无法查询培训安排，组织账号：{}", loginId);
            return PageSupport.of(page, size);
        }
        return trainingMapper.selectPageByOrganization(PageSupport.of(page, size), organizationNum, activityNum);
    }

    @Override
    public String saveTraining(String loginId, Training training) {

        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            return "未找到志愿者组织信息，请重新登录后再试";
        }
        if (training == null || training.getActivityNum() == null) {
            return "请选择培训所属的志愿活动";
        }
        if (training.getTrainingName() == null || training.getTrainingName().trim().isEmpty()) {
            return "培训名称不能为空，请重新填写";
        }
        if (!belongsToOrganization(organizationNum, training.getActivityNum())) {
            return "只能维护本组织申报活动的培训安排";
        }
        Date begin = training.getTrainingBegintime();
        Date end = training.getTrainingEndtime();
        if (begin == null || end == null) {
            return "请填写培训的开始时间与结束时间";
        }
        if (!end.after(begin)) {
            return "培训结束时间必须晚于开始时间，请重新填写";
        }

        if (training.getTrainingNum() == null) {
            training.setTrainingId(String.format(TRAINING_ID_FORMAT, trainingMapper.selectNextTrainingSeq()));
            training.setTrainingIsdeleted(0);
            trainingMapper.insert(training);
            log.info("志愿者组织新增培训安排，组织账号：{}，活动编号：{}，培训业务编号：{}",
                    loginId, training.getActivityNum(), training.getTrainingId());
            return MESSAGE_SAVE_SUCCESS;
        }

        Training exists = trainingMapper.selectById(training.getTrainingNum());
        if (exists == null) {
            return "培训安排不存在或已被删除";
        }
        if (!belongsToOrganization(organizationNum, exists.getActivityNum())) {
            return "只能维护本组织申报活动的培训安排";
        }

        training.setTrainingIsdeleted(null);
        training.setTrainingId(null);
        trainingMapper.updateById(training);
        log.info("志愿者组织修改培训安排，组织账号：{}，培训编号：{}", loginId, training.getTrainingNum());
        return MESSAGE_SAVE_SUCCESS;
    }

    @Override
    public String deleteTraining(String loginId, Integer trainingNum) {

        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            return "未找到志愿者组织信息，请重新登录后再试";
        }
        if (trainingNum == null) {
            return "请选择要删除的培训安排";
        }
        Training exists = trainingMapper.selectById(trainingNum);
        if (exists == null) {
            return "培训安排不存在或已被删除";
        }
        if (!belongsToOrganization(organizationNum, exists.getActivityNum())) {
            return "只能删除本组织申报活动的培训安排";
        }
        trainingMapper.deleteById(trainingNum);
        log.info("志愿者组织删除培训安排，组织账号：{}，培训编号：{}", loginId, trainingNum);
        return MESSAGE_DELETE_SUCCESS;
    }

    @Override
    public IPage<TrainingSituationView> pageSituations(String loginId, Integer trainingNum,
                                                       Integer page, Integer size) {

        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null || trainingNum == null) {
            return PageSupport.of(page, size);
        }
        Training training = trainingMapper.selectById(trainingNum);
        if (training == null || !belongsToOrganization(organizationNum, training.getActivityNum())) {
            log.warn("志愿者组织无权查询培训情况，组织账号：{}，培训编号：{}", loginId, trainingNum);
            return PageSupport.of(page, size);
        }
        return volunteerTrainingSituationMapper.selectPageByTraining(PageSupport.of(page, size), trainingNum);
    }

    @Override
    public String saveSituation(String loginId, Integer trainingNum, Integer volunteerNum, Date begin, Date end) {

        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            return "未找到志愿者组织信息，请重新登录后再试";
        }
        if (trainingNum == null) {
            return "请选择培训安排";
        }
        if (volunteerNum == null) {
            return "请选择志愿者";
        }
        if (begin == null || end == null) {
            return "请填写培训的开始时间与结束时间";
        }
        if (!end.after(begin)) {
            return "培训结束时间必须晚于开始时间，请重新填写";
        }
        Training training = trainingMapper.selectById(trainingNum);
        if (training == null) {
            return "培训安排不存在或已被删除";
        }
        if (!belongsToOrganization(organizationNum, training.getActivityNum())) {
            return "只能登记本组织申报活动下培训的参加情况";
        }
        if (!hasParticipated(volunteerNum, training.getActivityNum())) {
            return "该志愿者未报名本次培训所属的志愿活动";
        }

        VolunteerTrainingSituation situation = new VolunteerTrainingSituation();
        situation.setTrainingNum(trainingNum);
        situation.setVolunteerNum(volunteerNum);
        situation.setVtsituationId(String.format(SITUATION_ID_FORMAT,
                volunteerTrainingSituationMapper.selectNextSituationSeq()));
        situation.setVtsituationBegintime(begin);
        situation.setVtsituationEndtime(end);
        situation.setVtsituationIsdeleted(0);
        volunteerTrainingSituationMapper.insert(situation);
        log.info("志愿者组织登记志愿者培训情况，组织账号：{}，培训编号：{}，志愿者编号：{}，记录业务编号：{}",
                loginId, trainingNum, volunteerNum, situation.getVtsituationId());
        return MESSAGE_SITUATION_SAVE_SUCCESS;
    }

    @Override
    public IPage<TrainingView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size) {
        if (volunteerNum == null) {
            return PageSupport.of(page, size);
        }
        return trainingMapper.selectPageForVolunteer(PageSupport.of(page, size), volunteerNum);
    }

    private boolean belongsToOrganization(Integer organizationNum, Integer activityNum) {
        if (activityNum == null) {
            return false;
        }
        List<Activity> activities = activityService.activityInfo(activityNum);
        for (Activity activity : activities) {
            if (organizationNum.equals(activity.getOrganizationNum())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasParticipated(Integer volunteerNum, Integer activityNum) {
        return volunteerTrainingSituationMapper.countVolunteerParticipate(volunteerNum, activityNum) > 0;
    }
}
