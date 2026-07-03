At first glance, SETNX, Redisson Lock, and Redis Transactions (MULTI/EXEC) all seem to solve the same problem: preventing race conditions.
But they actually solve different kinds of concurrency problems.

Lets understand this concept with an example:

Imagine an E-commerce Website
    Suppose there is only 1 iPhone left.
    
    Two users:
        User A
        User B
    
    Both click Buy Now at exactly the same time.
    But we want only one order.

**Scenario 1 — No Protection**

**User A**                    **User B**

Read Stock = 1
                            Read Stock = 1
Stock > 0 ✔
                            Stock > 0 ✔
Decrease Stock
                            Decrease Stock
Stock = -1 ❌

Race condition.
Now let's see how each Redis feature handles this.

**1. Redis Transaction (MULTI / EXEC)**
    People often think Redis transactions are like SQL transactions. They are not.
    A Redis transaction only guarantees **"Execute these commands sequentially without another client's commands being interleaved."**

                Example:
                    MULTI
                    GET stock
                    DECR stock
                    EXEC
                Redis executes
                    GET
                    DECR
                without interruption.

The problem is the value was already read before EXEC.
    
                    User A
                        GET stock
                        returns 1
                ---------------------
                    User B
                        GET stock
                        returns 1
                ---------------------
                    User A
                        MULTI
                        DECR stock
                        EXEC
                    Stock = 0
                ---------------------
                    User B
                        MULTI
                        DECR stock
                        EXEC
                    Stock = -1

Redis Stock = 1

          User A                  User B
T1      GET -> 1
T2                              GET -> 1
        local stock = 1         local stock = 1

T3      if(1 > 0)
        DECR
        Redis = 0
T4                              if(1 > 0)
                                DECR
                                Redis = -1

As we can see from above, both have already stored the value, hence redis transaction didn't stop them here
Because Redis transaction is not isolation.

**Important**
    Redis Transaction does NOT lock data.
    It only batches commands.

Think of it like
    "Run these commands together."
    and not as "Nobody else can touch this data."

Imagine three clients.

    Client A
        MULTI
        INCR balance
        LPUSH transaction
        EXEC
    -------------------
    Client B
        GET balance
    -------------------
    Client C
        SET name John

Redis executes them like this:

    INCR balance
    LPUSH transaction
    GET balance
    SET name John

**Notice that GET balance cannot appear between INCR and LPUSH.
It waits until the transaction finishes.
This is what "Run these commands together" means.**

Now how do we fix the above issue:
    There are 2 ways possible
        1. Use **OPTIMISTIC LOCKING**
        2. Use **SETNX / Redisson** - Pessimistic Locking: "Someone else might modify this data, so I'll lock it before doing anything."

**OPTIMISTIC LOCKING**:

    WATCH stock
    GET stock
    if(stock > 0){
        MULTI
        DECR stock
        EXEC
    }

If another client changes stock after your GET, then EXEC fails instead of decrementing. Your application can retry with the latest value.
"I believe nobody will modify this data. I'll proceed without a lock. If someone does modify it, I'll detect it and retry."

Pessimistic says:
    Lock first.
Optimistic says:
    Don't lock. Just verify before committing.

Many people thinks Redis Transaction is similar to DB Transaction, but this wrong
Lets understand this with an example why both are different

    - In DB Transaction, only when overall logic written inside is executed then only it will modify the data
      if it breaks in between, then all the modified data for that particular action will be reverted

    - But with Redis transaction thats not the case, everything will be appended step by step inside our redis,
      and if some piece of code fails or crashes it stops there itself and exits, previously stored data in redis remains as it is and it will not be deleted

Example:

    MULTI
    SET age 25
    INCRBY city 10
    EXEC

Execution:

    SET age 25: Success
    INCRBY city 10: Fails

Result:

    SET age remains committed
    Redis does NOT rollback.
    Database behavior: Rollback Entire Transaction
    Redis behavior: Already executed commands stay

This is the visual flow

            WATCH stock
               │
            Read Stock
               │
        Someone Changed Stock?
               │
              YES
               ▼
        Abort Transaction

Problem With WATCH
    Suppose: 10,000 requests/sec
    all watching: 
        stock:iphone
            Many transactions fail.
            Clients keep retrying.
            Performance drops.

Redis Engineers Solution
Instead of:
    WATCH
    MULTI
    EXEC

they often use: Lua Scripts
because Lua Scripts is:
    Atomic
    Faster
    No Retry Logic

We will discuss about this in our next chapter