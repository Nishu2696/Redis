The reason why we are discussing about distributed locks is because what if there is a mismatch of data while performing write operation to the db 
Lets take an example to understand this issue:

Suppose we have
    Product A
    Stock = 1
2 Users try to buy them simultaneously, but we have only 1 stock left [how can 2 user try to buy the same product when only 1 stock is left]
Traditional Java way is to lock as soon as 1 user tries to purchase the item, so that the other user cannot buy unless the lock is released
This is achieved through, synchronized or re-entrant lock

But the issue with the above method runs on a single instance or single JVM, but in production we dont work with single JVM
Instead we will be having multiple load balancer, through which we will be running multiple JVM instances
Now the issue is if we have multiple JVM instances, then how will the lock be shared btwn multiple JVM instance,
because synchronized or re-entrant lock is valid to that particular server or JVM only

                       Load Balancer
                             │
                  ┌──────────┼──────────┐
                  │          │          │
                  ▼          ▼          ▼
                Server-1   Server-2   Server-3
                (or) App-1   App-2      App-3
                (or) JVM-1   JVM-2      JVM-3

Now:
User 1, tries to buy a product
we used synchronized
inside App-1

User 2, tries to buy a product
we used synchronized
inside App-2.

This has no effect: because Each JVM has its own memory.
                        App-1 Lock
                            │
                            │   ❌ Cannot See
                            ▼
                        App-2 Lock

So to fix the above we issue, Redis introduced Distributed Locks:

                          Redis
                             │
                     Lock: Product-1 (Distributed Lock)
                             │
                 ┌───────────┼───────────┐
                 │           │           │
                
                App-1     App-2      App-3

How to achieve Distributed Locks?

1. SETNX   : SET key value NX - NX Means: Set only if Not Exists. 
2. Redisson: 
3. RedLock :

**NOTE: In this folder we will discuss about SETNX alone.**

                        **SETNX**

User 1, tries to buy a product 1, if the key doesnt exist, then SET product:1 locked NX
Here, 
key is product: 1
value is locked(static string)

Now suppose User 2, tries to buy the same product, now the key exists, so he waits till the lock is released

                            App-1
                                │
                                ├── Acquire Lock ✅
                                │
                                ▼
                            Critical Section
                            
                            
                            App-2
                                │
                                ├── Acquire Lock ❌
                                │
                                ▼
                            Wait / Retry

