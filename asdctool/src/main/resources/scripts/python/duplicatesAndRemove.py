import json
import sys, getopt
from collections import OrderedDict

dict = {} 
dupliacteUid = {} 
#debugFlag = True 
debugFlag = False 

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

def log(desc, arg):
	'print log info'
	print desc, arg  

def getUid(vertex):
	uid = None
	nodeLabel=vertex.get('nodeLabel')
	debug(nodeLabel)
	if ( nodeLabel == 'user' ):
		uid = vertex['userId']
	elif ( nodeLabel == 'tag' ):
		uid = vertex['name']
	elif ( nodeLabel == None ):
		pass
	elif ( nodeLabel == 'lockNode' ):
		uid = vertex.get('uid')
	else: uid = vertex['uid']

	debug(nodeLabel, uid)

	return uid	   

def generateFile(inputFile, outputFile):
	
	with open(inputFile) as json_file:
		dupliacteUid = {}
		json_data = json.load(json_file)
		for x in json_data['vertices']:
			uid = getUid(x)

			existId = dict.get(uid)
			if (existId == None):
				dict[uid] = x.get('_id')
			else:
				dupliacteUid[uid] = existId 	    

		log("duplicate ids", dupliacteUid)

		json_data_vertices = json_data['vertices']
		log("number of vertices is", len(json_data_vertices))

		ids = {} 
		deleteIndexes = []
		 
		for i in xrange(len(json_data_vertices)):
		#print "****** ", i, " *************"
		#print json_data_vertices[i]
			id = json_data_vertices[i]["_id"]
			uid = getUid(json_data_vertices[i])
			isDuplicateId = dupliacteUid.get(uid)
			if (isDuplicateId != None):
				debug("uid to id pair", uid if uid != None else 'None', id)
				value = ids.get(uid)
				if (value == None):
					list = [id,]	
					ids[uid] = list 
				else:
					value.append(id)	
					deleteIndexes.append(id)	

		log("ids", ids)
		log("deleteIndexes", deleteIndexes)
		log("deleteIndexes size", len(deleteIndexes))

		filter_vertex = [ x for x in json_data_vertices if x.get('_id') not in deleteIndexes ]
		json_data['vertices'] = filter_vertex	

		log("number of vertexes after filter", len(filter_vertex))

		json_data_edges = json_data['edges']

		log("number of edges", len(json_data_edges))
		
		filter_edge = [ x for x in json_data_edges if x['_outV'] not in (deleteIndexes) and x['_inV'] not in (deleteIndexes) ]
                json_data['edges'] = filter_edge

		log("number of edges after filter", len(json_data['edges']))

		json_data = OrderedDict(sorted(json_data.items(), key=lambda t: t[0], reverse=True))	
	
		with open(outputFile, 'w') as outfile:
			#json.dump(json_data, outfile)
			json.dump(json_data, outfile)
		log("output file is", outputFile);

def main(argv):
        print 'Number of arguments:', len(sys.argv), 'arguments.'
        inputfile = None 
        outputfile = ''
        try:
                opts, args = getopt.getopt(argv,"h:i:o:",["ifile=","ofile="])
        except getopt.GetoptError:
                print sys.argv[0], '-i <inputfile>'
                sys.exit(2)
        for opt, arg in opts:
                if opt == '-h':
                        print sys.argv[0], '-i <inputfile>'
                        sys.exit(3)
                elif opt in ("-i", "--ifile"):
                        inputfile = arg

        if ( inputfile == None ):
                print sys.argv[0] ,'-i <inputfile>'
                sys.exit(3)

        print 'Input file is "', inputfile
        generateFile(inputfile, inputfile + '.noduplicates')
		

if __name__ == "__main__":
        main(sys.argv[1:])
    
#	print x['uid']
