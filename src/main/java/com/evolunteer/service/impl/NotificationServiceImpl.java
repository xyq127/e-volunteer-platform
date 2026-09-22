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

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

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

    @Override
    public void send(List<String> receiverIds, String receiverRole, String title, String detail) {
        if (receiverIds == null) {
            return;
        }
        for (String receiverId : receiverIds) {
            send(receiverId, receiverRole, title, detail);
        }
    }

    @Override
    public IPage<Notification> page(String receiverId, Integer pageNum, Integer pageSize) {
        Page<Notification> page = PageSupport.of(pageNum, pageSize);
        return notificationMapper.selectPageByReceiverId(page, receiverId);
    }

    @Override
    public int unreadCount(String receiverId) {
        return notificationMapper.countUnreadByReceiverId(receiverId);
    }

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

    @Override
    public int markAllRead(String receiverId) {
        return notificationMapper.updateReadByReceiverId(receiverId);
    }
}
