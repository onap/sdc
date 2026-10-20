/*
 * Copyright (C) 2026 Deutsche Telekom AG.
 *
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
 */

import {InputsTableComponent} from './inputs-table.component';

describe('InputsTableComponent', () => {
    const openInput = (readonly: boolean) => {
        const modalsHandler: any = {newOpenEditPropertyModal: jest.fn(() => new Promise(() => undefined))};
        const table = new InputsTableComponent(null, null, modalsHandler);
        table.readonly = readonly;
        table.updateProperty(<any>{name: 'in1', uniqueId: 'in1-id'});
        return modalsHandler.newOpenEditPropertyModal.mock.calls[0];
    };

    it('opens an input for editing while the table is editable', () => {
        expect(openInput(false)[7]).toBe(false);
    });

    it('opens an input view-only while the table is read-only', () => {
        expect(openInput(true)[7]).toBe(true);
    });
});
