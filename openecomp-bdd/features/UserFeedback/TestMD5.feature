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

Feature: User Feedback - Test MD5

  Scenario: Test MD5

    When I want to create a VLM
    Then I want to copy to property "licensingVersion" from response data path "version.id"

    #create feature group
    When I want to create input data
    Then I want to update the input property "name" with value "FG1"
    Then I want to update the input property "partNumber" with value "999"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/feature-groups" with the input data from the context
    Then I want to copy to property "featureGroupId" from response data path "value"

    #create license agreements
    When I want to create input data
    Then I want to update the input property "name" with value "LA"
    Then I want to update the input property "licenseTerm.choice" with value "Unlimited"
    Then I want to update the input property "addedFeatureGroupsIds[0]" from property "featureGroupId"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-agreements" with the input data from the context
    Then I want to copy to property "licenseAgreement" from response data path "value"

    When I want to create a VSP with onboarding type Manual
    Then I want to make sure this Item has status "Draft"

    When I want to add a component

    #create image
    When I want to create input data
    Then I want to update the input property "fileName" with value "Image_1"
    Then I want to create for path "/vendor-software-products/{vsp.id}/versions/{vsp.versionId}/components/{componentId}/images" with the input data from the context

    #update image questionnaire
    Then I want to get the questionnaire for this path "/vendor-software-products/{vsp.id}/versions/{vsp.versionId}/components/{componentId}/images/{responseData.id}/questionnaire"
    And I want to update this questionnaire with value "md5" for property "5555555555555555555555555555555555555555555555555555555555555"
    And I want to update this questionnaire with value "version" for property "1"
    And I want to update this questionnaire

    #create compute flavor
    When I want to create input data
    Then I want to update the input property "name" with value "ComputeFlavor1"
    When I want to create for path "/vendor-software-products/{vsp.id}/versions/{vsp.versionId}/components/{componentId}/compute-flavors" with the input data from the context
    Then I want to copy to property "computeFlavorId" from response data path "id"

    #create deployment flavor with Component , compute flavor associations
    When I want to create input data
    Then I want to update the input property "model" with value "DeploymentFlavorModel"
    Then I want to update the input property "componentComputeAssociations[0].componentId" from property "componentId"
    Then I want to update the input property "componentComputeAssociations[0].computeFlavorId" from property "computeFlavorId"
    Then I want to update the input property "featureGroupId" from property "featureGroupId"
    Then I want to create for path "/vendor-software-products/{vsp.id}/versions/{vsp.versionId}/deployment-flavors" with the input data from the context

    Then I want the following to fail
    When I want to submit this VSP
