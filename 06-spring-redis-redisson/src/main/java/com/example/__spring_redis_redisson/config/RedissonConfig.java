package com.example.__spring_redis_redisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }
}

//Note how here we have used .useSingleServer()
//the other variants that we have for this are

//useSingleServer(): I have ONE Redis instance.
//        Spring Boot
//              │
//              ▼
//        Redis Server
//        localhost:6379
//Use cases: Learning, local development

//useMasterSlaveServers()
//
//                          Master
//                             │
//                     ┌───────┴───────┐
//                     ▼               ▼
//                  Replica-1      Replica-2
//
//        config.useMasterSlaveServers()
//            .setMasterAddress(
//                    "redis://master:6379"
//            )
//            .addSlaveAddress(
//                    "redis://slave1:6379",
//                    "redis://slave2:6379"
//            );
//
//              Write: Master
//              Reads: Replicas


//useSentinelServers(): If master dies, Replica becomes Master automatically

//                   Sentinel
//                       │
//                       ▼
//                   Master
//                       │
//                   Replica

//            config.useSentinelServers()
//                    .setMasterName("mymaster")
//                    .addSentinelAddress(
//                            "redis://host1:26379",
//                                    "redis://host2:26379"
//            );

//useClusterServers()

//                      Redis Cluster
//                            |
//                    ┌──────┬──────┬──────┐
//                    ▼      ▼      ▼      ▼
//                  Node1  Node2   Node3  Node 4

//            config.useClusterServers()
//                    .addNodeAddress(
//                            "redis://node1:6379",
//                                    "redis://node2:6379",
//                                    "redis://node3:6379"
//            );
