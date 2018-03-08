# Copyright © 2016-2018 European Support Limited
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

Feature: Abort Flow - Test Submit

  Background: Init
    Given I want to create a VLM

  Scenario: Test submit if file is just uploaded
    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to make sure this Item has status "Draft"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
    Then I want the following to fail
    When I want to submit this VSP

  Scenario: Test submit if invalid file is uploaded and procced for validation
    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to make sure this Item has status "Draft"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/errorHeat.zip"
    Then I want to process the NetworkPackage file for this VSP
    Then I want the following to fail
    When I want to submit this VSP