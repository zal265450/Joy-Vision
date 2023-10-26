package org.luckyjourney.controller;


import org.luckyjourney.entity.Type;
import org.luckyjourney.entity.Video;
import org.luckyjourney.service.TypeService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@RestController
@RequestMapping("/luckyjourney/type")
public class TypeController {

    @Autowired
    private TypeService typeService;

    /**
     * 获取全部分类
     *
     * @return 视频信息
     */
    @GetMapping("/getAllType")
    public R getAllType() {
        List<Type> list = typeService.list(null);
        return R.ok().data(list);
    }


}

