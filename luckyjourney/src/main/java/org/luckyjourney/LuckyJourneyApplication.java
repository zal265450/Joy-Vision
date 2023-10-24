package org.luckyjourney;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "org.luckyjourney.mapper")
public class LuckyJourneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuckyJourneyApplication.class, args);

    }

}
