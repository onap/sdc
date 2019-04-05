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
import { Factory } from 'rosie';

export const VSPTestResultsFailureFactory = new Factory().attrs({
    vspTestResults: [
        {
            scenario: 'certification',
            description: 'Other Certifications',
            testCaseName: 'certification.certificationtests.certquery',
            testSuiteName: 'certificationtests',
            executionId: 'ebaa5f21-ed68-4098-97a9-775ac8800f09-1550575025614',
            parameters: {
                vspId: 'uuidval',
                vspVersion: 'ver',
                other: 'values'
            },
            results: {
                UnknownObject: {
                    someKeyanotherobject2: 'someValue',
                    someKey1: 'someValue',
                    someKey2: 'someValue',
                    someKey3: 'someValue',
                    someKey4: 'someValue',
                    someKey5: 'someValue',
                    someKey21: 'someValue11',
                    someKey111: 'someValue11',
                    someKey222: 'someValue'
                },
                StringResult: 'String Value of Result',
                EmptyObject: {},
                EmptyArray: []
            },
            status: 'COMPLETED',
            startTime: '2019-02-19T11:17:05.670',
            endTime: '2019-02-19T11:17:05.683'
        },
        {
            scenario: 'compliance',
            testCaseName: 'compliance.compliancetests.sriov',
            description: 'Allow_SR-IOV',
            testSuiteName: 'compliancetests',
            executionId: 'ebaa5f21-ed68-4098-97a9-775ac8800f09-1550575025614',
            parameters: {
                vspId: 'uuidval',
                vspVersion: 'ver',
                other: 'values'
            },
            results: {
                errors: [
                    {
                        attribute: '',
                        reason: 'Record Not Found',
                        advice:
                            'User must query with (vspId, vspVersion) values for a certifications record that is present in the Repository',
                        code: 40
                    },
                    {
                        attribute: '',
                        reason: 'Record Not Found',
                        advice:
                            'User must query with (vspId, vspVersion) values for a certifications record that is present in the Repository',
                        code: 40
                    }
                ]
            },
            status: 'FAILED',
            startTime: '2019-02-19T11:17:05.670',
            endTime: '2019-02-19T11:17:05.683'
        },
        {
            scenario: 'compliance',
            testCaseName: ' compliance.compliancetests.computeflavours',
            description: 'Allow  SR-IOV ',
            testSuiteName: 'compliancetests',
            executionId: 'ebaa5f21-ed68-4098-97a9-775ac8800f09-1550575025614',
            parameters: {
                vspId: 'uuidval',
                vspVersion: 'ver',
                other: 'values'
            },
            results: {},
            status: 'COMPLETED',
            startTime: '2019-02-19T11:17:05.670',
            endTime: '2019-02-19T11:17:05.683'
        },
        {
            code: '500',
            message: 'VTP Test(s) could not be completed',
            httpStatus: 500
        }
    ]
});

export const VSPTestResultsSuccessFactory = new Factory().attrs({
    vspTestResults: [
        {
            scenario: 'certification',
            description: 'Other Certifications',
            testCaseName: 'certification.certificationtests.certquery',
            testSuiteName: 'certificationtests',
            executionId: 'ebaa5f21-ed68-4098-97a9-775ac8800f09-1550575025614',
            parameters: {
                vspId: 'uuidval',
                vspVersion: 'ver',
                other: 'values'
            },
            results: {
                testResults: {
                    complianceTests: [
                        {
                            testName: 'Compute Flavors',
                            testResult: 'Pass',
                            notes:
                                'Diagnostic: test performed against GSMA NFVI Abstraction and Profiling Version 0.1 profiles.'
                        },
                        {
                            testName: 'SR-IOV',
                            testResult: 'Fail',
                            notes:
                                'Diagnostic: SR-IOV found in VNF Template. User advice: VNF binary and VNF Template must be modified to not require SR-IOV.'
                        },
                        {
                            testName: 'Heat',
                            testResult: 'Pass',
                            notes: ''
                        },
                        {
                            testName: 'TOSCA',
                            testResult: 'Pass',
                            notes:
                                'Diagnostic: test performed for ETSI GS NFV-SOL001v0.10.0.'
                        }
                    ],
                    validationTests: [
                        {
                            testName: 'OpenStack',
                            testResult: 'Pass',
                            notes:
                                'Diagnostic: test performed for OpenStack Rocky.'
                        },
                        {
                            testName: 'VMware',
                            testResult: 'Fail',
                            notes:
                                'Diagnostic: VMware compatible template not found. User advice: add a VMware compatible template to the VSP.'
                        },
                        {
                            testName: 'Kubernetes',
                            testResult: 'Fail',
                            notes:
                                'Diagnostic: Kubernetes compatible template not found. User advice: add a Kubernetes compatible template, such as Helm Chart, to the VSP.'
                        }
                    ],
                    performanceTests: [
                        {
                            testName: 'Max Throughput',
                            testResult: 'Pass',
                            notes: ''
                        },
                        {
                            testName: 'Latency',
                            testResult: 'Fail',
                            notes:
                                'Diagnostic: maximum latency threshold of 20ms signalling response time exceededUser advice: consider increasing VDU compute resource.'
                        }
                    ]
                }
            },

            status: 'COMPLETED',
            startTime: '2019-02-19T11:17:05.670',
            endTime: '2019-02-19T11:17:05.683'
        },
        {
            scenario: 'compliance',
            testCaseName: 'compliance.compliancetests.sriov',
            description: 'Allow_SR-IOV',
            testSuiteName: 'compliancetests',
            executionId: 'ebaa5f21-ed68-4098-97a9-775ac8800f09-1550575025614',
            parameters: {
                vspId: 'uuidval',
                vspVersion: 'ver',
                other: 'values'
            },
            results: {
                errors: [
                    {
                        attribute: '',
                        reason: 'Record Not Found',
                        advice:
                            'User must query with (vspId, vspVersion) values for a certifications record that is present in the Repository',
                        code: 40
                    },
                    {
                        attribute: '',
                        reason: 'Record Not Found',
                        advice:
                            'User must query with (vspId, vspVersion) values for a certifications record that is present in the Repository',
                        code: 40
                    }
                ]
            },
            status: 'FAILED',
            startTime: '2019-02-19T11:17:05.670',
            endTime: '2019-02-19T11:17:05.683'
        },
        {
            scenario: 'compliance',
            testCaseName: ' compliance.compliancetests.computeflavours',
            description: 'Allow  SR-IOV ',
            testSuiteName: 'compliancetests',
            executionId: 'ebaa5f21-ed68-4098-97a9-775ac8800f09-1550575025614',
            parameters: {
                vspId: 'uuidval',
                vspVersion: 'ver',
                other: 'values'
            },
            results: {},
            status: 'COMPLETED',
            startTime: '2019-02-19T11:17:05.670',
            endTime: '2019-02-19T11:17:05.683'
        },
        {
            code: '500',
            message: 'VTP Test(s) could not be completed',
            httpStatus: 500
        }
    ]
});
