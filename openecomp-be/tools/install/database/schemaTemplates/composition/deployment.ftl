{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "type": "object",
    "properties": {
      "model": {
        "type": "string",
         "maxLength": 30,
         "pattern": "^[A-Za-z0-9_,-]*$"
        },
      "description": {
        "type": "string",

         "maxLength": 300
        },
      "featureGroupId":{
        "type": "string",
        "enum": [<#if featureGroupIds??> <#list featureGroupIds as featureGroupId>
        "${featureGroupId}"<#sep>,</#list> </#if>
          ]
      },
     "componentComputeAssociations": {
       "type": "array",
        "properties": {
            "vfcid": {
               "type": "string"
             },
            "computeFlavorid": {
               "type": "string"
             }
        },
     "additionalProperties": false
      }

    },
   "additionalProperties": false,
   "required": [
      "model"
    ]
}