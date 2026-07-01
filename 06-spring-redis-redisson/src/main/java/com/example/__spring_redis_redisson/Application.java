package com.example.__spring_redis_redisson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

//			POST /products/1/purchase
//            			│
//						▼
//				ProductController
//            			│
//						▼
//				PurchaseService
//            			│
//						▼
//				redissonClient.getLock()
//            			│
//						▼
//				Redis Lock Created
//            			│
//						▼
//				Watchdog Started
//            			│
//						▼
//					Read Product
//            			│
//						▼
//					Update Stock
//            			│
//						▼
//					Save Product
//            			│
//						▼
//					unlock()
//            			│
//						▼
//				Watchdog Stopped
//            			│
//						▼
//				Redis Lock Deleted
