package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Notification;

import java.util.List;

public interface NotificationService {

    void send(String receiverId, String receiverRole, String title, String detail);

    void send(List<String> receiverIds, String receiverRole, String title, String detail);

    IPage<Notification> page(String receiverId, Integer pageNum, Integer pageSize);

    int unreadCount(String receiverId);

    boolean markRead(String receiverId, Integer notificationNum);

    int markAllRead(String receiverId);
}
