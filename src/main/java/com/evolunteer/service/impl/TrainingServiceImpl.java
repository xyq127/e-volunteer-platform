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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿培训业务实现类：志愿者组织在本组织申报的志愿活动下维护培训安排并登记志愿者参加培训的情况，
 * 志愿者只能看到本人已通过报名活动对应的培训安排，所有写操作均先校验培训所属活动的组织归属。
 */
@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    /**
     * 培训业务编号格式，前缀 tra_ 加 5 位自增序号
     */
    private static final String TRAINING_ID_FORMAT = "tra_%05d";

    /**
     * 培训情况业务编号格式，前缀 vts_ 加 5 位自增序号
     */
    private static final String SITUATION_ID_FORMAT = "vts_%05d";

    @Autowired
    private TrainingMapper trainingMapper;

    @Autowired
    private VolunteerTrainingSituationMapper volunteerTrainingSituationMapper;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ActivityService activityService;

    /**
     * 按本组织的志愿活动分页查询培训安排
     *
     * @param loginId     志愿者组织登录账号
     * @param activityNum 活动编号，为 null 时查询本组织的全部培训安排
     * @param page        页码
     * @param size        每页条数
     * @return 培训安排分页结果，已关联活动名称与已登记培训情况人数
     */
    @Override
    public IPage<TrainingView> pageByActivity(String loginId, Integer activityNum, Integer page, Integer size) {
        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            log.warn("未找到志愿者组织信息，无法查询培训安排，组织账号：{}", loginId);
            return PageSupport.of(page, size);
        }
        return trainingMapper.selectPageByOrganization(PageSupport.of(page, size), organizationNum, activityNum);
    }

    /**
     * 保存培训安排：培训编号为空时新增并生成培训业务编号，否则修改已有培训安排
     *
     * @param loginId  志愿者组织登录账号
     * @param training 培训安排，包含所属活动编号、培训名称、培训内容、开始与结束时间、地点与签到方式
     * @return 返回给用户的中文提示
     */
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
        // 修改培训安排时不改动删除标记与业务编号
        training.setTrainingIsdeleted(null);
        training.setTrainingId(null);
        trainingMapper.updateById(training);
        log.info("志愿者组织修改培训安排，组织账号：{}，培训编号：{}", loginId, training.getTrainingNum());
        return MESSAGE_SAVE_SUCCESS;
    }

    /**
     * 逻辑删除培训安排，只能删除本组织申报活动下的培训安排
     *
     * @param loginId     志愿者组织登录账号
     * @param trainingNum 培训编号
     * @return 返回给用户的中文提示
     */
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

    /**
     * 按培训分页查询志愿者参加培训的情况，只能查询本组织申报活动下的培训
     *
     * @param loginId     志愿者组织登录账号
     * @param trainingNum 培训编号
     * @param page        页码
     * @param size        每页条数
     * @return 志愿者培训情况分页结果，已关联志愿者编号、姓名与联系电话
     */
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

    /**
     * 登记志愿者参加培训的情况，只能登记本组织申报活动下培训的安排，
     * 且该志愿者须报名过培训所属活动
     *
     * @param loginId      志愿者组织登录账号
     * @param trainingNum  培训编号
     * @param volunteerNum 志愿者编号
     * @param begin        培训开始时间
     * @param end          培训结束时间
     * @return 返回给用户的中文提示
     */
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

    /**
     * 志愿者端分页查询本人已通过报名活动对应的培训安排
     *
     * @param volunteerNum 志愿者编号
     * @param page         页码
     * @param size         每页条数
     * @return 培训安排分页结果，已关联活动名称与已登记培训情况人数
     */
    @Override
    public IPage<TrainingView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size) {
        if (volunteerNum == null) {
            return PageSupport.of(page, size);
        }
        return trainingMapper.selectPageForVolunteer(PageSupport.of(page, size), volunteerNum);
    }

    /**
     * 判断活动是否属于指定志愿者组织
     */
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

    /**
     * 判断志愿者是否报名过指定活动
     */
    private boolean hasParticipated(Integer volunteerNum, Integer activityNum) {
        return volunteerTrainingSituationMapper.countVolunteerParticipate(volunteerNum, activityNum) > 0;
    }
}
