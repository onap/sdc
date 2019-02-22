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
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';

class VspTestInputSubmit extends React.Component {
    static propTypes = {
        onTestSubmit: PropTypes.func,
        onErrorThrown: PropTypes.func
    };

    constructor(props) {
        super(props);
        let { softwareProductId, version } = this.props;
        this.state = {
            vspId: softwareProductId,
            versionNumber:
                version.name > 1 ? (version.name - 1).toFixed(1) : version.name
        };
    }

    navigate() {
        let tests = [
            {
                testId: 'certquery',
                parameterValues: [
                    {
                        id: 'vspId',
                        value: this.state.vspId
                    },
                    {
                        id: 'vspVersion',
                        value: this.state.versionNumber
                    }
                ]
            }
        ];
        let {
            onErrorThrown,
            version,
            onTestSubmit,
            status,
            softwareProductId
        } = this.props;
        if (!this.state.vspId || !this.state.versionNumber) {
            onErrorThrown('Please enter values in all the mandatory fields');
        } else if (
            parseFloat(this.state.versionNumber) > parseFloat(version.name)
        ) {
            onErrorThrown(
                'Please enter a version number that is less than or equal to the current VSP version'
            );
        } else {
            onTestSubmit(softwareProductId, version, status, tests);
        }
    }

    render() {
        return (
            <div>
                <div className="div-clear-both">
                    <GridSection>
                        <GridItem colSpan={2}>
                            <Input
                                value={this.state.vspId}
                                label={i18n('VSP ID')}
                                data-test-id="vsp-id-input"
                                type="text"
                                isRequired={true}
                                onChange={newVspId =>
                                    this.setState({ vspId: newVspId })
                                }
                                className="field-section"
                            />
                        </GridItem>
                        <GridItem colSpan={1}>
                            <Input
                                isRequired={true}
                                value={this.state.versionNumber}
                                label={i18n('VSP Version')}
                                data-test-id="vsp-version-input"
                                overlayPos="bottom"
                                onChange={newVersionNumber =>
                                    this.setState({
                                        versionNumber: newVersionNumber
                                    })
                                }
                                type="text"
                                className="field-section"
                            />
                        </GridItem>
                    </GridSection>
                    <GridSection>
                        <GridItem>
                            <Button
                                size="default"
                                data-test-id="proceed-to-validation-monitor-btn"
                                disabled={false}
                                className="proceed-to-validation-btn"
                                onClick={() => this.navigate()}>
                                {i18n('Submit')}
                            </Button>
                        </GridItem>
                    </GridSection>
                </div>
            </div>
        );
    }
}

class VspInputs extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentWillMount() {}

    shouldComponentUpdate() {
        return true;
    }

    componentDidMount() {}

    render() {
        let {
            complianceChecked,
            vspFlatTestsMap,
            certificationChecked
        } = this.props;
        return (
            <div>
                {complianceChecked.map(row => {
                    if (vspFlatTestsMap[row].parameters.length === 0) {
                        return <div />;
                    } else {
                        return (
                            <div className="div-clear-both">
                                <GridSection
                                    title={
                                        vspFlatTestsMap[row].title + ' Inputs :'
                                    }>
                                    {vspFlatTestsMap[row].parameters.map(
                                        row => {
                                            if (row.inputType === 'select') {
                                                return (
                                                    <GridItem>
                                                        <Input
                                                            isRequired={
                                                                row.required
                                                            }
                                                            label={row.label}
                                                            type="select"
                                                            defaultValue={
                                                                row.defaultValue ||
                                                                ''
                                                            }
                                                            disabled={
                                                                row.disabled
                                                            }>
                                                            {row.choices.map(
                                                                selectOption => (
                                                                    <option
                                                                        key={
                                                                            selectOption.key
                                                                        }
                                                                        value={
                                                                            selectOption.key
                                                                        }>
                                                                        {
                                                                            selectOption.label
                                                                        }
                                                                    </option>
                                                                )
                                                            )}
                                                        </Input>
                                                    </GridItem>
                                                );
                                            }
                                            return (
                                                <GridItem>
                                                    <Input
                                                        isRequired={
                                                            row.required
                                                        }
                                                        label={row.label}
                                                        placeholder={
                                                            row.placeholder ||
                                                            ''
                                                        }
                                                        type="text"
                                                        value={
                                                            row.defaultValue ||
                                                            ''
                                                        }
                                                        disabled={row.disabled}
                                                    />
                                                </GridItem>
                                            );
                                        }
                                    )}
                                </GridSection>
                            </div>
                        );
                    }
                })}
                {certificationChecked.map(row => {
                    if (vspFlatTestsMap[row].parameters.length === 0) {
                        return <div />;
                    } else {
                        return (
                            <div className="div-clear-both">
                                <GridSection
                                    title={
                                        vspFlatTestsMap[row].title + ' Inputs :'
                                    }>
                                    {vspFlatTestsMap[row].parameters.map(
                                        row => {
                                            if (row.inputType === 'select') {
                                                return (
                                                    <GridItem>
                                                        <Input
                                                            isRequired={
                                                                row.required
                                                            }
                                                            label={row.label}
                                                            type="select"
                                                            defaultValue={
                                                                row.defaultValue ||
                                                                ''
                                                            }
                                                            disabled={
                                                                row.disabled
                                                            }>
                                                            {row.choices.map(
                                                                selectOption => (
                                                                    <option
                                                                        key={
                                                                            selectOption.key
                                                                        }
                                                                        value={
                                                                            selectOption.key
                                                                        }>
                                                                        {
                                                                            selectOption.label
                                                                        }
                                                                    </option>
                                                                )
                                                            )}
                                                        </Input>
                                                    </GridItem>
                                                );
                                            }
                                            return (
                                                <GridItem>
                                                    <Input
                                                        isRequired={
                                                            row.required
                                                        }
                                                        label={i18n(row.label)}
                                                        placeholder={
                                                            row.placeholder ||
                                                            ''
                                                        }
                                                        type="text"
                                                        value={
                                                            row.defaultValue ||
                                                            ''
                                                        }
                                                        disabled={row.disabled}
                                                    />
                                                </GridItem>
                                            );
                                        }
                                    )}
                                </GridSection>
                            </div>
                        );
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

    componentWillMount() {}

    shouldComponentUpdate() {
        return true;
    }

    componentDidMount() {}

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

    render() {
        let { softwareProductValidation } = this.props;
        return (
            <div className="vsp-validation-view">
                <VspInputs
                    complianceChecked={
                        softwareProductValidation.complianceChecked ===
                        undefined
                            ? []
                            : softwareProductValidation.complianceChecked
                    }
                    certificationChecked={
                        softwareProductValidation.certificationChecked ===
                        undefined
                            ? []
                            : softwareProductValidation.certificationChecked
                    }
                    vspFlatTestsMap={softwareProductValidation.vspTestsMap}
                />
                <VspTestInputSubmit
                    {...this.prepareDataForValidationSection()}
                />
            </div>
        );
    }
}

export default VspValidationInputs;
