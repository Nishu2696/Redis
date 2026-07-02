Previously we discussed about SETNX, A quick overview of what we learnt at SETNX

SSETNX was brought in to resolve the issue of distributed locks, and did it resolve the deadlock
Ideally, based on our understanding it did solve the issue, but is it really resolving deadlock?
No, what SETNX did was **MUTUAL EXCLUSION** which means **Only one app can enter**

ISSUE 1:

What if we are using SETNX with a TTL of 30 seconds, but our DB operation took 45 seconds, 
then after 30 seconds my lock gets freed and and another user can use the same lock and 
this causes concurrent user performing the same write operation on a particular field
we are eventually not solving the deadlock issue that we had earlier.

                        App-1
                            │
                            ├────── Long Task ──────┐
                            │                       │
                            ▼                       │
                                                    │
                        TTL Expires                 │
                            │                       │
                            ▼                       │
                                                    │
                        App-2 Acquires Lock    ◄────┘
                            │
                            ▼
                            
                            Both Running

Solution: 
Someone might say we should increase the TTL, every time on a static basis
What if tomorrow a db operation might increase the calculation time from 45ms to 5000ms, are we going to increase our TTL everytime we see a lag in our db operation

Answer: Absolutely no, if we do this we wont be able to make a production grade application
We need something dynamic, such as once the very time take operation completes only then we release our locks

                        Acquire Lock
                            ↓
                        Keep Extending TTL
                        While Work Is Running
                            ↓
                        Release When Done

Solution for this Use **Redisson**

Redisson is a Java Redis client that provides:

*) Distributed Locks
*) Distributed Maps
*) Distributed Queues
*) Distributed Semaphores

A sample code snippet how Redisson works

Instead of: SET NX EX

you write:
        RLock lock =
            redissonClient.getLock(
            "product:1"
        );

Acquire: lock.lock();
           |-> Acquires lock
           |-> TTL is set to say 30 sec
                    |-> Now starts Redissons internally working which is Watchdog thread
                            |-> What is this watchdog thread: Its kind of socket connection, 
                                which checks if the Application is still live then extend the TTL automatically

                    Acquire Lock
                    TTL=30
                    
                    10 sec
                    Watchdog Refresh
                    
                    TTL=30
                    10 sec
                    Watchdog Refresh
                    
                    TTL=30

As long as: Application Alive the lock never expires.

Release: lock.unlock();

Issue with the above behaviour:
    What if our app crashes, then our lock goes to indefinite open state
    if this happens, then no new lock will be created for this product

                    App Alive
                        ↓
                    Watchdog Refreshing
                        ↓
                    Lock Alive
                    
                    -----------------
                    
                    App Crashes
                        ↓
                    Watchdog Stops
                        ↓
                    TTL Expires
                        ↓
                    Lock Removed

NOTE: Redisson is only used to solve our issue of distributed locks, but if we need Locking + Caching
      We will be needing both redissonConfig.java file as well as redisConfig.java file