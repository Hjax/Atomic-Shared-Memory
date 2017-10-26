import socket
import sys


while True:
    # Create a TCP/IP socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Connect the socket to the port where the server is listening
    server_address = ('localhost', 2556)
    sock.connect(server_address)
    
    # Send data
    message = input(">>>")
    sock.sendall(message.encode())

    data = sock.recv(1024)
    print(data)

    sock.close()
     
