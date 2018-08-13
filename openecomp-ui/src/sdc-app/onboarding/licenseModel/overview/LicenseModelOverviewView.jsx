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
import PropTypes from 'prop-types';
import classNames from 'classnames';

import { selectedButton } from './LicenseModelOverviewConstants.js';

import SummaryView from './SummaryView.jsx';
import VLMListView from './VLMListView.jsx';
import ListButtons from './summary/ListButtons.jsx';

class LicenseModelOverviewView extends React.Component {
    static propTypes = {
        isDisplayModal: PropTypes.bool,
        isReadOnlyMode: PropTypes.bool,
        licenseModelId: PropTypes.string,
        licensingDataList: PropTypes.array,
        orphanDataList: PropTypes.array,
        modalHeader: PropTypes.string,
        selectedTab: PropTypes.string,
        onTabSelect: PropTypes.func,
        onCallVCAction: PropTypes.func,
        onClose: PropTypes.func
    };

    render() {
        let {
            licensingDataList,
            selectedTab,
            onTabSelect,
            orphanDataList,
            isReadOnlyMode
        } = this.props;
        let selectedInUse = selectedTab !== selectedButton.NOT_IN_USE;
        let dataList = selectedInUse ? licensingDataList : orphanDataList;
        return (
            <div className="license-model-overview">
                <SummaryView isReadOnlyMode={isReadOnlyMode} />
                <div
                    className={classNames(
                        'overview-list-section ',
                        !selectedInUse ? 'overview-list-orphans' : ''
                    )}>
                    <div className="vlm-list-tab-panel">
                        <ListButtons
                            onTabSelect={onTabSelect}
                            selectedTab={selectedTab}
                            hasOrphans={orphanDataList.length > 0}
                            hasLicensing={licensingDataList.length > 0}
                        />
                    </div>
                    <VLMListView
                        licensingDataList={dataList}
                        showInUse={selectedInUse}
                    />
                </div>
            </div>
        );
    }
}

export default LicenseModelOverviewView;
