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

Feature: Heat Example File
  Scenario: Test with update for heat file and check for validation warning
    Given I want to create a VLM

    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to make sure this Item has status "Draft"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
    And I want to process the NetworkPackage file for this VSP

    When I want to download the NetworkPackage for this VSP to path "resources/downloads/base_mux.zip"
    Then I want to check property "data[0].file" for value "CB_BASE.yaml"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX_with_no_base.zip"
    Then I want to process the NetworkPackage file for this VSP
    Then I want to check property "errors['CB_MUX.yaml'][0].level" for value "WARNING"