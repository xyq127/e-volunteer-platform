package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Notification;
import com.evolunteer.mapper.NotificationMapper;
import com.evolunteer.service.NotificationService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 站内通知业务实现类：通知随业务动作写入，是业务结果的提醒通道，
 * 因此写入失败只记录日志，不影响已完成的业务数据。
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 发送站内通知
     *
     * @param receiverId   接收账号
     * @param receiverRole 接收角色
     * @param title        通知标题
     * @param detail       通知内容
     */
    @Override
    public void send(String receiverId, String receiverRole, String title, String detail) {
        if (receiverId == null || receiverId.trim().isEmpty()) {
            return;
        }
        try {
            Notification notification = new Notification();
            notification.setReceiverId(receiverId);
            notification.setReceiverRole(receiverRole);
            notification.setNotificationTitle(title);
            notification.setNotificationDetail(detail);
            notification.setNotificationRead(0);
            notification.setNotificationTime(new Date());
            notificationMapper.insert(notification);
        } catch (RuntimeException e) {
            log.warn("站内通知写入失败，接收账号：{}，通知标题：{}", receiverId, title, e);
        }
    }

    /**
     * 批量发送站内通知
     *
     * @param receiverIds  接收账号列表
     * @param receiverRole 接收角色
     * @param title        通知标题
     * @param detail       通知内容
     */
    @Override
    public void send(List<String> receiverIds, String receiverRole, String title, String detail) {
        if (receiverIds == null) {
            return;
        }
        for (String receiverId : receiverIds) {
            send(receiverId, receiverRole, title, detail);
        }
    }

    /**
     * 分页查询接收账号的通知
     *
     * @param receiverId 接收账号
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return 通知分页结果
     */
    @Override
    public IPage<Notification> page(String receiverId, Integer pageNum, Integer pageSize) {
        Page<Notification> page = PageSupport.of(pageNum, pageSize);
        return notificationMapper.selectPageByReceiverId(page, receiverId);
    }

    /**
     * 统计接收账号的未读通知数量
     *
     * @param receiverId 接收账号
     * @return 未读通知数量
     */
    @Override
    public int unreadCount(String receiverId) {
        return notificationMapper.countUnreadByReceiverId(receiverId);
    }

    /**
     * 将指定通知标记为已读，只能标记本账号收到的通知
     *
     * @param receiverId      接收账号
     * @param notificationNum 通知编号
     * @return 标记成功返回 true
     */
    @Override
    public boolean markRead(String receiverId, Integer notificationNum) {
        Notification notification = notificationMapper.selectById(notificationNum);
        if (notification == null || !receiverId.equals(notification.getReceiverId())) {
            return false;
        }
        if (Integer.valueOf(1).equals(notification.getNotificationRead())) {
            return true;
        }
        Notification update = new Notification();
        update.setNotificationNum(notificationNum);
        update.setNotificationRead(1);
        return notificationMapper.updateById(update) > 0;
    }

    /**
     * 将接收账号的全部通知标记为已读
     *
     * @param receiverId 接收账号
     * @return 受影响的行数
     */
    @Override
    public int markAllRead(String receiverId) {
        return notificationMapper.updateReadByReceiverId(receiverId);
    }
}
