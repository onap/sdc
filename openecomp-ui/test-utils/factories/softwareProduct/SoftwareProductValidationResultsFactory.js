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
        "scenario": "onap-dublin",
        "testCaseName": "vsp-package",
        "testSuiteName": "sdc.onboarding",
        "executionId": "onap-dublin",
        "parameters": {
          "vsp-id": "0cf7923588df4877989d8c01243e1e69",
          "host-password": "demo123456!",
          "vsp-version": "b19f1f74b5874127bb7778a84eadd99b",
          "host-url": "http://192.168.209.129:30280",
          "host-username": "cs0008"
        },
        "results": [
          {}
        ],
        "status": "completed",
        "startTime": "2019-09-24T08:35:09.000",
        "endTime": "2019-09-24T08:35:10.000"
      },
      {
        "scenario": "onap-dublin",
        "testCaseName": "customer-create",
        "testSuiteName": "aai",
        "executionId": "onap-dublin",
        "parameters": {
          "host-password": "AAI",
          "subscriber-name": "ovp-uygCLjl9",
          "customer-name": "ovp-uygCLjl9",
          "host-url": "https://192.168.209.129:30233",
          "host-username": "AAI"
        },
        "results": [
          {}
        ],
        "status": "in-progress",
        "startTime": "2019-09-24T08:34:33.000",
        "endTime": "2019-09-24T08:34:33.000"
      },
      {
        "scenario": "onap-dublin",
        "testCaseName": "vnf-tosca-provision",
        "testSuiteName": "vnf-validation",
        "executionId": "uygCLjl9-1569314036837",
        "parameters": {
          "mode": "validate",
          "vsp": "/tmp/data/vtp-tmp-files/1569313993969.csar",
          "vnfm-driver": "gvnfmdriver",
          "config-json": "/opt/oclip/conf/vnf-tosca-provision.json",
          "vnf-vendor-name": "VM",
          "ns-csar": "/tmp/data/vtp-tmp-files/1569314002901.csar",
          "onap-objects": "{}",
          "timeout": "600000",
          "vnf-name": "SWITCH",
          "vnf-csar": "/tmp/data/vtp-tmp-files/1569313997224.csar"
        },
        "results": {
          "error": "1::0x6001::Command vnf-tosca-provision failed to execute, "
        },
        "status": "failed",
        "startTime": "2019-09-24T08:33:56.000",
        "endTime": "2019-09-24T08:35:13.000"
      },
      {
        "scenario": "onap-dublin",
        "testCaseName": "vlm-submit",
        "testSuiteName": "sdc.onboarding",
        "executionId": "onap-dublin",
        "parameters": {
          "vlm-version": "115d4d29994a41a38c2ed2bf75c93f5d",
          "vlm-id": "7fbb14e88d9e45c48021e96f35970419",
          "host-password": "demo123456!",
          "host-url": "http://192.168.209.129:30280",
          "host-username": "cs0008"
        },
        "results": [
          {}
        ],
        "status": "completed",
        "startTime": "2019-09-24T08:34:13.000",
        "endTime": "2019-09-24T08:34:13.000"
      },
      {
        "scenario": "onap-dublin",
        "testCaseName": "vsp-validate",
        "testSuiteName": "sdc.onboarding",
        "executionId": "onap-dublin",
        "parameters": {
          "vsp-id": "0cf7923588df4877989d8c01243e1e69",
          "host-password": "demo123456!",
          "vsp-version": "b19f1f74b5874127bb7778a84eadd99b",
          "host-url": "http://192.168.209.129:30280",
          "host-username": "cs0008"
        },
        "results": {
          "errors": {},
          "status": "Success"
        },
        "status": "completed",
        "startTime": "2019-09-24T08:34:58.000",
        "endTime": "2019-09-24T08:34:59.000"
      },
      {
        "scenario": "onap-dublin",
        "testCaseName": "vlm-feature-group-create",
        "testSuiteName": "sdc.onboarding",
        "executionId": "onap-dublin",
        "parameters": {
          "vlm-entitle-pool-id": "fa33494286004b4ebec6703da43f92a5",
          "vlm-id": "7fbb14e88d9e45c48021e96f35970419",
          "vlm-version": "115d4d29994a41a38c2ed2bf75c93f5d",
          "part-number": "1000VM00",
          "host-password": "demo123456!",
          "vlm-key-group-id": "ea95efa4ccb149d49640166972a4e8f8",
          "name": "VM-uygCLjl9 Feature group",
          "host-url": "http://192.168.209.129:30280",
          "host-username": "cs0008"
        },
        "results": {
          "id": "4dc4f39e28ea488b946aedb0f74c436a"
        },
        "status": "completed",
        "startTime": "2019-09-24T08:34:08.000",
        "endTime": "2019-09-24T08:34:08.000"
      } ,
      {
        "scenario": "onap-dublin",
        "testCaseName": "subscription-list",
        "testSuiteName": "aai",
        "executionId": "onap-dublin",
        "parameters": {
          "host-password": "AAI",
          "customer-name": "ovp-uygCLjl9",
          "host-url": "https://192.168.209.129:30233",
          "host-username": "AAI"
        },
        "results": [
          {
            "resource-version": "1569314083390",
            "service-type": "tosca_vnf_validation-uygCLjl9"
          }
        ],
        "status": "completed",
        "startTime": "2019-09-24T08:34:45.000",
        "endTime": "2019-09-24T08:34:46.000"
      }
    ]
});
