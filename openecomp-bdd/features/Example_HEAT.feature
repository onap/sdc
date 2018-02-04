Feature: Heat Example File
  Scenario: Test with update for heat file and check for validation warning

    #Given Item "c99b775cc0764746b15a32b728c10402" and version Id "f044f9e265ac46c2882cb14c4b1732a5"
    Given I want to create a VLM

    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to make sure this Item has status "Draft"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
    And I want to process the NetworkPackage file for this VSP

    When I want to download the NetworkPackage for this VSP to path "resources/downloads/base_mux.zip"
    Then I want to check property "data[0].file" for value "CB_BASE.yaml"

    Then I want to set the input data to:
    """
    {"modules":[{"name":"module_1","isBase":false,"yaml":"CB_BASE.yaml"}],"unassigned":[],"artifacts":["MUX_Parameters.env","CB_MUX.yaml"],"nested":[]}
    """
    Then I want to update for path "/vendor-software-products/{item.id}/versions/{item.versionId}/orchestration-template-candidate/manifest" with the input data from the context
    Then I want to process the NetworkPackage file for this VSP
    Then I want to check property "errors['CB_MUX.yaml'][0].level" for value "WARNING"