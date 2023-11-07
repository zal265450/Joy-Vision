package org.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.response.AuditResponse;
import org.luckyjourney.entity.user.Favorites;
import org.luckyjourney.entity.user.Follow;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.user.UserSubscribe;
import org.luckyjourney.entity.video.Type;
import org.luckyjourney.entity.vo.*;
import org.luckyjourney.exception.BaseException;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.user.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.audit.ImageAuditService;
import org.luckyjourney.service.audit.TextAuditService;
import org.luckyjourney.service.user.FavoritesService;
import org.luckyjourney.service.user.FollowService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.service.user.UserSubscribeService;
import org.luckyjourney.service.video.TypeService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
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
    private RedisTemplate redisTemplate;


    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private TextAuditService textAuditService;

    @Autowired
    private ImageAuditService imageAuditService;

    @Override
    public boolean register(RegisterVO registerVO) throws Exception {

        // 邮箱是否存在
        final int count = count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1){
            throw new BaseException("邮箱已被注册");
        }
        final String code = registerVO.getCode();
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + registerVO.getEmail());
        if (o == null){
            throw new BaseException("验证码为空");
        }
        if (!code.equals(o)){
            return false;
        }

        final User user = new User();
        user.setNickName(registerVO.getNickName());
        user.setEmail(registerVO.getEmail());
        user.setDescription("这个人很懒...");
        user.setPassword(registerVO.getPassword());
        user.setAvatar("");
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
        if (!ObjectUtils.isEmpty(userVO.getAvatar())){
            if (!ObjectUtils.isEmpty(user.getAvatar())){
                userVO.setAvatar(QiNiuConfig.CNAME+"/"+user.getAvatar());
            }
        }
        return userVO;
    }


    public void initModel(ModelVO modelVO) {
        // 初始化模型
        interestPushService.initUserModel(modelVO.getUserId(),modelVO.getLabels());
    }

    @Override
    public Page<User> getFollows(Long userId, BasePage basePage) {
        Page<User> page = new Page<>();
        // 获取关注列表
        final Collection<Long> followIds = followService.getFollow(userId, basePage);
        if (ObjectUtils.isEmpty(followIds)) return page;
        // 获取粉丝列表
        final HashSet<Long> fans = new HashSet<>();
        // 这里需要将数据转换，因为存到redis中数值小是用int保存，取出来需要用long比较
        fans.addAll(followService.getFans(userId, null));
        Map<Long,Boolean> map = new HashMap<>();
        for (Long followId : followIds) {
            map.put(followId,fans.contains(followId));
        }
        String t = QiNiuConfig.CNAME+"/";
        final ArrayList<User> users = new ArrayList<>();
        final Map<Long, User> userMap = getBaseInfoUserToMap(map.keySet());
        for (Long followId : followIds) {
            final User user = userMap.get(followId);
            user.setEach(map.get(user.getId()));
            if (!ObjectUtils.isEmpty(user.getAvatar())){
                user.setAvatar(t+user.getAvatar());
            }

            users.add(user);
        }
        page.setRecords(users);
        page.setTotal(users.size());

        return page;
    }

    @Override
    public Page<User> getFans(Long userId, BasePage basePage) {
        final Page<User> page = new Page<>();
        // 获取粉丝列表
        final Collection<Long> fansIds = followService.getFans(userId, basePage);
        if (ObjectUtils.isEmpty(fansIds)) return page;
        // 获取关注列表
        final HashSet<Long> followIds = new HashSet<>();
        followIds.addAll(followService.getFollow(userId,null));
        Map<Long,Boolean> map = new HashMap<>();
        // 遍历粉丝，查看关注列表中是否有
        for (Long fansId : fansIds) {
            map.put(fansId,followIds.contains(fansId));
        }
        final Map<Long, User> userMap = getBaseInfoUserToMap(map.keySet());
        String t = QiNiuConfig.CNAME+"/";
        final ArrayList<User> users = new ArrayList<>();
        // 遍历粉丝列表,保证有序性
        for (Long fansId : fansIds) {
            final User user = userMap.get(fansId);
            user.setEach(map.get(user.getId()));
            user.setAvatar(t+user.getAvatar());
            users.add(user);
        }

        page.setRecords(users);
        page.setTotal(users.size());
        return page;
    }

    private Map<Long,User> getBaseInfoUserToMap(Collection<Long> userIds){
        List<User> users = new ArrayList<>();
        if (!ObjectUtils.isEmpty(userIds)){
            users = list(new LambdaQueryWrapper<User>().in(User::getId, userIds).select(User::getId, User::getNickName, User::getDescription
                    , User::getSex, User::getAvatar));
        }
        return users.stream().collect(Collectors.toMap(User::getId,Function.identity()));
    }

    @Override
    public List<User> list(Collection<Long> userIds) {
        return list(new LambdaQueryWrapper<User>().in(User::getId,userIds)
                .select(User::getId,User::getNickName,User::getSex,User::getAvatar,User::getDescription));
    }

    @Override
    @Transactional
    public void subscribe(Set<Long> typeIds) {
        if (ObjectUtils.isEmpty(typeIds)) return;
        // 校验分类
        final Collection<Type> types = typeService.listByIds(typeIds);
        if (typeIds.size()!=types.size()){
            throw new BaseException("不存在的分类");
        }
        final Long userId = UserHolder.get();
        final ArrayList<UserSubscribe> userSubscribes = new ArrayList<>();
        for (Long typeId : typeIds) {
            final UserSubscribe userSubscribe = new UserSubscribe();
            userSubscribe.setUserId(userId);
            userSubscribe.setTypeId(typeId);
            userSubscribes.add(userSubscribe);
        }
        // 删除之前的
        userSubscribeService.remove(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId,userId));
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

    @Override
    public boolean follows(Long followsUserId) {

        final Long userId = UserHolder.get();

        return followService.follows(followsUserId,userId);
    }

    @Override
    public void updateUserModel(UserModel userModel) {
        interestPushService.updateUserModel(userModel);
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {

        // 从redis中取出
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + findPWVO.getEmail());
        if (o==null){
            return false;
        }
        // 校验
        if (Integer.parseInt(o.toString()) != findPWVO.getCode()){
            return false;
        }
        // 修改
        final User user = new User();
        user.setEmail(findPWVO.getEmail());
        user.setPassword(findPWVO.getNewPassword());
        update(user,new UpdateWrapper<User>().lambda().set(User::getPassword,findPWVO.getNewPassword()).eq(User::getEmail,findPWVO.getEmail()));
        return true;
    }

    @Override
    public void updateUser(UpdateUserVO user) {

        final Long userId = UserHolder.get();

        final User oldUser = getById(userId);
        // 需要审核
        if (!oldUser.getNickName().equals(user.getNickName())){
            oldUser.setNickName(user.getNickName());
            final AuditResponse audit = textAuditService.audit(user.getNickName());
            if (audit.getAuditStatus() != AuditStatus.SUCCESS) {
                throw new BaseException(audit.getMsg());
            }
        }
        if (!ObjectUtils.isEmpty(user.getDescription()) && !oldUser.getDescription().equals(user.getDescription())){
            oldUser.setDescription(user.getDescription());
            final AuditResponse audit = textAuditService.audit(user.getNickName());
            if (audit.getAuditStatus() != AuditStatus.SUCCESS) {
                throw new BaseException(audit.getMsg());
            }
        }
        if (!ObjectUtils.isEmpty(user.getAvatar()) && !oldUser.getAvatar().equals(user.getAvatar())){
            final AuditResponse audit = imageAuditService.audit(QiNiuConfig.CNAME+"/"+user.getAvatar());
            if (audit.getAuditStatus() != AuditStatus.SUCCESS) {
                throw new BaseException(audit.getMsg());
            }
            oldUser.setAvatar(user.getAvatar());
        }

        if (!ObjectUtils.isEmpty(user.getDefaultFavoritesId())){
            // 校验收藏夹
            favoritesService.exist(userId,user.getDefaultFavoritesId());
        }


        oldUser.setSex(user.getSex());

        oldUser.setDefaultFavoritesId(user.getDefaultFavoritesId());

        updateById(oldUser);
    }

    @Override
    public Collection<String> searchHistory(Long userId) {
        List<String> searchs = new ArrayList<>();
        if (userId!=null){
            searchs.addAll(redisCacheUtil.zGet(RedisConstant.USER_SEARCH_HISTORY+userId));
            searchs = searchs.subList(0,searchs.size() < 20 ? searchs.size() : 20);
        }
        return searchs;
    }

    @Override
    @Async
    public void addSearchHistory(Long userId, String search) {
        if (userId!=null){
          redisCacheUtil.zadd(RedisConstant.USER_SEARCH_HISTORY+userId,new Date().getTime(),search,-1);
        }
    }

    @Override
    public void deleteSearchHistory(Long userId) {
        if (userId!=null){
            redisCacheUtil.del(RedisConstant.USER_SEARCH_HISTORY+userId);
        }
    }


    public List<User> getUsers(Collection<Long> ids){
        final Map<Long, User> userMap = listByIds(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<User> result = new ArrayList<>();
        for (Long followId : ids) {
            final User user = new User();
            user.setId(followId);
            final User u = userMap.get(followId);
            user.setNickName(u.getNickName());
            user.setSex(u.getSex());
            user.setDescription(u.getDescription());
            result.add(user);
        }
        return result;
    }
}
