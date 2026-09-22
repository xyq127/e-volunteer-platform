package com.evolunteer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    @DisplayName("成功结果的状态码与提示信息正确")
    void successShouldCarrySuccessCode() {
        ApiResponse response = ApiResponse.success();

        assertEquals(100, response.getCode());
        assertEquals("处理成功！", response.getMsg());
        assertTrue(response.getExtend().isEmpty());
    }

    @Test
    @DisplayName("失败结果的状态码与提示信息正确")
    void failShouldCarryFailCode() {
        ApiResponse response = ApiResponse.fail();

        assertEquals(200, response.getCode());
        assertEquals("处理失败！", response.getMsg());
    }

    @Test
    @DisplayName("添加业务数据后可被读取且支持连续追加")
    void addShouldStoreBusinessData() {
        List<String> activities = List.of("社区爱心助学志愿活动", "敬老院陪伴志愿服务");

        ApiResponse response = ApiResponse.success().add("auditActivity", activities).add("total", activities.size());

        assertSame(activities, response.getExtend().get("auditActivity"));
        assertEquals(2, response.getExtend().get("total"));
    }
}
