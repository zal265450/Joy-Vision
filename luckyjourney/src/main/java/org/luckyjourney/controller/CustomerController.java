package org.luckyjourney.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.user.Favorites;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.entity.vo.FollowVO;
import org.luckyjourney.entity.vo.ModelVO;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.service.user.FavoritesService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-25 15:27
 */
@RestController
@RequestMapping("/luckyjourney/customer")
public class CustomerController {


    @Autowired
    private UserService userService;

    @Autowired
    private FavoritesService favoritesService;


    /**
     * 获取个人信息
     * @param userId
     * @return
     * @throws Exception
     */
    @GetMapping("/getInfo/{userId}")
    public R getInfo(@PathVariable Long userId){
        return R.ok().data(userService.getInfo(userId));
    }





    /**
     * 获取关注人员
     * @param basePage
     * @param userId
     * @return
     */
    @GetMapping("/follows")
    public R getFollows(BasePage basePage,Long userId){
        return R.ok().data(userService.getFollows(userId,basePage));
    }

    /**
     * 获取粉丝
     * @param basePage
     * @param userId
     * @return
     */
    @GetMapping("/fans")
    public R getFans(BasePage basePage,Long userId){
        return R.ok().data(userService.getFans(userId,basePage));
    }


    /**
     * 获取所有的收藏夹
     * @return
     */
    @GetMapping("/favorites")
    public R listFavorites(){
        final Long userId = UserHolder.get();
        List<Favorites> favorites = favoritesService.listByUserId(userId);
        return R.ok().data(favorites);
    }


    /**
     * 获取指定收藏夹
     * @param id
     * @return
     */
    @GetMapping("/favorites/{id}")
    public R getFavorites(@PathVariable Long id){
        return R.ok().data(favoritesService.getById(id));
    }

    /**
     * 添加/修改收藏夹
     * @param favorites
     * @return
     */
    @PostMapping("/favorites")
    public R saveOrUpdateFavorites(@RequestBody @Validated Favorites favorites){
        final Long userId = UserHolder.get();
        final Long id = favorites.getId();
        favorites.setUserId(userId);
        final int count = favoritesService.count(new LambdaQueryWrapper<Favorites>().eq(Favorites::getName, favorites.getName()).eq(Favorites::getUserId, userId));
        if (count == 1){
            return R.error().message("已存在相同名称的收藏夹");
        }
        favoritesService.saveOrUpdate(favorites);
        return R.ok().message(id !=null ? "修改成功" : "添加成功");
    }

    /**
     * 删除收藏夹
     * @param ids
     * @return
     */
    @DeleteMapping("/favorites/{ids}")
    public R deleteFavorites(@PathVariable String ids){
        final List<Long> idList = Arrays.asList(ids.split(",")).stream().map(i -> Long.valueOf(i)).collect(Collectors.toList());
        favoritesService.remove(idList,UserHolder.get());
        return R.ok().message("删除成功");
    }


    /**
     * 用户订阅分类
     */
    @PostMapping("/subscribe")
    public R subscribe(@RequestParam(required = false) String types){
        final HashSet<Long> typeSet = new HashSet<>();
        for (String s : types.split(",")) {
            typeSet.add(Long.parseLong(s));
        }
        userService.subscribe(typeSet);
        return R.ok().message("订阅成功");
    }

    /**
     * 获取用户订阅的分类
     * @return
     */
    @GetMapping("/subscribe")
    public R listSubscribeType(){
        return R.ok().data(userService.listSubscribeType(UserHolder.get()));
    }
}
