Feature: Tenant Isolation Feature

  Scenario Outline: Recieve Notification Create Environment
    #Example {"operationalEnvironmentId": "28122015552391", "operationalEnvironmentType": "ECOMP","action": "Create" }
    Given Dmaap consumer recieved notification with fields <notificationFields>
    #UP, DOWN
    And AAI service status is <AAIServiceStatus> and Tenant returned is <tenant> and worload returned is <worload>
    And AFT_DME service status is <AftDmeServiceStatus>
    And UEB service status is <UebServiceStatus>
    And Cassandra service status is <CassandraServiceStatus>
    #NOT_RELEVANT, NOT_FOUND, FOUND_IN_PROGRESS (Status In Progress), FOUND_COMPLETED (Status Complete), FOUND_FAILED (Status Failed)
    And Record status is <recordStatus>
    ####################################################################################
    When handle message is activated
    ####################################################################################
    Then handle message activates validation of eventType <isEventTypeValidated>
    And trying to write message to audit log and table <isAuditUpdated>
    And handle message activates validation of action <isActionValidated>
    And handle message activates validation of state <isStateValidated>
    And trying to save in-progress record <isSaveActivated>
    And trying to get environment info from A&AI API <isAAIActivated>
    And trying to retrieve Ueb Addresses From AftDme <isAftDmeActivated>
    And trying to create Ueb keys <isCreateKeysActivated>
    And trying to create Ueb Topics <isCreateTopicsActivated>
    And handle message finished successfully <isSuccessfull>

    Examples: 
      | notificationFields                                              | AAIServiceStatus | tenant | worload       | AftDmeServiceStatus | UebServiceStatus | CassandraServiceStatus | recordStatus      | isEventTypeValidated | isAuditUpdated | isActionValidated | isStateValidated | isSaveActivated | isAAIActivated | isAftDmeActivated | isCreateKeysActivated | isCreateTopicsActivated | isSuccessfull |
      | {"operationalEnvironmentType": "NON-ECOMP","action": "Create" } | UP               | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | FOUND_COMPLETED   | true                 | true           | false             | false            | false           | false          | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Delete" }     | UP               | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | FOUND_COMPLETED   | true                 | false          | true              | false            | false           | false          | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | DOWN                   | NOT_RELEVANT      | true                 | false          | true              | true             | false           | false          | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | FOUND_IN_PROGRESS | true                 | false          | true              | true             | false           | false          | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               |        | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | FOUND_FAILED      | true                 | false          | true              | true             | true            | true           | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               | TEST   |               | DOWN                | DOWN             | UP                     | FOUND_FAILED      | true                 | false          | true              | true             | true            | true           | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               |        |               | DOWN                | DOWN             | UP                     | FOUND_FAILED      | true                 | false          | true              | true             | true            | true           | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | DOWN             | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | FOUND_COMPLETED   | true                 | false          | true              | true             | true            | true           | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | DOWN             | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | FOUND_FAILED      | true                 | false          | true              | true             | true            | true           | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Update" }     | DOWN             | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | FOUND_COMPLETED   | true                 | false          | true              | true             | true            | true           | false             | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               | TEST   | ECOMP_E2E-IST | DOWN                | DOWN             | UP                     | NOT_FOUND         | true                 | false          | true              | true             | true            | true           | true              | false                 | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               | TEST   | ECOMP_E2E-IST | UP                  | DOWN             | UP                     | NOT_FOUND         | true                 | false          | true              | true             | true            | true           | true              | true                  | false                   | false         |
      | {"operationalEnvironmentType": "ECOMP","action": "Create" }     | UP               | TEST   | ECOMP_E2E-IST | UP                  | UP               | UP                     | NOT_FOUND         | true                 | false          | true              | true             | true            | true           | true              | true                  | true                    | true          |
      | {"operationalEnvironmentType": "ECOMP","action": "Update" }     | UP               | TEST   | ECOMP_E2E-IST | UP                  | UP               | UP                     | NOT_FOUND         | true                 | false          | true              | true             | true            | true           | true              | true                  | true                    | true          |
