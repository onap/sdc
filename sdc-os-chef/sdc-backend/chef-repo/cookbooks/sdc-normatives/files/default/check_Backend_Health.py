#!/usr/bin/python
import subprocess
#from time import sleep
import time
from datetime import datetime

class bcolors:
    HEADER    = '\033[95m'
    OKBLUE    = '\033[94m'
    OKGREEN   = '\033[92m'
    WARNING   = '\033[93m'
    FAIL      = '\033[91m'
    ENDC      = '\033[0m'
    BOLD      = '\033[1m'
    UNDERLINE = '\033[4m'


##############################
#    Functions
##############################
def checkBackend():
    command="curl -s -o /dev/null -I -w \"%{http_code}\" -i http://localhost:8080/sdc2/rest/v1/user/jh0003"

    proc = subprocess.Popen( command , shell=True , stdout=subprocess.PIPE )
    (out, err) = proc.communicate()
    result = out.strip()
    return result




##############################
#    Main
##############################
beStat=0

for i in range(1,10):
    myResult = checkBackend()
    if myResult == '200':
        print '[INFO]: Backend is up and running'
        beStat=1
        break
    else:
        currentTime = datetime.now()
        print '[ERROR]: ' + currentTime.strftime('%Y/%m/%d %H:%M:%S') + bcolors.FAIL + ' Backend not responding, try #' + str(i) + bcolors.ENDC
        time.sleep(10)

if beStat == 0:
    print '[ERROR]: ' + time.strftime('%Y/%m/%d %H:%M:%S') + bcolors.FAIL + 'Backend is DOWN :-(' + bcolors.ENDC
    exit()

