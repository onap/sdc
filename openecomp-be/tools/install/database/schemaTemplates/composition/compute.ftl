{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "name": {
      "type": "string" <#if !manual>,
       "enum": [
       "${compute.name}"
        ],
     "default": "${compute.name}"</#if>
    },
    "description": {
      "type": "string",
      "maxLength": 300
    }
  }
}