{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "name": {
      "type": "string"<#if !manual>,
      "enum": [
        "${component.name}"
      ],
      "default": "${component.name}"</#if>
    },
    "displayName": {
      "type": "string"<#if !manual && component.displayName??>,
      "enum": [
        "${component.displayName}"
      ],
      "default": "${component.displayName}"</#if>
    },
    "description": {
      "type": "string"
    }
  },
  "additionalProperties": false,
  "required": [
    "name"<#if !manual && component.displayName??>,
    "displayName"</#if>
  ]
}