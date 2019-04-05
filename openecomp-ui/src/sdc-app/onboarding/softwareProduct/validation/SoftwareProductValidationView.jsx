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
            complianceCheckList: null,
            certificationCheckList: null,
            flatTestsMap: {},
            generalInfo: {},
            activeTab: tabsMapping.SETUP,
            goToValidationInput: false
        };
    }

    setMapAndGeneralData(element, testScenario) {
        let flatTestMap = this.state.flatTestsMap;
        let generalInputData = this.state.generalInfo;
        flatTestMap[element.testCaseName] = {
            title: element.description,
            parameters: element.inputs,
            endpoint: element.endpoint,
            testCaseName: element.testCaseName,
            testSuiteName: element.testSuiteName,
            scenario: testScenario
        };
        generalInputData[element.testCaseName] = {};
        element.inputs.forEach(key => {
            generalInputData[element.testCaseName][key.name] = {
                isValid: true,
                errorText: ''
            };
        });

        this.setState({
            flatTestsMap: flatTestMap,
            generalInfo: generalInputData
        });
    }

    buildChildElements(setItem, testScenario) {
        let parentElement = {};
        parentElement.value = setItem.name;
        parentElement.label = setItem.description;
        parentElement.children = [];
        if (setItem.children !== undefined) {
            setItem.children.forEach(element => {
                let childElement = this.buildChildElements(
                    element,
                    testScenario
                );
                if (childElement.children.length !== 0) {
                    parentElement.children.push(childElement);
                }
            });
        }
        if (setItem.tests !== undefined) {
            setItem.tests.forEach(element => {
                parentElement.children.push({
                    value: element.testCaseName,
                    label: element.description
                });
                this.setMapAndGeneralData(element, testScenario);
            });
        }
        return parentElement;
    }

    buildCheckboxParentNode(parentNode, data) {
        parentNode.value = data.description;
        parentNode.label = 'All';
        parentNode.children = [];
        let scenario = data.name;
        data.children.forEach(element => {
            let childElement = this.buildChildElements(element, scenario);
            if (childElement.children.length !== 0) {
                parentNode.children.push(childElement);
            }
        });
        if (data.tests !== undefined) {
            data.tests.forEach(element => {
                parentNode.children.push({
                    value: element.testCaseName,
                    label: element.description
                });
                this.setMapAndGeneralData(element, scenario);
            });
        }
        return parentNode;
    }

    prepareDataForCheckboxes(res) {
        let complianceData = {};
        let certificationData = {};
        let complianceList = [];
        let certificationList = [];
        let { setVspTestsMap } = this.props;
        if (Object.keys(res).length !== 0 && res.children) {
            res.children.forEach(element => {
                if (element.name === 'certification') {
                    certificationData = element;
                } else if (element.name === 'compliance') {
                    complianceData = element;
                }
            });

            let complianceParentNode = {};
            if (
                Object.keys(complianceData).length !== 0 &&
                complianceData.children !== undefined
            ) {
                complianceParentNode = this.buildCheckboxParentNode(
                    complianceParentNode,
                    complianceData
                );
                if (complianceParentNode.children.length !== 0) {
                    complianceList.push(complianceParentNode);
                }
            }

            let certificationParentNode = {};
            if (
                Object.keys(certificationData).length !== 0 &&
                certificationData.children !== undefined
            ) {
                certificationParentNode = this.buildCheckboxParentNode(
                    certificationParentNode,
                    certificationData
                );
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

    resetState() {
        this.setState({
            complianceCheckList: [],
            certificationCheckList: [],
            flatTestsMap: {},
            activeTab: tabsMapping.SETUP,
            goToValidationInput: false
        });
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

    componentWillUnmount() {
        this.resetState();
    }

    componentWillReceiveProps(nextProps) {
        if (
            nextProps.softwareProductValidation.vspChecks !==
            this.props.softwareProductValidation.vspChecks
        ) {
            let { softwareProductValidation, setActiveTab } = nextProps;
            if (softwareProductValidation.vspChecks !== undefined) {
                this.prepareDataForCheckboxes(
                    softwareProductValidation.vspChecks
                );
            }
            this.setState({
                activeTab: tabsMapping.SETUP,
                goToValidationInput: false
            });
            setActiveTab({ activeTab: tabsMapping.SETUP });
        }
    }

    prepareDataForValidationInputsSection() {
        let {
            softwareProductId,
            version,
            onTestSubmit,
            onErrorThrown,
            setTestsRequest,
            setGeneralInfo
        } = this.props;
        return {
            softwareProductId,
            version,
            onTestSubmit,
            onErrorThrown,
            setTestsRequest,
            setGeneralInfo
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
            case tabsMapping.SETUP:
                this.setState({ activeTab: tabsMapping.SETUP });
                setActiveTab({ activeTab: tabsMapping.SETUP });
                return;
            case tabsMapping.INPUTS:
            default:
                setActiveTab({ activeTab: tabsMapping.INPUTS });
                this.setState({
                    goToValidationInput: true,
                    activeTab: tabsMapping.INPUTS
                });
                return;
        }
    }

    fetchDefaultValue(value) {
        let { softwareProductId, version } = this.props;
        let versionNumber =
                version.name > 1 ? (version.name - 1).toFixed(1) : version.name,
            versionUUID = version.id;
        return value === '$vspid'
            ? softwareProductId
            : value === '$vspPreviousVersion'
              ? versionNumber
              : value === '$vspVersionUUID' ? versionUUID : value || '';
    }

    formTestsRequest(item, testsRequest) {
        let { vspTestsMap } = this.props.softwareProductValidation;
        testsRequest[item] = {
            parameters: {},
            scenario: vspTestsMap[item]['scenario'],
            testCaseName: vspTestsMap[item]['testCaseName'],
            testSuiteName: vspTestsMap[item]['testSuiteName'],
            endpoint: vspTestsMap[item]['endpoint']
        };
        vspTestsMap[item].parameters.forEach(parameter => {
            testsRequest[item].parameters[
                parameter.name
            ] = this.fetchDefaultValue(parameter.defaultValue);
        });
        return testsRequest;
    }

    onGoToInputs() {
        let {
            setActiveTab,
            softwareProductValidation,
            setTestsRequest
        } = this.props;
        setActiveTab({ activeTab: tabsMapping.INPUTS });
        let testsRequest = {};
        if (softwareProductValidation.complianceChecked) {
            softwareProductValidation.complianceChecked.forEach(item => {
                testsRequest = this.formTestsRequest(item, testsRequest);
            });
        }
        if (softwareProductValidation.certificationChecked) {
            softwareProductValidation.certificationChecked.forEach(item => {
                testsRequest = this.formTestsRequest(item, testsRequest);
            });
        }
        setTestsRequest(testsRequest, this.state.generalInfo);
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
                            data-test-id="go-to-vsp-validation-inputs"
                            disabled={isNextDisabled}
                            className="change-tabs-btn"
                            onClick={() => this.onGoToInputs()}>
                            {i18n('NEXT')}
                        </Button>
                    )}
                    {this.state.activeTab === tabsMapping.INPUTS && (
                        <Button
                            btnType="secondary"
                            data-test-id="go-to-vsp-validation-setup"
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
                        title={i18n('Setup')}
                        disabled={this.state.goToValidationInput}>
                        <div className="validation-view-tab">
                            {this.state.complianceCheckList &&
                                this.state.certificationCheckList && (
                                    <VspValidationSetup
                                        {...this.prepareDataForCheckboxTreeSection()}
                                    />
                                )}
                        </div>
                    </Tab>
                    <Tab
                        tabId={tabsMapping.INPUTS}
                        title={i18n('Inputs')}
                        disabled={!this.state.goToValidationInput}>
                        <div className="validation-view-tab">
                            <VspValidationInputs
                                {...this.prepareDataForValidationInputsSection()}
                            />
                        </div>
                    </Tab>
                </Tabs>
            </div>
        );
    }
}

export default SoftwareProductValidation;
