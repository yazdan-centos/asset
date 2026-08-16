package org.mapnaom.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetApplication.class, args);
    }

}
