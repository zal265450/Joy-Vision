package org.luckyjourney.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 *
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Video extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "url不能为空")
    private String url;

    private Long userId;

    /**
     * 公开/私密，0：公开，1：私密，默认为0
     */
    private Byte open;
    /**
     * 视频分类id
     */
    @NotBlank(message = "分类不能为空")
    private Long typeId;

}

