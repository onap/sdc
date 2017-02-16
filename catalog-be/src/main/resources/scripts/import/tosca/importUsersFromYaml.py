import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy
import yaml

########################################################################################################################################################
#																																				       #	
# Import all users from a given YAML file																											   #
# 																																					   #		
# activation :																																		   #
#       python importUsersFromYaml.py [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]     #
#																																				  	   #			
# shortest activation (be host = localhost, be port = 8080): 																						   #																       #
#		python importUsersFromYaml.py [-f <input file> | --ifile=<input file> ]												 				           #
#									    																											   #	
#   PyYAML module shall be added to python.																											   #	
#   pip install PyYAML>=3.1.0 --proxy=http://one.proxy.att.com:8080														                               #	
########################################################################################################################################################


def importUsers(beHost, bePort, users, adminUser):
	
	result = []	

	for user in users:
			
		#print("Going to add user " + user['userId'])
	
		getRes = getUser(beHost, bePort, user)	
		userId = getRes[0]
		error = getRes[1]
		#print error
		if ( error != None and error == 404 ):
			res = createUser(beHost, bePort, user ,adminUser)			
			result.append(res)			
		else:
			if ( error == 200 ):
				curResult = (userId, 409)
				result.append(curResult)	
			else:
				result.append(getRes)

	return result				


def getUser(beHost, bePort, user):

	if (user.get('userId') == None):
                print "Ignoring record", user
                return ('NotExist', 200)
	userId = user['userId']
	try:
		buffer = StringIO()
		c = pycurl.Curl()

		#print type(userId)
		url = 'http://' + beHost + ':' + bePort + '/sdc2/rest/v1/user/' + str(userId)
		c.setopt(c.URL, url)

		#adminHeader = 'USER_ID: ' + adminUser
		c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json'])
		c.setopt(c.WRITEFUNCTION, lambda x: None)
		res = c.perform()
					
		#print("Before get response code")	
		httpRes = c.getinfo(c.RESPONSE_CODE)
		#print("After get response code")	
		responseCode = c.getinfo(c.RESPONSE_CODE)
		
		#print('Status: ' + str(responseCode))

		c.close()

		return (userId, httpRes)

	except Exception as inst:
		print(inst)
		return (userId, None)				

		

def createUser(beHost, bePort, user, adminUser):
	
	if (user.get('userId') == None):
		print "Ignoring record", user
		return ('NotExist', 200)
	
	userId = user['userId']
	try:
		buffer = StringIO()
		c = pycurl.Curl()

		url = 'http://' + beHost + ':' + bePort + '/sdc2/rest/v1/user'
		c.setopt(c.URL, url)
		c.setopt(c.POST, 1)		

		adminHeader = 'USER_ID: ' + adminUser
		c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', adminHeader])

		data = json.dumps(user)
		c.setopt(c.POSTFIELDS, data)	

		c.setopt(c.WRITEFUNCTION, lambda x: None)
		#print("before perform")	
		res = c.perform()
        #print(res)
	
		#print("Before get response code")	
		httpRes = c.getinfo(c.RESPONSE_CODE)
		#print("After get response code")	
		responseCode = c.getinfo(c.RESPONSE_CODE)
		
		#print('Status: ' + str(responseCode))

		c.close()

		return (userId, httpRes)

	except Exception as inst:
		print(inst)
		return (userId, None)				


def errorAndExit(errorCode, errorDesc):
	if ( errorCode > 0 ):
		print("status=" + str(errorCode) + ". " + errorDesc) 
	else:
		print("status=" + str(errorCode))
	sys.exit(errorCode)
	
def usage():
	print sys.argv[0], '[-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]'

def main(argv):
	print 'Number of arguments:', len(sys.argv), 'arguments.'

	beHost = 'localhost' 
	bePort = '8080'
	inputfile = None 

	adminUser = 'jh0003'

	try:
		opts, args = getopt.getopt(argv,"i:p:f:h:",["ip=","port=","ifile="])
	except getopt.GetoptError:
		usage()
		errorAndExit(2, 'Invalid input')
		 
	for opt, arg in opts:
	#print opt, arg
		if opt == '-h':
			usage()                        
			sys.exit(3)
		elif opt in ("-i", "--ip"):
			beHost = arg
		elif opt in ("-p", "--port"):
			bePort = arg
		elif opt in ("-f", "--ifile"):
			inputfile = arg

	print 'be host =',beHost,', be port =', bePort,', users file =',inputfile
	
	if ( inputfile == None ):
		usage()
		sys.exit(3)

	print 'Input file is ', inputfile

	
	usersAsYamlFile = open(inputfile, 'r')
    	usersDoc = yaml.load(usersAsYamlFile)	
	print usersDoc 

	cloneUsers = [] 
	for users in usersDoc.values():
		for x,y in users.items():		
			copiedUser = y
			copiedUser['userId'] = x
			#print copiedUser 
			cloneUsers.append(copiedUser)
	
	print cloneUsers

	usersAsYamlFile.close()

	#activeUsers = filter(lambda x: x.get('status') == None or x['status'] == 'ACTIVE', cloneUsers)

	resultTable = importUsers(beHost, bePort, cloneUsers, adminUser)

	g = lambda x: x[1] != 201 and x[1] != 409

	result = filter(g, resultTable)

	if ( len(result) > 0 ):
		#print("ERROR: Failed to load the users " + ', '.join(map(lambda x: x[0],result)))
		errorAndExit(3, "Failed to load the users " + ', '.join(map(lambda x: x[0],result)))	

	g = lambda x: x[1] == 409
	result = filter(g, resultTable)

	print("-------------------------------------------")
	print("Existing users: " + ', '.join(map(lambda x: x[0],result)))

	result = filter(lambda x: x[1] == 201, resultTable)
	if ( len(result) == 0 ):
		print("-------------------------------------------")
		print("No NEW user was loaded. All users are already exist")
		print("-------------------------------------------")
	else:
		print("-------------------------------------------")
		print("Loaded users: " + ', '.join(map(lambda x: x[0],result)))
		print("-------------------------------------------")

	errorAndExit(0, None)


if __name__ == "__main__":
        main(sys.argv[1:])

