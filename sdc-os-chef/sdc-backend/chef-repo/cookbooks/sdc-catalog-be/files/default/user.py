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


def checkUser(userName):
    command="curl -s -o /dev/null -I -w \"%{http_code}\" -i -H \"Accept: application/json; charset=UTF-8\" -H \"Content-Type: application/json\" -H \"USER_ID: jh0003\"   http://localhost:8080/sdc2/rest/v1/user/" + userName

    proc = subprocess.Popen( command , shell=True , stdout=subprocess.PIPE )
    (out, err) = proc.communicate()
    result = out.strip()
    return result




def createUser( firstName, lastName, userId , email_dom , role ):
    print '[INFO] create first:[' + firstName + '], last:[' + lastName + '], Id:[' + userId + '], email:[' + userId + '@' + email_dom + '], role:[' + role +']'
    command="curl -s -o /dev/null -w \"%{http_code}\" -X POST -i -H \"Accept: application/json; charset=UTF-8\" -H \"Content-Type: application/json\" -H \"USER_ID: jh0003\" http://localhost:8080/sdc2/rest/v1/user/ -d '{\"firstName\": '" + firstName + "', \"lastName\": '" + lastName + "',\"userId\": '" + userId + "',\"email\": '" + userId + "@" + email_dom + "',\"role\": '" + role + "'}'"

    proc = subprocess.Popen( command , shell=True , stdout=subprocess.PIPE)
    (out, err) = proc.communicate()
    result = out.strip()
    return result




##############################
#    Definitions
##############################
userId    = [ "demo" , "op0001" , "gv0001" , "jh0003" , "jm0007" , "cs0008" ]
firstName = [ "demo" , "Oper" , "Giuseppe" , "Jimmy" , "Joni" , "Carlos" ]
lastName  = [ "demo" , "P" , "Verdi" , "Hendrix" , "Mitchell" , "Santana" ]
role      = [ "ADMIN" , "OPS" , "GOVERNOR" , "ADMIN" , "TESTER" , "DESIGNER" ]
email_dom = "openecomp.org"
beStat=0


##############################
#    Main
##############################

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

for user in userId:
    myResult = checkUser(user)
    pos = userId.index(user)
    if myResult == '200':
        print '[INFO]: ' + user + ' already exists'
    else:
        myResult = createUser( firstName[pos], lastName[pos], userId[pos], email_dom, role[pos] )
        if myResult == '201':
            print '[INFO]: ' + userId[pos] + ' created, result: [' + myResult + ']'
        else:
            print '[ERROR]: ' + bcolors.FAIL + userId[pos] + bcolors.ENDC + ' error creating , result: [' + myResult + ']'
