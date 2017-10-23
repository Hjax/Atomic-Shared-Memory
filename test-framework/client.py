import socket
import sys

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('localhost', 2555)
print >>sys.stderr, 'connecting to %s port %s' % server_address
sock.connect(server_address)
while True:
    try:
        
        # Send data
        message = raw_input(">>>")
        sock.sendall(message)

        data = sock.recv(1024)
        print(data)

    finally:
        print >>sys.stderr, 'closing socket'
        sock.close()
     
