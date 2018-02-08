{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "vmSizing": {
      "type": "object",
      "properties": {
        "numOfCPUs": {
          "type": "number",
          "minimum": 0,
          "exclusiveMinimum": true,
          "maximum": 16,
          "default": 2
        },
        "fileSystemSizeGB": {
          "type": "number",
          "minimum": 0,
          "exclusiveMinimum": true,
          "default": 5
        },
        "persistentStorageVolumeSize": {
          "type": "number",
          "minimum": 0,
          "exclusiveMinimum": true
        },
        "ioOperationsPerSec": {
          "type": "number",
          "minimum": 0,
          "exclusiveMinimum": true
        },
        "cpuOverSubscriptionRatio": {
          "type": "string",
          "enum": [
            "1:1",
            "4:1",
            "16:1"
          ],
          "default": "4:1"
        },
        "memoryRAM": {
          "type": "string",
          "enum": [
            "1 GB",
            "2 GB",
            "4 GB",
            "8 GB",
            "16 GB",
            "32 GB"
          ],
          "default": "1 GB"
        }
      },
      "additionalProperties": false
    }
  }
}