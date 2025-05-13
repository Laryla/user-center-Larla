package com.larly.usercenter.contact;

/**
 * 用户常量
 *
 */
public interface UserContact {
    /**
     * 用户登录键（用户设置session和取session）
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 默认用户
     */
    Integer DEFAULT_ROLE = 0;

    /**
     * 管理员用户
      */
    Integer ADMIN_ROLE = 1;
}
