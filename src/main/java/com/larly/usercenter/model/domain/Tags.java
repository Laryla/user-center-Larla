package com.larly.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;

/**
 * 标签
 * @TableName tags
 */
@TableName(value ="tags")
@Data
public class Tags {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String tag_name;

    /**
     * 用户 id
     */
    private Long user_id;

    /**
     * 父标签 id
     */
    private Long parent_id;

    /**
     * 0 - 不是， 1 - 是
     */
    private Integer is_parent;

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * 
     */
    private Date update_time;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer is_delete;
}