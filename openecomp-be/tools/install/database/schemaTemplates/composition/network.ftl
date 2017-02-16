{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "name": {
      "type": "string"<#if !manual>,
      "enum": [
        "${network.name}"
      ],
      "default": "${network.name}"</#if>
    },
    "dhcp": {
      "type": "boolean"<#if !manual>,
      "enum": [
      ${network.dhcp?c}
      ],
      "default": ${network.dhcp?c}</#if>
    }
  },
  "additionalProperties": false,
  "required": [
    "name",
    "dhcp"
  ]
}