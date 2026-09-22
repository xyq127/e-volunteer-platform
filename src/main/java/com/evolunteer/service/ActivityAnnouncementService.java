package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.ActivityAnnouncement;
import com.evolunteer.entity.ActivityAnnouncementView;

public interface ActivityAnnouncementService {

    String MESSAGE_SAVE_SUCCESS = "活动公告保存成功！";

    String MESSAGE_DELETE_SUCCESS = "活动公告删除成功！";

    IPage<ActivityAnnouncementView> pageByOrganization(String loginId, Integer activityNum, Integer page, Integer size);

    IPage<ActivityAnnouncementView> pageForVolunteer(Integer volunteerNum, Integer page, Integer size);

    String saveAnnouncement(String loginId, ActivityAnnouncement announcement);

    String deleteAnnouncement(String loginId, Integer announcementNum);
}
