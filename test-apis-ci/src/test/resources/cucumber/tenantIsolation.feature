Feature: Tenant Isolation Feature

  #Get individual external AT&T Certificate API
  Scenario Outline: Create operational Envrionment
    Given AAI returns <retcode> and aai_body contains <tenant> and <workload>
    When The Number Of Operational Envrinoments that created is <numberOfEnvs> and Records are added with data <recordData>
    Then The Number Of Environment is <numberOfEnvs> with status <recordStatus>
    #Verify Succesfull A&AI Call
    And Operational Environment record contains tenant field <isTenantExist>
    And Operational Environment record contains workload field <isWorkloadtExist>
    #Verify Succesfull DME Call
    And Operational Environment record contains UEB Address field <isUebAddressExist>

    Examples: 
      | retcode | tenant | workload        | recordData                                     | numberOfEnvs | recordStatus | isTenantExist | isWorkloadtExist | isUebAddressExist |
      |     200 | Test   | VNF_Development | {"status": "failed", "last_modified_delta": 0} |            1 | completed    | true          | true             | true              |

  Scenario Outline: Distribute To Operational Environment
    Given ASDC Address is 127.0.0.1
    And The number of complete environments is <numberOfEnvs>
    #Start SImulator for each envrionment
    And The number of artifacts each Simulator downloads from a service is <numberOfArtifactsDownloaded>
    #5 (3 days) - run jar
    And MSO-WD Simulators Started with topic name MSHITRIT-D2D
    And ASDC Contains the following services <ListOfServicesUUID>
    # (1.5 days)
    And MSO Final Distribution Simulator is UP
    #4 (Sends 75 request )(2 days)
    When Distribution Requests are Sent By MSO
    Then All MSO-WD Simulators Sent The Distribution Complete Notifications
    And All Artifacts were downloaded by Simulators
    #6
    And MSO Final Distribution Recieved Correct Number Of Request

    Examples: 
      | numberOfEnvs | numberOfArtifactsDownloaded | ListOfServicesUUID                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
      #|            5 |                           8 | 74e05aac-48d5-4642-8dc1-dc1ddeb5c0f5,fa86100a-409b-4310-835c-e401896181a5,8dbc41cc-2076-49c0-b95b-cb37b1539367,0c7eb2ac-06c0-41e1-b635-825037942e28,840123d1-b9d6-4036-8e94-471b268026bf,74331473-1eb0-4628-84b8-ab47948f7023, 3277af19-d152-4d94-a4d0-44ad6f226b52, e553c12b-b67b-4d66-a6a9-8676d5e0becf, f648052e-3207-4eb2-9336-29f2fe9df618, 535c6ad3-ad2b-429f-ab9b-b654cf647c86,611c764d-8862-4f09-a8e0-1622f8d97ce9, b5df0774-2445-4daf-b893-61c014652145, 01481626-b58a-49b5-ba1e-74eb8508ed15, 9e17345a-d11d-4219-b4e3-e86cfdb51d07, 5ff40fbc-f4e9-45f1-85de-0a04b8a5d60c |
      |            2 |                           2 | 74e05aac-48d5-4642-8dc1-dc1ddeb5c0f5,fa86100a-409b-4310-835c-e401896181a5                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
