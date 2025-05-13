package com.larly.usercenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TestInsertBatch {

    @Resource
    private UserService userService;

    @Test
     void doInsert() {
        System.out.println("开始插入数据");
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            User user = new User();
            user.setUserName("xhxhxhx");
            user.setUserAccount("xhxhxhx");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("123433141");
            user.setPhone("123141412");
            user.setEmail("321313@qq.com");
            user.setUserStatus(0);
            user.setCreateTime(new Date());
            user.setProfile("开导开导开导开导开导的，世界第哦啊who的雾霭的hi我丢件奥i我哦叽歪");
            user.setTags("");
            userList.add(user);
        }

        boolean result = userService.saveBatch(userList, 10000);
        assertTrue(result, "批量插入应该成功");
    }

//    学习多线程和线程池
@Test
void doInsert2() throws InterruptedException {
    // 线程1：执行任务
    Thread thread1 = new Thread(() -> {
        for (int i = 0; i < 5; i++) {
            System.out.println("线程1正在运行 - " + i);
            try {
                Thread.sleep(500); // 模拟耗时操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }, "Thread-1");

    // 线程2：执行任务
    Thread thread2 = new Thread(() -> {
        for (int i = 0; i < 5; i++) {
            System.out.println("线程2正在运行 - " + i);
            try {
                Thread.sleep(500); // 模拟耗时操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }, "Thread-2");

    // 启动线程
    thread1.start();
    thread2.start();

    // 等待两个线程执行完毕
    thread1.join();
    thread2.join();
}

//线程池
@Test
void doInsert3() throws InterruptedException {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            2,
            10,
            1000,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100)
    );

    for (int i = 0; i < 100; i++) {
        threadPoolExecutor.execute(() -> {
            Thread currentThread = Thread.currentThread();
            System.out.println("线程池正在运行 - " + currentThread.getName());

            try {
                Thread.sleep(500); // 模拟耗时操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

//    // 关闭线程池，不再接受新任务
//    threadPoolExecutor.shutdown();
//
    // 等待所有任务执行完毕，最多等待 60 秒
    if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
        // 如果超时，则强制终止
        threadPoolExecutor.shutdownNow();
    }
}

}
