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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动公告业务实现类：志愿者组织只能针对本组织申报的志愿活动发布公告，
 * 公告发布成功后向该活动已通过报名的志愿者发送站内通知，志愿者只能看到本人已报名活动的公告。
 */
@Slf4j
@Service
public class ActivityAnnouncementServiceImpl implements ActivityAnnouncementService {

    /**
     * 公告业务编号格式，前缀 ann_ 加 5 位自增序号
     */
    private static final String ANNOUNCEMENT_ID_FORMAT = "ann_%05d";

    /**
     * 站内通知的接收角色：志愿者
     */
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

    /**
     * 按本组织的志愿活动分页查询活动公告
     *
     * @param loginId     志愿者组织登录账号
     * @param activityNum 活动编号，为 null 时查询本组织的全部活动公告
     * @param page        页码
     * @param size        每页条数
     * @return 活动公告分页结果，已关联活动名称
     */
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

    /**
     * 志愿者端分页查询本人已报名（待审核、已通过、候补）活动的公告
     *
     * @param volunteerNum 志愿者编号
     * @param page         页码
     * @param size         每页条数
     * @return 活动公告分页结果，已关联活动名称
     */
    @Override
    public IPage<ActivityAnnouncementView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size) {
        if (volunteerNum == null) {
            return PageSupport.of(page, size);
        }
        return activityAnnouncementMapper.selectPageForVolunteer(PageSupport.of(page, size), volunteerNum);
    }

    /**
     * 保存活动公告：公告编号为空时新增并生成公告业务编号，同时向该活动已通过报名的志愿者发送站内通知，
     * 否则修改已有活动公告
     *
     * @param loginId      志愿者组织登录账号
     * @param announcement 活动公告，包含所属活动编号、公告标题与公告内容
     * @return 返回给用户的中文提示
     */
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
            // 公告发布成功后向该活动已通过报名的志愿者发送站内通知
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
        // 修改活动公告时不改动删除标记、业务编号与发布组织
        announcement.setActannouncementIsdeleted(null);
        announcement.setActannouncementId(null);
        announcement.setOrganizationNum(organizationNum);
        activityAnnouncementMapper.updateById(announcement);
        log.info("志愿者组织修改活动公告，组织账号：{}，公告编号：{}", loginId, announcement.getActannouncementNum());
        return MESSAGE_SAVE_SUCCESS;
    }

    /**
     * 逻辑删除活动公告，只能删除本组织发布的活动公告
     *
     * @param loginId         志愿者组织登录账号
     * @param announcementNum 公告编号
     * @return 返回给用户的中文提示
     */
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
}
