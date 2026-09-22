package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.PolicyAnnouncement;
import com.evolunteer.entity.PortalAnnouncementView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyAnnouncementMapper extends BaseMapper<PolicyAnnouncement> {

    IPage<PortalAnnouncementView> selectPageWithFileCount(Page<PortalAnnouncementView> page,
                                                          @Param("keyword") String keyword);

    Integer selectAdminNumByAdminId(@Param("adminId") String adminId);
}
