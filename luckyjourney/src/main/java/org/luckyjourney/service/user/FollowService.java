package org.luckyjourney.service.user;


import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.user.Follow;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.entity.vo.FollowVO;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-10-25
 */
public interface FollowService extends IService<Follow> {
    /**
     * 获取关注数量
     * @param userId
     * @return
     */
    int getFollowCount(Long userId);

    /**
     * 获取粉丝数量
     * @param userId
     * @return
     */
    int getFansCount(Long userId);

    /**
     * 获取关注人员且按照关注时间排序
     * @param userId
     * @return
     */
    Collection<Long> getFollow(Long userId, BasePage basePage);

    /**
     * 获取所有关注人员
     * @param userId
     * @return
     */
    Collection<Long> getFollow(Long userId);

    /**
     * 获取粉丝人员且安排关注时间排序
     * @param userId
     * @return
     */
    Collection<Long> getFans(Long userId,BasePage basePage);

    /**
     * 关注/取关
     * @param followsId 对方id
     * @param userId 自己id
     * @return
     */
    Boolean follows(Long followsId,Long userId);
}
