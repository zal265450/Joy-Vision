package org.luckyjourney.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.luckyjourney.entity.json.SettingScoreJson;

/**
 * <p>
 * 
 * </p>
 *
 * @author xhy
 * @since 2023-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_setting")
public class Setting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String auditPolicy;

    private Double hotLimit;

    @TableField(exist = false)
    private SettingScoreJson settingScoreJson;

}
