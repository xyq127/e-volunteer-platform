package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Notification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 站内通知表 notification 的数据访问接口，提供通知写入、按接收账号分页查询与已读标记能力。
 *
 * @Entity com.evolunteer.entity.Notification
 */
@Repository
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 按接收账号分页查询通知，未读通知优先展示
     *
     * @param page       分页对象
     * @param receiverId 接收账号
     * @return 通知分页结果
     */
    IPage<Notification> selectPageByReceiverId(Page<Notification> page, @Param("receiverId") String receiverId);

    /**
     * 统计接收账号的未读通知数量
     *
     * @param receiverId 接收账号
     * @return 未读通知数量
     */
    int countUnreadByReceiverId(@Param("receiverId") String receiverId);

    /**
     * 将接收账号的全部未读通知标记为已读
     *
     * @param receiverId 接收账号
     * @return 受影响的行数
     */
    int updateReadByReceiverId(@Param("receiverId") String receiverId);
}
