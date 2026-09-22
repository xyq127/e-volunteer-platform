package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.PolicyFile;
import com.evolunteer.entity.PortalAnnouncementView;
import com.evolunteer.entity.PortalShowView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalMapper {

    IPage<PortalAnnouncementView> selectAnnouncementPage(Page<PortalAnnouncementView> page,
                                                         @Param("keyword") String keyword);

    PortalAnnouncementView selectAnnouncementByNum(@Param("policyannouncementNum") Integer policyannouncementNum);

    List<PolicyFile> selectFilesByAnnouncementNums(@Param("policyannouncementNums") List<Integer> policyannouncementNums);

    IPage<Activity> selectActivityPage(Page<Activity> page, @Param("keyword") String keyword);

    IPage<Organization> selectOrganizationPage(Page<Organization> page, @Param("keyword") String keyword);

    IPage<PortalShowView> selectShowPage(Page<PortalShowView> page, @Param("keyword") String keyword);

    PortalShowView selectShowByNum(@Param("showNum") Integer showNum);

    List<String> selectPictureRoutes(@Param("showNum") Integer showNum);

    List<Integer> selectLikedShowNums(@Param("volunteerNum") Integer volunteerNum,
                                      @Param("showNums") List<Integer> showNums);
}
