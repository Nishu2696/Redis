This is the most important interview question being asked.

Redis usually stores data in-memory which means "data lives in memory" 

we have data stored in our Redis -> power goes off -> and once the power is back redis restart again -> but all the data gets cleared off
Resultant is data is lost

Solution for this: **REDIS PERSISTANCE**
    What does this do, save all the data from RAM to Disk
    So once power goes off, and comes back redis restarts and fetches the data from disk

2 Types of **REDIS PERSISTANCE**:
    *) RDB: Redis Database Snapshot
    *) AOF: Append Only File

RDB: 
    Take a photo of Redis Memory, redis creates dump.rdb and inside everything stored in redis will be stored as a snapshot

            Memory
                │
                ▼
            Snapshot
                │
                ▼
            dump.rdb

12:00 PM
Snapshot Taken

12:05 PM
Snapshot Taken

12:10 PM
Snapshot Taken

inside our redis.conf file we will store something like this
    a) save 900 1
    b) save 300 10
    c) save 60 10000

a) save 900 1
    After 900 sec
    if at least 1 key changed
    Take Snapshot

b) save 300 10
    After 300 sec
    if 10 keys changed
    Take Snapshot

c) save 60 10000
    After 60 sec
    if 10000 keys changed
    Take Snapshot

**RDB: Dis-Advantage**

So in the above as we have mentioned every 5 minutes we will be taking a screenshot, Now assume
10:01 -> user A data added
10:05 -> snapshot taken and stored in dump.rdf
10:06 -> User B data added
10:07 -> Redis crashes

Now what happens is only user A data added is stored, but we have lost User B data is lost

**RDB Advantages:**

    *) Read one file, Load Everything
    *) Smaller disk usage, Compressed binary format



---------------------------------**************************************-------------------------------------


**AOF: Append Only File**
Why do we need this, because to resolve the issue, what if in the 5 minutes we get almost 1L data, and before we take snapshot, redis crashes

Instead of: Take Photo
they chose: Write Every Command

In our local if redis already installed then we would have redis.conf file which already has basic information like port no, username, password, RDB data, AOF data

So currently we are discussing about RDB and AOF, will discuss about this alone for now

#############################################
# AOF
#############################################

appendonly yes 

appendfilename "appendonly.aof"

appenddirname "appendonlydir"

appendfsync everysec

no-appendfsync-on-rewrite no

auto-aof-rewrite-percentage 100

auto-aof-rewrite-min-size 64mb

aof-load-truncated yes

#############################################
# SNAPSHOTS (RDB)
#############################################

save 900 1
save 300 10
save 60 10000

#############################################

appendonly yes / no: AOF should be enabled / disabled
appendfsync always / everysec / no / 
always: sync every command [write/ always/ confirm]
everysec: sync to db every second -> most common in prod, because the loss will be ~1sec
no: OS decides when to flush.

Working Example for AOF:

    You execute: SET user:101 Nishaanth
    Redis appends:
        SET user:101 Nishaanth
        to: appendonly.aof

    Then: SET user:102 John
    AOF:
        SET user:101 Nishaanth
        SET user:102 John
    
    Then: DEL user:101
    AOF:
        SET user:101 Nishaanth
        SET user:102 John
        DEL user:101

Restart Recovery:
    Redis reads: appendonly.aof from top to bottom.

    Replays:
        SET user:101 Nishaanth
        SET user:102 John
        DEL user:101

    Final state:
        user:102 exists
        Exactly as before crash.

Data Loss Window

    RDB: Minutes possible.
    AOF: Maximum 1 second usually.

Why does Redis use fork() for snapshots?
    Redis uses fork() so the child process can write the snapshot while the parent process continues serving requests.
    Copy-On-Write ensures memory pages are copied only when modified, minimizing overhead.