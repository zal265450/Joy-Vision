package org.luckyjourney.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @description: 模型
 * @Author: Xhy
 * @CreateTime: 2023-10-25 17:49
 */
@Data
public class ModelVO {

    // 性别
    @NotBlank(message = "你好歹有个性别吧?")
    private Boolean sex;

    // 兴趣视频分类
    private List<Long> videoTypes;
}
