This Java Project includes an implementation of a server that receives and sends messages (through abstract methods) and stores data (through abstract methods). The environment this project is meant to be run is a testing environment that includes a client or several clients that can manipulate the meta state of the server. This meta state includes: awake/asleep; simulated location (and therefore ping); simulated packet loss for received messages; simulated packet loss going to the client (which should be kept consistent across all servers for consistent results); the other servers in the network this server knows about (which should not be changed while the server is actively handling messages); and the ability to clear the contents of the server. See MessageParser class for the specifics on these messages.

This project can be run using the "java" command in command line (in the same fashion any other Java project can be), or can be exported as a .jar and run with the "java -jar" command. Feeding a "-help" argument (or misformating any input) will provide the following help message: 

"Run this program from the Atomic-Shared-Memory/server directory in the form: java -cp bin main.Main <-server | -make-bats | -h>"
	"-help		:	display this help message"
	"-server	:	server_ip:server_port other_known_server_ip_1:other_known_server_port1;other_known_server_ip_2;other_known_server_port_2;..."
	"-server	:	server_ip:server_port"
	"			Providing no list of server ips to teach it creates a server that does not know about any other server in the network"
	"-bats		:	<srcDirectory> <directory> <ip1>:<port1> <ip2>:<port2> ..."
	"			This command is how you can generate all the .bat files you need for running all servers, provided you have all the IPs."
	"			Note that if you are using this codebase for simulation and testing and will be adding/dropping servers, you can "
	" 			simply use add-server and remove-server messages from the client."
	"			<srcDirectory> should be where you want all the .bat files to go."
	"			<directory> should be the absolute path of the Atomic-Shared-Memory/server directory"
	"<noArgs>:	starts a server on localhost at port 2000 without any other known servers"; 

