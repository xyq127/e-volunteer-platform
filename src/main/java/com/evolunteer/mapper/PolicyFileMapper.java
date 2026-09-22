package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.PolicyFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyFileMapper extends BaseMapper<PolicyFile> {

    List<PolicyFile> selectByAnnouncementNum(@Param("policyannouncementNum") Integer policyannouncementNum);
}
