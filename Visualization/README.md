The final visualization client can be found in client2.py and has two library requirements: pexpect and pygame, it is intended to be run under python 2.7.x

The client is straightforward to configure and use: simply create/edit the file servers.conf and specify the servers that the client should connect to as a newline seperated list of address:port pairs 

After the file is created, start the client and it will connect to the servers and begin sending reliable read requests to automatically update the value of the key "color" on the servers.

Click on the colors on the right side of the application to do write requests, drag the servers around the map to adjust their pings from the centralized client (the stick figure), finally right click on clients to enable and disable them.