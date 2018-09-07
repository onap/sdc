Feature: Interface Operation Feature

 Background: Init
  Given I want to create a Service

 Scenario: Test InterfaceOperation CRUD
   #Create Operations
  When I want to create an Operation
  Then I want to check property "uniqueId" exists
  And  I want to create an Operation
  Then I want to check property "uniqueId" exists
  And  I want to create an Operation
  Then I want to check property "uniqueId" exists
  When I want to create an Operation with input output
  Then I want to check property "uniqueId" exists

   #List All Operations
  When I want to list Operations

   #Get Operation By OperationId
  When I want to get an Operation by Id
  Then I want to check property "uniqueId" exists

   #Update Operation
  When I want to update an Operation
  Then I want to check property "uniqueId" exists

   #Delete Operation
  When I want to delete an Operation

   #Checkin
  When I want to checkin this component
  Then I want to check property "lifecycleState" for value "NOT_CERTIFIED_CHECKIN"

   #Submit
  Then I want to submit this component
  And I want to check property "lifecycleState" for value "READY_FOR_CERTIFICATION"
