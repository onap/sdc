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
        'compliance.compliancetests.sriov',
        'compliance.compliancetests.computeflavors'
    ]
});

export const VSPCertificationCheckedFactory = new Factory().attrs({
    certificationChecked: ['certification.certificationtests.certquery']
});

export const VSPGeneralInfoFactory = new Factory().attrs({
    generalInfo: {
        'certification.certificationtests.certquery': {
            vspId: {
                isValid: false,
                errorText: 'Please Enter a Value in the Mandatory Field'
            },
            vspVersion: {
                isValid: false,
                errorText: 'Please Enter a Value in the Mandatory Field'
            }
        },
        'compliance.compliancetests.sriov': {
            vspId: {
                isValid: false,
                errorText: 'Please Enter a Value in the Mandatory Field'
            },
            vspVersion: {
                isValid: false,
                errorText: 'Please Enter a Value in the Mandatory Field'
            },
            allowSriov: {
                isValid: true,
                errorText: ''
            }
        },
        'compliance.compliancetests.computeflavors': {
            vspId: {
                isValid: false,
                errorText: 'Please Enter a Value in the Mandatory Field'
            },
            vspVersion: {
                isValid: false,
                errorText: 'Please Enter a Value in the Mandatory Field'
            },
            csp: {
                isValid: true,
                errorText: ''
            },
            profilespec: {
                isValid: true,
                errorText: ''
            },
            vnftype: {
                isValid: true,
                errorText: ''
            }
        }
    }
});

export const VSPTestsRequestFactory = new Factory().attrs({
    testsRequest: {
        'compliance.compliancetests.sriov': {
            parameters: {
                vspId: '',
                vspVersion: '',
                allowSriov: 'false'
            },
            scenario: 'compliance',
            testCaseName: 'compliance.compliancetests.sriov',
            testSuiteName: 'compliancetests',
            endpoint: 'vtp'
        },
        'compliance.compliancetests.computeflavors': {
            parameters: {
                vspId: '',
                vspVersion: '',
                csp: 'ZZFT',
                profilespec: 'gsmafnw14',
                vnftype: 'B'
            },
            scenario: 'compliance',
            testCaseName: 'compliance.compliancetests.computeflavors',
            testSuiteName: 'compliancetests',
            endpoint: 'ovp'
        },
        'certification.certificationtests.certquery': {
            parameters: {
                vspId: '',
                vspVersion: ''
            },
            scenario: 'certification',
            testCaseName: 'certification.certificationtests.certquery',
            testSuiteName: 'certificationtests',
            endpoint: 'repository'
        }
    }
});

export const VSPTestsMapFactory = new Factory().attrs({
    vspTestsMap: {
        'compliance.compliancetests.sriov': {
            title: ' SR-IOV Test',
            parameters: [
                {
                    name: 'vspId',
                    description: 'VSP ID',
                    type: 'text',
                    isOptional: false,
                    metadata: {
                        disabled: false,
                        maxLength: '36',
                        minLength: '1'
                    }
                },
                {
                    name: 'vspVersion',
                    description: 'VSP Version',
                    type: 'text',
                    isOptional: false,
                    metadata: {
                        disabled: false,
                        maxLength: '36',
                        minLength: '1'
                    }
                },
                {
                    name: 'allowSriov',
                    description: 'Allow  SR-IOV?',
                    type: 'select',
                    defaultValue: 'false',
                    isOptional: false,
                    metadata: {
                        disabled: true,
                        choices: [
                            {
                                key: 'true',
                                label: 'Yes'
                            },
                            {
                                key: 'false',
                                label: 'No'
                            }
                        ]
                    }
                }
            ],
            endpoint: 'vtp',
            testCaseName: 'compliance.compliancetests.sriov',
            testSuiteName: 'compliancetests',
            scenario: 'compliance'
        },
        'compliance.compliancetests.computeflavors': {
            title: 'Compute Flavours Test',
            parameters: [
                {
                    name: 'vspId',
                    description: 'VSP ID',
                    type: 'text',
                    isOptional: false,
                    metadata: {
                        disabled: false,
                        maxLength: '36',
                        minLength: '1'
                    }
                },
                {
                    name: 'vspVersion',
                    description: 'VSP Version',
                    type: 'text',
                    isOptional: false,
                    metadata: {
                        disabled: false,
                        maxLength: '36',
                        minLength: '1'
                    }
                },
                {
                    name: 'csp',
                    description: 'CSP',
                    type: 'select',
                    defaultValue: 'ZZFT',
                    isOptional: false,
                    metadata: {
                        disabled: false,
                        choices: [
                            {
                                key: 'ZZTF',
                                label: 'Vodafone Group'
                            }
                        ]
                    }
                },
                {
                    name: 'profilespec',
                    description: 'Profile Specification',
                    type: 'select',
                    defaultValue: 'gsmafnw14',
                    isOptional: false,
                    metadata: {
                        disabled: false,
                        choices: [
                            {
                                key: 'gsmafnw14',
                                label: 'GSMA NFVI Profiles'
                            }
                        ]
                    }
                },
                {
                    name: 'vnftype',
                    description: 'VNF Type',
                    type: 'select',
                    defaultValue: 'B',
                    isOptional: false,
                    metadata: {
                        disabled: false,
                        choices: [
                            {
                                key: 'B',
                                label: 'Basic'
                            },
                            {
                                key: 'N',
                                label: 'Network Intensive'
                            },
                            {
                                key: 'C',
                                label: 'Compute Intensive'
                            }
                        ]
                    }
                }
            ],
            endpoint: 'ovp',
            testCaseName: 'compliance.compliancetests.computeflavors',
            testSuiteName: 'compliancetests',
            scenario: 'compliance'
        },
        'certification.certificationtests.certquery': {
            title: 'Other Certifications',
            parameters: [
                {
                    name: 'vspId',
                    description: 'VSP ID',
                    type: 'text',
                    defaultValue: '$vspid',
                    isOptional: true,
                    metadata: {
                        maxLength: 36,
                        minLength: 1,
                        disabled: true
                    }
                },
                {
                    name: 'vspVersion',
                    description: 'Previous VSP Version',
                    type: 'text',
                    defaultValue: '$vspPreviousVersion',
                    isOptional: true,
                    metadata: {
                        maxLength: 36,
                        minLength: 1,
                        disabled: true
                    }
                }
            ],
            endpoint: 'repository',
            testCaseName: 'certification.certificationtests.certquery',
            testSuiteName: 'certificationtests',
            scenario: 'certification'
        }
    }
});

export const VSPChecksFactory = new Factory().attrs({
    name: 'root',
    description: 'root',
    children: [
        {
            name: 'certification',
            description: 'Available Certifications Query',
            children: [
                {
                    name: 'certificationtests',
                    description: 'Additional Certification',
                    tests: [
                        {
                            testCaseName:
                                'certification.certificationtests.certquery',
                            testSuiteName: 'certificationtests',
                            description: 'Other Certifications',
                            author: 'jg@example.com',
                            inputs: [
                                {
                                    name: 'vspId',
                                    description: 'VSP ID',
                                    type: 'text',
                                    defaultValue: '$vspid',
                                    isOptional: true,
                                    metadata: {
                                        maxLength: 36,
                                        minLength: 1,
                                        disabled: true
                                    }
                                },
                                {
                                    name: 'vspVersion',
                                    description: 'Previous VSP Version',
                                    type: 'text',
                                    defaultValue: '$vspPreviousVersion',
                                    isOptional: true,
                                    metadata: {
                                        maxLength: 36,
                                        minLength: 1,
                                        disabled: true
                                    }
                                }
                            ],
                            endpoint: 'repository'
                        }
                    ]
                }
            ]
        },
        {
            name: 'compliance',
            description: 'Available ComplianceChecks',
            tests: [],
            children: [
                {
                    name: 'compliancetests',
                    description: 'Compliance Tests',
                    tests: [
                        {
                            testCaseName: 'compliance.compliancetests.sriov',
                            testSuiteName: 'compliancetests',
                            description: ' SR-IOV Test',
                            author: 'Jim',
                            inputs: [
                                {
                                    name: 'vspId',
                                    description: 'VSP ID',
                                    type: 'text',
                                    isOptional: false,
                                    metadata: {
                                        disabled: false,
                                        maxLength: '36',
                                        minLength: '1'
                                    }
                                },
                                {
                                    name: 'vspVersion',
                                    description: 'VSP Version',
                                    type: 'text',
                                    isOptional: false,
                                    metadata: {
                                        disabled: false,
                                        maxLength: '36',
                                        minLength: '1'
                                    }
                                },
                                {
                                    name: 'allowSriov',
                                    description: 'Allow  SR-IOV?',
                                    type: 'select',
                                    defaultValue: 'false',
                                    isOptional: false,
                                    metadata: {
                                        disabled: true,
                                        choices: [
                                            {
                                                key: 'true',
                                                label: 'Yes'
                                            },
                                            {
                                                key: 'false',
                                                label: 'No'
                                            }
                                        ]
                                    }
                                }
                            ],
                            endpoint: 'vtp'
                        },
                        {
                            testCaseName:
                                'compliance.compliancetests.computeflavors',
                            testSuiteName: 'compliancetests',
                            description: 'Compute Flavours Test',
                            author: 'Jim',
                            inputs: [
                                {
                                    name: 'vspId',
                                    description: 'VSP ID',
                                    type: 'text',
                                    isOptional: false,
                                    metadata: {
                                        disabled: false,
                                        maxLength: '36',
                                        minLength: '1'
                                    }
                                },
                                {
                                    name: 'vspVersion',
                                    description: 'VSP Version',
                                    type: 'text',
                                    isOptional: false,
                                    metadata: {
                                        disabled: false,
                                        maxLength: '36',
                                        minLength: '1'
                                    }
                                },
                                {
                                    name: 'csp',
                                    description: 'CSP',
                                    type: 'select',
                                    defaultValue: 'ZZFT',
                                    isOptional: false,
                                    metadata: {
                                        disabled: false,
                                        choices: [
                                            {
                                                key: 'ZZTF',
                                                label: 'Vodafone Group'
                                            }
                                        ]
                                    }
                                },
                                {
                                    name: 'profilespec',
                                    description: 'Profile Specification',
                                    type: 'select',
                                    defaultValue: 'gsmafnw14',
                                    isOptional: false,
                                    metadata: {
                                        disabled: false,
                                        choices: [
                                            {
                                                key: 'gsmafnw14',
                                                label: 'GSMA NFVI Profiles'
                                            }
                                        ]
                                    }
                                },
                                {
                                    name: 'vnftype',
                                    description: 'VNF Type',
                                    type: 'select',
                                    defaultValue: 'B',
                                    isOptional: false,
                                    metadata: {
                                        disabled: false,
                                        choices: [
                                            {
                                                key: 'B',
                                                label: 'Basic'
                                            },
                                            {
                                                key: 'N',
                                                label: 'Network Intensive'
                                            },
                                            {
                                                key: 'C',
                                                label: 'Compute Intensive'
                                            }
                                        ]
                                    }
                                }
                            ],
                            endpoint: 'ovp'
                        }
                    ]
                }
            ]
        }
    ]
});
