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
        self.command('newclient clientAPI %d %d serversetAPI 0.0 0.0' % (port + 1, port + 1))
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

    def clean(self):
        subprocess.call(['taskkill', '/F', '/T', '/PID', str(self.child.pid)], shell=True)



active = []
inactive = []

#for i in range(72):
 #   inactive.append(("localhost", 2000 + i))

servers = open("servers.txt", "r").read().split("\n")
for server in servers:
    inactive.append((server, 2000))
    inactive.append((server, 2001))
    inactive.append((server, 2002))

current_port = 12000

def start(count):
    global current_port
    manager = API()
    active = []
    for i in range(1, count+1):
        active.append(inactive[i-1])
        manager.add_server(active[-1][0], active[-1][1])
    manager.start(current_port)

    for i in range(len(active)):
        manager.raw_command("clear " + active[i][0] + ":" + str(active[i][1]))
        for j in range(len(active)):
            if i == j:
                continue
            manager.raw_command("learnserver " + active[i][0] + ":" + str(active[i][1]) + " " + active[j][0] + ":" + str(active[j][1]))
            
    current_port += 1
    return manager


        
def run(manager, drop_rate, drop_rate_stddev, ping_rate, ping_stddev, dead_servers):

    for i in range(0, len(active)):
        ping = numpy.random.normal(ping_rate, ping_stddev*ping_rate)
        while ping < 0:
            ping = numpy.random.normal(ping_rate, ping_stddev*ping_rate)
        angle = random.uniform(0, 2 * math.pi)
        x = math.cos(angle) * ping
        y = math.sin(angle) * ping
        #manager.set_location("localhost:%d" % (2000 + i), (x, y))
        #manager.set_location("localhost:%d" % (2000 + i), (0, ping))

    for i in range(0, len(active)):
        drop = numpy.random.normal(drop_rate, drop_rate_stddev*drop_rate)
        while drop < 0:
            drop = numpy.random.normal(drop_rate, drop_rate_stddev*drop_rate)
        #manager.set_drop("localhost:%d" % (2000 + i), drop)
        
    #for i in range(0, int(dead_servers)):
        #manager.kill("localhost:%d" % (2000 + i))

    results = []

    i = random.randint(1000,2000)

    manager.write("foo", str(i))

    start = time.time()
    value = manager.read("foo")
    if value[:-1] != str(i):
        print "got " + str(value[:-1]) + " expected " + str(i)
        return -999999
    end = time.time()
    print(end-start)
    return end-start
    

stds = [0.05 * x for x in range(0,9)]
drops = [0.05 * x for x in range(0,12)]


total = 0

result = open("ohsam-google.csv", "w")
result.write("%s,%s,%s,%s,%s,%s,%s,%s\n" % ("Server Count", "Drop Rate", "Drop STD", "Ping", "Ping STD", "Dead", "Mean", "Variance"))

def runner():
    time.sleep(1)
    f = 0
    for count in range(1, 73):
        time.sleep(5)
        current = start(count)
        print("%s,%s,%s,%s,%s,%s\n" % (count, 0, 0, 200, 0, 0))
        results = []
        for i in range(0, 5):
            time.sleep(2)
            results.append(run(current, 0, 0, 200, 0, 0))
        data = (numpy.mean(results), numpy.var(results))
        #print(data)
        if data == -1:
            print("An error has occured")
            return
        print("%s,%s,%s,%s,%s,%s,%s,%s\n" % (count, 0, 0, 200, 0, 0, data[0], data[1]))
        result.write("%s,%s,%s,%s,%s,%s,%s,%s\n" % (count, 0, 0, 200, 0, 0, data[0], data[1]))
        current.clean()
            

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

    
