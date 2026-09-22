package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.PortalShowView;
import com.evolunteer.entity.VolunteerShow;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface VolunteerShowMapper extends BaseMapper<VolunteerShow> {

    IPage<PortalShowView> selectPageByVolunteer(Page<PortalShowView> page,
                                                @Param("volunteerNum") Integer volunteerNum);

    VolunteerShow selectShowByNum(@Param("showNum") Integer showNum);

    int logicDeleteByShowNum(@Param("showNum") Integer showNum);

    void volunteer_publish_show(Map<Object, Object> map);

    int increaseBrowse(@Param("showNum") Integer showNum);

    int increaseLike(@Param("showNum") Integer showNum);

    int decreaseLike(@Param("showNum") Integer showNum);

    int refreshLikeCount(@Param("showNum") Integer showNum);
}
