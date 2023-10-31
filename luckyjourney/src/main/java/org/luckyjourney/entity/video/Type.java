package org.luckyjourney.entity.video;

import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Arrays;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.luckyjourney.entity.BaseEntity;

/**
 * <p>
 * 
 * </p>
 * 分类,隐藏视频标签
 * @author xhy
 * @since 2023-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Type extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private String icon;

    private Boolean open;

    private String labelNames;

    public List<String> getLabels(){
        return Arrays.asList(labelNames.split(","));
    }
}

