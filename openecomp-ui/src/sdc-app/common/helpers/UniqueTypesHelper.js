/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import { featureToggleNames } from 'sdc-app/features/FeaturesConstants.js';
import { restToggle } from 'sdc-app/features/featureToggleUtils.js';

const itemTypesMapper = {
    vsp: 'VspName',
    vlm: 'VlmName'
};

function baseUrl() {
    const restPrefix = Configuration.get('restPrefix');
    return `${restPrefix}/v1.0/unique-types/`;
}

function uniqueValue(type, value) {
    return restToggle({
        restFunction: () =>
            RestAPIUtil.fetch(`${baseUrl()}${type}/values/${value}`),
        featureName: featureToggleNames.FILTER,
        mockResult: { occupied: false }
    });
}

export default {
    async isNameUnique(
        dispatch,
        { value, name, formName, errorText, itemType }
    ) {
        const { occupied } = await uniqueValue(
            itemTypesMapper[itemType],
            value
        );
        const validation = occupied
            ? {
                  isValid: false,
                  errorText
              }
            : { isValid: true, errorText: '' };

        let deltaData = {};
        deltaData[name] = value;
        let customValidations = {};
        customValidations[name] = () => validation;

        ValidationHelper.dataChanged(dispatch, {
            deltaData,
            formName,
            customValidations
        });
    }
};
