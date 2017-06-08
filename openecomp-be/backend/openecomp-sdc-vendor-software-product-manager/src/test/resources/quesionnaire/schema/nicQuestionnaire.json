{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "protocols": {
      "type": "object",
      "properties": {
        "protocols": {
          "type": "array",
          "items": {
            "type": "string",
            "enum": [
              "",
              "TCP",
              "UDP",
              "SCTP",
              "IPsec"
            ],
            "default": ""
          },
          "minItems": 1
        },
        "protocolWithHighestTrafficProfile": {
          "$ref": "#/properties/protocols/properties/protocols/items"
        }
      },
      "additionalProperties": false
    },
    "ipConfiguration": {
      "type": "object",
      "properties": {
        "ipv4Required": {
          "type": "boolean",
          "default": true
        },
        "ipv6Required": {
          "type": "boolean",
          "default": false
        }
      },
      "additionalProperties": false
    },
    "network": {
      "type": "object",
      "properties": {
        "networkDescription": {
          "type": "string",
          "pattern": "[A-Za-z]+",
          "maxLength": 300
        }
      },
      "additionalProperties": false
    },
    "sizing": {
      "type": "object",
      "definitions": {
        "peakAndAvg": {
          "type": "object",
          "properties": {
            "peak": {
              "type": "number"
            },
            "avg": {
              "type": "number"
            }
          },
          "additionalProperties": false
        },
        "packetsAndBytes": {
          "type": "object",
          "properties": {
            "packets": {
              "$ref": "#/properties/sizing/definitions/peakAndAvg"
            },
            "bytes": {
              "$ref": "#/properties/sizing/definitions/peakAndAvg"
            }
          },
          "additionalProperties": false
        }
      },
      "properties": {
        "describeQualityOfService": {
          "type": "string"
        },
        "inflowTrafficPerSecond": {
          "$ref": "#/properties/sizing/definitions/packetsAndBytes"
        },
        "outflowTrafficPerSecond": {
          "$ref": "#/properties/sizing/definitions/packetsAndBytes"
        },
        "flowLength": {
          "$ref": "#/properties/sizing/definitions/packetsAndBytes"
        },
        "acceptableJitter": {
          "type": "object",
          "properties": {
            "mean": {
              "type": "number"
            },
            "max": {
              "type": "number"
            },
            "variable": {
              "type": "number"
            }
          },
          "additionalProperties": false
        },
        "acceptablePacketLoss": {
          "type": "number",
          "minimum": 0,
          "maximum": 100
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}