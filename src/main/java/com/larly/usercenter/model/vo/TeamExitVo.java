package com.larly.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 队伍和用户信息封装类
 */
@Data
public class TeamExitVo {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

}
