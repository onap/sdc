/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';

const EmptyListContent = props => {
    let { heatDataExist } = props;
    let displayText = heatDataExist ? 'All Files Are Assigned' : '';
    return (
        <div className="go-to-validation-button-wrapper">
            <div className="all-files-assigned">{i18n(displayText)}</div>
        </div>
    );
};

export default EmptyListContent;
