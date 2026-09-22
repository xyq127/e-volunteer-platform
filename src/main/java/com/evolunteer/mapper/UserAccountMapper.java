package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.entity.UserAccount;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    List<UserAccount> selectByLoginId(@Param("id") String id);

    int updatePasswordById(@Param("id") String id, @Param("password") String password);

    void admin_create_organization(Map<Object, Object> map);

    void admin_set_account_enabled(Map<Object, Object> map);

    IPage<Volunteer> selectVolunteerAccountPage(Page<Volunteer> page, @Param("keyword") String keyword);

    List<Volunteer> selectVolunteerAccountList(@Param("keyword") String keyword);
}
