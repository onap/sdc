import pycurl
import sys, getopt
from StringIO import StringIO
import json


####################################################################################################################################################################################
#                                                                                                                                                                                  #
# Export all active users to file - for 1602+                                                                                                                                      #
#                                                                                                                                                                                  #
# activation :                                                                                                                                                                     #
#       python exportUsers.py  [-s <scheme> | --scheme=<scheme> ] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <output file> | --ofile=<output file> ]   #
#                                                                                                                                                                                  #
# shortest activation (be host = localhost, be port = 8080):                                                                                                                       #
#        python exportUsers.py [-f <output file> | --ofile=<output file> ]                                                                                                         #
#                                                                                                                                                                                  #
####################################################################################################################################################################################

ALL_USERS_SUFFIX = '/sdc2/rest/v1/user/users'

def errorAndExit(errorCode, errorDesc):
        if ( errorCode > 0 ):
                print("status=" + str(errorCode) + ". " + errorDesc)
        else:
                print("status=" + str(errorCode))
        sys.exit(errorCode)

def getUsers(scheme, beHost, bePort, adminUser):

	try:
		buffer = StringIO()
		c = pycurl.Curl()

		url = scheme + '://' + beHost + ':' + bePort + ALL_USERS_SUFFIX 
		print(url)
		c.setopt(c.URL, url)
		c.setopt(c.WRITEFUNCTION, buffer.write)
		#c.setopt(c.WRITEFUNCTION, lambda x: None)
		adminHeader = 'USER_ID: ' + adminUser
		c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', adminHeader])

		if scheme == 'https':
			c.setopt(pycurl.SSL_VERIFYPEER, 0)
			c.setopt(pycurl.SSL_VERIFYHOST, 0)

		res = c.perform()
		#print(res)

		#print('Status: %d' % c.getinfo(c.RESPONSE_CODE))

		c.close()

		body = buffer.getvalue()

        #print(body)

		return (body, None)

	except Exception as inst:
		print inst
        #print type(inst)     # the exception instance
        #print inst.args      # arguments stored in .args
        #print inst           # __str__ allows args to be printed directly
        #x, y = inst.args
        #print 'x =', x
		
		return (None, inst)


def usage():
	print sys.argv[0], '[optional -s <scheme> | --scheme=<scheme>, default http] [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <output file> | --ofile=<output file> ]'

def main(argv):
	print 'Number of arguments:', len(sys.argv), 'arguments.'

	adminHeader = 'jh0003'
	beHost = 'localhost'
	bePort = '8080'
	outputfile = None 
	scheme = 'http'

	try:
		opts, args = getopt.getopt(argv,"i:p:f:h:s:",["ip=","port=","ofile=","scheme="])
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
		elif opt in ("-f", "--ofile"):
			outputfile = arg
		elif opt in ("-s", "--scheme"):
			scheme = arg

	print 'scheme =',scheme,', be host =',beHost,', be port =', bePort,', output file =',outputfile

	if ( outputfile == None ):
		usage()
		sys.exit(3)

	users = getUsers(scheme, beHost, bePort, adminHeader)
	error = users[1]
	body = users[0]

	if ( error != None ):
		errorAndExit(5, str(error))

	#print body

	io = StringIO(body)
	usersAsJson = json.load(io)

	writeFile = open(outputfile, 'w')

	json.dump(usersAsJson, writeFile)

	writeFile.close()

	print("-------------------------------------------")
	errorAndExit(0, None)

if __name__ == "__main__":
        main(sys.argv[1:])
