package org.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.user.Favorites;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-10-30
 */
public interface FavoritesService extends IService<Favorites> {

    /**
     * 删除收藏夹,连收藏夹下的视频一块删除
     * @param idList
     * @param userId
     */
    void remove(List<Long> idList, Long userId);

    /**
     * 根据用户获取收藏夹
     * @param userId
     * @return
     */
    List<Favorites> listByUserId(Long userId);

    /**
     * 获取收藏夹下的所有视频id
     * @param favoritesId
     * @param userId
     * @return
     */
    List<Long> listVideoIds(Long favoritesId,Long userId);

    /**
     * 收藏视频
     * @param fId
     * @param vId
     */
    boolean favorites(Long fId, Long vId);
}
