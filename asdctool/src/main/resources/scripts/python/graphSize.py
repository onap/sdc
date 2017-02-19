import json
import sys, getopt

dict = {} 
dupliacteUid = {} 
#debugFlag = True 
debugFlag = False 

def debug(desc, *args):
	'print only if debug enabled'
	if (debugFlag == True): 
		print desc, join_strings(args)  

def log(desc, arg):
	'print log info'
	print desc, arg  

def graphSize(inputFile):
	
	with open(inputFile) as json_file:
		json_data = json.load(json_file)

		json_data_vertices = json_data['vertices']
		log("number of vertices is", len(json_data_vertices))

		json_data_edges = json_data['edges']
		log("number of edges is", len(json_data_edges))

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
                print sys.argv[0], '-i <inputfile>'
                sys.exit(3)

        print 'Input file is ', inputfile
        graphSize(inputfile)
		

if __name__ == "__main__":
        main(sys.argv[1:])
    
#	print x['uid']
