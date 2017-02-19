{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "name": {
      "type": "string"<#if !manual>,
      "enum": [
        "${nic.name}"
      ],
      "default": "${nic.name}"
    </#if>
    },
    "description": {
      "type": "string"
    }<#if !manual><#if nic.networkId??>,
    "networkId": {
      "type": "string",
      "enum": [
        "${nic.networkId}"
      ],
      "default": "${nic.networkId}"
    }
  </#if><#else>,
    "networkId": {
      "type": "string",
      "enum": [<#list networkIds as networkId>
        "${networkId}"<#sep>,</#list>
      ]
    }
  </#if>
  },
  "additionalProperties": false,
  "required": [
    "name"
  ]
}