import itertools
import string
import json
from datetime import datetime
from elasticsearch import Elasticsearch
import elasticsearch
import elasticsearch.helpers
from elasticsearch.client import IndicesClient, CatClient
import sys, os, getopt
from file_utils import readFileToJson
from config_properties import getGlobalVar 



def createIndex(client, indexName, createBody):
    try:
      print "start createIndex"
      if (client == None):
         client = Elasticsearch(['localhost'])
      esIndexClient = IndicesClient(client)
      res = deleteIndex(client, indexName)
      if (res != 0):
         print "operation failed"
         return 2
      create_res=elasticsearch.client.IndicesClient.create(esIndexClient, index=indexName, body=createBody)
      print "create index response: ", create_res
      if (create_res['acknowledged'] != True):
         print "failed to create index"
         return 1
      else:
         print "index ",indexName, " created successfully"
         return 0
    except Exception, error:
      print "An exception was thrown!"
      print str(error)
      return 2
  

def deleteIndex(client, indexName):
   try:
     print "start deleteIndex"
     if (client == None):
         client = Elasticsearch(['localhost'])
     esIndexClient = IndicesClient(client)
     isExists=elasticsearch.client.IndicesClient.exists(esIndexClient, indexName)
     if ( isExists == True ):
        delete_res=elasticsearch.client.IndicesClient.delete(esIndexClient, index=indexName)
        if (delete_res['acknowledged'] != True):
           print "failed to delete index"
           return 1
        else:
           print "index ",indexName, " deleted"
           return 0
     else:
        print "index not found - assume already deleted"
        return 0
   except Exception, error:
      print "An exception was thrown!"
      print str(error)
      return 2

def copyIndex(client, fromIndex, toIndex):
    try: 
      print "start copyIndex"
      if (client == None):
         client = Elasticsearch(['localhost'])
      client.indices.refresh(index=fromIndex)
      count=client.search(fromIndex, search_type='count')
      print "original index count: ",count
      docNum, docErrors = elasticsearch.helpers.reindex(client, fromIndex, toIndex)
      print "copy result: ", docNum, docErrors 
      if (docNum != count['hits']['total']):
         print "Failed to copy all documents. expected: ", count['hits']['total'], " actual: ", docNum
         return 1
      # if (len(docErrors) != 0):
         # print "copy returned with errors"
         # print docErrors
         # return 1
      return 0
    except Exception, error:
      print "An exception was thrown!"
      print str(error)
      return 2
  

def usage():
     print 'USAGE: ', sys.argv[0], '-o <operation : create | delete | move> -n <indexName> -a <address> -f <mappingFile (for create)> -t <toIndex (for move operation)>' 
     


def main(argv):
   print "start script with ", len(sys.argv), 'arguments.'
   print "=============================================="

   try:
         opts, args = getopt.getopt(argv, "h:o:a:n:f:t:", ["operation","address","indexName","file","toIndex"])
   except getopt.GetoptError:
         usage()
         sys.exit(2)
 
   host = None
   for opt, arg in opts:
         print opt, arg
         if opt == '-h':
             usage()
             sys.exit(2)
         elif opt in ('-f', '--file'):
            mapping=readFileToJson(arg)
         elif opt in ('-a', '--address'):
            host=arg
         elif opt in ('-o', '--operation'):
            operation=arg
         elif opt in ('-n', '--indexName'):
            indexName=arg
         elif opt in ('-t', '--toIndex'):
            destIndexName=arg

   if (operation == None):
       usage()
       sys.exit(2)
   elif (host == None):
       print "address is mandatory argument"
       usage()
       sys.exit(2)
   elif operation == 'create':
       print "create new index ", indexName
       client = Elasticsearch([{'host': host, 'timeout':5}] )
       res = createIndex(client, indexName, mapping)
   
   elif operation == 'delete':
       print "delete index ", indexName
       client = Elasticsearch([{'host': host, 'timeout':5}] )
       res = deleteIndex(client, indexName)

   elif operation == 'move':
       print "move index ", indexName, " to ", destIndexName
       client = Elasticsearch([{'host': host, 'timeout':5}] )
       res = copyIndex(client, indexName, destIndexName)
   else:
       usage()
       exit(2)
   if res != 0:
      print "ERROR: operation Failed"
      exit(1)
    

  
if __name__ == "__main__":
        main(sys.argv[1:])


