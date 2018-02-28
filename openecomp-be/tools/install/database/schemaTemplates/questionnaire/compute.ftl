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
            "3 GB",
            "4 GB",
            "5 GB",
            "6 GB",
            "7 GB",
            "8 GB",
            "9 GB",
            "10 GB",
            "11 GB",
            "12 GB",
            "13 GB",
            "14 GB",
            "15 GB",
            "16 GB",
            "17 GB",
            "18 GB",
            "19 GB",
            "20 GB",
            "21 GB",
            "22 GB",
            "23 GB",
            "24 GB",
            "25 GB",
            "26 GB",
            "27 GB",
            "28 GB",
            "29 GB",
            "30 GB",
            "31 GB",
            "32 GB"

            ],
          "default": "1 GB"
        }
      },
      "additionalProperties": false
    }
  }
}