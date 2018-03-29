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

import { connect } from 'react-redux';
import React from 'react';
import PropTypes from 'prop-types';
import featureToggle from 'sdc-app/features/featureToggle.js';
import { featureToggleNames } from 'sdc-app/features/FeaturesConstants.js';
import { tabsMapping as onboardTabsMapping } from '../OnboardConstants.js';
import FilterActionHelper from './FilterActionHelper.js';

import {
    ItemStatus,
    ByVendorView,
    EntityType,
    Role,
    OnboardingProcedure
} from './FilterComponents.jsx';

const mapStateToProps = ({ onboard: { filter, activeTab } }) => {
    return {
        data: filter,
        activeTab
    };
};

const mapActionsToProps = dispatch => {
    return {
        onDataChanged: (deltaData, data) =>
            FilterActionHelper.onDataChanged(dispatch, { deltaData, data })
    };
};

const Filter = ({ onDataChanged, data, activeTab }) => {
    const dataWithTab = { ...data, activeTab };
    return (
        <div className="catalog-filter">
            <ItemStatus data={dataWithTab} onDataChanged={onDataChanged} />
            {/*activeTab === onboardTabsMapping.CATALOG && (
                <ItemStatus data={dataWithTab} onDataChanged={onDataChanged} />
            )*/}
            {activeTab === onboardTabsMapping.CATALOG && (
                <ByVendorView
                    data={dataWithTab}
                    onDataChanged={onDataChanged}
                />
            )}
            <EntityType data={dataWithTab} onDataChanged={onDataChanged} />
            <Role data={dataWithTab} onDataChanged={onDataChanged} />
            <OnboardingProcedure
                data={dataWithTab}
                onDataChanged={onDataChanged}
            />
        </div>
    );
};

Filter.PropTypes = {
    onDataChanged: PropTypes.func,
    data: PropTypes.object
};

export default featureToggle(featureToggleNames.FILTER)(
    connect(mapStateToProps, mapActionsToProps)(Filter)
);
