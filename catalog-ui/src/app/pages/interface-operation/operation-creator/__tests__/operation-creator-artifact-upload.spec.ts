/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {OperationCreatorInterfaceDefinitionComponent} from '../../../interface-definition/operation-creator/operation-creator-interface-definition.component';
import {OperationCreatorComponent} from '../operation-creator.component';

/**
 * The external-workflow artifact file input used to carry the AngularJS `base-sixty-four-input`
 * directive (npm `angular-base64-upload`) alongside `maxsize="10240"`. Both templates are Angular,
 * so that attribute was never compiled — the base64 payload has always come from the component's
 * own FileReader.readAsDataURL. These tests pin that behaviour so the dropped attribute cannot be
 * mistaken for a lost feature.
 */
describe('operation creator - external workflow artifact upload', () => {

    const A_TXT_FILE = () => new File(['hello sdc'], 'workflow.zip', {type: 'application/zip'});

    // Both components implement the handler identically; run the same contract against each.
    const subjects: Array<[string, () => any]> = [
        ['OperationCreatorComponent', () => Object.create(OperationCreatorComponent.prototype)],
        ['OperationCreatorInterfaceDefinitionComponent', () => Object.create(OperationCreatorInterfaceDefinitionComponent.prototype)]
    ];

    subjects.forEach(([name, make]) => {

        describe(name, () => {

            let comp: any;

            beforeEach(() => {
                comp = make();
                comp.operation = {};
                comp.validityChanged = jest.fn();
            });

            it('reads the picked file to base64 through FileReader (no AngularJS directive involved)', async () => {
                comp.onChangeArtifactFile({target: {files: [A_TXT_FILE()]}});

                expect(comp.operation.artifactFileName).toBe('workflow.zip');
                expect(comp.isLoading).toBe(true);

                // FileReader is async — wait for onloadend to fire.
                await new Promise((resolve) => {
                    const poll = () => comp.operation.artifactData !== undefined ? resolve(undefined) : setTimeout(poll, 10);
                    poll();
                });

                // base64 of "hello sdc", with the "data:...;base64," prefix stripped by the handler.
                expect(comp.operation.artifactData).toBe(Buffer.from('hello sdc').toString('base64'));
                expect(comp.isLoading).toBe(false);
                expect(comp.validityChanged).toHaveBeenCalled();
            });

            it('clears filename and data when the file is removed', () => {
                comp.operation.artifactFileName = 'stale.zip';
                comp.operation.artifactData = 'c3RhbGU=';

                comp.onChangeArtifactFile({target: {}});

                expect(comp.operation.artifactFileName).toBeFalsy();
                expect(comp.operation.artifactData).toBeNull();
                expect(comp.validityChanged).toHaveBeenCalled();
            });
        });
    });
});
