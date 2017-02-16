import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy
from importCommon import *
################################################################################################################################################
#																																		       #	
# Import all users from a given file																										   #
# 																																			   #		
# activation :																																   #
#       python importUsers.py [-i <be host> | --ip=<be host>] [-p <be port> | --port=<be port> ] [-f <input file> | --ifile=<input file> ]     #
#																																		  	   #			
# shortest activation (be host = localhost, be port = 8080): 																				   #																       #
#		python importUsers.py [-f <input file> | --ifile=<input file> ]												 				           #
#																																		       #	
################################################################################################################################################

def createNormativeElement(beHost, bePort, adminUser, fileDir, urlSuffix, ELEMENT_NAME, elementFormName):
	
	try:
		log("in create normative element ", ELEMENT_NAME)

		buffer = StringIO()
		c = pycurl.Curl()

		url = 'http://' + beHost + ':' + bePort + urlSuffix
		c.setopt(c.URL, url)
		c.setopt(c.POST, 1)		

		adminHeader = 'USER_ID: ' + adminUser
		#c.setopt(pycurl.HTTPHEADER, ['Content-Type: application/json', 'Accept: application/json', adminHeader])
		c.setopt(pycurl.HTTPHEADER, [adminHeader])

			
		path = fileDir + "/" + ELEMENT_NAME + ".zip"
		debug(path)

		send = [(elementFormName, (pycurl.FORM_FILE, path))]
		debug(send)
		c.setopt(pycurl.HTTPPOST, send)		

		#data = json.dumps(user)
		#c.setopt(c.POSTFIELDS, data)	

		#c.setopt(c.WRITEFUNCTION, lambda x: None)
		c.setopt(c.WRITEFUNCTION, buffer.write)
		#print("before perform")	
		res = c.perform()
	
		#print("Before get response code")	
		httpRes = c.getinfo(c.RESPONSE_CODE)
		if (httpRes != None):
			debug("http response=", httpRes)
		#print('Status: ' + str(responseCode))
		debug("response buffer", buffer.getvalue())
		c.close()

		return (ELEMENT_NAME, httpRes, buffer.getvalue())

	except Exception as inst:
		print("ERROR=" + str(inst))
		return (ELEMENT_NAME, None, None)				


