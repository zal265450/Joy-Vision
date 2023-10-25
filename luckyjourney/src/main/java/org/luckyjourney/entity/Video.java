package org.luckyjourney.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class Video  extends BaseEntity  {

    private static final long serialVersionUID = 1L;

    private String title;

    private String description;

    private String url;

    private Long userId;

    /**
     * 公开/私密，0：公开，1：私密，默认为0
     */
    private Boolean open;

}

