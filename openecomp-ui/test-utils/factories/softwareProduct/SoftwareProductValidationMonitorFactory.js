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
    id: '8ba32df1-a86a-41c4-98a5-2e54f56c1a1a',
    status: 'Completed',
    results: [
        {
            test: {
                id: 'certquery',
                title: 'VSP Certifications'
            },
            status: 'Failure',
            details: {
                errors: [
                    {
                        attribute: '',
                        reason: 'Record Not Found',
                        advice:
                            'User must query with (vspId, vspVersion) values for a certifications record that is present in the Repository',
                        code: 40
                    }
                ]
            }
        }
    ],
    total: 1,
    failures: 1,
    successes: 0,
    startDateTime: '2019-02-15T17:47:10.622+0000',
    completionDateTime: '2019-02-15T17:47:10.630+0000'
});

export const VSPTestResultsSuccessFactory = new Factory().attrs({
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
