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
import isEqual from 'lodash/isEqual.js';
import React from 'react';
import PropTypes from 'prop-types';
import { catalogItemTypes } from './onboardingCatalog/OnboardingCatalogConstants.js';
import { filterCatalogItemsByType } from './onboardingCatalog/OnboardingCatalogUtils.js';
import CatalogList from './CatalogList.jsx';
import CatalogItemDetails from './CatalogItemDetails.jsx';

function renderCatalogItems({
    items,
    type,
    filter,
    onSelect,
    onMigrate,
    users,
    activeTabName
}) {
    const filteredValue = filter[activeTabName];
    const filteredItems = items.length
        ? filterCatalogItemsByType({ items, filter: filteredValue })
        : [];
    return filteredItems.map(item => (
        <CatalogItemDetails
            key={item.id}
            catalogItemData={item}
            catalogItemTypeClass={type}
            onMigrate={onMigrate}
            onSelect={() => onSelect(item, users)}
        />
    ));
}

class DetailsCatalogView extends React.Component {
    static propTypes = {
        VLMList: PropTypes.array,
        VSPList: PropTypes.array,
        onSelectVLM: PropTypes.func.isRequired,
        onSelectVSP: PropTypes.func.isRequired,
        onAddVLM: PropTypes.func,
        onAddVSP: PropTypes.func,
        filter: PropTypes.object.isRequired,
        activeTabName: PropTypes.string
    };

    shouldComponentUpdate(nextProps) {
        const shouldUpdate =
            isEqual(nextProps.VLMList, this.props.VLMList) &&
            isEqual(nextProps.VSPList, this.props.VSPList) &&
            isEqual(nextProps.users, this.props.users) &&
            isEqual(nextProps.filter, this.props.filter);
        return !shouldUpdate;
    }
    render() {
        let {
            VLMList,
            VSPList,
            users,
            onAddVSP,
            onAddVLM,
            onSelectVLM,
            onSelectVSP,
            filter,
            onMigrate,
            activeTabName
        } = this.props;
        return (
            <CatalogList onAddVLM={onAddVLM} onAddVSP={onAddVSP}>
                {renderCatalogItems({
                    items: VLMList,
                    type: catalogItemTypes.LICENSE_MODEL,
                    filter,
                    onSelect: onSelectVLM,
                    onMigrate,
                    users,
                    activeTabName
                })}
                {renderCatalogItems({
                    items: VSPList,
                    type: catalogItemTypes.SOFTWARE_PRODUCT,
                    filter,
                    onSelect: onSelectVSP,
                    onMigrate,
                    users,
                    activeTabName
                })}
            </CatalogList>
        );
    }
}
export default DetailsCatalogView;
