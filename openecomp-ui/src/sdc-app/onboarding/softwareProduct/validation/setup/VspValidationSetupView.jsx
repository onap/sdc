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
import CheckboxTree from 'react-checkbox-tree';

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
        let { certificationChecked, certificationNodes } = this.props;
        this.state = {
            checked:
                certificationChecked === undefined ? [] : certificationChecked,
            expanded: certificationNodes[0] ? [certificationNodes[0].value] : []
        };
    }

    expandFirstNode() {
        let { certificationNodes } = this.props;
        this.setState({
            expanded: certificationNodes[0] ? [certificationNodes[0].value] : []
        });
    }

    componentDidMount() {
        this.expandFirstNode();
    }

    shouldComponentUpdate() {
        return true;
    }

    componentWillReceiveProps(nextProps) {
        if (
            nextProps.certificationChecked !== this.props.certificationChecked
        ) {
            let expand = this.state.expanded;
            this.setState({
                checked: nextProps.certificationChecked || [],
                expanded: expand
            });
        }
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
                <GridSection title={i18n('Certifications Query')}>
                    <GridItem colSpan={2}>
                        <div className="validation-view-title">
                            {certificationNodes[0]
                                ? certificationNodes[0].value
                                : ''}
                        </div>
                        <div
                            className="validation-setup-available-tests-section"
                            data-test-id={
                                'vsp-validation-certifications-query-checkbox-tree'
                            }>
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
                                    {i18n(
                                        'No Certifications Query are Available'
                                    )}
                                </div>
                            )}
                        </div>
                    </GridItem>
                    <GridItem colSpan={2}>
                        {certificationNodes.length > 0 && (
                            <div>
                                <div className="validation-view-title">
                                    {i18n('Selected Certifications Query')}
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
        let { complianceChecked, complianceNodes } = this.props;
        this.state = {
            checked: complianceChecked === undefined ? [] : complianceChecked,
            expanded: complianceNodes[0] ? [complianceNodes[0].value] : []
        };
    }

    shouldComponentUpdate() {
        return true;
    }

    expandFirstNode() {
        let { complianceNodes } = this.props;
        this.setState({
            expanded: complianceNodes[0] ? [complianceNodes[0].value] : []
        });
    }

    componentDidMount() {
        this.expandFirstNode();
    }

    componentWillUnmount() {}

    componentWillReceiveProps(nextProps) {
        let expand = this.state.expanded;

        if (nextProps.complianceChecked !== this.props.complianceChecked) {
            this.setState({
                checked: nextProps.complianceChecked || [],
                expanded: expand
            });
        }
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
                <GridSection title={i18n('Compliance Checks')}>
                    <GridItem colSpan={2}>
                        <div className="validation-view-title">
                            {complianceNodes[0] ? complianceNodes[0].value : ''}
                        </div>
                        <div
                            className="validation-setup-available-tests-section"
                            data-test-id={
                                'vsp-validation-compliance-checks-checkbox-tree'
                            }>
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
                                <div>
                                    {i18n('No Compliance Checks are Available')}
                                </div>
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

    shouldComponentUpdate() {
        return true;
    }

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
