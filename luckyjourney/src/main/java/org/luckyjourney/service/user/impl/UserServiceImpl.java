package org.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.user.Favorites;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.user.UserSubscribe;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.vo.*;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.user.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.user.FavoritesService;
import org.luckyjourney.service.user.FollowService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.user.UserSubscribeService;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private TypeService typeService;

    @Autowired
    private UserSubscribeService userSubscribeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private FavoritesService favoritesService;

    @Override
    public boolean register(RegisterVO registerVO) throws Exception {

        // 邮箱是否存在
        final int count = count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1){
            throw new Exception("邮箱已被注册");
        }
        final String code = registerVO.getCode();
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + code);
        if (o == null){
            throw new IllegalArgumentException("验证码为空");
        }
        final User user = new User();
        user.setNickName(UUID.randomUUID().toString().substring(0,10));
        user.setEmail(registerVO.getEmail());
        user.setPassword(registerVO.getPassword());

        save(user);

        // 创建默认收藏夹
        final Favorites favorites = new Favorites();
        favorites.setUserId(user.getId());
        favorites.setName("默认收藏夹");
        favoritesService.save(favorites);

        // 这里如果单独抽出一个用户配置表就好了,但是没有必要再搞个表
        user.setDefaultFavoritesId(favorites.getId());
        updateById(user);
        return true;
    }

    @Override
    public UserVO getInfo(Long userId){

        final User user = getById(userId);
        if (ObjectUtils.isEmpty(user)){
            return new UserVO();
        }
        final UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);

        // 查出关注数量
        final long followCount = followService.getFollowCount(userId);

        // 查出粉丝数量
        final long fansCount = followService.getFansCount(userId);
        userVO.setFollow(followCount);
        userVO.setFans(fansCount);
        return userVO;
    }


    public void initModel(ModelVO modelVO) {
        // 初始化模型
        interestPushService.initUserModel(modelVO.getUserId(),modelVO.getLabels());
    }

    @Override
    public List<User> getFollows(Long userId, BasePage basePage) {

        final List<Long> followIds = followService.getFollow(userId, basePage);
        return getUsers(followIds);
    }

    @Override
    public List<User> getFans(Long userId, BasePage basePage) {
        final List<Long> fansIds = followService.getFans(userId, basePage);
        return getUsers(fansIds);
    }

    @Override
    public List<User> list(Collection<Long> userIds) {
        return list(new LambdaQueryWrapper<User>().in(User::getId,userIds).select(User::getId,User::getNickName,User::getSex));
    }

    @Override
    public void subscribe(Set<Long> typeIds) {
        if (ObjectUtils.isEmpty(typeIds)) return;
        // 校验分类
        final Collection<Type> types = typeService.listByIds(typeIds);
        if (typeIds.size()!=types.size()){
            throw new IllegalArgumentException("不存在的分类");
        }
        final Long userId = UserHolder.get();
        final ArrayList<UserSubscribe> userSubscribes = new ArrayList<>();
        for (Long typeId : typeIds) {
            final UserSubscribe userSubscribe = new UserSubscribe();
            userSubscribe.setUserId(userId);
            userSubscribe.setTypeId(typeId);
            userSubscribes.add(userSubscribe);
        }
        userSubscribeService.saveBatch(userSubscribes);
        // 初始化模型
        final ModelVO modelVO = new ModelVO();
        modelVO.setUserId(UserHolder.get());
        // 获取分类下的标签
        List<String> labels = new ArrayList();
        for (Type type : types) {
            labels.addAll(type.buildLabel());
        }
        modelVO.setLabels(labels);
        initModel(modelVO);

    }

    @Override
    public Collection<Type> listSubscribeType(Long userId) {
        final List<Long> typeIds = userSubscribeService.list(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId, userId))
                .stream().map(UserSubscribe::getTypeId).collect(Collectors.toList());
        final List<Type> types = typeService.list(new LambdaQueryWrapper<Type>()
                .in(Type::getId, typeIds).select(Type::getId, Type::getName, Type::getIcon));
        return types;
    }


    public List<User> getUsers(List<Long> ids){
        final Map<Long, String> userMap = listByIds(ids).stream().collect(Collectors.toMap(User::getId, User::getNickName));
        List<User> result = new ArrayList<>();
        for (Long followId : ids) {
            final User user = new User();
            user.setId(followId);
            user.setNickName(userMap.get(followId));
            result.add(user);
        }
        return result;
    }
}
