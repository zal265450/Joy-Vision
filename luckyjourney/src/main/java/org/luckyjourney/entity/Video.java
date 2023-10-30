package org.luckyjourney.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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

    // YV ID 以YV+UUID
    private String YV;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "url不能为空")
    private String url;

    private Long userId;

    private String cover;
    /**
     * 公开/私密，0：公开，1：私密，默认为0
     */
    private Boolean open;

    // 审核状态:通过,未通过,审核中,人工审核。这里不该出现人工审核
    private Integer status;

    // 审核状态的消息，当前嵌套在这里，应该有一个审核表?
    private String msg;

    // 审核快慢状态
    private Boolean auditStatus;

    private Long startCount;

    private Long shareCount;

    private Long historyCount;

    /**
     * 视频分类id
     */
    @NotNull(message = "分类不能为空")
    private Long typeId;

    @TableField(exist = false)
    private String typeName;

    @TableField(exist = false)
    private String userName;
}

