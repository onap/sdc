{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "general": {
      "type": "object",
      "properties": {
        "affinityData": {
          "type": "string",
          "enum": [
            "",
            "Affinity",
            "Anti Affinity",
            "None"
          ],
          "default": ""
        },
        "availability": {
          "type": "object",
          "properties": {
            "useAvailabilityZonesForHighAvailability": {
              "type": "boolean",
              "default": false
            }
          },
          "additionalProperties": false
        },
        "regionsData": {
          "type": "object",
          "properties": {
            "multiRegion": {
              "type": "boolean",
              "default": false
            },
            "regions": {
              "type": "array",
              "items": {
                "type": "string",
                "enum": [
                  "",
                  "Alphareta",
                  "Birmingham",
                  "Dallas",
                  "Fairfield CA",
                  "Hayward CA",
                  "Lisle",
                  "Mission",
                  "San Diego",
                  "Secaucus"
                ],
                "default": ""
              }
            }
          },
          "additionalProperties": false
        },
        "storageDataReplication": {
          "type": "object",
          "properties": {
            "storageReplicationAcrossRegion": {
              "type": "boolean",
              "default": false
            },
            "storageReplicationSize": {
              "type": "number",
			  "minimum": 0,
              "maximum": 100,
              "exclusiveMaximum": true
            },
            "storageReplicationFrequency": {
              "type": "number",
              "minimum": 5
            },
            "storageReplicationSource": {
              "type": "string",
              "maxLength": 300
            },
            "storageReplicationDestination": {
              "type": "string",
              "maxLength": 300
            }
          },
          "additionalProperties": false
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}