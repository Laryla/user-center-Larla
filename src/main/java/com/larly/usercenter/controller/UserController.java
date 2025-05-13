package com.larly.usercenter.controller;

import com.larly.usercenter.common.BaseResponse;
import com.larly.usercenter.common.ErrorCode;
import com.larly.usercenter.common.ResultUtils;
import com.larly.usercenter.contact.UserContact;
import com.larly.usercenter.exception.BusinessException;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.request.UserDeleteParam;
import com.larly.usercenter.model.request.UserLoginParam;
import com.larly.usercenter.model.request.UserRegisterParam;
import com.larly.usercenter.model.request.UserSearchParam;
import com.larly.usercenter.model.response.PageResult;
import com.larly.usercenter.model.response.UserResult;
import com.larly.usercenter.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/api/user")
//跨域问题
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials = "true")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * 用户注册
     * @param userRegisterParam （用户名， 密码， 二次密码）user
     * @return （用户ID）
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterParam userRegisterParam) {
        if(userRegisterParam == null){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Long l = userService.userRegister(userRegisterParam);
        return ResultUtils.success(l);
    }

    /**
     *  用户登录
     * @param userLoginParam （用户名，密码）
     * @param request 前端里的 cokie/session，用于区分哪个用户
     * @return 用户对象
     */
    @PostMapping("/login")
//     HttpServletRequest request:用户登录的凭证在请求头中保存，这里用的session保存在服务器中
    public BaseResponse<UserResult> userLogin(@RequestBody UserLoginParam userLoginParam, HttpServletRequest request){
        if(userLoginParam == null){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginParam.getUserAccount();
        String userPassword = userLoginParam.getUserPassword();
        UserResult user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);

    }

    /**
     *
     * 根据用户名查询用户（模糊.仅管理员）
     * @param userSearchParam （用户名，当前页，每页数量）
     * @param request session
     * @return 用户列表
     */
    @GetMapping("/search")
    public BaseResponse<PageResult<UserResult>> searchUsers(UserSearchParam userSearchParam, HttpServletRequest request){
        if(userService.isAdmin(request) ){
            System.out.println("无权限");
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        PageResult<UserResult> userResultPageResult = userService.searchUsers(userSearchParam);
        return ResultUtils.success(userResultPageResult);
    }

    /**
     *
     * 推荐用户
     * @return 用户列表
     */
    @GetMapping("/recommend")
    public BaseResponse<PageResult<UserResult>> recommendUsers( @RequestParam(defaultValue = "10") Integer pageSize,
                                                                @RequestParam(defaultValue = "1") Integer pageNum, HttpServletRequest request ){
        PageResult<UserResult> result = userService.recommendUsers( pageNum,pageSize, request);
        return ResultUtils.success(result);
    }

    /**
     *
     * 根据标签查询用户
     * @param tagNameList 标签列表
     * @return 用户列表
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<UserResult>> searchUsersByTags( @RequestParam(required = false) List<String> tagNameList){
       if(CollectionUtils.isEmpty(tagNameList)){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
        // tagNameList 会自动封装成 ["aaa"]
        List<UserResult> userResults = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userResults);

    }

    /**
     *
     * 根据ID删除用户(仅管理员)
     * @param userDeleteParam （ID）
     * @param request session
     * @return （true/false）
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteParam userDeleteParam, HttpServletRequest request){
//        仅仅管理员可查看
        Long id = userDeleteParam.getId();
        if(userService.isAdmin(request)){
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        if(id <= 0 ){
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        Boolean b = userService.deleteUser(id);
        return ResultUtils.success(b);
    }


    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<UserResult> getCurrentUser(HttpServletRequest request){
        Object attribute = request.getSession().getAttribute(UserContact.USER_LOGIN_STATE);
        if(attribute == null){
            return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = (User)attribute;
        UserResult safetyUser = userService.getCurrentUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return true/false
     */
    @GetMapping("/phone")
    public BaseResponse<Boolean> selectPhone(String phone){
        boolean b = userService.selectPhone(phone);
        return ResultUtils.success(b);
    }

    /**
     *
     * 更新用户信息
     * @param user 用户信息
     * @param request session
     * @return true/false
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest request) {
//        非空
        if(user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
//        修改数据
        Boolean result = userService.updateUser(user,userLogin);

        return ResultUtils.success(result);

    }

}














