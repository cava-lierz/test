package com.mentara.config;

import com.mentara.entity.Expert;
import com.mentara.entity.User;
import com.mentara.entity.UserAuth;
import com.mentara.repository.ExpertRepository;
import com.mentara.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.data.init.enabled:true}")
    private boolean dataInitEnabled;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (!dataInitEnabled) {
                System.out.println("数据初始化已禁用");
                return;
            }
            initExperts();
            initUserAuths();
        };
    }

    private void initExperts() {
        if (expertRepository.count() == 0) {
            createExpertData();
        }
    }

    private void createExpertData() {
        Expert[] experts = {
            createExpert("张医生", "抑郁症治疗", "zhang@psychology.com", "online"),
            createExpert("李咨询师", "焦虑症咨询", "li@psychology.com", "offline"),
            createExpert("王心理师", "青少年心理辅导", "wang@psychology.com", "online"),
            createExpert("陈咨询师", "婚姻家庭咨询", "chen@psychology.com", "online"),
            createExpert("刘专家", "创伤后应激障碍治疗", "liu@psychology.com", "offline")
        };

        for (Expert expert : experts) {
            expertRepository.save(expert);
        }
    }

    private Expert createExpert(String name, String specialty, String contact, String status) {
        Expert expert = new Expert();
        expert.setName(name);
        expert.setSpecialty(specialty);
        expert.setContact(contact);
        expert.setStatus(status);
        return expert;
    }

    private void initUserAuths() {
        long usersWithoutAuth = userRepository.findAll().stream()
            .filter(user -> user.getUserAuth() == null)
            .count();

        if (usersWithoutAuth > 0) {
            createSpecialUserAuths();
            createTestUserAuths();
        }
    }

    private void createSpecialUserAuths() {
        String[][] specialUsers = {
            {"7", "000000", "user7@mentara.com"},      // 智慧的彩虹97
            {"8", "000000", "admin@mentara.com"},      // 管理员
            {"9", "000000", "admin2@mentara.com"},     // 快乐的小鸟69
            {"10", "000000", "user10@mentara.com"},    // 温柔的海豚407
            {"11", "000000", "user11@mentara.com"},    // 友善的彩虹263
            {"12", "000000", "user12@mentara.com"}     // 细心的小狼139
        };

        for (String[] userInfo : specialUsers) {
            Long userId = Long.parseLong(userInfo[0]);
            String password = userInfo[1];
            String email = userInfo[2];

            userRepository.findById(userId).ifPresent(user -> {
                if (user.getUserAuth() == null) {
                    createUserAuth(user, email, password);
                }
            });
        }
    }

    private void createTestUserAuths() {
        for (int i = 1; i <= 100; i++) {
            final int testNum = i;
            userRepository.findAll().stream()
                .filter(user -> user.getNickname() != null && user.getNickname().equals("测试用户" + testNum))
                .findFirst()
                .ifPresent(user -> {
                    if (user.getUserAuth() == null) {
                        createUserAuth(user, "testuser@" + testNum + ".com", "000000");
                    }
                });
        }
    }

    private void createUserAuth(User user, String email, String password) {
        UserAuth userAuth = new UserAuth();
        userAuth.setUser(user);
        userAuth.setEmail(email);
        userAuth.setPassword(passwordEncoder.encode(password));
        user.setUserAuth(userAuth);
        userRepository.save(user);
    }
} 