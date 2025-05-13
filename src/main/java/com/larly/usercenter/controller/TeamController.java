package com.larly.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.larly.usercenter.common.BaseResponse;
import com.larly.usercenter.common.ErrorCode;
import com.larly.usercenter.common.ResultUtils;
import com.larly.usercenter.exception.BusinessException;
import com.larly.usercenter.model.domain.Team;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.dto.TeamQuery;
import com.larly.usercenter.model.request.*;

import com.larly.usercenter.service.TeamService;
import com.larly.usercenter.service.impl.UserServiceImpl;
import org.apache.commons.beanutils.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;



@RestController
@RequestMapping("/api/team")
//跨域问题
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials = "true")
public class TeamController {


    @Autowired
    private TeamService teamService;

    @Autowired
    private UserServiceImpl userService;

    /**
     * 创建队伍
     *
     * @param teamAddRequest 队伍对象
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getUserLogin(request);
//        映射
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamAddRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long l = teamService.addTeam(team, userLogin);
        return ResultUtils.success(l);
    }

    /**
     * 删除队伍
     *
     * @param id 队伍ID
     * @return
     */
    @PostMapping("/delect")
    public BaseResponse<Boolean> delectTeam(@RequestBody long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if(!result){
            throw new BusinessException(ErrorCode.SERVER_ERROR,"删除队伍失败");
        }
        return ResultUtils.success(result);
    }

    /**
     * 更新队伍
     *
     * @param team 队伍对象
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateById(team);
        if(!result){
            throw new BusinessException(ErrorCode.SERVER_ERROR,"更新队伍失败");
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取队伍
     *
     * @param id 队伍id
     * @return
     */
//    @GetMapping("/list")
//    public BaseResponse<Team> getTeamById(long id){
//        if(id<=0){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        Team team = teamService.getById(id);
//        return ResultUtils.success(team);
//    }
    @GetMapping("/list")
    public BaseResponse<Team> getTeamById(long id){
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        return ResultUtils.success(team);
    }

    /**
     * 获取队伍列表
     *
     * @param teamQuery 队伍查询对象
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeams(TeamQuery teamQuery){
//        这是封装一个DTO ：作用 就是把前端传过来的参数封装成一个对象，方便后续使用. 增强安全性
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
//            建议使用TDO配合BeanUtils.copyProperties
//            这个是用来把teamQuery中的属性值赋给team（映射）
            BeanUtils.copyProperties(team,teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        List<Team> list = teamService.list(queryWrapper);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Page<Team> teamPage = teamService.page(page, queryWrapper);
//        PageResult<Team> result = new PageResult<>(teamPage.getTotal(), teamPage.getRecords());
        return ResultUtils.success(teamPage);
    }

}














