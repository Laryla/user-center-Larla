package com.larly.usercenter;

import com.larly.usercenter.model.domain.Team;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@SpringBootTest
public class MapTest {
    @Test
    public void testMap(){
        try {
            Team team = new Team();
            team.setId(1L);
            team.setName("测试队伍");
            Map<String, String> map = BeanUtils.describe(team);
            System.out.println(team);
            System.out.println(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 结果: {"id": 1, "name": "测试队伍", ...}

    }

}
