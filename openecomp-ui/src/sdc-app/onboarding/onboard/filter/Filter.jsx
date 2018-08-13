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
import { tabsMapping as onboardTabsMapping } from '../OnboardConstants.js';
import { actionTypes } from './FilterConstants.js';

import Panel from 'sdc-ui/lib/react/Panel.js';
import {
    ItemStatus,
    ByVendorView,
    EntityType,
    Permissions,
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
        onDataChanged: deltaData => {
            dispatch({
                type: actionTypes.FILTER_DATA_CHANGED,
                deltaData
            });
        }
    };
};

const Filter = ({ onDataChanged, data, activeTab }) => {
    return (
        <Panel className="catalog-filter">
            <ItemStatus data={data} onDataChanged={onDataChanged} />
            <EntityType data={data} onDataChanged={onDataChanged} />
            <Permissions data={data} onDataChanged={onDataChanged} />
            <OnboardingProcedure data={data} onDataChanged={onDataChanged} />
            {activeTab === onboardTabsMapping.CATALOG && (
                <ByVendorView data={data} onDataChanged={onDataChanged} />
            )}
        </Panel>
    );
};

Filter.propTypes = {
    onDataChanged: PropTypes.func,
    data: PropTypes.object,
    activeTab: PropTypes.number
};

export default connect(mapStateToProps, mapActionsToProps)(Filter);
