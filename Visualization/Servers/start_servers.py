import subprocess
import time

NUM_SERVERS = 5

for i in range(0, NUM_SERVERS):
    string = "-server localhost:%d " % (2000 + i)
    for j in range(0,NUM_SERVERS):
        if i == j:
            continue
        string = string + "localhost:%d;" % (2000 + j)
    subprocess.Popen("java -jar Atomic-Shared-Memory.jar " + string)
    time.sleep(0.05)
    
