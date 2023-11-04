package org.luckyjourney.entity.vo;

import lombok.Data;

/**
 * @description: 修改用户信息
 * @Author: Xhy
 * @CreateTime: 2023-11-04 15:41
 */
@Data
public class UpdateUserVO {


    private String nickName;

    private String avatar;

    private Boolean sex;

    private String description;

    private Long defaultFavoritesId;

}
