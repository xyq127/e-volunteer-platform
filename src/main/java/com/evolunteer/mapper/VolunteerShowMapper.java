package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.PortalShowView;
import com.evolunteer.entity.VolunteerShow;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀表 show 的数据访问接口，志愿者发布志愿秀通过数据库存储过程完成，
 * 并提供志愿者本人志愿秀分页查询与浏览、点赞计数的维护能力。
 * <p>
 * 志愿秀表表名 show 是 MySQL 保留字，MyBatis-Plus 生成的通用语句不会加反引号，
 * 因此志愿秀的查询与写入统一在本接口的映射文件中完成。
 *
 * @Entity com.evolunteer.entity.VolunteerShow
 */
@Repository
public interface VolunteerShowMapper extends BaseMapper<VolunteerShow> {

    /**
     * 按志愿者分页查询本人发布的志愿秀，附带未删除图片数量，按分享时间倒序排列
     *
     * @param page         分页对象
     * @param volunteerNum 志愿者编号
     * @return 志愿秀分页结果
     */
    IPage<PortalShowView> selectPageByVolunteer(Page<PortalShowView> page,
                                                @Param("volunteerNum") Integer volunteerNum);

    /**
     * 按志愿秀编号查询未删除的志愿秀
     *
     * @param showNum 志愿秀编号
     * @return 志愿秀信息，志愿秀不存在或已删除时返回 null
     */
    VolunteerShow selectShowByNum(@Param("showNum") Integer showNum);

    /**
     * 逻辑删除志愿秀，将删除标记置 1
     *
     * @param showNum 志愿秀编号
     * @return 受影响的行数
     */
    int logicDeleteByShowNum(@Param("showNum") Integer showNum);

    /**
     * 调用数据库存储过程 volunteer_publish_show 发布志愿秀，并回填志愿秀编号与提示信息
     *
     * @param map 发布参数，包含志愿者编号、活动编号、分享内容与输出参数 showNum、msg、ok
     */
    void volunteer_publish_show(Map<Object, Object> map);

    /**
     * 志愿秀被查看时累加浏览次数
     *
     * @param showNum 志愿秀编号
     * @return 受影响的行数
     */
    int increaseBrowse(@Param("showNum") Integer showNum);

    /**
     * 志愿者点赞时累加点赞次数
     *
     * @param showNum 志愿秀编号
     * @return 受影响的行数
     */
    int increaseLike(@Param("showNum") Integer showNum);

    /**
     * 志愿者取消点赞时递减点赞次数，点赞次数已为 0 时不再递减
     *
     * @param showNum 志愿秀编号
     * @return 受影响的行数
     */
    int decreaseLike(@Param("showNum") Integer showNum);

    /**
     * 按点赞记录重新核算点赞次数，保证点赞次数与点赞记录行数一致且不小于 0
     *
     * @param showNum 志愿秀编号
     * @return 受影响的行数
     */
    int refreshLikeCount(@Param("showNum") Integer showNum);
}
