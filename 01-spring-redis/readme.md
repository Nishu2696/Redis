In RedisConfig File, we have this code

@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
redisTemplate.setConnectionFactory(redisConnectionFactory);
return redisTemplate;
}

At line 5 we are initializing a redis object
At line 6, we are connecting our redis cli with our spring application so that there is a sync in data between spring application and redis cli or redis GUI
At line 7 we are returning the object to be used in over all of the application

In Service file

redisTemplate.opsForValue().set("User" + user.getId(), user); ---> Used for saving or setting the value in redis
return (User) redisTemplate.opsForValue().get("User" + id);   ---> Used for getting the value from the Redis for that particular id

Note: we have used 
1. opsForValue(): this is used for storing or retriving a string
2. opsForHash(): this is used for storing or retriving a Hash / Object
3. opsForList(): this is used for storing or retriving an array
4. opsForSet(): this is used for storing or retriving an array with no duplicates
5. opsForZSet(): this is used for storing or retriving an array with no duplicates and in sorted manner

Eg:
1. opsForValue()
            user = {
                id: 1,
                name: "Nishaanth"
            }
    Note in the code we have written redisTemplate.opsForValue().set()/get().....
    Other Way we could have written is:
                     ValueOperations<String, String> value= redisTemplate.opsForValue();
                     value.set()/.get()
    To Set the data, we will be using: opsForValue().set(user.getId(), user.getName());
    To Get the data, we will be using: opsForValue().get(user.getId());
2. opsForHash()
           user = {
                id: 1,
                name: "Nishaanth",
                city: "bangalore"
           }
   To Set the data, we will be using: opsForHash().put(user.getId(), name, user.getName(), email, user.getEmail());
   public void saveUser() {

          HashOperations<String, Object, Object> hash =
                        redisTemplate.opsForHash();

          hash.put("user:1", "name", "Nishaanth");
          hash.put("user:1", "city", "Bangalore");
   }   
   To get one Field only
       String cityName = hash.get("user:1", "city")
   To get entire hashed obj: we are using the method as entries() instead of get()
       Map<Object, Object> userDetails = hash.entries("user:1)
3. opsForList()
   Recent Searches:   iphone
                      macbook
                      ipad
   TO set the data we have many options such as 
      rightPush          : Adds to the beginning of the list.
      leftPush           : Adds to the end.
      leftPushAll        : Push multiple values from the left. [listOps.leftPushAll("numbers", 3, 2, 1)] // 1 2 3
      rightPushAll       : Push multiple values from the right. [listOps.rightPushAll("numbers", 4, 5, 6);] // 4, 5, 6
      leftPushIfPresent  ; Push only if the list already exists. [listOps.leftPushIfPresent("numbers", 100);] // Empty array
      rightPushIfPresent : Same as above

              public void saveSearches() {

                        ListOperations<String, Object> list =
                                  redisTemplate.opsForList();

                        list.rightPush("search", "iphone");
                        list.rightPush("search", "macbook");
                        list.rightPush("search", "ipad");
              }
 To fetch the elements similarly we have many options
      leftpop                          : Remove first element. [Object value = listOps.leftPop("numbers");] // Already available 1, 2, 3 // after this line the list will contain only 2, 3 and the output of this line will be 1.
      rightPop                         : Remove last element. [Object value = listOps.rightPop("numbers");] // Already available 1, 2, 3 // after this line the list will contain only 1, 2 and the output of this line will be 3.
      range                            : Read a range of elements. [listOps.range("numbers", 0, -1);] // o/p: 1,2,3,4
      index                            : Read by index. [Object value = listOps.index("numbers", 2);] list has [12. 20, 30, 40] o/p is 30
      update                           : Replace an element by index. [listOps.set("numbers", 1, 100);] previously it contained 10,20,30, and after this line the list will have 10,100,30
      size                             : Get list length. [Long size = listOps.size("numbers");] // list contains 10, 20, 30 the o/p will be 3
      trim                             : Keep only a specified range. [listOps.trim("numbers", 0, 2);] // lis contains 1,2,3,4,5,6, the o/p will be 1,2,3 Note: the end index is also considered
      remove by value                  : Remove matching values. [listOps.remove("numbers", 1, 20);] // syntax: listOps.remove(key, count, value), list had 10,20,30,40,50, after this line the o/p will be 10,30,40,50
                                         key, count, value ---> if count is 1, only the first occurence is removed
                                                           ---> if count is 0, all the occurence is removed.
      Insert relative to another value : Insert before a pivot value.
                                               leftPush(key, pivot, value) ----> . [listOps.leftPush("numbers", 20, 15);] list already contains 10,20,30 and after this line it would be 10,15,20,30
                                               rightPush(key, pivot, value) ----> . [listOps.rightPush("numbers", 20, 25);] list already contains 10,20,30 and after this line it would be 10,20,25,30
      Move btwn lists                  : Atomically move an element between lists
                                                public void processNextJob() {

                                                     ListOperations<String, Object> listOps = redisTemplate.opsForList();

                                                     Object job = listOps.move(
                                                                          "pendingJobs",
                                                                            RedisListCommands.Direction.LEFT,   // Remove from left
                                                                            "processingJobs",
                                                                            RedisListCommands.Direction.RIGHT   // Add to right
                                                    );

                                                    System.out.println("Processing: " + job);
                                                }
                                                // PendingJobs: 1,2,3
                                                // ProcessingJob: []
                                                // After this line.....
                                                // PendingJobs: 2,3 . [Removed from left]
                                                // ProcessingJob: [1]. [Add to the right]

4. opsForSet(): A Redis Set stores unique values (no duplicates).
        // list: [1,2,3]
           public void saveSkills() {
        
                SetOperations<String, Object> set =
                        redisTemplate.opsForSet();
        
                set.add("skills", "1");
                set.add("skills", "2");
                set.add("skills", "3");
                set.add("skills", "1"); // Duplicate
           }

        To get the set back we use the below line
            redisTemplate
            .opsForSet()
            .members("skills");
        Check if an element exists
            Boolean.TRUE.equals(
            redisTemplate.opsForSet()
            .isMember("skills", "1")
        Remove a value
            redisTemplate.opsForSet()
            .remove("skills", "Redis");

5. opsForZSet() (Sorted Set), stores Value + Score
        The list already contains:
                   Nishaanth 95
                   Rahul 80
                   Ankit 100

                           public void saveLeaderboard() {
                        
                                ZSetOperations<String, Object> zset =
                                        redisTemplate.opsForZSet();
                        
                                zset.add("leaderboard", "Nishaanth", 95);
                                zset.add("leaderboard", "Rahul", 80);
                                zset.add("leaderboard", "Ankit", 100);
                           }
        Get leaderboard: range is the keyword
        
                            redisTemplate
                            .opsForZSet()
                            .range("leaderboard", 0, -1); // o/p: [Rahul, Nishaanth, ankit]
        Get highest scorer: reverseRange is the keyword
        
                            redisTemplate
                            .opsForZSet()
                            .reverseRange("leaderboard", 0, 0); // o/p: Ankit
        
        Get score of a player: score is the keyword
        
                            redisTemplate
                            .opsForZSet()
                            .score("leaderboard", "Nishaanth"); // 95.0