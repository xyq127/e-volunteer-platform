package com.evolunteer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 统一响应结果类单元测试：校验成功/失败状态码、提示信息与业务数据装载。
 */
class MsgTest {

    @Test
    @DisplayName("成功结果的状态码与提示信息正确")
    void successShouldCarrySuccessCode() {
        Msg msg = Msg.success();

        assertEquals(100, msg.getCode());
        assertEquals("处理成功！", msg.getMsg());
        assertTrue(msg.getExtend().isEmpty());
    }

    @Test
    @DisplayName("失败结果的状态码与提示信息正确")
    void failShouldCarryFailCode() {
        Msg msg = Msg.fail();

        assertEquals(200, msg.getCode());
        assertEquals("处理失败！", msg.getMsg());
    }

    @Test
    @DisplayName("添加业务数据后可被读取且支持连续追加")
    void addShouldStoreBusinessData() {
        List<String> activities = List.of("社区爱心助学志愿活动", "敬老院陪伴志愿服务");

        Msg msg = Msg.success().add("auditActivity", activities).add("total", activities.size());

        assertSame(activities, msg.getExtend().get("auditActivity"));
        assertEquals(2, msg.getExtend().get("total"));
    }
}
