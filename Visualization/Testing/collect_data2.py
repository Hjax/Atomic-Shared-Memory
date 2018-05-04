import subprocess
import time

import pygame, sys, random, pexpect, threading, time, sys, math, os 
import numpy
from pexpect.popen_spawn import PopenSpawn



myport = 5000

        
class API:
    def __init__(self):
        self.child = PopenSpawn('java -jar client3.jar')
        self.child.logfile = open("log.txt", "w")
        self.lock = threading.Lock()
        self.lock.acquire()
        self.command('newserverset serversetAPI')
        port = random.randint(4000,5000)
        self.raw_command('managerport ' + str(port))
        self.raw_command('managerpcid ' + str(port))
        self.lock.release()
        

    def add_server(self, address, port):
        self.lock.acquire()
        self.command('newserver ' + address + ":" + str(port) + " " + address + " " + str(port))
        self.command('addserverset serversetAPI ' + address + ":" + str(port))
        self.lock.release()

    def start(self, port):
        self.lock.acquire()
        self.command('newclient clientAPI 1 %d serversetAPI 0.0 0.0' % (port + 1))
        self.raw_command('resend clientAPI 2000')
        self.lock.release()

    def raw_command(self, command):
        #sys.stdout.write(command + '\n')
        #sys.stdout.flush()
        self.child.sendline(command)
    
    def command(self, command):
        #sys.stdout.write(command + '\n')
        #sys.stdout.flush()
        self.child.sendline(command)
        self.child.readline()

    def read_old(self, command):
        self.child.sendline(command)
        return self.child.readline().split("->")[-1].split("\n")[0]

    def read(self, key):
        self.child.sendline("ohsamread clientAPI " + str(key))
        return self.child.readline().split("->")[-1].split("\n")[0]

    def write(self, key, value):
        self.command("write clientAPI " + str(key) + " " + str(value))

    def set_location(self, server, location):
        self.raw_command("setloc " + server + " " + str(float(location[0])) + " " + str(float(location[1])))

    def set_drop(self, server, rate):
        self.raw_command("drop " + str(int(rate * 100)) + " " +  server)

    def kill(self, server):
        self.raw_command("kill " + server)

    def revive(self, server):
        self.raw_command("revive " + server)

manager = API()

children = []
active = []


def start(num_servers):
    for i in range(0, num_servers):
        string = "-server localhost:%d " % (2000 + i)
        for j in range(0,num_servers):
            if i == j:
                continue
            string = string + "localhost:%d;" % (2000 + j)
        outfile = open(str(i)  + ".txt","wb")
        children.append(subprocess.Popen("java -jar Atomic-Shared-Memory.jar " + string, shell=True, stdout=outfile, stderr=outfile))

    for server in children:
        active.append(server)
    
    for i in range(0, len(active)):
        manager.add_server("localhost", 2000 + i)
    manager.start(random.randint(4000,5000) + num_servers)

def set_active(num_servers):
    for i in range(num_servers):
        active.append(children[i])

def kill():
    for server in children:
        subprocess.call(['taskkill', '/F', '/T', '/PID', str(server.pid)], shell=True)

def clean():
    for i in range(0, int(len(active))):
        manager.revive("localhost:%d" % (2000 + i))
    
        
def run(drop_rate, drop_rate_stddev, ping_rate, ping_stddev, dead_servers):

    for i in range(0, len(active)):
        ping = numpy.random.normal(ping_rate, ping_stddev*ping_rate)
        while ping < 0:
            ping = numpy.random.normal(ping_rate, ping_stddev*ping_rate)
        angle = random.uniform(0, 2 * math.pi)
        x = math.cos(angle) * ping
        y = math.sin(angle) * ping
        manager.set_location("localhost:%d" % (2000 + i), (x, y))
        #manager.set_location("localhost:%d" % (2000 + i), (0, ping))

    for i in range(0, len(active)):
        drop = numpy.random.normal(drop_rate, drop_rate_stddev*drop_rate)
        while drop < 0:
            drop = numpy.random.normal(drop_rate, drop_rate_stddev*drop_rate)
        manager.set_drop("localhost:%d" % (2000 + i), drop)
        
    for i in range(0, int(dead_servers)):
        manager.kill("localhost:%d" % (2000 + i))

    results = []


    manager.write("foo", str(i))

    start = time.time()
    value = manager.read("foo")
    if value[:-1] != str(i):
        print "got " + str(value[:-1]) + " expected " + str(i)
    end = time.time()
    print(end-start)
    return end-start
    

stds = [0.05 * x for x in range(0,9)]
drops = [0.05 * x for x in range(0,12)]


total = 0

result = open("results26.csv", "w")
result.write("%s,%s,%s,%s,%s,%s,%s,%s\n" % ("Server Count", "Drop Rate", "Drop STD", "Ping", "Ping STD", "Dead", "Mean", "Variance"))

def runner():
    start(15)
    time.sleep(1)
    f = 0
    #for f in drops:
    #for d in stds:
    d = 0.3
    for dead in range(0,8):
        if dead < 7:
            continue
        print("%s,%s,%s,%s,%s,%s\n" % (15, f, 0, 200, d, dead))
        results = []
        for i in range(0, 5):
            results.append(run(f, 0, 200, d, dead))
        data = (numpy.mean(results), numpy.var(results))
        #print(data)
        if data == -1:
            print("An error has occured")
            return
        print("%s,%s,%s,%s,%s,%s,%s,%s\n" % (15, f, 0, 200, d, dead, data[0], data[1]))
        result.write("%s,%s,%s,%s,%s,%s,%s,%s\n" % (15, f, 0, 200, d, dead, data[0], data[1]))
        clean()
            

runner()
result.close()

#for num_servers in range(1, 30):
 #   for ping in ints:    
  #      for pingstd in drops:
   #         for drop in ints:
    #            for dropstd in drops:
     #               for dead in floats:
      #                  data = []
       #                 for test in range(0,5):
        #                    total += 1
         #                   print(run(num_servers, drop, dropstd, ping, pingstd, dead))

    
