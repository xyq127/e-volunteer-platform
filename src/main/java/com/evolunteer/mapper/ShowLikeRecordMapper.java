package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evolunteer.entity.ShowLikeRecord;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀点赞记录表 show_like_record 的数据访问接口，用于判断志愿者是否已点赞并保证点赞唯一。
 *
 * @Entity com.evolunteer.entity.ShowLikeRecord
 */
@Repository
public interface ShowLikeRecordMapper extends BaseMapper<ShowLikeRecord> {
}
