/*-
* ============LICENSE_START=======================================================
* SDC
* =========================================================
* Copyright (C) 2020 Nokia. All rights reserved.
* =========================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=====================================
*/

import {InputFEModel} from "./input-fe-model";

describe('InputFEModel', () => {
  [
    { inputValue: undefined, expectedValue: null },
    { inputValue: null, expectedValue: null },
    { inputValue: ' this is a test ', expectedValue: 'this is a test' }
    { inputValue: '       this is another test            ', expectedValue: 'this is another test' }
  ].forEach(({inputValue, expectedValue}) => {
      describe(`input is ${inputValue}`, () => {
        it(`should return ${expectedValue}`, () => {
          const inputFeModel = new InputFEModel({} as any);
          inputFeModel.updateDefaultValueObj(inputValue, true);
          expect(inputFeModel.getJSONDefaultValue()).toBe(expectedValue);
        });
      });
  });
});
