{
"$schema": "http://json-schema.org/draft-04/schema#",
"type": "object",
"properties": {
    "format": {
        "type": "string",
        "enum": [
            "aki","ami","ari","iso","qcow2","raw", "vdi","vhd","vmdk"
        ]
    },
    "version": {
        "type": "string",
        "minLength": 1
    },
    "md5": {
        "type": "string"
    }
},
    "additionalProperties": false,
    "required": [
     "version"
    ]
}