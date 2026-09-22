package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Notification;

import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 站内通知业务接口：在报名、审核、时长复核、活动提醒与结算等关键节点向志愿者与志愿者组织发送站内通知，
 * 并提供通知列表查询与已读标记能力。
 */
public interface NotificationService {

    /**
     * 发送站内通知
     *
     * @param receiverId   接收账号
     * @param receiverRole 接收角色
     * @param title        通知标题
     * @param detail       通知内容
     */
    void send(String receiverId, String receiverRole, String title, String detail);

    /**
     * 批量发送站内通知
     *
     * @param receiverIds  接收账号列表
     * @param receiverRole 接收角色
     * @param title        通知标题
     * @param detail       通知内容
     */
    void send(List<String> receiverIds, String receiverRole, String title, String detail);

    /**
     * 分页查询接收账号的通知
     *
     * @param receiverId 接收账号
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return 通知分页结果
     */
    IPage<Notification> page(String receiverId, Integer pageNum, Integer pageSize);

    /**
     * 统计接收账号的未读通知数量
     *
     * @param receiverId 接收账号
     * @return 未读通知数量
     */
    int unreadCount(String receiverId);

    /**
     * 将指定通知标记为已读
     *
     * @param receiverId      接收账号
     * @param notificationNum 通知编号
     * @return 标记成功返回 true
     */
    boolean markRead(String receiverId, Integer notificationNum);

    /**
     * 将接收账号的全部通知标记为已读
     *
     * @param receiverId 接收账号
     * @return 受影响的行数
     */
    int markAllRead(String receiverId);
}
