{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "type": "object",
    "properties": {
        "licensing version": {
            "type": "string"
        },
        "license agreement": {
            "type": "string"
        },
        "feature groups":{
            "type": "string",
            "enum": [
                     <#if featureGroupIds??>
                         <#list featureGroupIds as featureGroupId>"${featureGroupId}"<#sep>,</#list>
                     </#if>
                    ]
        }
    },
    "additionalProperties": false,
    "required": [
        "licensing version",
        "license agreement",
        "feature groups"
    ]
}