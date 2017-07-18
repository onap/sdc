{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "enum": [
        "${nic.name}"
      ],
      "default": "${nic.name}"
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
  </#if>
  <#elseif manual><#if nic.networkId??>,
  "networkId": {
  "type": "string",
  "enum": [
  "${nic.networkId}"
  ],
  "default": "${nic.networkId}"
  }
  </#if>
<#else>,
    "networkId": {
      "type": "string",
      "enum": [<#list networkIds as networkId>
        "${networkId}"<#sep>,</#list>
      ]
    }
  </#if>,
    "networkDescription": {
      "type": "string"
    },
    "networkType": {
      "type": "string",
      "enum": [
        "${nic.networkType}"
      ],
      "default": "${nic.networkType}"
    }
  },

  "additionalProperties": false,
  "required": [
    "name"
  ]
}