As we discussed in the previous topic, Redis Transaction are not fully Atmoic and doesnt completely follow the ACID principle
Hence for that we will be Using **LUA SCRIPTING** 

Why Lua Scripts Are Atomic?
    Because Redis executes the entire script within its single-threaded event loop before processing any other command.

Lets understand **LUA SCRIPTING** with an example: