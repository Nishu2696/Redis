In this project we will learn about Spring Cache, so first hit checks whether the data is available in spring cache or not
if available return from here, else go and check redis, and the further process continues as before

One more reason why we should prefer Spring Cache, if we are using this functionality boiler plate of redis code reduces,
because internally spring cache check if available in spring memory, return from here or call redis cache internally