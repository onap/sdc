# Copyright © 2018 European Support Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

Feature: User Feedback - Enhance EP & LKG to include MRN

  Scenario: Test Feature Group and Entitlement Pool with toggle off

    When I want to set all Togglz to be "false"

    Then I want to create a VLM
    Then I want to make sure this Item has status "Draft"

    When I want to get path "/items/{item.id}/versions/{item.versionId}/revisions"
    Then I want to check property "listCount" for value 1

    Then I want to create input data
    Then I want to update the input property "name" with value "FG_01"
    Then I want to update the input property "description" with value "FG without MRN"
    Then I want to update the input property "partNumber" with value "999"
    Then I want the following to fail
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/feature-groups" with the input data from the context

    When I want to create input data
    Then I want to update the input property "name" with value "EP_01"
    Then I want to update the input property "description" with value "EP without MRN"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/entitlement-pools" with the input data from the context

  Scenario: Test Feature Group and Entitlement Pool with toggle on

    When I want to set all Togglz to be "true"

    Then I want to create a VLM
    Then I want to make sure this Item has status "Draft"

    When I want to get path "/items/{item.id}/versions/{item.versionId}/revisions"
    Then I want to check property "listCount" for value 1

    Then I want to create input data
    Then I want to update the input property "name" with value "FG_01"
    Then I want to update the input property "description" with value "FG without MRN"
    Then I want to update the input property "partNumber" with value "999"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/feature-groups" with the input data from the context

    When I want to create input data
    Then I want to update the input property "name" with value "EP_01"
    Then I want to update the input property "description" with value "EP without MRN"
    Then I want the following to fail
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/entitlement-pools" with the input data from the context


    When I want to create input data
    Then I want to update the input property "name" with value "EP_02"
    Then I want to update the input property "description" with value "EP with MRN"
    Then I want to update the input property "manufacturerReferenceNumber" with value "12345"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/entitlement-pools" with the input data from the context
    Then I want to get path "/vendor-license-models/{item.id}/versions/{item.versionId}/entitlement-pools/{responseData.value}"
    Then I want to check property "manufacturerReferenceNumber" for value "12345"






