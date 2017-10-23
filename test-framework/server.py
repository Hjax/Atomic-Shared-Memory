import os, socket

# javac -d bin -cp src server/src/client/*.java server/src/dataserver/*.java server/src/main/*.java server/src/util/*.java

# message format: "command string" eg "build directory"

def build(directory):
    os.system("rm -rf bin")
    os.system("mkdir bin")
    base = "javac -d bin -cp src "
    for file in next(os.walk(directory))[1]:
        base = base + directory + "/" + file + "/*.java "
    os.system(base)

def pull(target):
    os.system("git clone " + target)

def rm():
    for file in next(os.walk("."))[1]:
        os.system("rm -rf " + file)
    
if __name__ == "__main__":
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_address = ('localhost', 255)
    print('starting up on %s port %s' % server_address)
    sock.bind(server_address)
    sock.listen(1)
    while True:
        connection, client_address = sock.accept()
        data = connection.recv(1024).decode()
        print("Recieved command: %s" % (data))
        data = data.split(" ")
        if data[0] == "build":
            build(data[1])
            connection.sendall(b"ok\n")
        if data[0] == "pull":
            pull(data[1])
            connection.sendall(b"ok\n")
        if data[0] == "rm":
            rm()
            connection.sendall(b"ok\n")
