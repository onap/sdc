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
        'compliance.compliancetests.computeflavors',
        'vnf-validation'
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
        },
        'vnf-validation': {
            'vspId': {
                isValid: true,
                errorText: ''
            },
            'host-password': {
                isValid: true,
                errorText: ''
            },
            'vsp-csar': {
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
        },
        'vnf-validation': {
            parameters: {
                'vspId': 'abc',
                'host-password': '123',
                'vsp-csar': 'vsp.csar',
            },
              testCaseName: 'vnf-validation',
              testSuiteName: 'vnf-validation',
              scenario: 'onap-dublin',
              endpoint: 'vtp'
        }
    }
});
export const VSPTestRequestFactory = new Factory().attrs({
    vspTestRequest:  [
      {
        'parameters': {
          'config-json': '/opt/oclip/conf/vnf-tosca-provision.json',
          'vsp': '',
          'vnf-csar': 'file://1574080373688.csar',
          'ns-csar': '',
          'vnfm-driver': 'gvnfmdriver ',
          'onap-objects': '{}',
          'mode': 'provision ',
          'vnf-name': 'ABC',
          'vnf-vendor-name': 'ABC',
          'timeout': '60000'
        },
        'scenario': 'onap-dublin',
        'testCaseName': 'vnf-tosca-provision',
        'testSuiteName': 'vnf-validation',
        'endpoint': 'vtp'
      }
     ]

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
        },
        'vnf-validation': {
                      title: 'vnf-validation',
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
                              name: 'vsp-csar',
                              description: 'Vsp Csar',
                              type: 'binary',
                              defaultValue: '',
                              isOptional: true
                          },
                          {
                                name: 'host-password',
                                description: 'host-password',
                                type: 'binary',
                                defaultValue: '',
                                isOptional: true
                          }
                      ],
                      endpoint: 'vtp',
                      testCaseName: 'vnf-validation',
                      testSuiteName: 'vnf-validation',
                      scenario: 'onap-dublin'
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
        },
        {
          'name': 'onap-dublin',
          'children': [
            {
              'name': 'vnf-validation',
              'tests': [
                {
                  'scenario': 'onap-dublin',
                  'testCaseName': 'vnf-tosca-provision',
                  'testSuiteName': 'vnf-validation',
                  'description': 'ONAP TOSCA VNF validation',
                  'author': 'ONAP VTP Team kanagaraj.manickam@huawei.com',
                  'inputs': [
                    {
                      'name': 'config-json',
                      'description': 'Configuration file path',
                      'type': 'string',
                      'defaultValue': '$s{env:OPEN_CLI_HOME}/conf/vnf-tosca-provision.json',
                      'isOptional': false
                    },
                    {
                      'name': 'vsp',
                      'description': 'Path to the ONAP vendor service product (VSP) for the VNF to provision',
                      'type': 'binary',
                      'isOptional': false
                    },
                    {
                      'name': 'vnf-csar',
                      'description': 'Path to the TOSCA CSAR for the VNF to provision',
                      'type': 'binary',
                      'isOptional': false
                    },
                    {
                      'name': 'ns-csar',
                      'description': 'Path to the TOSCA CSAR for the NS service to provision',
                      'type': 'binary',
                      'isOptional': true
                    },
                    {
                      'name': 'vnfm-driver',
                      'description': 'VNFM driver to use. One of gvnfmdriver or hwvnfmdriver',
                      'type': 'string',
                      'isOptional': false
                    },
                    {
                      'name': 'onap-objects',
                      'description': 'Existing ONAP object ids to use instead of creating them while running this task',
                      'type': 'json',
                      'isOptional': true
                    },
                    {
                      'name': 'mode',
                      'description': 'setup or standup or cleanup or provision or validate',
                      'type': 'string',
                      'defaultValue': 'checkup',
                      'isOptional': true
                    },
                    {
                      'name': 'vnf-name',
                      'description': 'VNF Name',
                      'type': 'string',
                      'isOptional': false
                    },
                    {
                      'name': 'vnf-vendor-name',
                      'description': 'VNF Vendor Name',
                      'type': 'string',
                      'isOptional': false
                    },
                    {
                      'name': 'timeout',
                      'description': 'timeout for command to complete the given task in milliseconds',
                      'type': 'string',
                      'defaultValue': '60000',
                      'isOptional': true
                    }
                  ],
                  'endpoint': 'vtp'
                }
              ]
            }
          ]
        }
    ]
});
