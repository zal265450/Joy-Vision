package org.luckyjourney.entity.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-27 11:53
 */
@Data
public class UserModel {
    private List<Model> models;
    private Long userId;

}
