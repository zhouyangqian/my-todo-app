package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典项实体
 */
@Data
@TableName("sys_dict_item")
public class DictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 字典类型ID
     */
    private Long dictTypeId;

    /**
     * 字典项编码
     */
    private String itemCode;

    /**
     * 字典项名称
     */
    private String itemName;

    /**
     * 字典项值
     */
    private String itemValue;

    /**
     * 扩展值1
     */
    private String extValue1;

    /**
     * 扩展值2
     */
    private String extValue2;

    /**
     * 扩展值3
     */
    private String extValue3;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 是否默认: 0-否, 1-是
     */
    private Integer isDefault;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 软删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
