package com.larly.usercenter.service.impl;
import java.util.Arrays;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.larly.usercenter.common.ErrorCode;
import com.larly.usercenter.contact.UserContact;
import com.larly.usercenter.exception.BusinessException;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.request.UserRegisterParam;
import com.larly.usercenter.model.request.UserSearchParam;
import com.larly.usercenter.model.response.PageResult;
import com.larly.usercenter.model.response.UserResult;
import com.larly.usercenter.service.UserService;
import com.larly.usercenter.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;;
import java.util.stream.Collectors;

import static com.larly.usercenter.contact.UserContact.USER_LOGIN_STATE;

/**
* @author 许颢达
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-04-26 21:51:28
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Long userRegister(UserRegisterParam userRegisterParam) {

        String userPassword = userRegisterParam.getUserPassword();
        String checkPassword = userRegisterParam.getCheckPassword();
        String userAccount = userRegisterParam.getUserAccount();
        String phone = userRegisterParam.getPhone();
        Integer gender = userRegisterParam.getGender();
//        非空
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
//      账号长度不小于4位
        if(userAccount.length() <= 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不小于4位数");
        }

//        密码不小于8位数
        if(userPassword.length() <= 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不小于8位数");
        }



//        账户不包含特殊字符
        String pattern = "^[a-zA-Z0-9_]*$";
        if(!userAccount.matches(pattern)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帐号不包含特殊字符");
        }



//        密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验密码相同");
        }

//        账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
//        eq（数据库里的字段，传参）
        Long isExists =  userMapper.selectCount(queryWrapper);
        if(isExists > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该账号被注册");
        }

//        加密密码
        final String SALT = "larly";
        String newPassword = DigestUtils.md5DigestAsHex(( SALT + userPassword).getBytes());

//        插入用户
        User user = new User();
        user.setUserPassword(newPassword);
        user.setUserAccount(userAccount);
        user.setGender(gender);
        user.setPhone(phone);
        user.setUserName(userAccount);
        int i = userMapper.insert(user);
        if( i == 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"添加失败请重试");
        }



        return user.getId();
    }

    @Override
    public UserResult userLogin(String userAccount, String userPassword, HttpServletRequest request) {
//        非空
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
//      账号长度不小于4位
        if(userAccount.length() <= 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不小于4位数");
        }

//        密码不小于8位数
        if(userPassword.length() <= 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不小于8位数");
        }



//        账户不包含特殊字符
        String pattern = "^[a-zA-Z0-9_]*$";
        if(!userAccount.matches(pattern)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"帐号不包含特殊字符");
        }

//        加密密码
        final String SALT = "larly";
        String newPassword = DigestUtils.md5DigestAsHex(( SALT + userPassword).getBytes());

//        查询该用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        设置查询条件账号和加密密码相同
        queryWrapper.eq("user_account",userAccount);
        queryWrapper.eq("user_password",newPassword);
        User user = userMapper.selectOne(queryWrapper);

        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码错误");
        }

//        （如果找到）找到该用户并将用户的最新状态并返回
        User selected = userMapper.selectOne(queryWrapper);

//        设置session
        request.getSession().setAttribute(USER_LOGIN_STATE,user);

//        脱敏
        UserResult safetyUser = safetyUser(user);
        return safetyUser;
    }

    @Override
    public PageResult<UserResult> searchUsers(UserSearchParam userSearchParam) {
//        如果username为空，则返回所有用户
        String username = userSearchParam.getUsername();

//        分页处理
        Page<User> page = PageHelper.startPage(userSearchParam.getPageNum(), userSearchParam.getPageSize());
//        普通查询
        if (StringUtils.isBlank(username)){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            List<User> users = userMapper.selectList(queryWrapper);
            List<UserResult> list = users.stream().map(this::safetyUser).collect(Collectors.toList());
            PageResult<UserResult> pageResult = new PageResult<>(page.getTotal(),page.getPageNum(),list);
            return pageResult;
        }else{
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("user_name",username);
            List<User> users = userMapper.selectList(queryWrapper);
            List<UserResult> list = users.stream().map(this::safetyUser).collect(Collectors.toList());
            PageResult<UserResult> pageResult = new PageResult<>(page.getTotal(),page.getPageNum(),list);
            return pageResult;
        }



    }

    @Override
    public boolean deleteUser(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean selectPhone(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone",phone);
        User selected = userMapper.selectOne(queryWrapper);
        if(selected == null){
//            手机号不存在，可以注册
            return true;
        }
        return false;
    }

    //    脱敏
    public UserResult safetyUser(User user){
        UserResult safetyUser = new UserResult();
        safetyUser.setId(user.getId());
        safetyUser.setUserName(user.getUserName());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setProfile(user.getProfile());
        safetyUser.setTags(user.getTags());
        safetyUser.setPlanetCode(user.getPlanetCode());
        return safetyUser;
    }

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    public List<UserResult> searchUsersByTags(List<String> tagNameList) {
//        非空
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        SQL查询(select * .... like .. and like ... )

//        and拼接like查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        tagNameList.forEach(tagName -> {
            queryWrapper.like("tags",tagName);
        });
        List<User> users = userMapper.selectList(queryWrapper);
        List<UserResult> userResults = users.stream().map(this::safetyUser).collect(Collectors.toList());
        return userResults;
//        内存查询
//        查询所有用户，看用户的tags（json转对象）包不包含传过来的tagNameList
//        List<User> userList = userMapper.selectList(null);
//        Gson gson = new Gson();
////        for (User user : userList) {
////            if(StringUtils.isBlank(user.getTags())){
////                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户标签为空");
////            }
////        }
//        return userList.stream().filter(user -> {
//            List<String> TemptagNameList = gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType());
//            for (String s : tagNameList) {
//                if(TemptagNameList == null){
//                    return false;
//                }
//                if (!TemptagNameList.contains(s)) {
//                    return false;
//                }
//            }
//            return true;
//        }).map(this::safetyUser).collect(Collectors.toList());

//    }
    }

    @Override
    public Boolean updateUser(User user, User userLogin) {
        // 参数校验
        if (user == null || userLogin == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        Long userId = user.getId();
        Long loginUserId = userLogin.getId();

        // 查询被修改用户是否存在
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        // 权限校验：管理员或本人可以操作
        if (!isAdmin(userLogin) && !loginUserId.equals(oldUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改该用户");
        }


//        如果用户没有传入任何要更新的值，抛异常
        List<Object> fields = Arrays.asList(
                user.getUserName(),
                user.getUserAccount(),
                user.getAvatarUrl(),
                user.getGender(),
                user.getPhone(),
                user.getEmail(),
                user.getUserStatus(),
                user.getUserRole(),
                user.getProfile(),
                user.getTags(),
                user.getPlanetCode()
        );

        if (fields.stream().allMatch(Objects::isNull)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请传入要更新的值");
        }


        // 执行更新
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户更新失败");
        }
        return true;
    }

    @Override
    public User getUserLogin(HttpServletRequest request) {
//        非空
        if(request == null ){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
//        拿到登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return (User) userObj;
    }

    @Override
    public Boolean isAdmin(HttpServletRequest request) {
        User attribute = (User)(request.getSession().getAttribute(UserContact.USER_LOGIN_STATE));
        //        仅仅管理员可查看
        if(!Objects.equals(attribute.getUserRole(), UserContact.ADMIN_ROLE)){
            return false;
        }
        if(attribute == null){
            return false;
        }
        return true;
    }

    @Override
    public Boolean isAdmin(User userLogin) {
        if(userLogin == null ){
            return false;
        }
        return Objects.equals(userLogin.getUserRole(), UserContact.ADMIN_ROLE);
    }

    @Override
    public UserResult getCurrentUser(User user) {
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long id = user.getId();
        User userObj = userMapper.selectById(id);
        return safetyUser(userObj);
    }

    @Override
    public PageResult<UserResult> recommendUsers(Integer  pageNum, Integer pageSize, HttpServletRequest request) {
        // 获取登录用户
        User userLogin = this.getUserLogin(request);
        Long userId = userLogin.getId();

        // 构建 Redis 缓存键
        String redisKey = String.format("user:recommend:%s", userId);

        // 先查缓存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        PageResult<UserResult> cachedUserList = (PageResult<UserResult>) valueOperations.get(redisKey);

        if (cachedUserList != null) {
            // 缓存命中，直接返回
            return cachedUserList;

        }

        // 缓存未命中，查询数据库（带分页）
        PageHelper.startPage(pageNum,pageSize);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(userQueryWrapper);
        Page<User> page = (Page<User>) userList;



        // 返回分页结果
        List<UserResult> results = userList.stream().map(this::safetyUser).collect(Collectors.toList());
        // 将结果写入缓存（设置5分钟过期时间）
        PageResult<UserResult> result = new PageResult<>(page.getTotal(),page.getPageNum(), results);
        valueOperations.set(redisKey, result, 5, TimeUnit.MINUTES);
        return result;
    }



}







