globalVars={
  "host": "127.0.0.1",
  "origIndexName": "temp_audit",
  "tempIndexName": "temp_audit2",
  "addUTC": False,
  "mappingFileName": "auditMappings.txt",
  "matchAllQuery":{"query": {"match_all": {}}}
}

def getGlobalVar(propertyName):
  return globalVars.get(propertyName)