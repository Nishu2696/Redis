package com.example.__spring_redis_redisson.config;

import org.apache.catalina.Cluster;
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
//                    |      |      |      |
//                    ▼      ▼      ▼      ▼
//                Master1 Master2 Master3 Master4
//                    |      |      |      |
//                    ▼      ▼      ▼      ▼
//              Replica1 Replica2 Replica3 Replica4

//            config.useClusterServers()
//                    .addNodeAddress(
//                            "redis://node1:6379",
//                                    "redis://node2:6379",
//                                    "redis://node3:6379"
//            );

// Redis cluster helps in high availability + horizontal scaling. Data split across multiple redis server, this is called as Sharding
// we create multiple master cluster(6379, 6380, 6381,...) and corresponding replicas(6385, 6386, 6387,...)
//
//        Suppose
//            Master2 crashes.
//            Master1
//            Master2 ❌
//            Master3
//
//        Replica2 becomes
//            Master2 (New)
//
//        Automatically: Unlike Sentinel,
//            Redis Cluster already knows all the nodes.
//            There is no separate Sentinel process.
//            The cluster handles failover internally.

//  We are taking an example, where we need Redis Server, and we are not going to define which one is master or replica
//        Terminal 1:
//        redis-server \
//                --port 6379 \
//                --cluster-enabled yes \
//                --cluster-config-file nodes-6379.conf \
//                --cluster-node-timeout 5000 \
//                --appendonly yes
//        Terminal 2:
//        redis-server \
//                --port 6380 \
//                --cluster-enabled yes \
//                --cluster-config-file nodes-6380.conf \
//                --cluster-node-timeout 5000 \
//                --appendonly yes
//        Terminal 3:
//        redis-server \
//                --port 6381 \
//                --cluster-enabled yes \
//                --cluster-config-file nodes-6381.conf \
//                --cluster-node-timeout 5000 \
//                --appendonly yes
//        Terminal 4:
//        redis-server \
//                --port 6382 \
//                --cluster-enabled yes \
//                --cluster-config-file nodes-6382.conf \
//                --cluster-node-timeout 5000 \
//                --appendonly yes
//        Terminal 5:
//        redis-server \
//                --port 6383 \
//                --cluster-enabled yes \
//                --cluster-config-file nodes-6383.conf \
//                --cluster-node-timeout 5000 \
//                --appendonly yes
//        Terminal 6:
//        redis-server \
//                --port 6384 \
//                --cluster-enabled yes \
//                --cluster-config-file nodes-6384.conf \
//                --cluster-node-timeout 5000 \
//                --appendonly yes

// Terminal 7:
//        redis-cli --cluster create \
//                127.0.0.1:6379 \
//                127.0.0.1:6380 \
//                127.0.0.1:6381 \
//                127.0.0.1:6382 \
//                127.0.0.1:6383 \
//                127.0.0.1:6384 \
//                --cluster-replicas 1

//    What does "--cluster-replicas 1" mean?
//    It means
//                                    Each Master
//                                        ↓
//                                    Gets one Replica

//    so, Redis automatically creates
//
//            Master1
//                ↓
//            Replica1
//
//            Master2
//                ↓
//            Replica2
//
//            Master3
//                ↓
//            Replica3
//
//    You don't manually say: 6382 is replica of 6379
//
//    Redis decides the mapping automatically.

//Now lets understand how Redis cluster is powerful and its internal working
// Lets understand this with an example
// we have 3 masters configured for now lets assume

//    Slot Range        Owner
//    ------------------------
//    0-5460            Master1
//    5461-10922        Master2
//    10923-16383       Master3
//
//    what is this slot, and what is this number 16384?
//        Consider slot has a broker which decides a particular data, needs to be stored in which master
//        Now lets see whats this number 16384,
//            Before this first understand what stores inside slots -> it stores nothing, it just generates a hash code region
//            Assume i have to store "user:101" data inside our master cluster
//                Once this request comes (CRC 16) user:101 -> and we would get 16 digiti hashcode
//                What is this CRC - 16, it is a string -> integer based generator, its different from MD5 hash or UUID, because this contains alphaNumeric value
//                But CRC-16 only generates 16 digit by converting string to Integer so this will be always unique for a particular stringInteger value
//
//            (CRC 16) user:101 -> Generates a hash code as 43890 as the hashcode
//                43890%16384 = 13867
//                Now we have 3 Master and 13867 lies inside Master 3, and the data goes and stores inside Master 3
//
//            Now when the next time user wants to fetch the details of user:101
//                slot comes into play, CRC 16 will generate a similar hashcode how we generated initially
//                We would get the value as 13867, and this is in Master 3, now the Redis goes and search user:101 in Master 3
//
//        Now lets understand this number 16384,
//            this is just a max number which have been figured out till now, because each slot can stores billions of records
//            now lets see 1,000,000,000 * 16384 -> this is more than enough, thats why this is just a number


//    Now lets understand one more concept of Redis Cluster
//    As we discussed above, initially we created 3 cluster, and lets assume there are few data stored inside our masters, like user:101
//
//    Now due to some reasons, like Memory full or CPU overloaded, we add one more Master, so now we have Master 4
//    Now the slots split will get modified
//
//    //    Slot Range        Owner
//    //    ------------------------
//    //    0-4095           Master1
//    //    4096-8191        Master2
//    //    8192-12287       Master3
//    //    12288-16383      Master4
//
//    as soon as we add another Master, now the data will also get transfered automatically to there respective Masters based on the slot
//            previously we discussed user:101 -> had a hashcode of 13867 and its data was stored in Master 3
//            Now as we have added another Master cluster, and based on this this user data "user:101" will get automatically transfered to Master 4 and this is called as SLOT MIGRATION
//
//            So Redis copies every key whose slot lies in that range.
//
//            Now if the client tries to fetch the data for user:101, it will return the data from Master 4

//            STEP 1
//
//                user:104
//                    │
//                    ▼
//                CRC16
//                    │
//                    ▼
//                Slot = 9000
//                    │
//                    ▼
//                Routing Table
//                9000 → Master2
//                    │
//                    ▼
//                Master2 stores:
//                user:104 = David
//
//                =========================
//                Add Master4
//                =========================
//
//                Routing changes
//                9000 → Master3
//
//            Redis immediately migrates all keys
//            belonging to Slot 9000
//
//                Master2 --------------------> Master3
//                user:104                     user:104
//                order:10                     order:10
//                cart:50                      cart:50
//
//
//                =========================
//                Future Request
//                =========================
//
//                user:104
//                    │
//                    ▼
//                CRC16
//                    │
//                    ▼
//                Slot = 9000
//                    │
//                    ▼
//                Routing Table
//                9000 → Master3
//                    │
//                    ▼
//                Master3 returns David