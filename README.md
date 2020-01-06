# 445a3
comp 445 a3

To use the router, you have to go into the directory of the executible from your command line. 
Then us a command with the syntax as follows:
router_x64 --port=3001 --drop-rate=0.2 --max-delay=10ms --seed=1

Basically, you are substituting the router with the actual .exe file name 
Usage:
 router --port int --drop-rate float --max-delay duration --seed int

 --port int-number
 port number that the router is listening for the incoming packets.
 default value is 3000.

 --drop-rate float-number
 drop rate is the probability of packets will be dropped during on the way.
 use 0 to disable the drop feature.
 --max-delay duration (eg. 5ms, 4s, or 1m)
 max delay the maximum duration that any packet can be delayed.
 any packet will be subject to a delay duration between 0 and this value.
 the duration is in format 5s, 10ms. Uses 0 to deliver packets immediately.
 --seed int
 seed is used to initialize the random generator.
 if the same seed is provided, the random behaviors are expected to repeat.
Example:
 router --port=3000 --drop-rate=0.2 --max-delay=10ms --seed=1
 
 
 Also, Resource Monitor on Windows allows you to see all the services running, including the ports that eclipse claims are still being in use.


Here's an example for Selective Repeat from somewhere else, yay 
https://github.com/michellefish/UDP-SelectiveRepeat/blob/master/UDP-SelectiveRepeat/src/UDPServer.java
