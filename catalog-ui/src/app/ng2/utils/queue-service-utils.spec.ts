/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2022 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import { TestBed } from '@angular/core/testing';
import { QueueServiceUtils } from './queue-service-utils'

describe('QueueServiceUtils', () => {
    let service: QueueServiceUtils;
    let updateMock;
    let updateParamMock;
    let updateFunctMock;

    beforeEach(() => {
        updateMock = jest.fn();
        updateParamMock = jest.fn();
        updateFunctMock = jest.fn();
        TestBed.configureTestingModule({
            imports: [],
            providers: [QueueServiceUtils],
        });
        service = TestBed.get(QueueServiceUtils);
    });

    it('QueueServiceUtils should be created', () => {
        expect(service).toBeTruthy();
    });

    it('function should be executed when added to queue using addNonBlockingUIAction', (doneCallback) => {
        jest.setTimeout(1000);
        service.addNonBlockingUIAction(() => {
            doneCallback();
        });
    });

    it('function should be executed when added to queue using addBlockingUIAction', (doneCallback) => {
        jest.setTimeout(1000);
        service.addBlockingUIAction(() => {
            doneCallback();
        });
    });

});
