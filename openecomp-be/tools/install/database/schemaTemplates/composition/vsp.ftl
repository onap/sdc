{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "type": "object",
    "properties": {
        "vlmVersion": {
            "type": "string"
        },
        "licenseAgreement": {
            "type": "string"
        },
        "featureGroups":{
            "type": "array"
        }
    },
    "additionalProperties": false,
    "required": [
        "vlmVersion",
        "licenseAgreement",
        "featureGroups"
    ]
}