package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.ActivityAnnouncement;
import com.evolunteer.entity.ActivityAnnouncementView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityAnnouncementMapper extends BaseMapper<ActivityAnnouncement> {

    IPage<ActivityAnnouncementView> selectPageByOrganization(Page<ActivityAnnouncementView> page,
                                                             @Param("organizationNum") Integer organizationNum,
                                                             @Param("activityNum") Integer activityNum);

    IPage<ActivityAnnouncementView> selectPageForVolunteer(Page<ActivityAnnouncementView> page,
                                                           @Param("volunteerNum") Integer volunteerNum);

    Integer selectNextAnnouncementSeq();
}
