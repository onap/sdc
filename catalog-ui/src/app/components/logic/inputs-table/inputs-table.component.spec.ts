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

import * as _ from 'lodash';
(window as any)._ = _;

import {Component, EventEmitter, Input, NO_ERRORS_SCHEMA, Output} from '@angular/core';
import {ComponentFixture} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ConfigureFn, configureTests} from '../../../../jest/test-config.helper';
import {InputsTableComponent} from './inputs-table.component';
import {InputFEModel} from 'app/models/properties-inputs/input-fe-model';
import {PropertiesUtils} from '../../../pages/properties-assignment/services/properties.utils';
import {InputsUtils} from '../../../pages/properties-assignment/services/inputs.utils';
import {ContentAfterLastDotPipe} from '../../../pipes/content-after-last-dot.pipe';
import {ModalService} from '../../../services/modal.service';
import {DataTypeService} from '../../../services/data-type.service';
import {ModalsHandler} from 'app/utils/modals-handler';

@Component({selector: 'dynamic-property', template: ''})
class DynamicPropertyStubComponent {
    @Input() property: any;
    @Input() rootProperty: any;
    @Input() readonly: boolean;
    @Input() hideCheckBox: boolean;
    @Input() expandedChildId: string;
    @Output() propertyChanged = new EventEmitter<void>();
    @Output() expandChild = new EventEmitter<string>();
}

describe('InputsTableComponent', () => {
    const dataTypeServiceMock: any = {checkForCustomBehavior: jest.fn()};
    const propertiesUtils = new PropertiesUtils(dataTypeServiceMock, null);

    const input = (type: string, defaultValue?: string): InputFEModel =>
        new InputsUtils().convertInputBEToInputFE(<any>{
            name: 'patterns', uniqueId: 'patterns-id', type,
            schema: {property: {type: 'string'}}, defaultValue
        });

    const openInput = (readonly: boolean) => {
        const modalsHandler: any = {newOpenEditPropertyModal: jest.fn(() => new Promise(() => undefined))};
        const table = new InputsTableComponent(null, null, modalsHandler, propertiesUtils);
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

    describe('list and map value editor', () => {
        const table = () => new InputsTableComponent(null, null, null, propertiesUtils);

        it('uses the entry editor for list and map inputs only', () => {
            const cmp = table();
            expect(cmp.hasEntryEditor(input('list'))).toBe(true);
            expect(cmp.hasEntryEditor(input('map'))).toBe(true);
            expect(cmp.hasEntryEditor(input('string'))).toBe(false);
            expect(cmp.hasEntryEditor(input('org.openecomp.datatypes.Naming'))).toBe(false);
        });

        it('shows one row per stored entry', () => {
            const editor = table().getEntryEditor(input('list', '["a","b"]'));
            expect(editor.valueObj).toEqual(['a', 'b']);
            expect(editor.flattenedChildren.length).toBe(2);
        });

        it('stores the edited entries as the input default value and emits the change', () => {
            const cmp = table();
            const listInput = input('list', '["a","b"]');
            const emitted = [];
            cmp.inputChanged.subscribe((changed) => emitted.push(changed));
            const editor = cmp.getEntryEditor(listInput);

            editor.valueObj.splice(0, 1);
            cmp.onEntriesChanged(listInput);

            expect(listInput.getJSONDefaultValue()).toBe('["b"]');
            expect(listInput.defaultValueObjIsChanged).toBe(true);
            expect(emitted).toEqual([listInput]);
        });

        it('keeps a newly added empty entry in the editor while the input stores none', () => {
            const cmp = table();
            const listInput = input('list', '["a"]');
            const editor = cmp.getEntryEditor(listInput);

            editor.valueObj.push(null);
            cmp.onEntriesChanged(listInput);

            expect(listInput.getJSONDefaultValue()).toBe('["a"]');
            expect(cmp.getEntryEditor(listInput)).toBe(editor);
            expect(editor.valueObj).toEqual(['a', null]);
        });

        it('rebuilds the editor from the stored value after the edit is discarded', () => {
            const cmp = table();
            const listInput = input('list', '["a","b"]');
            const editor = cmp.getEntryEditor(listInput);
            editor.valueObj.splice(0, 1);
            cmp.onEntriesChanged(listInput);

            new InputsUtils().resetInputDefaultValue(listInput, listInput.defaultValue);

            const rebuilt = cmp.getEntryEditor(listInput);
            expect(rebuilt).not.toBe(editor);
            expect(rebuilt.valueObj).toEqual(['a', 'b']);
        });

        it('does not bring deleted entries back from the stored default', () => {
            const cmp = table();
            const listInput = input('list', '["a","b"]');
            cmp.getEntryEditor(listInput).valueObj.splice(0, 2);
            cmp.onEntriesChanged(listInput);

            listInput.defaultValueObj = _.cloneDeep(listInput.defaultValueObj);

            expect(cmp.getEntryEditor(listInput).valueObj).toEqual([]);
        });
    });

    describe('value cell', () => {
        let fixture: ComponentFixture<InputsTableComponent>;

        beforeEach(() => {
            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [InputsTableComponent, ContentAfterLastDotPipe, DynamicPropertyStubComponent],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: ModalService, useValue: {}},
                        {provide: DataTypeService, useValue: dataTypeServiceMock},
                        {provide: ModalsHandler, useValue: {}},
                        {provide: PropertiesUtils, useValue: propertiesUtils}
                    ]
                });
            };
            return configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(InputsTableComponent);
            });
        });

        const valueCell = (inputs: InputFEModel[], readonly = false): HTMLElement => {
            fixture.componentInstance.inputs = inputs;
            fixture.componentInstance.instanceNamesMap = {};
            fixture.componentInstance.readonly = readonly;
            fixture.detectChanges();
            return fixture.nativeElement.querySelector('.table-body .valueCol');
        };

        it('renders the entry editor instead of a text box for a list input', () => {
            const cell = valueCell([input('list', '["a"]')]);
            expect(cell.querySelector('dynamic-property')).not.toBeNull();
            expect(cell.querySelector('dynamic-element')).toBeNull();
        });

        const entryEditor = (): DynamicPropertyStubComponent =>
            fixture.debugElement.query(By.directive(DynamicPropertyStubComponent)).componentInstance;

        it('passes the read-only state to the entry editor', () => {
            valueCell([input('list', '["a"]')], true);
            expect(entryEditor().readonly).toBe(true);
            expect(entryEditor().hideCheckBox).toBe(true);
        });

        it('saves the entries when the entry editor reports a change', () => {
            const listInput = input('list', '["a","b"]');
            valueCell([listInput]);
            const emitted = [];
            fixture.componentInstance.inputChanged.subscribe((changed) => emitted.push(changed));

            entryEditor().property.valueObj.splice(1, 1);
            entryEditor().propertyChanged.emit();

            expect(listInput.getJSONDefaultValue()).toBe('["a"]');
            expect(emitted).toEqual([listInput]);
        });

        it('keeps the text box for a string input', () => {
            const cell = valueCell([input('string', 'x')]);
            expect(cell.querySelector('dynamic-element')).not.toBeNull();
            expect(cell.querySelector('dynamic-property')).toBeNull();
        });
    });
});
