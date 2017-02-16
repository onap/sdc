import pycurl
import sys, getopt
from StringIO import StringIO
import json
import copy

###############################################################################################################
#
#
###############################################################################################################

debugFlag = True

def join_strings(lst):
    concat = ""
    for string in lst:
	if (string != None):
		if (type(string) == int):
			string = str(string)
        	concat += (string + " ")
    return concat

def debug(desc, *args):
	'print only if debug enabled'
	if (debugFlag == True): 
		print desc, join_strings(args)  

def log(desc, arg=None):
	'print log info'
	print desc, arg  

def errorAndExit(errorCode, errorDesc):
	if ( errorCode > 0 ):
		print "status={0}. {1}".format(errorCode, '' if errorDesc == None else errorDesc) 
	else:
		print "status={0}".format(errorCode)
	sys.exit(errorCode)

def printNameAndReturnCode(name, code):
	print "{0:30} | {1:6}".format(name, code)	

def printFrameLine():
	print "----------------------------------------"	
