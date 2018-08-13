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
import React from 'react';
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import InputOptions, {
    other as optionInputOther
} from 'nfvo-components/input/validation/InputOptions.jsx';
import { optionsInputValues } from './LicenseKeyGroupsConstants';

class LicenseKeyGroupsListEditorView extends React.Component {
    static propTypes = {
        vendorName: PropTypes.string,
        licenseModelId: PropTypes.string.isRequired,
        licenseKeyGroupsList: PropTypes.array,
        isReadOnlyMode: PropTypes.bool.isRequired,
        onAddLicenseKeyGroupClick: PropTypes.func,
        onEditLicenseKeyGroupClick: PropTypes.func,
        onDeleteLicenseKeyGroupClick: PropTypes.func
    };

    static defaultProps = {
        licenseKeyGroupsList: []
    };

    state = {
        localFilter: ''
    };

    render() {
        let { isReadOnlyMode } = this.props;
        let { onAddLicenseKeyGroupClick } = this.props;
        const { localFilter } = this.state;

        return (
            <div className="license-model-list-editor license-key-groups-list-editor">
                <ListEditorView
                    title={i18n('License Key Groups')}
                    plusButtonTitle={i18n('Add License Key Group')}
                    onAdd={onAddLicenseKeyGroupClick}
                    filterValue={localFilter}
                    onFilter={value => this.setState({ localFilter: value })}
                    isReadOnlyMode={isReadOnlyMode}>
                    {this.filterList().map(licenseKeyGroup =>
                        this.renderLicenseKeyGroupListItem(
                            licenseKeyGroup,
                            isReadOnlyMode
                        )
                    )}
                </ListEditorView>
            </div>
        );
    }

    filterList() {
        let { licenseKeyGroupsList } = this.props;
        let { localFilter } = this.state;
        if (localFilter.trim()) {
            const filter = new RegExp(escape(localFilter), 'i');
            return licenseKeyGroupsList.filter(
                ({ name = '', description = '', type = '' }) => {
                    return (
                        escape(name).match(filter) ||
                        escape(description).match(filter) ||
                        escape(type).match(filter)
                    );
                }
            );
        } else {
            return licenseKeyGroupsList;
        }
    }

    renderLicenseKeyGroupListItem(licenseKeyGroup, isReadOnlyMode) {
        let {
            id,
            name,
            description,
            type,
            manufacturerReferenceNumber
        } = licenseKeyGroup;
        let {
            onEditLicenseKeyGroupClick,
            onDeleteLicenseKeyGroupClick
        } = this.props;
        return (
            <ListEditorItemView
                key={id}
                onSelect={() =>
                    onEditLicenseKeyGroupClick(licenseKeyGroup, isReadOnlyMode)
                }
                onDelete={() => onDeleteLicenseKeyGroupClick(licenseKeyGroup)}
                className="list-editor-item-view"
                isReadOnlyMode={isReadOnlyMode}>
                <div className="list-editor-item-view-field">
                    <div className="title">{i18n('Name')}</div>
                    <div className="text name">{name}</div>
                </div>

                <div className="list-editor-item-view-field">
                    <div className="title">{i18n('Type')}</div>
                    <div className="text type">
                        {InputOptions.getTitleByName(optionsInputValues, type)}
                    </div>
                </div>
                <div className="list-editor-item-view-field">
                    <div className="title">
                        {i18n('Manufacturer Reference Number')}
                    </div>
                    <div className="text description">
                        {manufacturerReferenceNumber}
                    </div>
                </div>
                <div className="list-editor-item-view-field">
                    <div className="title">{i18n('Description')}</div>
                    <div className="text description">{description}</div>
                </div>
            </ListEditorItemView>
        );
    }

    extractValue(item) {
        if (item === undefined) {
            return '';
        } //TODO fix it sooner rather than later

        return item
            ? item.choice === optionInputOther.OTHER
              ? item.other
              : InputOptions.getTitleByName(optionsInputValues, item.choice)
            : '';
    }
}

export default LicenseKeyGroupsListEditorView;

export function generateConfirmationMsg(licenseKeyGroupToDelete) {
    let name = licenseKeyGroupToDelete ? licenseKeyGroupToDelete.name : '';
    let msg = i18n('Are you sure you want to delete "{name}"?', { name: name });
    let subMsg =
        licenseKeyGroupToDelete.referencingFeatureGroups &&
        licenseKeyGroupToDelete.referencingFeatureGroups.length > 0
            ? i18n(
                  'This license key group is associated with one or more feature groups'
              )
            : '';
    return (
        <div>
            <p>{msg}</p>
            <p>{subMsg}</p>
        </div>
    );
}
