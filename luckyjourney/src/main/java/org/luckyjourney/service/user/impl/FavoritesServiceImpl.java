package org.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.user.Favorites;
import org.luckyjourney.entity.user.FavoritesVideo;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.user.FavoritesMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.user.FavoritesService;
import org.luckyjourney.service.user.FavoritesVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-30
 */
@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {


    @Autowired
    private FavoritesVideoService favoritesVideoService;


    @Override
    @Transactional
    public void remove(List<Long> idList, Long userId) {

        final boolean result = remove(new LambdaQueryWrapper<Favorites>().in(Favorites::getId, idList).eq(Favorites::getUserId, userId));
        // 如果能删除成功说明是自己的收藏夹
        if (result){
            favoritesVideoService.remove(new LambdaQueryWrapper<FavoritesVideo>().in(FavoritesVideo::getFavoritesId,idList));
        }else {
            throw new IllegalArgumentException("你小子想删别人的收藏夹?");
        }
    }

    @Override
    public List<Favorites> listByUserId(Long userId) {
        final List<Favorites> favorites = list(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId));
        return favorites;
    }

    @Override
    public List<Long> listVideoIds(Long favoritesId, Long userId) {

        // 不直接返回中间表是为了隐私性 (当前没实现收藏夹公开功能)

        // 校验
        final Favorites favorites = getOne(new LambdaQueryWrapper<Favorites>().eq(Favorites::getId, favoritesId).eq(Favorites::getUserId, userId));
        if (favorites == null){
            throw new IllegalArgumentException("收藏夹为空");
        }

        final List<Long> videoIds = favoritesVideoService.list(new LambdaQueryWrapper<FavoritesVideo>().eq(FavoritesVideo::getFavoritesId, favoritesId))
                .stream().map(FavoritesVideo::getVideoId).collect(Collectors.toList());

        return videoIds;
    }

    @Override
    public boolean favorites(Long fId, Long vId) {

        final Long userId = UserHolder.get();

        try {
            final FavoritesVideo favoritesVideo = new FavoritesVideo();
            favoritesVideo.setFavoritesId(fId);
            favoritesVideo.setVideoId(vId);
            favoritesVideo.setUserId(userId);
            favoritesVideoService.save(favoritesVideo);

        }catch (Exception e){
            favoritesVideoService.remove(new LambdaQueryWrapper<FavoritesVideo>().eq(FavoritesVideo::getFavoritesId, fId)
                    .eq(FavoritesVideo::getVideoId, vId).eq(FavoritesVideo::getUserId, userId));
            return false;
        }
        return true;
    }
}
