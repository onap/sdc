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
import DetailsCatalogView from '../DetailsCatalogView.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { tabsMapping } from 'sdc-app/onboarding/onboard/OnboardConstants.js';

const WorkspaceView = props => {
    let {
        onAddLicenseModelClick,
        users,
        onAddSoftwareProductClick,
        onSelectLicenseModel,
        onSelectSoftwareProduct,
        searchValue,
        onMigrate,
        filteredItems: { vspList, vlmList }
    } = props;

    return (
        <div className="catalog-wrapper workspace-view">
            <div className="catalog-header workspace-header">
                {i18n('WORKSPACE')}
            </div>
            <DetailsCatalogView
                VLMList={vlmList}
                VSPList={vspList}
                users={users}
                onAddVLM={onAddLicenseModelClick}
                onAddVSP={onAddSoftwareProductClick}
                onSelectVLM={(item, users) =>
                    onSelectLicenseModel(item, users, tabsMapping.WORKSPACE)
                }
                onSelectVSP={(item, users) =>
                    onSelectSoftwareProduct(item, users, tabsMapping.WORKSPACE)
                }
                onMigrate={onMigrate}
                filter={searchValue}
            />
        </div>
    );
};

export default WorkspaceView;
