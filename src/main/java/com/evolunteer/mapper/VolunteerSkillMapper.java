package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.VolunteerSkill;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者技能表 volunteer_skill 的数据访问接口，保存志愿者登记的服务技能标签。
 *
 * @Entity com.evolunteer.entity.VolunteerSkill
 */
@Repository
public interface VolunteerSkillMapper extends BaseMapper<VolunteerSkill> {

    /**
     * 按志愿者查询其登记的服务技能标签
     *
     * @param volunteerNum 志愿者编号
     * @return 技能标签列表
     */
    List<String> selectSkillNamesByVolunteerNum(@Param("volunteerNum") Integer volunteerNum);

    /**
     * 清除志愿者已登记的服务技能标签，保存档案时先清空再写入
     *
     * @param volunteerNum 志愿者编号
     * @return 受影响的行数
     */
    int deleteSkillNamesByVolunteerNum(@Param("volunteerNum") Integer volunteerNum);
}
