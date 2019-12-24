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
import UUID from 'uuid-js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { Button } from 'onap-ui-react';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';

class VspInputs extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    shouldComponentUpdate() {
        return true;
    }

    changeInputs(e, check, parameterName) {
        let { testsRequest, generalInfo, setTestsRequest } = this.props;
        if (e instanceof File) {
            var timestamp = new Date().getTime();
            var fileExtension = (
                e.name.match(/[^\\\/]\.([^.\\\/]+)$/) || [null]
            ).pop();
            var fileName = fileExtension
                ? timestamp + '.' + fileExtension
                : timestamp;
            testsRequest[check].parameters[parameterName] =
                'file://' + fileName;

            testsRequest[check].files = testsRequest[check].files
                ? testsRequest[check].files
                : [];
            testsRequest[check].files.push({ file: e, name: fileName });
        } else {
            testsRequest[check].parameters[parameterName] = e;
        }

        generalInfo[check][parameterName] = { isValid: true, errorText: '' };
        setTestsRequest(testsRequest, generalInfo);
    }

    renderInputs(check, indexKey) {
        let { vspTestsMap, testsRequest, generalInfo } = this.props;
        return (
            <div key={indexKey} className="div-clear-both">
                <GridSection
                    title={i18n('{title} Inputs :', {
                        title: vspTestsMap[check].title.split(/\r?\n/)[0]
                    })}>
                    {vspTestsMap[check].parameters.map((parameter, index) => {
                        parameter.metadata = parameter.metadata
                            ? parameter.metadata
                            : {};
                        if (
                            !this.props.filterField(parameter) ||
                            parameter.metadata.hidden
                        ) {
                            return;
                        }
                        if (
                            parameter.type === 'text' ||
                            parameter.type === 'string' ||
                            parameter.type === 'json'
                        ) {
                            return (
                                <GridItem key={index}>
                                    <Input
                                        data-test-id={
                                            check +
                                            '_' +
                                            parameter.name +
                                            '_input'
                                        }
                                        isRequired={!parameter.isOptional}
                                        label={parameter.description}
                                        isValid={
                                            generalInfo[check][parameter.name]
                                                .isValid
                                        }
                                        errorText={
                                            generalInfo[check][parameter.name]
                                                .errorText
                                        }
                                        type={
                                            parameter.metadata.choices
                                                ? 'select'
                                                : 'text'
                                        }
                                        value={
                                            testsRequest[check].parameters[
                                                parameter.name
                                            ] || ''
                                        }
                                        onChange={e => {
                                            this.changeInputs(
                                                e.target ? e.target.value : e,
                                                check,
                                                parameter.name
                                            );
                                        }}
                                        disabled={
                                            parameter.metadata.disabled || false
                                        }>
                                        {parameter.metadata.choices && (
                                            <option key="placeholder" value="">
                                                {i18n('Select...')}
                                            </option>
                                        )}
                                        {parameter.metadata.choices &&
                                            parameter.metadata.choices.map(
                                                selectOption => (
                                                    <option
                                                        key={selectOption.key}
                                                        value={
                                                            selectOption.key
                                                        }>
                                                        {selectOption.label}
                                                    </option>
                                                )
                                            )}
                                    </Input>
                                </GridItem>
                            );
                        } else if (parameter.type === 'binary') {
                            return (
                                <GridItem key={index}>
                                    <Input
                                        label={parameter.description}
                                        type="file"
                                        isRequired={!parameter.isOptional}
                                        isValid={
                                            generalInfo[check][parameter.name]
                                                .isValid
                                        }
                                        errorText={
                                            generalInfo[check][parameter.name]
                                                .errorText
                                        }
                                        onChange={e => {
                                            this.changeInputs(
                                                e.target ? e.target.value : e,
                                                check,
                                                parameter.name
                                            );
                                        }}
                                    />
                                </GridItem>
                            );
                        }
                    })}
                </GridSection>
            </div>
        );
    }

    render() {
        let {
            complianceChecked,
            vspTestsMap,
            certificationChecked
        } = this.props;
        return (
            <div>
                {complianceChecked.map((complianceCheck, index) => {
                    if (vspTestsMap[complianceCheck].parameters.length === 0) {
                        return <div />;
                    } else {
                        return this.renderInputs(complianceCheck, index);
                    }
                })}
                {certificationChecked.map((certificateCheck, index) => {
                    if (vspTestsMap[certificateCheck].parameters.length === 0) {
                        return <div />;
                    } else {
                        return this.renderInputs(certificateCheck, index);
                    }
                })}
            </div>
        );
    }
}

class VspValidationInputs extends Component {
    static propTypes = {
        softwareProductValidation: PropTypes.object
    };

    constructor(props) {
        super(props);
        this.state = {};
    }

    shouldComponentUpdate() {
        return true;
    }
    filterField(parameter) {
        if (
            parameter.name === 'host-username' ||
            parameter.name === 'vsp' ||
            parameter.name === 'vsp-zip' ||
            parameter.name === 'host-password' ||
            parameter.name === 'host-url'
        ) {
            return false;
        } else {
            return true;
        }
    }
    validateInputs() {
        let areInputsValid = true;
        let { softwareProductValidation, setGeneralInfo } = this.props;
        let generalInfo = softwareProductValidation.generalInfo;
        Object.keys(softwareProductValidation.testsRequest).forEach(
            testCaseName => {
                let requestParameters =
                    softwareProductValidation.testsRequest[testCaseName]
                        .parameters;
                let validationParameters =
                    softwareProductValidation.vspTestsMap[testCaseName]
                        .parameters;
                Object.keys(requestParameters).forEach(parameterName => {
                    let parameter = validationParameters.find(
                        o => o.name === parameterName
                    );
                    let isParameterValid = true;
                    let errorText = '';
                    if (!this.filterField(parameter)) {
                        // Not required any action
                    } else {
                        if (
                            (parameter.type === 'text' ||
                                parameter.type === 'string') &&
                            parameter.metadata.choices
                        ) {
                            if (
                                !parameter.isOptional &&
                                !requestParameters[parameterName]
                            ) {
                                isParameterValid = false;
                                errorText = i18n('Field is required');
                            }
                        } else if (
                            parameter.type === 'text' ||
                            parameter.type === 'string' ||
                            parameter.type === 'json' ||
                            parameter.type === 'binary'
                        ) {
                            if (
                                !parameter.isOptional &&
                                !requestParameters[parameterName]
                            ) {
                                isParameterValid = false;
                                errorText = i18n('Field is required');
                            } else if (
                                (!parameter.isOptional &&
                                    !requestParameters[parameterName]) ||
                                (parameter.metadata.maxLength &&
                                    requestParameters[parameterName].length >
                                        parseInt(
                                            parameter.metadata.maxLength
                                        )) ||
                                (parameter.metadata.minLength &&
                                    requestParameters[parameterName].length <
                                        parseInt(
                                            parameter.metadata.minLength
                                        ) &&
                                    requestParameters[parameterName].length > 0)
                            ) {
                                isParameterValid = false;
                                errorText = i18n(
                                    'Value Should Be Minimum of {minLength} characters and a Maximum of {maxLength} characters',
                                    {
                                        minLength: parameter.metadata.minLength,
                                        maxLength: parameter.metadata.maxLength
                                    }
                                );
                            }
                        }
                    }
                    generalInfo[testCaseName][
                        parameterName
                    ].isValid = isParameterValid;
                    generalInfo[testCaseName][
                        parameterName
                    ].errorText = errorText;
                    areInputsValid = areInputsValid && isParameterValid;
                });
            }
        );
        if (!areInputsValid) {
            setGeneralInfo(generalInfo);
        }
        return areInputsValid;
    }

    performVSPTests() {
        let tests = [];
        let {
            version,
            onTestSubmit,
            status,
            softwareProductId,
            softwareProductValidation
        } = this.props;

        Object.keys(softwareProductValidation.testsRequest).forEach(key => {
            var testReq = softwareProductValidation.testsRequest[key];
            this.removeParameterFromTest(testReq);
            tests.push(testReq);
        });
        if (this.validateInputs()) {
            var requestId = UUID.create()
                .toString()
                .split('-')[0];
            onTestSubmit(softwareProductId, version, status, tests, requestId);
        }
    }
    removeParameterFromTest(testReq) {
        delete testReq.parameters['host-username'];
        delete testReq.parameters['host-password'];
        delete testReq.parameters['host-url'];
    }
    prepareDataForVspInputs() {
        let { setTestsRequest } = this.props;
        let {
            complianceChecked,
            certificationChecked,
            vspTestsMap,
            testsRequest,
            generalInfo
        } = this.props.softwareProductValidation;
        return {
            setTestsRequest,
            complianceChecked,
            certificationChecked,
            vspTestsMap,
            testsRequest,
            generalInfo
        };
    }

    render() {
        return (
            <div className="vsp-validation-view">
                <Form
                    hasButtons={false}
                    formReady={null}
                    isValid={true}
                    onSubmit={() => this.performVSPTests()}
                    isReadOnlyMode={false}>
                    <VspInputs
                        {...this.prepareDataForVspInputs()}
                        filterField={this.filterField}
                    />
                    <Button
                        size="default"
                        data-test-id="proceed-to-validation-results-btn"
                        disabled={false}
                        type="submit"
                        className="proceed-to-validation-monitor-btn">
                        {i18n('Submit')}
                    </Button>
                </Form>
            </div>
        );
    }
}

export default VspValidationInputs;
