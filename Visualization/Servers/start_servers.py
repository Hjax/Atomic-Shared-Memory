import subprocess
import time

for i in range(0, 5):
    string = "-server localhost:%d " % (2000 + i)
    for j in range(0,5):
        if i == j:
            continue
        string = string + "localhost:%d;" % (2000 + j)
    subprocess.Popen("java -jar Atomic-Shared-Memory.jar " + string)
    time.sleep(0.05)
    
