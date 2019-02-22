/**
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Button from 'sdc-ui/lib/react/Button.js';
import { Tab, Tabs } from 'sdc-ui/lib/react';
import { tabsMapping } from './SoftwareProductValidationConstants.js';
import VspValidationInputs from './inputs/VspValidationInputs.js';
import VspValidationSetup from './setup/VspValidationSetup.js';

class SoftwareProductValidation extends Component {
    static propTypes = {
        onErrorThrown: PropTypes.func,
        softwareProductValidation: PropTypes.object,
        onTestSubmit: PropTypes.func,
        setVspTestsMap: PropTypes.func,
        setActiveTab: PropTypes.func,
        setComplianceChecked: PropTypes.func,
        setCertificationChecked: PropTypes.func
    };

    constructor(props) {
        super(props);
        this.state = {
            complianceCheckList: [],
            certificationCheckList: [],
            flatTestsMap: {},
            activeTab: tabsMapping.SETUP,
            goToValidationInput: false
        };
    }

    buildChildElements(setItem) {
        let parentElement = {};
        parentElement.value = setItem.id;
        parentElement.label = setItem.title;
        parentElement.children = [];
        if (setItem.sets !== undefined) {
            setItem.sets.forEach(element => {
                let childElement = this.buildChildElements(element);
                if (childElement.children.length !== 0) {
                    parentElement.children.push(childElement);
                }
            });
        }
        if (setItem.tests !== undefined) {
            setItem.tests.forEach(element => {
                parentElement.children.push({
                    value: element.id,
                    label: element.title
                });
                let flatTestMap = this.state.flatTestsMap;
                flatTestMap[element.id] = {
                    title: element.title,
                    parameters: element.parameters
                };

                this.setState({ flatTestsMap: flatTestMap });
            });
        }
        return parentElement;
    }

    prepareDataForCheckboxes(res) {
        let complianceData = {};
        let certificationData = {};
        let complianceList = [];
        let certificationList = [];
        let {
            setVspTestsMap /*,
            setCertificationChecked,
            setComplianceChecked*/
        } = this.props;
        if (Object.keys(res).length !== 0) {
            res.sets.forEach(element => {
                if (element.id === 'certification') {
                    certificationData = element;
                } else if (element.id === 'compliance') {
                    complianceData = element;
                }
            });

            let complianceParentNode = {};
            complianceParentNode.value = 'compliance_all';
            complianceParentNode.label = 'All';
            complianceParentNode.children = [];
            if (
                Object.keys(complianceData).length !== 0 &&
                complianceData.sets !== undefined
            ) {
                complianceData.sets.forEach(element => {
                    let childElement = this.buildChildElements(element);
                    if (childElement.children.length !== 0) {
                        complianceParentNode.children.push(childElement);
                    }
                });
                if (complianceData.tests !== undefined) {
                    complianceData.tests.forEach(element => {
                        complianceParentNode.children.push({
                            value: element.id,
                            label: element.title
                        });
                    });
                }
                if (complianceParentNode.children.length !== 0) {
                    complianceList.push(complianceParentNode);
                }
            }

            let certificationParentNode = {};
            certificationParentNode.value = 'certification_all';
            certificationParentNode.label = 'All';
            certificationParentNode.children = [];
            if (
                Object.keys(certificationData).length !== 0 &&
                certificationData.sets !== undefined
            ) {
                certificationData.sets.forEach(element => {
                    let childElement = this.buildChildElements(element);
                    if (childElement.children.length !== 0) {
                        certificationParentNode.children.push(childElement);
                    }
                });
                if (certificationData.tests !== undefined) {
                    certificationData.tests.forEach(element => {
                        certificationParentNode.children.push({
                            value: element.id,
                            label: element.title
                        });
                    });
                }
                if (certificationParentNode.children.length !== 0) {
                    certificationList.push(certificationParentNode);
                }
            }
        }
        this.setState({
            certificationCheckList: certificationList,
            complianceCheckList: complianceList
        });
        setVspTestsMap(this.state.flatTestsMap);
    }

    componentWillMount() {}

    shouldComponentUpdate() {
        return true;
    }

    componentDidMount() {
        let { softwareProductValidation } = this.props;
        if (softwareProductValidation.vspChecks !== undefined) {
            this.prepareDataForCheckboxes(softwareProductValidation.vspChecks);
        }
    }

    prepareDataForValidationSection() {
        let {
            softwareProductId,
            version,
            onTestSubmit,
            onErrorThrown
        } = this.props;
        return {
            softwareProductId,
            version,
            onTestSubmit,
            onErrorThrown
        };
    }

    prepareDataForCheckboxTreeSection() {
        let {
            softwareProductValidation,
            setComplianceChecked,
            setCertificationChecked
        } = this.props;
        let complianceCheckList = this.state.complianceCheckList;
        let certificationCheckList = this.state.certificationCheckList;
        return {
            softwareProductValidation,
            setComplianceChecked,
            setCertificationChecked,
            complianceCheckList,
            certificationCheckList
        };
    }

    handleTabPress(key) {
        let { setActiveTab } = this.props;
        switch (key) {
            case tabsMapping.INPUTS:
                setActiveTab({ activeTab: tabsMapping.INPUTS });
                this.setState({
                    goToValidationInput: true,
                    activeTab: tabsMapping.INPUTS
                });
                return;
            case tabsMapping.SETUP:
                this.setState({ activeTab: tabsMapping.SETUP });
                setActiveTab({ activeTab: tabsMapping.INPUTS });
                return;
        }
    }

    onGoToInputs() {
        let { setActiveTab } = this.props;
        setActiveTab({ activeTab: tabsMapping.INPUTS });
        this.setState({
            goToValidationInput: true,
            activeTab: tabsMapping.INPUTS
        });
    }

    onGoToSetup() {
        let { setActiveTab } = this.props;
        setActiveTab({ activeTab: tabsMapping.SETUP });
        this.setState({
            goToValidationInput: false,
            activeTab: tabsMapping.SETUP
        });
    }

    render() {
        let { softwareProductValidation } = this.props;
        let isNextDisabled =
            (softwareProductValidation.certificationChecked === undefined ||
                softwareProductValidation.certificationChecked.length === 0) &&
            (softwareProductValidation.complianceChecked === undefined ||
                softwareProductValidation.complianceChecked.length === 0);

        return (
            <div className="vsp-validation-view">
                <div className="validation-view-controllers">
                    {this.state.activeTab === tabsMapping.SETUP && (
                        <Button
                            btnType="secondary"
                            data-test-id="go-to-inputs"
                            disabled={isNextDisabled}
                            className="change-tabs-btn"
                            onClick={() => this.onGoToInputs()}>
                            {i18n('NEXT')}
                        </Button>
                    )}
                    {this.state.activeTab === tabsMapping.INPUTS && (
                        <Button
                            btnType="secondary"
                            data-test-id="go-to-setup"
                            className="change-tabs-btn"
                            onClick={() => this.onGoToSetup()}>
                            {i18n('BACK')}
                        </Button>
                    )}
                </div>
                <Tabs
                    className="validation-tabs"
                    type="header"
                    activeTab={this.state.activeTab}
                    onTabClick={key => this.handleTabPress(key)}>
                    <Tab
                        tabId={tabsMapping.SETUP}
                        title="Setup"
                        disabled={this.state.goToValidationInput}>
                        <div className="validation-view-tab">
                            <VspValidationSetup
                                {...this.prepareDataForCheckboxTreeSection()}
                            />
                        </div>
                    </Tab>
                    <Tab
                        tabId={tabsMapping.INPUTS}
                        title="Inputs"
                        disabled={!this.state.goToValidationInput}>
                        <div className="validation-view-tab">
                            <VspValidationInputs
                                {...this.prepareDataForValidationSection()}
                            />
                        </div>
                    </Tab>
                </Tabs>
            </div>
        );
    }
}

export default SoftwareProductValidation;
