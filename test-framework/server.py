import os, socket, subprocess

# javac -d bin -cp src server/src/client/*.java server/src/dataserver/*.java server/src/main/*.java server/src/util/*.java

# message format: "command string" eg "build directory"

def build(directory):
    os.system("killall java")
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

def run(target):
    os.system("killall java")
    subprocess.Popen(['java', '-classpath bin', target])
    
if __name__ == "__main__":
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_address = ('localhost', 2556)
    print('starting up on %s port %s' % server_address)
    sock.bind(server_address)
    sock.listen(1)
    while True:
        try:
            connection, client_address = sock.accept()
            data = connection.recv(1024).decode()
            print("Recieved command: %s" % (data))
            data = data.split(" ")
            try:
                if data[0] == "build":
                    build(data[1])
                if data[0] == "pull":
                    pull(data[1])
                if data[0] == "rm":
                    rm()
                if data[0] == "run":
                    run(data[1])
                connection.sendall(b"ok\n")
            except:
                connection.sendall(b"fail\n")
        except:
            print("disconnect")
        
