package com.larly.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.larly.usercenter.common.BaseResponse;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.request.UserRegisterParam;
import com.larly.usercenter.model.request.UserSearchParam;
import com.larly.usercenter.model.response.PageResult;
import com.larly.usercenter.model.response.UserResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 许颢达
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-04-26 21:51:28
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @return 用户ID
     */
    public Long userRegister(UserRegisterParam userRegisterParam);

    /**
     * 用户登录
     * @param userAccount 账号
     * @param userPassword 密码
     * @return 用户信息
     */
    public UserResult userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户查询根据用户名字（模糊）
     * @param userSearchParam 用户查询参数
     * @return 用户信息
     */
    public PageResult<UserResult> searchUsers(UserSearchParam userSearchParam);

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return true/false
     */
    public boolean deleteUser(Long id);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return true/false
     */
    public boolean selectPhone(String phone);

    /**
     * 根据标签查询用户
     * @param tagNameList 标签列表
     * @return 用户列表
     */
    public List<UserResult> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @param userLogin
     * @return true/false
     */
    public Boolean updateUser(User user, User userLogin);

    /**
     * 获取用户登陆状态（用于区分管理员和拿到用户session）
     * @param request
     * @return 返回用户User
     */
    public User getUserLogin(HttpServletRequest request);

    /**
     * 是否是管理员
     * @param request
     * @return
     */
    public Boolean isAdmin(HttpServletRequest request);


    /**
     * 是否是管理员
     * @param userLogin
     * @return
     */
    public Boolean isAdmin(User userLogin);

    /**
     * 获取当前用户信息
     * @param user
     * @return
     */
    public UserResult getCurrentUser(User user);

    /**
     * 推荐用户
     * @param userSearchParam
     * @return
     */
    PageResult<UserResult> recommendUsers(Integer  pageNum, Integer pageSize,  HttpServletRequest request);
}
