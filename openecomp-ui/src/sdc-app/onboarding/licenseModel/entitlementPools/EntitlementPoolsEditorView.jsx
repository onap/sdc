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

import Form from 'nfvo-components/input/validation/Form.jsx';
import Button from 'sdc-ui/lib/react/Button.js';
import ModalButtons from 'sdc-app/onboarding/licenseModel/components/ModalButtons.jsx';

import {
    SP_ENTITLEMENT_POOL_FORM,
    tabIds
} from './EntitlementPoolsConstants.js';
import {
    validateStartDate,
    thresholdValueValidation
} from '../LicenseModelValidations.js';

import Tabs from 'sdc-ui/lib/react/Tabs.js';
import Tab from 'sdc-ui/lib/react/Tab.js';
import EntitlementPoolsLimits from './EntitlementPoolsLimits.js';
import {
    limitType,
    NEW_LIMIT_TEMP_ID
} from '../limits/LimitEditorConstants.js';
import EntitlementPoolsFormContent from './components/FormContent.jsx';

const EntitlementPoolPropType = PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    description: PropTypes.string,
    thresholdUnits: PropTypes.string,
    thresholdValue: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    increments: PropTypes.string,
    startDate: PropTypes.string,
    expiryDate: PropTypes.string
});

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

class EntitlementPoolsEditorView extends React.Component {
    static propTypes = {
        data: EntitlementPoolPropType,
        previousData: EntitlementPoolPropType,
        EPNames: PropTypes.object,
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
        selectedTab: tabIds.GENERAL,
        selectedLimit: ''
    };

    render() {
        let {
            data = {},
            onDataChanged,
            isReadOnlyMode,
            genericFieldInfo,
            onCloseLimitEditor,
            limitsList = []
        } = this.props;
        const { selectedTab } = this.state;
        const isTabsDisabled = !data.id || !this.props.isFormValid;

        return (
            <div className="entitlement-pools-modal license-model-modal">
                <Tabs
                    type="menu"
                    activeTab={selectedTab}
                    onTabClick={tabIndex => {
                        if (tabIndex === tabIds.ADD_LIMIT_BUTTON) {
                            this.onAddLimit();
                        } else {
                            this.setState({ selectedTab: tabIndex });
                            this.setState({ selectedLimit: '' });
                            onCloseLimitEditor();
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
                                labledButtons={false}
                                isReadOnlyMode={isReadOnlyMode}
                                isValid={this.props.isFormValid}
                                formReady={this.props.formReady}
                                onValidateForm={() =>
                                    this.props.onValidateForm(
                                        SP_ENTITLEMENT_POOL_FORM
                                    )
                                }
                                className="license-model-form entitlement-pools-form">
                                <EntitlementPoolsFormContent
                                    data={data}
                                    genericFieldInfo={genericFieldInfo}
                                    onDataChanged={onDataChanged}
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
                        disabled={isTabsDisabled}
                        tabId={tabIds.SP_LIMITS}
                        data-test-id="sp-limits-tab"
                        title={i18n('SP Limits')}>
                        {selectedTab === tabIds.SP_LIMITS && (
                            <EntitlementPoolsLimits
                                isReadOnlyMode={isReadOnlyMode}
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
                            />
                        )}
                    </Tab>
                    <Tab
                        disabled={isTabsDisabled}
                        tabId={tabIds.VENDOR_LIMITS}
                        data-test-id="vendor-limits-tab"
                        title={i18n('Vendor Limits')}>
                        {selectedTab === tabIds.VENDOR_LIMITS && (
                            <EntitlementPoolsLimits
                                isReadOnlyMode={isReadOnlyMode}
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
                                disabled={
                                    !!this.state.selectedLimit || isReadOnlyMode
                                }
                                btnType="link"
                                iconName="plus">
                                {i18n('Add Limit')}
                            </Button>
                        </TabButton>
                    ) : (
                        <TabButton key="empty_ep_tab_key" />
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
            data: entitlementPool,
            previousData: previousEntitlementPool,
            formReady
        } = this.props;
        if (!formReady) {
            this.props.onValidateForm(SP_ENTITLEMENT_POOL_FORM);
        } else {
            this.props.onSubmit({ entitlementPool, previousEntitlementPool });
        }
    };

    validateName(value) {
        const { data: { id }, EPNames } = this.props;
        const isExists = Validator.isItemNameAlreadyExistsInList({
            itemId: id,
            itemName: value,
            list: EPNames
        });

        return !isExists
            ? { isValid: true, errorText: '' }
            : {
                  isValid: false,
                  errorText: i18n(
                      "Entitlement pool by the name '" +
                          value +
                          "' already exists. Entitlement pool name must be unique"
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

export default EntitlementPoolsEditorView;
