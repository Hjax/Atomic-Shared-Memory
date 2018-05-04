To use the Client class:

import the Client and the util.Server classes.
Create a HashSet of Server objects.
Create a Client object with the pc id, port, and server set as arguments.
Use client.write(key, value) and client.singlewrite(key, value) to write data following multi- or single-writer protocols.
Use client.read(key) and client.ohsamread(key) to read data following ABD and oh-SAM protocols.


To use the DynamicRuntimeTests test interface:
Run the main method provided or call DynamicRuntimeTests.runDynamicTests() to begin a session.
The following commands are provided for performing operations. Each command should be on its own line
   To create a client:			"newclient" *clientName* *pcId* *port* *serverSet* *xpos* *ypos*
   To create a server:			"newserver" *serverName* *IpAddress* *port*
   To create a serverSet:		"newserverset" *serversetName*
   To add a server to a serverSet:	"addserverset" *serverSetName* *server*
   To add a server to a client:		"addserver" *clientName* *serverName*
   To remove a server from a client:	"removeserver" *clientName* *serverName*
   To read:				"read" *clientName* *key*
   To oh-SAM read:			"ohsamread" *clientName* *key*
   To write with multiple writers:	"write" *clientName* *key* *value*
   To write with single writer:		"singleWrite" *clientName* *key* *value*
   To oh-SAM write:			"ohsamwrite" *clientName* *key* *value*
   To oh-MAM write:			"ohmamwrite" *clientName* *key* *value*

The following commands are provided for testing purposes. They use the managerport and managerpcid,
which can be set using these commands and default to port 1998 and pcid 99.
   To set the port to use for manager:	"managerport" *port#*
   To set the PCid to use for manager:	"managerpcid" *pcid*
   To set a location for a server:	"setloc" *serverName* *xfloat* *yfloat*
   To set drop rate for a server:	"drop" *drop%* *serverName*
   To set drop rate for server set:	"dropset" *drop%* *serverName*
   To kill a server:			"kill" *serverName*
   To kill all servers in a set:	"killset" *serverSetName*
   To revive a server:			"revive" *serverName*
   To revive all servers in a set:	"reviveset" *serverSetName*
   
   To set location for a client:	"clientloc" *client* *xfloat* *yfloat*
   To set droprate for a client:	"clientdrop" *client* *droprate*
   To set resend delay for a client:	"resend" *client* *delayMS*
   
   To do a reliable read:		"reliableread" *server* *key*
   
   To add a server to a server:		"learnserver" *learningServer* *serverToLearnAbout*
   To remove a server from a server:	"forgetserver" *forgettingServer* *serverToForgetAbout*
   To clear all data from a server:	"clear" *server*

To end a session, simply write
					"end"