import json
import sys

dict = {} 
dupliacteUid = {} 
#debugFlag = True 
debugFlag = False 

def debug(str1, str2=""):
    'print only if debug enabled'
    if (debugFlag == True): print str1, str2

print 'Number of arguments:', len(sys.argv), 'arguments.'


with open(sys.argv[1]) as json_file:
    json_data = json.load(json_file)
    for x in json_data['vertices']:
	uid = None
	nodeLabel=x.get('nodeLabel')
	debug(nodeLabel)
	if ( nodeLabel == 'user' ):
	    uid = x['userId']
 	elif ( nodeLabel == 'tag' ):
	    uid = x['name'] 
 	elif ( nodeLabel == None ):
 	    pass
 	elif ( nodeLabel == 'lockNode' ):
	    uid = x.get('uid')
	else: uid = x['uid']		

	debug(nodeLabel, uid)

	existId = dict.get(uid)
	if (existId == None):
	    dict[uid] = x.get('_id')
	else:
            dupliacteUid[uid] = existId 	    

    print dupliacteUid 

#    with open('data.txt', 'w') as outfile:
#        json.dump(json_data, outfile)


    
#	print x['uid']
