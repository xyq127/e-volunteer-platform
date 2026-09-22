package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ActivityAnnouncement;
import com.evolunteer.entity.ActivityAnnouncementView;
import com.evolunteer.mapper.ActivityAnnouncementMapper;
import com.evolunteer.service.ActivityAnnouncementService;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.NotificationService;
import com.evolunteer.service.OrganizationService;
import com.evolunteer.service.ParticipationService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ActivityAnnouncementServiceImpl implements ActivityAnnouncementService {

    private static final String ANNOUNCEMENT_ID_FORMAT = "ann_%05d";

    private static final String RECEIVER_ROLE_VOLUNTEER = "ROLE_VOLUNTEER";

    @Autowired
    private ActivityAnnouncementMapper activityAnnouncementMapper;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public IPage<ActivityAnnouncementView> pageByOrganization(String loginId, Integer activityNum,
                                                              Integer page, Integer size) {
        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            log.warn("未找到志愿者组织信息，无法查询活动公告，组织账号：{}", loginId);
            return PageSupport.of(page, size);
        }
        return activityAnnouncementMapper.selectPageByOrganization(
                PageSupport.of(page, size), organizationNum, activityNum);
    }

    @Override
    public IPage<ActivityAnnouncementView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size) {
        if (volunteerNum == null) {
            return PageSupport.of(page, size);
        }
        return activityAnnouncementMapper.selectPageForVolunteer(PageSupport.of(page, size), volunteerNum);
    }

    @Override
    public String saveAnnouncement(String loginId, ActivityAnnouncement announcement) {

        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            return "未找到志愿者组织信息，请重新登录后再试";
        }
        if (announcement == null || announcement.getActivityNum() == null) {
            return "请选择公告所属的志愿活动";
        }
        if (announcement.getActannouncementName() == null
                || announcement.getActannouncementName().trim().isEmpty()) {
            return "公告标题不能为空，请重新填写";
        }
        if (announcement.getActannouncementDetail() == null
                || announcement.getActannouncementDetail().trim().isEmpty()) {
            return "公告内容不能为空，请重新填写";
        }
        if (!belongsToOrganization(organizationNum, announcement.getActivityNum())) {
            return "只能为本组织申报的志愿活动发布公告";
        }

        if (announcement.getActannouncementNum() == null) {
            announcement.setActannouncementId(String.format(ANNOUNCEMENT_ID_FORMAT,
                    activityAnnouncementMapper.selectNextAnnouncementSeq()));
            announcement.setOrganizationNum(organizationNum);
            announcement.setActannouncementIsdeleted(0);
            activityAnnouncementMapper.insert(announcement);

            List<String> receiverIds =
                    participationService.approvedVolunteerAccounts(announcement.getActivityNum());
            notificationService.send(receiverIds, RECEIVER_ROLE_VOLUNTEER,
                    "活动公告：" + announcement.getActannouncementName(),
                    announcement.getActannouncementDetail());
            log.info("志愿者组织发布活动公告，组织账号：{}，活动编号：{}，公告业务编号：{}，通知志愿者人数：{}",
                    loginId, announcement.getActivityNum(), announcement.getActannouncementId(),
                    receiverIds == null ? 0 : receiverIds.size());
            return MESSAGE_SAVE_SUCCESS;
        }

        ActivityAnnouncement exists = activityAnnouncementMapper.selectById(announcement.getActannouncementNum());
        if (exists == null) {
            return "活动公告不存在或已被删除";
        }
        if (!belongsToOrganization(organizationNum, exists.getActivityNum())) {
            return "只能修改本组织发布的活动公告";
        }

        announcement.setActannouncementIsdeleted(null);
        announcement.setActannouncementId(null);
        announcement.setOrganizationNum(organizationNum);
        activityAnnouncementMapper.updateById(announcement);
        log.info("志愿者组织修改活动公告，组织账号：{}，公告编号：{}", loginId, announcement.getActannouncementNum());
        return MESSAGE_SAVE_SUCCESS;
    }

    @Override
    public String deleteAnnouncement(String loginId, Integer announcementNum) {

        Integer organizationNum = organizationService.getOrganizationNum(loginId);
        if (organizationNum == null) {
            return "未找到志愿者组织信息，请重新登录后再试";
        }
        if (announcementNum == null) {
            return "请选择要删除的活动公告";
        }
        ActivityAnnouncement exists = activityAnnouncementMapper.selectById(announcementNum);
        if (exists == null) {
            return "活动公告不存在或已被删除";
        }
        if (!belongsToOrganization(organizationNum, exists.getActivityNum())) {
            return "只能删除本组织发布的活动公告";
        }
        activityAnnouncementMapper.deleteById(announcementNum);
        log.info("志愿者组织删除活动公告，组织账号：{}，公告编号：{}", loginId, announcementNum);
        return MESSAGE_DELETE_SUCCESS;
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
}
