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
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import CheckboxTree from 'nfvo-components/checkboxTree/CheckboxTree.js';

const icons = {
    check: <span className="glyphicon glyphicon-check" />,
    uncheck: <span className="glyphicon glyphicon-unchecked" />,
    halfCheck: <span className="glyphicon glyphicon-stop" />,
    expandClose: <span className="glyphicon glyphicon-plus" />,
    expandOpen: <span className="glyphicon glyphicon-minus" />,
    expandAll: <span className="glyphicon glyphicon-collapse-down" />,
    collapseAll: <span className="glyphicon glyphicon-collapse-up" />,
    parentClose: <span className="glyphicon glyphicon-folder-close" />,
    parentOpen: <span className="glyphicon glyphicon-folder-open" />,
    leaf: <span className="glyphicon glyphicon-bookmark" />
};

class CertificationQuery extends React.Component {
    constructor(props) {
        super(props);
        let { certificationChecked } = this.props;
        this.state = {
            checked:
                certificationChecked === undefined ? [] : certificationChecked,
            expanded: []
        };
    }

    shouldComponentUpdate() {
        return true;
    }

    populateOptions(checkedCertificationQuery) {
        let { flatTestsMap } = this.props;
        return (
            <option>
                {flatTestsMap[checkedCertificationQuery].title +
                    ' (' +
                    checkedCertificationQuery +
                    ')'}
            </option>
        );
    }

    render() {
        let { certificationNodes, setCertificationChecked } = this.props;
        return (
            <div className="validation-setup-checkbox-tree-section">
                <GridSection title="Certification Query">
                    <GridItem colSpan={2}>
                        <div className="validation-view-title">
                            {i18n('Available Certification Query')}
                        </div>
                        <div className="validation-setup-available-tests-section">
                            {certificationNodes.length > 0 && (
                                <CheckboxTree
                                    nodes={certificationNodes}
                                    checked={this.state.checked}
                                    expanded={this.state.expanded}
                                    onCheck={checked => {
                                        this.setState(
                                            { checked },
                                            setCertificationChecked({
                                                checked
                                            })
                                        );
                                    }}
                                    onExpand={expanded =>
                                        this.setState({ expanded })
                                    }
                                    icons={icons}
                                    className="field-section"
                                />
                            )}
                            {certificationNodes.length === 0 && (
                                <div>
                                    No Certification Queries are Available
                                </div>
                            )}
                        </div>
                    </GridItem>
                    <GridItem colSpan={2}>
                        {certificationNodes.length > 0 && (
                            <div>
                                <div className="validation-view-title">
                                    {i18n('Selected Certification Query')}
                                </div>
                                <div>
                                    <select
                                        className="validation-setup-selected-tests"
                                        multiple>
                                        {this.state.checked.map(row =>
                                            this.populateOptions(row)
                                        )}
                                    </select>
                                </div>
                            </div>
                        )}
                    </GridItem>
                </GridSection>
            </div>
        );
    }
}

class ComplianceTests extends React.Component {
    constructor(props) {
        super(props);
        let { complianceChecked } = this.props;
        this.state = {
            checked: complianceChecked === undefined ? [] : complianceChecked,
            expanded: []
        };
    }

    shouldComponentUpdate() {
        return true;
    }

    populateOptions(checkedComplianceTests) {
        let { flatTestsMap } = this.props;
        return (
            <option>
                {flatTestsMap[checkedComplianceTests].title +
                    ' (' +
                    checkedComplianceTests +
                    ')'}
            </option>
        );
    }
    render() {
        let { complianceNodes, setComplianceChecked } = this.props;
        return (
            <div className="validation-setup-checkbox-tree-section">
                <GridSection title="Compliance Tests">
                    <GridItem colSpan={2}>
                        <div className="validation-view-title">
                            {i18n('Available Compliance Tests')}
                        </div>
                        <div className="validation-setup-available-tests-section">
                            {complianceNodes.length > 0 && (
                                <CheckboxTree
                                    nodes={complianceNodes}
                                    checked={this.state.checked}
                                    expanded={this.state.expanded}
                                    onCheck={checked => {
                                        this.setState(
                                            { checked },
                                            setComplianceChecked({
                                                checked
                                            })
                                        );
                                    }}
                                    onExpand={expanded =>
                                        this.setState({ expanded })
                                    }
                                    icons={icons}
                                    className="field-section"
                                />
                            )}
                            {complianceNodes.length === 0 && (
                                <div>No Compliance Tests are Available</div>
                            )}
                        </div>
                    </GridItem>
                    <GridItem colSpan={2}>
                        {complianceNodes.length > 0 && (
                            <div>
                                <div className="validation-view-title">
                                    {i18n('Selected Compliance Tests')}
                                </div>
                                <div>
                                    <select
                                        className="validation-setup-selected-tests"
                                        multiple>
                                        {this.state.checked.map(row =>
                                            this.populateOptions(row)
                                        )}
                                    </select>
                                </div>
                            </div>
                        )}
                    </GridItem>
                </GridSection>
            </div>
        );
    }
}

class VspValidationSetup extends Component {
    static propTypes = {
        softwareProductValidation: PropTypes.object,
        setComplianceChecked: PropTypes.func,
        setCertificationChecked: PropTypes.func
    };

    constructor(props) {
        super(props);
        this.state = {
            complianceCheckList: [],
            certificationCheckList: []
        };
    }

    componentWillMount() {}

    shouldComponentUpdate() {
        return true;
    }

    componentDidMount() {}

    render() {
        let {
            softwareProductValidation,
            setComplianceChecked,
            setCertificationChecked,
            complianceCheckList,
            certificationCheckList
        } = this.props;
        return (
            <div className="vsp-validation-view">
                <CertificationQuery
                    certificationNodes={certificationCheckList}
                    flatTestsMap={softwareProductValidation.vspTestsMap}
                    setCertificationChecked={setCertificationChecked}
                    certificationChecked={
                        softwareProductValidation.certificationChecked
                    }
                />
                <ComplianceTests
                    complianceNodes={complianceCheckList}
                    flatTestsMap={softwareProductValidation.vspTestsMap}
                    setComplianceChecked={setComplianceChecked}
                    complianceChecked={
                        softwareProductValidation.complianceChecked
                    }
                />
            </div>
        );
    }
}

export default VspValidationSetup;
