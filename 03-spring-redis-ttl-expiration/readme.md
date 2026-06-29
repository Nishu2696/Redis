**TTL:** Time to live [Reads remaining time]
     How long a key should Survive
     So the question arises, does redis reads the remaining time of all its key every second?
        Ans: No, consider we have 100 million keys in our redis, then reading this 100 million keys every second is a very memory consuming operation
        Solution: Redis uses 2 strategies
            1. Passive Expiration:
                    When a specific key is fetched, it first checks the remaining time left, if its expired then redis automatically deletes this key immediately at the time when it tried to fetch
            2. Active Expiration : 
                    Suppose a key is never accessed, then in this case Redis periodically samples the list of keys available in redis
                    so in this period check, it checks for Expired key and removes them, in this way its memory is maintained and no memory leak happens

**Expiration:** Exact expiry timestamp

Example:
    SET user "John"
    EXPIRE user 60
    Lets say current time is: 10:00:00 and we have set the expiration as 1 minute
    So expiration will always give exact timestamp which 10:01:00 irrespective of when we call either at 10:00:00 or at 10:00:30

    Now TTL, this gives us the remaining time
    So if we access at "TTL user"
        10:00:00 -> 60 seconds
        10:00:20 -> 40 seconds

**Cache Aside Pattern:** 
    The application is responsible for interacting with both the cache and the database.

                      Request
                         |
                         ▼
                 Check Redis
                  /       \
                 /         \
              Hit          Miss
              |             |
              |             ▼
              |      Query Database
              |             |
              |             ▼
              |      Store in Redis
              |             |
               \           /
                \         /
                 Return Response

    Now What happens when we update any particular key:
    Eg: 
        Price of a phone is 10K, and same is stored in Redis as well
        Now someone updates the price of that aprticular phone from 10k to 15k, now our db is updated but our redis uses stale data which still has 10k
        so when re-accessed we will still get 10k alone which is wrong

    Solution:
        When ever we are updating our any specific product, at that time we need to delete the key present in our redis against that product.
        So what happens in our next hit against that product, Redis miss, it goes to fetch the data from DB and then store in Redis again and return the updated price
        The above solution is called as CACHE-INVALIDATION

Issue 1:
    Assume we are building a Rate Limiter, and for a get API, we are setting a following condition that
            /api/users, has a limit of 
                TTL: 1 minutes
                No_of_hit: 100
            User hits at 10:00:00, recorded as hit = 1, ttl = 60s
            Second hits comes at 10:00:30, recorded as hit = 2, ttl = 30s
        
            The above implementation is called as Fixed TTL, the issue happens here is what if within first 5 second we exhaust our 100 request, 
            then are we going to hit our DB every 5 seconds?

            Solution: Instead of Fixed TTL, we implement Sliding TTL, we take our above example itself:

                User hits at 10:00:00, recorded as hit = 1, ttl = 60s, Expiration is 10:01:00
                Second hits comes at 10:00:30, instead of using fixed TTL, we can update our TTL, hit = 1, TTL = 60s, New Expiration is 10:01:30

                    Drawback of this Solution: Now every request performs 2 redis operation, 
                        1. redis Get
                        2. redis update new TTL
                    If we have 10 million users trying to access a Get API then this will be a time taking process. To fix this we will be using LUA Script [Discussed Next....]

