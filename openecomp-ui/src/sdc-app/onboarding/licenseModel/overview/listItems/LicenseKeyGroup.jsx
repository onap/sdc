/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import React, { Component } from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {
    extractValue,
    getOperationalScopes
} from '../../licenseKeyGroups/LicenseKeyGroupsConstants.js';
import ArrowCol from './listItemsComponents/ArrowCol.jsx';
import ItemInfo from './listItemsComponents/ItemInfo.jsx';
import IconCol from './listItemsComponents/IconCol.jsx';
import {
    AdditionalDataCol,
    AdditionalDataElement
} from './listItemsComponents/AdditionalDataCol.jsx';

class LicenseKeyGroup extends Component {
    render() {
        let {
            lkgData: { name, description, operationalScope, type },
            isOrphan
        } = this.props;
        return (
            <div
                className={`vlm-list-item vlm-list-item-lkg ${
                    isOrphan ? 'orphan-list-item' : ''
                }`}
                data-test-id="vlm-list-item-lkg">
                {!isOrphan && <ArrowCol />}
                <IconCol className="lkg-icon" text="LKG" />
                <ItemInfo name={name} description={description} />
                <AdditionalDataCol>
                    {operationalScope &&
                        operationalScope.choices && (
                            <AdditionalDataElement
                                className="vlm-list-item-operational-scope"
                                name={i18n('Operational Scope')}
                                value={getOperationalScopes(operationalScope)}
                            />
                        )}
                    <AdditionalDataElement
                        className="vlm-list-item-group-type"
                        name={i18n('Type')}
                        value={extractValue(type)}
                    />
                </AdditionalDataCol>
            </div>
        );
    }
}

export default LicenseKeyGroup;
