package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Notification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationMapper extends BaseMapper<Notification> {

    IPage<Notification> selectPageByReceiverId(Page<Notification> page, @Param("receiverId") String receiverId);

    int countUnreadByReceiverId(@Param("receiverId") String receiverId);

    int updateReadByReceiverId(@Param("receiverId") String receiverId);
}
