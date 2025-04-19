package csu.limon.chat;

import csu.limon.chat.core.server.ChatServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@MapperScan("csu.limon.chat.mapper")
@SpringBootApplication
public class SpringBootNettyProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootNettyProjectApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ChatServer chatServer) {
        return args -> {
            try {
                chatServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}