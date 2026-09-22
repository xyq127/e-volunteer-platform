package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.Volunteer;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者表 volunteer 的数据访问接口，提供志愿者注册、手机号查重与按条件查询志愿者等能力。
 *
 * @Entity com.evolunteer.entity.Volunteer
 */
/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者表 volunteer 的数据访问接口。
 *
 * @Entity com.evolunteer.entity.Volunteer
 */
@Repository
public interface VolunteerMapper extends BaseMapper<Volunteer> {

    /**
     * 按志愿者姓名查询志愿者
     *
     * @param volunteerName 志愿者姓名
     * @return 志愿者列表
     */
    List<Volunteer> selectByVolunteerName(@Param("volunteerName") String volunteerName);

    /**
     * 按志愿者编号查询志愿者
     *
     * @param volunteerId 志愿者编号
     * @return 志愿者列表
     */
    List<Volunteer> selectByVolunteerId(@Param("volunteerId") String volunteerId);

    /**
     * 统计使用指定手机号注册的志愿者数量
     *
     * @param signUpTel 注册手机号
     * @return 志愿者数量
     */
    int countByVolunteerTel(@Param("signUpTel") String signUpTel);

    /**
     * 调用数据库存储过程 volunteer_insert 完成志愿者注册，并返回数据库生成的志愿者编号
     *
     * @param map 注册参数，包含志愿者姓名、手机号、密码，以及输出参数 volunteerId
     */
    void volunteer_insert(Map<Object, Object> map);

}
