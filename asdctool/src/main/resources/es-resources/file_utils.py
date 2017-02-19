import itertools
import string
import json
from datetime import datetime
from elasticsearch import Elasticsearch
import elasticsearch
import elasticsearch.helpers
from elasticsearch.client import IndicesClient
import sys, os

def readFileToJson(fileName):
   print "read file ", fileName
   fo=open(fileName)
   try:
     json_mapping=json.load(fo)
     fo.close()
   except ValueError:
     print "error in reading file " , fileName
     fo.close()
     raise
   return json_mapping
