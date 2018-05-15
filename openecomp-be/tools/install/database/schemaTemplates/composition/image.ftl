{
"$schema": "http://json-schema.org/draft-04/schema#",
"type": "object",
"properties": {
    "fileName": {
         "type": "string" <#if !manual>,
            "enum":["${image.fileName}"]
         </#if>
    },
    "description": {
         "type": "string"
    }
 },
    "additionalProperties": false
}
