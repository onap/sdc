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

export const VSPComplianceCheckedFactory = new Factory().attrs({
    complianceChecked: [
        'computeflavor',
        'sriov',
        'heat',
        'tosca',
        'maxthroughput',
        'latency'
    ]
});

export const VSPCertificationCheckedFactory = new Factory().attrs({
    certificationChecked: ['certquery']
});

export const VSPTestsMapFactory = new Factory().attrs({
    computeflavor: {
        title: 'Compute Flavours Test',
        parameters: [
            {
                id: 'something',
                label: 'Something',
                inputType: 'select',
                placeholder: 'B/N/H',
                disabled: false,
                choices: ['B', 'N', 'H'],
                required: true
            },
            {
                id: 'vnfId',
                label: 'Id of VNF',
                inputType: 'text',
                placeholder: 'VNF ID',
                disabled: false,
                required: true
            }
        ]
    },
    sriov: {
        title: 'SR-IOV Test',
        parameters: [
            {
                id: 'allowSrIov',
                label: 'SR-IOV Test',
                inputType: 'select',
                placeholder: 'No',
                defaultValue: 'No',
                disabled: true,
                choices: ['Yes', 'No'],
                required: true
            },
            {
                id: 'vnfId',
                label: 'Id of VNF',
                inputType: 'text',
                placeholder: 'VNF ID',
                disabled: false,
                required: true
            }
        ]
    },
    heat: {
        title: 'Future HEAT Test',
        parameters: []
    },
    tosca: {
        title: 'Future TOSCA Test',
        parameters: []
    },
    maxthroughput: {
        title: 'Future Max Throughput Test',
        parameters: []
    },
    latency: {
        title: 'Future Latency Test',
        parameters: []
    },
    certquery: {
        title: 'VSP Certifications',
        parameters: [
            {
                id: 'vspId',
                label: 'VSP ID',
                inputType: 'text',
                maxLength: 36,
                minLength: 1,
                placeholder: 'VSP ID',
                disabled: false,
                required: true
            },
            {
                id: 'vspVersion',
                label: 'Previous VSP Version',
                inputType: 'text',
                maxLength: 36,
                minLength: 1,
                placeholder: 'VSP Version',
                disabled: false,
                required: true
            }
        ]
    }
});

export const VSPTestResponseFactory = new Factory().attrs({
    id: '113e1ee2-187e-41e3-b45c-3aadc57d8425',
    status: 'Completed',
    results: [
        {
            test: {
                id: 'certquery',
                title: 'VSP Certifications'
            },
            status: 'Success',
            details: {
                vspId: '7146d53b69ed48aebe116ba7e8c9ddbd',
                vspVersion: '1.0',
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
                            testResult: 'Pass',
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
                                'Diagnostic: maximum latency threshold of 20ms signalling response time exceeded. User advice: consider increasing VDU compute resource.'
                        }
                    ]
                }
            }
        }
    ],
    total: 1,
    failures: 0,
    successes: 1,
    startDateTime: '2019-02-16T07:50:41.451+0000',
    completionDateTime: '2019-02-16T07:50:41.456+0000'
});

export const VSPChecksFactory = new Factory().attrs({
    id: 'ALL',
    title: 'ALL',
    sets: [
        {
            id: 'certification',
            title: 'Certification Query',
            tests: [
                {
                    id: 'certquery',
                    title: 'VSP Certifications',
                    parameters: [
                        {
                            id: 'vspId',
                            label: 'VSP ID',
                            inputType: 'text',
                            maxLength: 36,
                            minLength: 1,
                            placeholder: 'VSP ID',
                            disabled: false,
                            required: true
                        },
                        {
                            id: 'vspVersion',
                            label: 'Previous VSP Version',
                            inputType: 'text',
                            maxLength: 36,
                            minLength: 1,
                            placeholder: 'VSP Version',
                            disabled: false,
                            required: true
                        }
                    ]
                }
            ]
        },
        {
            id: 'compliance',
            title: 'Compliance Tests',
            sets: [
                {
                    id: 'compliancetests',
                    title: 'Compliance Tests',
                    tests: [
                        {
                            id: 'computeflavor',
                            title: 'Compute Flavours Test',
                            parameters: [
                                {
                                    id: 'something',
                                    label: 'Something',
                                    inputType: 'select',
                                    placeholder: 'B/N/H',
                                    disabled: false,
                                    choices: ['B', 'N', 'H'],
                                    required: true
                                },
                                {
                                    id: 'vnfId',
                                    label: 'Id of VNF',
                                    inputType: 'text',
                                    placeholder: 'VNF ID',
                                    disabled: false,
                                    required: true
                                }
                            ]
                        },
                        {
                            id: 'sriov',
                            title: 'SR-IOV Test',
                            parameters: [
                                {
                                    id: 'allowSrIov',
                                    label: 'SR-IOV Test',
                                    inputType: 'select',
                                    placeholder: 'No',
                                    defaultValue: 'No',
                                    disabled: true,
                                    choices: ['Yes', 'No'],
                                    required: true
                                },
                                {
                                    id: 'vnfId',
                                    label: 'Id of VNF',
                                    inputType: 'text',
                                    placeholder: 'VNF ID',
                                    disabled: false,
                                    required: true
                                }
                            ]
                        }
                    ]
                },
                {
                    id: 'validationtests',
                    title: 'Validation Tests',
                    tests: [
                        {
                            id: 'heat',
                            title: 'Future HEAT Test',
                            parameters: []
                        },
                        {
                            id: 'tosca',
                            title: 'Future TOSCA Test',
                            parameters: []
                        }
                    ]
                },
                {
                    id: 'performancetests',
                    title: 'Performance Tests',
                    tests: [
                        {
                            id: 'maxthroughput',
                            title: 'Future Max Throughput Test',
                            parameters: []
                        },
                        {
                            id: 'latency',
                            title: 'Future Latency Test',
                            parameters: []
                        }
                    ]
                }
            ]
        }
    ]
});
