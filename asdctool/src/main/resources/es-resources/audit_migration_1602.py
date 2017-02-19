import itertools
import string
import json
from datetime import datetime
from elasticsearch import Elasticsearch
import elasticsearch
import elasticsearch.helpers
from elasticsearch.client import IndicesClient
import sys, os
from index_ops import createIndex, deleteIndex, copyIndex
from config_properties import getGlobalVar 
from file_utils import readFileToJson

def updateFieldNames(client, queryFrom, fromIndex, destIndex, addUTC):
    typesDir="types"
    typeFields = {}
    for filename in os.listdir(typesDir):
       print filename
       fieldNames=readFileToJson(typesDir+os.sep+filename)
       
       type=filename.split(".")[0]
       typeFields[type] = fieldNames
   
    client.indices.refresh(index=fromIndex)
    res = elasticsearch.helpers.scan(client, query=queryFrom, index=fromIndex)
       
    actions = []
    for i in res:
       res_type = i['_type']
       fieldNames = typeFields.get(res_type)
       if (fieldNames != None):
         action={}
         for field in i['_source']:
             updatedName=fieldNames.get(field)
             if (updatedName != None):        
                 if (field == 'timestamp' and addUTC == True):
                     value+=" UTC"
                 value=i['_source'].get(field)   
                 action[updatedName]=value
             else:
                 action[field]=i['_source'].get(field)
         i['_source']=action
       
       i['_index']=destIndex
       i.pop('_id', None)
       actions.append(i)

    bulk_res = elasticsearch.helpers.bulk(client, actions)
    print "bulk response: ", bulk_res



def updateAllrecordsWithUTC(client, queryFrom, fromIndex, destIndex):

    #scan indices
    client.indices.refresh(index=fromIndex)
    res = elasticsearch.helpers.scan(client, query=queryFrom, index=fromIndex)

    actions = []
    for i in res:
        print i
        i['_index']=destIndex
        i['_source']['TIMESTAMP']+=" UTC"
        actions.append(i)

    bulk_res = elasticsearch.helpers.bulk(client, actions)
    print "bulk response: ", bulk_res


def printQueryResults(client, myQuery, indexName):
    client.indices.refresh(index=indexName)
    res = elasticsearch.helpers.scan(client, query=myQuery, index=indexName)
    for i in res:
       print i

def main():
   print "start script for changing fields"
   print "================================="
   
   # initialize es
   es = Elasticsearch([getGlobalVar('host')])

   try:
    mapping=readFileToJson(getGlobalVar('mappingFileName'))
    res = createIndex(es, getGlobalVar('tempIndexName'), mapping)
    if (res != 0):
      print "script results in error"
      sys.exit(1)

    print "scan audit index and manipulate data"
    print "===================================="

    print "start time: ", datetime.now().time()
    updateFieldNames(es, getGlobalVar('matchAllQuery'), getGlobalVar('origIndexName'), getGlobalVar('tempIndexName'), getGlobalVar('addUTC'))
   
    print "re-create original index"
    print "========================="
    res = createIndex(es, getGlobalVar('origIndexName'), mapping)
    if (res != 0):
      print "script results in error"
      sys.exit(1)
   
    print "copy data from temp index to original"
    print "======================================="
    res = copyIndex(es, getGlobalVar('tempIndexName'), getGlobalVar('origIndexName'))
    if (res != 0):
      print "script results in error"
      sys.exit(1)
   
    print "delete temp index"
    print "=================="
    res = deleteIndex(es, getGlobalVar('tempIndexName'))
    if (res != 0):
      print "script results in error"
      sys.exit(1)
   
   
    print "end time: ", datetime.now().time()

   except Exception, error:
      print "An exception was thrown!"
      print str(error)
      return 2
  

if __name__ == "__main__":
        main()





