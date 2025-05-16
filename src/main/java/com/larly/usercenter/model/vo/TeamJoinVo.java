package com.larly.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.larly.usercenter.model.response.UserResult;
import lombok.Data;

import java.util.Date;

/**
 * 队伍和用户信息封装类
 */
@Data
public class TeamJoinVo {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 密码
     */
    private String password;

}
