package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.PolicyFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 公告附件表 policy_file 的数据访问接口，并提供按公告查询全部有效附件的能力。
 *
 * @Entity com.evolunteer.entity.PolicyFile
 */
@Repository
public interface PolicyFileMapper extends BaseMapper<PolicyFile> {

    /**
     * 按公告查询附件列表，按附件编号升序排列
     *
     * @param policyannouncementNum 公告编号
     * @return 附件列表
     */
    List<PolicyFile> selectByAnnouncementNum(@Param("policyannouncementNum") Integer policyannouncementNum);
}
