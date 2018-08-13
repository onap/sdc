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
import Validator from 'nfvo-utils/Validator.js';

import Tabs from 'sdc-ui/lib/react/Tabs.js';
import Tab from 'sdc-ui/lib/react/Tab.js';

import Button from 'sdc-ui/lib/react/Button.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
//import GridSection from 'nfvo-components/grid/GridSection.jsx';
import { LKG_FORM_NAME, tabIds } from './LicenseKeyGroupsConstants.js';

import {
    validateStartDate,
    thresholdValueValidation
} from '../LicenseModelValidations.js';

import LicenseKeyGroupsLimits from './LicenseKeyGroupsLimits.js';
import {
    limitType,
    NEW_LIMIT_TEMP_ID
} from '../limits/LimitEditorConstants.js';
import LicenseKeyGroupFormContent from './components/FormContent.jsx';
import ModalButtons from 'sdc-app/onboarding/licenseModel/components/ModalButtons.jsx';

const TabButton = props => {
    const { onClick, disabled, className } = props;
    const dataTestId = props['data-test-id'];
    return (
        <div
            className={className}
            onClick={disabled ? undefined : onClick}
            data-test-id={dataTestId}
            role="tab"
            disabled={disabled}>
            {props.children}
        </div>
    );
};

const LicenseKeyGroupPropType = PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    description: PropTypes.string,
    increments: PropTypes.string,
    type: PropTypes.string,
    thresholdUnits: PropTypes.string,
    thresholdValue: PropTypes.number,
    startDate: PropTypes.string,
    expiryDate: PropTypes.string
});

class LicenseKeyGroupsEditorView extends React.Component {
    static propTypes = {
        data: LicenseKeyGroupPropType,
        previousData: LicenseKeyGroupPropType,
        LKGNames: PropTypes.object,
        isReadOnlyMode: PropTypes.bool,
        onDataChanged: PropTypes.func.isRequired,
        onSubmit: PropTypes.func.isRequired,
        onCancel: PropTypes.func.isRequired
    };

    static defaultProps = {
        data: {}
    };

    componentDidUpdate(prevProps) {
        if (
            this.props.formReady &&
            this.props.formReady !== prevProps.formReady
        ) {
            // if form validation succeeded -> continue with submit
            this.submit();
        }
    }

    state = {
        localFeatureGroupsListFilter: '',
        selectedTab: tabIds.GENERAL,
        selectedLimit: ''
    };

    render() {
        let {
            data = {},
            onDataChanged,
            isReadOnlyMode,
            onCloseLimitEditor,
            genericFieldInfo,
            limitsList = []
        } = this.props;
        let { selectedTab } = this.state;
        const isTabsDisabled = !data.id || !this.props.isFormValid;
        return (
            <div className="license-keygroup-editor license-model-modal license-key-groups-modal">
                <Tabs
                    type="menu"
                    activeTab={selectedTab}
                    onTabClick={tabIndex => {
                        if (tabIndex === tabIds.ADD_LIMIT_BUTTON) {
                            this.onAddLimit();
                        } else {
                            this.setState({ selectedTab: tabIndex });
                            onCloseLimitEditor();
                            this.setState({ selectedLimit: '' });
                        }
                    }}
                    invalidTabs={[]}>
                    <Tab
                        tabId={tabIds.GENERAL}
                        data-test-id="general-tab"
                        title={i18n('General')}>
                        {genericFieldInfo && (
                            <Form
                                ref="validationForm"
                                hasButtons={false}
                                isValid={this.props.isFormValid}
                                formReady={this.props.formReady}
                                onValidateForm={() =>
                                    this.props.onValidateForm(LKG_FORM_NAME)
                                }
                                labledButtons={true}
                                isReadOnlyMode={isReadOnlyMode}
                                className="license-model-form license-key-groups-form">
                                <LicenseKeyGroupFormContent
                                    data={data}
                                    onDataChanged={onDataChanged}
                                    genericFieldInfo={genericFieldInfo}
                                    validateName={value =>
                                        this.validateName(value)
                                    }
                                    validateStartDate={(value, state) =>
                                        validateStartDate(value, state)
                                    }
                                    thresholdValueValidation={(value, state) =>
                                        thresholdValueValidation(value, state)
                                    }
                                />
                            </Form>
                        )}
                    </Tab>
                    <Tab
                        tabId={tabIds.SP_LIMITS}
                        disabled={isTabsDisabled}
                        data-test-id="general-tab"
                        title={i18n('SP Limits')}>
                        {selectedTab === tabIds.SP_LIMITS && (
                            <LicenseKeyGroupsLimits
                                limitType={limitType.SERVICE_PROVIDER}
                                limitsList={limitsList.filter(
                                    item =>
                                        item.type === limitType.SERVICE_PROVIDER
                                )}
                                selectedLimit={this.state.selectedLimit}
                                onCloseLimitEditor={() =>
                                    this.onCloseLimitEditor()
                                }
                                onSelectLimit={limit =>
                                    this.onSelectLimit(limit)
                                }
                                isReadOnlyMode={isReadOnlyMode}
                            />
                        )}
                    </Tab>
                    <Tab
                        tabId={tabIds.VENDOR_LIMITS}
                        disabled={isTabsDisabled}
                        data-test-id="general-tab"
                        title={i18n('Vendor Limits')}>
                        {selectedTab === tabIds.VENDOR_LIMITS && (
                            <LicenseKeyGroupsLimits
                                limitType={limitType.VENDOR}
                                limitsList={limitsList.filter(
                                    item => item.type === limitType.VENDOR
                                )}
                                selectedLimit={this.state.selectedLimit}
                                onCloseLimitEditor={() =>
                                    this.onCloseLimitEditor()
                                }
                                onSelectLimit={limit =>
                                    this.onSelectLimit(limit)
                                }
                                isReadOnlyMode={isReadOnlyMode}
                            />
                        )}
                    </Tab>
                    {selectedTab !== tabIds.GENERAL ? (
                        <TabButton
                            tabId={tabIds.ADD_LIMIT_BUTTON}
                            disabled={
                                !!this.state.selectedLimit || isReadOnlyMode
                            }
                            data-test-id="add-limits-tab"
                            className="add-limit-button">
                            <Button
                                btnType="link"
                                iconName="plus"
                                disabled={
                                    !!this.state.selectedLimit || isReadOnlyMode
                                }>
                                {i18n('Add Limit')}
                            </Button>
                        </TabButton>
                    ) : (
                        <TabButton key="empty_lm_tab_key" />
                    ) // Render empty div to not break tabs
                    }
                </Tabs>
                <ModalButtons
                    className="sdc-modal__footer"
                    selectedLimit={this.state.selectedLimit}
                    isFormValid={this.props.isFormValid}
                    isReadOnlyMode={isReadOnlyMode}
                    onSubmit={this.submit}
                    onCancel={this.props.onCancel}
                />
            </div>
        );
    }

    submit = () => {
        const {
            data: licenseKeyGroup,
            previousData: previousLicenseKeyGroup,
            formReady,
            onValidateForm,
            onSubmit
        } = this.props;
        if (!formReady) {
            onValidateForm(LKG_FORM_NAME);
        } else {
            onSubmit({ licenseKeyGroup, previousLicenseKeyGroup });
        }
    };

    validateName(value) {
        const { data: { id }, LKGNames } = this.props;
        const isExists = Validator.isItemNameAlreadyExistsInList({
            itemId: id,
            itemName: value,
            list: LKGNames
        });

        return !isExists
            ? { isValid: true, errorText: '' }
            : {
                  isValid: false,
                  errorText: i18n(
                      "License key group by the name '" +
                          value +
                          "' already exists. License key group name must be unique"
                  )
              };
    }

    onSelectLimit(limit) {
        if (limit.id === this.state.selectedLimit) {
            this.setState({ selectedLimit: '' });
            return;
        }
        this.setState({ selectedLimit: limit.id });
        this.props.onOpenLimitEditor(limit);
    }

    onCloseLimitEditor() {
        this.setState({ selectedLimit: '' });
        this.props.onCloseLimitEditor();
    }

    onAddLimit() {
        this.setState({ selectedLimit: NEW_LIMIT_TEMP_ID });
        this.props.onOpenLimitEditor();
    }
}

export default LicenseKeyGroupsEditorView;
