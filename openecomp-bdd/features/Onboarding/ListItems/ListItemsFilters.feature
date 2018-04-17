Feature: List items with various filters

  Background: Init - create various items in order to test list items filters
    Given I want to set the user to "cs0008"
    Given I want to create a VLM
    Then I want to copy to property "noPermissionDraftVlmId" from response data path "itemId"

    Given I want to create a VLM
    Then I want to copy to property "noPermissionCertifiedVlmId" from response data path "itemId"
    And I want to submit this VLM

    Given I want to set the user to "mb1001"

    Given I want to create a VLM
    Then I want to copy to property "draftVlmId" from response data path "itemId"

    Given I want to create a VLM
    Then I want to copy to property "certifiedVlmId" from response data path "itemId"
    And I want to submit this VLM

    Given I want to create a VLM
    Then I want to copy to property "archivedDraftVlmId" from response data path "itemId"
    And I want to archive this item

    Given I want to create a VLM
    Then I want to copy to property "archivedCertifiedVlmId" from response data path "itemId"
    And I want to submit this VLM
    And I want to archive this item

    Given I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to copy to property "npDraftVspId" from response data path "itemId"

    Given I want to create a VSP with onboarding type "Manual"
    Then I want to copy to property "manualDraftVspId" from response data path "itemId"

  Scenario: List workspace items - active, draft, editable by the user
    When I want to get path "/items?itemStatus=ACTIVE&versionStatus=Draft&permission=Owner,Contributor"

    Then I want to check that element in the response list with "id" equals to value of saved property "draftVlmId" exists
    And I want to check that element in the response list with "id" equals to value of saved property "npDraftVspId" exists
    And I want to check that element in the response list with "id" equals to value of saved property "manualDraftVspId" exists

    But I want to check that element in the response list with "id" equals to value of saved property "noPermissionDraftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "noPermissionCertifiedVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "certifiedVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "archivedDraftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "archivedCertifiedVlmId" does not exist

  Scenario: List catalog items - active, certified
    When I want to get path "/items?itemStatus=ACTIVE&versionStatus=Certified"

    Then I want to check that element in the response list with "id" equals to value of saved property "noPermissionCertifiedVlmId" exists
    And I want to check that element in the response list with "id" equals to value of saved property "certifiedVlmId" exists

    But I want to check that element in the response list with "id" equals to value of saved property "draftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "npDraftVspId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "manualDraftVspId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "noPermissionDraftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "archivedDraftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "archivedCertifiedVlmId" does not exist

  Scenario: List archived certified items
    When I want to get path "/items?itemStatus=ARCHIVED&versionStatus=Certified"

    Then I want to check that element in the response list with "id" equals to value of saved property "archivedCertifiedVlmId" exists

    But I want to check that element in the response list with "id" equals to value of saved property "archivedDraftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "noPermissionCertifiedVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "certifiedVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "draftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "npDraftVspId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "manualDraftVspId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "noPermissionDraftVlmId" does not exist

  Scenario: List only active draft manual vsps + vlms owned by the user
    When I want to get path "/items?itemStatus=ACTIVE&versionStatus=Draft&onboardingMethod=Manual&permission=Owner"

    Then I want to check that element in the response list with "id" equals to value of saved property "draftVlmId" exists
    And I want to check that element in the response list with "id" equals to value of saved property "manualDraftVspId" exists

    But I want to check that element in the response list with "id" equals to value of saved property "npDraftVspId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "noPermissionDraftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "noPermissionCertifiedVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "certifiedVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "archivedDraftVlmId" does not exist
    And I want to check that element in the response list with "id" equals to value of saved property "archivedCertifiedVlmId" does not exist
