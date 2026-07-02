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

//Building Master-Replica Locally
//
//Create 3 Redis instances.
//
//Terminal 1:-
//    Master:
//        Port: 6379 [this is just to define or to know in which port we are running our master]
//        Start: redis-server --port 6379
//
//Terminal 2:-
//    Replica 1
//        Port: 6380 [this is just to define or to know in which port we are running our replica 1]
//        Start:
//            redis-server \
//                --port 6380 \
//                --replicaof 127.0.0.1 6379
//
//Terminal 3:-
//    Replica 2
//        Port:6381 [this is just to define or to know in which port we are running our replica 2]
//        Start:
//            redis-server \
//                    --port 6381 \
//                    --replicaof 127.0.0.1 6379
//
//Terminal 4:-
//    Verify:
//        Connect to replica: redis-cli -p 6380
//        Run: INFO replication
//
//    Output:
//        role:slave
//        master_host:127.0.0.1
//        master_port:6379
//
//            Terminal 1
//                ----------
//                redis-server --port 6379
//                        ↓
//                Master Redis Server
//
//
//            Terminal 2
//                ----------
//                redis-server --port 6380 --replicaof 127.0.0.1 6379
//                        ↓
//                Replica Redis Server
//
//
//            Terminal 3
//                ----------
//                redis-cli -p 6379
//                        ↓
//                Connected to Master
//
//
//            Terminal 4
//                ----------
//                redis-cli -p 6380
//                        ↓
//                Connected to Replica

//   ------------------NOTE----------------
//    Problem With Replication
//    What if Master dies?
//        Master ❌
//
//        Replica-1
//        Replica-2
//
//    No writes possible.
//    Need: Automatic Failover
//    To Fix this issue thats why we use Sentinel
//   ------------------NOTE----------------


//useSentinelServers(): If master dies, Replica becomes Master automatically
//  sentinel is used for high availability only

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

//    What Does Sentinel Do?
//        Sentinel constantly checks:
//            Is Master Alive?
//            If Master crashes:
//            Master ❌
//        Sentinel promotes:
//            Replica1 to: New Master
//            Automatically.

//    Building Sentinel Locally
//    Create:
//        sentinel.conf
//            port 26379
//
//            sentinel monitor mymaster 127.0.0.1 6379 2
//            sentinel down-after-milliseconds mymaster 5000
//            sentinel failover-timeout mymaster 10000
//
//    Start: redis-server sentinel.conf --sentinel

// Explanation of the above terminal code:

// port 26379 -> This is similar to what we have our Redis port number, for sentinel its 26379
// sentinel monitor mymaster 127.0.0.1 6379 2
//          Monitor sentinel
//          mymaster: This is just a name, [redis-master, primary, production-master, abc]
//          127.0.0.1: This is a local IP,for production this will be diff
//          6379: Redis master port number
//          2: This is called as quorum. At least 2 Sentinels must agree that the master is down before failover starts.
// sentinel down-after-milliseconds mymaster 5000
//          If I cannot reach the master for 5 seconds, I will consider it down.
// sentinel failover-timeout mymaster 10000
//          how long Sentinel waits for a failover process before considering it failed and retrying or allowing another Sentinel to take over.
//          It helps prevent multiple overlapping failovers and coordinates recovery.

//                        Master crashes
//                              │
//                              ▼
//                        Wait 5 sec
//                              │
//                              ▼
//                        Marked DOWN
//                              │
//                              ▼
//                        Start failover
//                              │
//                              ▼
//                Try to finish within ~10 sec

//   Now start it with:
//        Terminal 5: redis-server sentinel.conf --sentinel

//    And we follow the above mention steps for master and replicas

// How to create multiple sentinels:
//      Look how we have created sentinel-conf file locally above, it means we have created 1 sentinel file
//      Now to create multiple sentinel file locally
//          we will have [only change the port number, rest below 3 lines remain the same]
//                  1. sentinel1-conf: port 26380
//                  2. sentinel2-conf: port 26381
//                  3. sentinel3-conf: port 26382
//      Now to run all these sentinels, we are going to open 3 terminals
//              Terminal-6: redis-server sentinel-1.conf --sentinel
//              Terminal-7: redis-server sentinel-2.conf --sentinel
//              Terminal-8: redis-server sentinel-3.conf --sentinel

//                            Sentinel-1
//                                    26379
//                                    │
//                                    │
//                            Sentinel-2
//                                    26380
//                                    │
//                                    │
//                            Sentinel-3
//                                    26381
//                                    │
//                        ────────────┼────────────
//                                    │
//                            Master 6379
//                                    │
//                            Replication
//                                    │
//                            Replica 6380

// Issue with Sentinel: It helps with replicating the data, but what if we have 100mn records of 100gb data in our redis
//                      Then are we going to create 3 replicas, which is 100gb(master) + 3 * 100gb(replicas) = totally we are occupying 400gb

// to fix this we have clusters basically called as Sharding, which means to divide the 100gb of data in different different nodes say 33gb + 33gb + 33gb

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

// Redis cluster helps in high availability + horizontal scaling. Data split across multiple redis server, this is called as Sharding