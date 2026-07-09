/*-
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
import {ChangeDetectorRef, NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import {PropertyBEModel, PropertyFEModel, PropertyModel} from 'app/models';
import {ValidationUtils} from 'app/utils';
import {WorkspaceService} from 'app/ng2/pages/workspace/workspace.service';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {TranslateServiceConfigToken} from 'app/ng2/shared/translator/translate.service.config';
import {PropertiesUtils} from 'app/ng2/pages/properties-assignment/services/properties.utils';
import {CompositionService} from 'app/ng2/pages/composition/composition.service';
import {TopologyTemplateService} from 'app/ng2/services/component-services/topology-template.service';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {ModalService} from 'app/ng2/services/modal.service';
import {DataTypesService} from 'app/services/data-types-service';
import {SdcUiServices} from 'onap-ui-angular';
import {PropertyFormModalComponent} from './property-form-modal.component';
import {PropertyFormModalService} from './property-form-modal.service';

const mockTranslateConfig = {
    filePrefix: '/assets/i18n/',
    fileSuffix: '.json',
    allowedLanguages: ['en_US'],
    defaultLanguage: 'en_US'
};

function makeProperty(overrides: any = {}): PropertyModel {
    return new PropertyModel(Object.assign({
        uniqueId: 'prop-1',
        name: 'myProp',
        type: 'string',
        description: 'a description'
    }, overrides));
}

// Stand-in for the AngularJS ValidationUtils service (bridged into Angular DI via $injector in prod).
function makeValidationUtils(): any {
    return {
        getValidationPattern: jest.fn((type: string) => {
            switch (type) {
                case 'integer':
                    return /^(0|[-+]?[1-9][0-9]*)$/;
                case 'string':
                    return /^[\u0000-\u00BF]*$/;
                default:
                    return null;
            }
        }),
        validateJson: jest.fn((json: string) => {
            try {
                JSON.parse(json);
                return true;
            } catch (e) {
                return false;
            }
        }),
        validateIntRange: jest.fn(() => true),
        stripAndSanitize: jest.fn((text: string) => text)
    };
}

function makeWorkspaceService(isService = false, isVfc = false): any {
    return {
        metadata: {
            isService: jest.fn(() => isService),
            isVfc: jest.fn(() => isVfc),
            model: null
        }
    };
}

function makeModalService(): any {
    return {
        save: jest.fn((ctx: any) => Observable.of(ctx.property)),
        deleteProperty: jest.fn(() => Observable.of(undefined))
    };
}

// Stand-in for CompositionService (provides componentInstances used to build the componentInstanceMap).
function makeCompositionService(instances: any[] = []): any {
    return {componentInstances: instances};
}

// Stand-in for TopologyTemplateService (getDefaultCustomFunction feeds customToscaFunctions).
function makeTopologyTemplateService(customFns: any[] = []): any {
    return {getDefaultCustomFunction: jest.fn(() => Observable.of(customFns))};
}

// Stand-in for TranslateService (delete-confirm title/text).
function makeTranslateService(): any {
    return {translate: jest.fn((key: string) => key)};
}

// Stand-in for SdcUiServices.ModalService (delete-confirm dialog). captures the button callback so the
// spec can drive the OK path without a real modal.
function makeSdcUiModalService(): any {
    return {openInfoModal: jest.fn()};
}

// Stand-in for the ng2 ModalService — the modal-close seam used by the delete-success path.
function makeNg2ModalService(): any {
    return {closeCurrentModal: jest.fn()};
}

// Stand-in for PropertiesUtils (provided app-wide by PropertiesAssignmentModule in prod). By default
// convertAddPropertyBAToPropertyFE builds a REAL PropertyFEModel from the passed property, so the
// build+serialize plumbing is exercised end-to-end (stringifyValueObj runs for real).
function makePropertiesUtils(): any {
    return {
        convertAddPropertyBAToPropertyFE: jest.fn((property: PropertyBEModel) => new PropertyFEModel(property))
    };
}

// OnPush change detector stand-in (the modal is created dynamically; detectChanges is a no-op in unit tests).
function makeCdr(): any {
    return {detectChanges: jest.fn(), markForCheck: jest.fn(), detach: jest.fn(), reattach: jest.fn(), checkNoChanges: jest.fn()};
}

// Stand-in for the (AngularJS) DataTypesService used to populate the Type dropdown's non-primitive types.
// fetchDataTypesByModel returns an IHttpPromise-like resolving to {data: DataTypesMap}; default is the two
// datatypes below so a spec can assert nonPrimitiveTypes is filtered/sorted, and error paths can override.
function makeDataTypesService(dataTypes: any = {'tosca.datatypes.Root': {}, 'org.openecomp.datatypes.Foo': {}}): any {
    return {fetchDataTypesByModel: jest.fn(() => Promise.resolve({data: dataTypes}))};
}

function createComp(opts: any = {}) {
    const workspaceService = makeWorkspaceService(opts.isService, opts.isVfc);
    const validationUtils = makeValidationUtils();
    const modalService = opts.modalService || makeModalService();
    const propertiesUtils = opts.propertiesUtils || makePropertiesUtils();
    const cdr = makeCdr();
    const compositionService = opts.compositionService || makeCompositionService(opts.componentInstances || []);
    const topologyTemplateService = opts.topologyTemplateService || makeTopologyTemplateService(opts.customToscaFunctions || []);
    const translateService = opts.translateService || makeTranslateService();
    const dataTypesService = opts.dataTypesService || makeDataTypesService(opts.dataTypes);
    const sdcUiModalService = opts.sdcUiModalService || makeSdcUiModalService();
    const ng2ModalService = opts.ng2ModalService || makeNg2ModalService();
    const comp = new PropertyFormModalComponent(workspaceService, validationUtils, modalService, propertiesUtils, cdr,
        compositionService, topologyTemplateService, translateService, dataTypesService, sdcUiModalService, ng2ModalService);
    comp.input = Object.assign({
        property: 'property' in opts ? opts.property : makeProperty(),
        component: {isService: () => opts.isService},
        filteredProperties: [],
        isPropertyValueOwner: false,
        propertyOwnerType: 'component',
        propertyOwnerId: 'comp-1',
        isViewOnly: false,
        inputProperty: null
    }, opts.inputOverrides || {});
    return {comp, workspaceService, validationUtils, modalService, propertiesUtils, cdr,
        compositionService, topologyTemplateService, translateService, dataTypesService, sdcUiModalService, ng2ModalService};
}

describe('PropertyFormModalComponent', () => {

    describe('scalar form build + init from input.property', () => {

        it('builds the scalar form from an existing string PropertyModel', () => {
            const {comp} = createComp({property: makeProperty({name: 'myProp', type: 'string', value: 'hello', description: 'a description'})});
            comp.ngOnInit();
            expect(comp.form.get('name').value).toBe('myProp');
            expect(comp.form.get('type').value).toBe('string');
            expect(comp.form.get('description').value).toBe('a description');
            expect(comp.form.get('value').value).toBe('hello');
            // entry-schema control exists but is empty for a scalar type
            expect(comp.form.get('schemaType').value).toBe('');
        });

        it('populates value from defaultValue when the property has no value (create/default path)', () => {
            const {comp} = createComp({property: makeProperty({value: undefined, defaultValue: 'theDefault'})});
            comp.ngOnInit();
            expect(comp.form.get('value').value).toBe('theDefault');
        });

        it('derives componentMetadata from the workspace service metadata', () => {
            const {comp} = createComp({isService: true, isVfc: false});
            comp.ngOnInit();
            expect(comp.componentMetadata.isService).toBe(true);
            expect(comp.componentMetadata.isVfc).toBe(false);
        });

        it('seeds primitive types synchronously in ngOnInit', () => {
            const {comp} = createComp();
            comp.ngOnInit();
            expect(comp.types.length).toBeGreaterThan(0);
            expect(comp.types).toContain('string');
        });
    });

    describe('Type dropdown non-primitive (data) types', () => {

        it('fetches the model data types and keeps only the non-primitive ones, sorted', async () => {
            const {comp, dataTypesService} = createComp({
                dataTypes: {'org.openecomp.datatypes.Zeta': {}, 'tosca.datatypes.Alpha': {}, 'string': {}}
            });
            comp.ngOnInit();
            await Promise.resolve(); // let fetchDataTypesByModel's promise resolve
            expect(dataTypesService.fetchDataTypesByModel).toHaveBeenCalled();
            // 'string' is a primitive (in comp.types) → filtered out; remaining sorted alphabetically
            expect(comp.nonPrimitiveTypes).toEqual(['org.openecomp.datatypes.Zeta', 'tosca.datatypes.Alpha'].sort((a, b) => a.localeCompare(b)));
            expect(comp.nonPrimitiveTypes).not.toContain('string');
        });

        it('marks for check after the async data types resolve (OnPush repaint)', async () => {
            const {comp, cdr} = createComp();
            comp.ngOnInit();
            await Promise.resolve();
            expect(cdr.markForCheck).toHaveBeenCalled();
        });
    });

    describe('empty-option / null-vs-empty-string parity (Q)', () => {

        it('initialises type and schemaType to "" (not null) when the property has no type', () => {
            // A brand-new (create) property: no uniqueId, no type yet.
            const {comp} = createComp({property: new PropertyModel()});
            comp.ngOnInit();
            // AngularJS <option value=""> wrote "" to the model — match it exactly.
            expect(comp.form.get('type').value).toBe('');
            expect(comp.form.get('schemaType').value).toBe('');
        });
    });

    describe('required-validator gating by component type (S)', () => {

        it('requires the name for a non-service component', () => {
            const {comp} = createComp({isService: false, property: makeProperty({name: '', type: 'string'})});
            comp.ngOnInit();
            comp.form.get('name').setValue('');
            expect(comp.form.get('name').hasError('required')).toBe(true);
        });

        it('does NOT require the name for a service component', () => {
            const {comp} = createComp({isService: true, property: makeProperty({name: '', type: 'string'})});
            comp.ngOnInit();
            comp.form.get('name').setValue('');
            expect(comp.form.get('name').hasError('required')).toBe(false);
        });

        it('always requires the type', () => {
            const {comp} = createComp({isService: true, property: makeProperty({name: 'n', type: 'string'})});
            comp.ngOnInit();
            comp.form.get('type').setValue('');
            expect(comp.form.get('type').hasError('required')).toBe(true);
        });
    });

    describe('showSchema / entry-schema gating', () => {

        it('shows the entry-schema control only for map/list', () => {
            const {comp: mapComp} = createComp({property: makeProperty({type: 'map'})});
            mapComp.ngOnInit();
            expect(mapComp.showSchema()).toBe(true);

            const {comp: listComp} = createComp({property: makeProperty({type: 'list'})});
            listComp.ngOnInit();
            expect(listComp.showSchema()).toBe(true);

            const {comp: strComp} = createComp({property: makeProperty({type: 'string'})});
            strComp.ngOnInit();
            expect(strComp.showSchema()).toBe(false);
        });

        it('makes schemaType required only while the schema is shown (map/list)', () => {
            const {comp} = createComp({property: makeProperty({type: 'map', schema: {property: {type: ''}} as any})});
            comp.ngOnInit();
            comp.form.get('schemaType').setValue('');
            expect(comp.form.get('schemaType').hasError('required')).toBe(true);
        });
    });

    describe('boolean value control', () => {

        it('uses the boolean select for boolean type', () => {
            const {comp} = createComp({property: makeProperty({type: 'boolean', value: 'true'})});
            comp.ngOnInit();
            expect(comp.isSimpleType('boolean')).toBe(true);
            // the value control exists and carries the model value
            expect(comp.form.get('value').value).toBe('true');
        });

        // Reviewer finding #1 (Critical): the value-widget gate must be driven by the PROPERTY's
        // type, mirroring the old (simpleType || type) === 'boolean'. Passing the literal 'boolean'
        // to isSimpleType() is always true, which forced the boolean <select> on for EVERY type and
        // dropped the scalar text input (data-tests-id="defaultvalue") entirely.
        describe('value-widget selection driven by property type (reviewer #1)', () => {

            it('isBooleanValue() is false for a string property (text branch)', () => {
                const {comp} = createComp({property: makeProperty({type: 'string', value: 'x'})});
                comp.ngOnInit();
                expect(comp.isBooleanValue()).toBe(false);
            });

            it('isBooleanValue() is true for a boolean property (boolean branch)', () => {
                const {comp} = createComp({property: makeProperty({type: 'boolean', value: 'true'})});
                comp.ngOnInit();
                expect(comp.isBooleanValue()).toBe(true);
            });

            it('isBooleanValue() follows the current form type after onTypeChange (string -> boolean)', () => {
                const {comp} = createComp({property: makeProperty({type: 'string', value: 'x'})});
                comp.ngOnInit();
                expect(comp.isBooleanValue()).toBe(false);
                comp.form.get('type').setValue('boolean');
                comp.onTypeChange();
                expect(comp.isBooleanValue()).toBe(true);
            });

            it('isBooleanValue() honours simpleType when it is set (old simpleType||type)', () => {
                const {comp} = createComp({property: makeProperty({type: 'string', simpleType: 'boolean'})});
                comp.ngOnInit();
                // property.simpleType wins even though the declared type is 'string'
                expect(comp.isBooleanValue()).toBe(true);
            });
        });

        describe('rendered value widget (DOM parity)', () => {
            let fixture: ComponentFixture<PropertyFormModalComponent>;

            function render(property: PropertyModel): PropertyFormModalComponent {
                fixture = TestBed.createComponent(PropertyFormModalComponent);
                const comp = fixture.componentInstance;
                comp.input = {
                    property,
                    component: {isService: () => false} as any,
                    filteredProperties: [],
                    isPropertyValueOwner: false,
                    propertyOwnerType: 'component',
                    propertyOwnerId: 'comp-1',
                    isViewOnly: false,
                    inputProperty: null
                };
                fixture.detectChanges(); // ngOnInit + template render
                return comp;
            }

            beforeEach(() => {
                TestBed.configureTestingModule({
                    // NO_ERRORS_SCHEMA stubs the <dynamic-property>/<tosca-function>/<app-constraints>/
                    // <app-property-metadata> elements the template now renders, so this DOM-parity render stays
                    // a unit test of the scalar widgets. FormsModule is needed for the [(ngModel)] radio the
                    // Task-6 default-value TYPE toggle uses alongside the reactive [formGroup].
                    imports: [ReactiveFormsModule, FormsModule, TranslateModule],
                    declarations: [PropertyFormModalComponent],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {
                            provide: WorkspaceService,
                            useValue: {metadata: {isService: () => false, isVfc: () => false, model: null}}
                        },
                        {provide: ValidationUtils, useValue: makeValidationUtils()},
                        {provide: PropertyFormModalService, useValue: makeModalService()},
                        {provide: PropertiesUtils, useValue: makePropertiesUtils()},
                        {provide: CompositionService, useValue: makeCompositionService()},
                        {provide: TopologyTemplateService, useValue: makeTopologyTemplateService()},
                        {provide: TranslateService, useValue: makeTranslateService()},
                        {provide: DataTypesService, useValue: makeDataTypesService()},
                        {provide: SdcUiServices.ModalService, useValue: makeSdcUiModalService()},
                        {provide: ModalService, useValue: makeNg2ModalService()},
                        {provide: TranslateServiceConfigToken, useValue: mockTranslateConfig}
                    ]
                }).compileComponents();
            });

            it('renders the scalar text input (defaultvalue) and no boolean select for a string type', () => {
                render(makeProperty({type: 'string', value: 'hello'}));
                expect(fixture.debugElement.query(By.css('[data-tests-id=defaultvalue]'))).toBeTruthy();
                expect(fixture.debugElement.query(By.css('[data-tests-id=booleantype]'))).toBeNull();
            });

            it('renders the boolean select (booleantype) and no text input for a boolean type', () => {
                render(makeProperty({type: 'boolean', value: 'true'}));
                expect(fixture.debugElement.query(By.css('[data-tests-id=booleantype]'))).toBeTruthy();
                expect(fixture.debugElement.query(By.css('[data-tests-id=defaultvalue]'))).toBeNull();
            });
        });
    });

    describe('ported helper methods', () => {

        it('onTypeChange resets the value control and recomputes maxLength', () => {
            const {comp} = createComp({property: makeProperty({type: 'string', value: 'x'})});
            comp.ngOnInit();
            comp.form.get('type').setValue('json');
            comp.onTypeChange();
            expect(comp.form.get('value').value).toBe('');
            expect(comp.maxLength).toBe(4096); // JSON_MAX_LENGTH
        });

        it('setMaxLength returns MAX_LENGTH for a plain string type', () => {
            const {comp} = createComp({property: makeProperty({type: 'string'})});
            comp.ngOnInit();
            comp.setMaxLength();
            expect(comp.maxLength).toBe(2500);
        });

        it('getValidationPattern delegates to ValidationUtils', () => {
            const {comp, validationUtils} = createComp({property: makeProperty({type: 'integer'})});
            comp.ngOnInit();
            const pattern = comp.getValidationPattern('integer');
            expect(validationUtils.getValidationPattern).toHaveBeenCalledWith('integer');
            expect(pattern).toBeInstanceOf(RegExp);
        });

        it('validateJson returns true for empty input and delegates otherwise', () => {
            const {comp, validationUtils} = createComp();
            comp.ngOnInit();
            expect(comp.validateJson('')).toBe(true);
            expect(validationUtils.validateJson).not.toHaveBeenCalled();
            expect(comp.validateJson('{"a":1}')).toBe(true);
            expect(validationUtils.validateJson).toHaveBeenCalledWith('{"a":1}');
        });

        it('validateIntRange returns true for empty input and delegates otherwise', () => {
            const {comp, validationUtils} = createComp();
            comp.ngOnInit();
            expect(comp.validateIntRange('')).toBe(true);
            expect(validationUtils.validateIntRange).not.toHaveBeenCalled();
            comp.validateIntRange('5');
            expect(validationUtils.validateIntRange).toHaveBeenCalledWith('5');
        });

        it('isSimpleType is true for a simple type and false for a complex/undefined one', () => {
            const {comp} = createComp();
            comp.ngOnInit();
            expect(comp.isSimpleType('string')).toBe(true);
            expect(comp.isSimpleType('org.openecomp.datatypes.Foo')).toBe(false);
            expect(comp.isSimpleType(undefined)).toBeFalsy();
        });

        it('onSchemaTypeChange recomputes maxLength', () => {
            const {comp} = createComp({property: makeProperty({type: 'map'})});
            comp.ngOnInit();
            comp.onSchemaTypeChange();
            expect(comp.maxLength).toBe(2500);
        });
    });

    describe('isValid()', () => {

        it('is invalid in view-only mode even when the form is valid', () => {
            const {comp} = createComp({inputOverrides: {isViewOnly: true}, property: makeProperty({name: 'n', type: 'string'})});
            comp.ngOnInit();
            expect(comp.isValid()).toBe(false);
        });

        it('is valid when the form is valid and not view-only', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'string', value: 'v'})});
            comp.ngOnInit();
            expect(comp.isValid()).toBe(true);
        });
    });

    describe('save() delegation + ctx assembly', () => {

        it('delegates to PropertyFormModalService.save and returns its Observable', (done) => {
            const {comp, modalService} = createComp({property: makeProperty({name: 'n', type: 'string', value: 'v'})});
            comp.ngOnInit();
            comp.save().subscribe((saved) => {
                expect(modalService.save).toHaveBeenCalledTimes(1);
                expect(saved).toBeDefined();
                done();
            });
        });

        it('builds the ctx from the form + input (owner type, ids, filteredProperties)', () => {
            const filtered = [makeProperty({name: 'n', type: 'string'})];
            const {comp, modalService} = createComp({
                property: makeProperty({name: 'n', type: 'string', value: 'v'}),
                filteredProperties: undefined,
                inputOverrides: {filteredProperties: filtered, propertyOwnerType: 'group', propertyOwnerId: 'grp-1', isPropertyValueOwner: true}
            });
            comp.ngOnInit();
            comp.save().subscribe();
            const ctx = modalService.save.mock.calls[0][0];
            expect(ctx.propertyOwnerType).toBe('group');
            expect(ctx.propertyOwnerId).toBe('grp-1');
            expect(ctx.isPropertyValueOwner).toBe(true);
            expect(ctx.filteredProperties).toBe(filtered);
            expect(ctx.property.name).toBe('n');
            expect(ctx.property.value).toBe('v');
        });

        it('strips and sanitizes the description before delegating', () => {
            const {comp, modalService, validationUtils} = createComp({property: makeProperty({name: 'n', type: 'string', description: '  spaced  '})});
            validationUtils.stripAndSanitize = jest.fn((d: string) => (d || '').trim());
            comp.ngOnInit();
            comp.form.get('description').setValue('  spaced  ');
            comp.save().subscribe();
            expect(validationUtils.stripAndSanitize).toHaveBeenCalledWith('  spaced  ');
            const ctx = modalService.save.mock.calls[0][0];
            expect(ctx.property.description).toBe('spaced');
        });

        // Reviewer Important #2 / §Q: the scalar form inits type to '' (matching the old AngularJS
        // <option value="">), but the old VM held null. Serializing type:'' keys the BE datatype cache
        // differently from null/omitted, so the component MUST coerce ''->null before the BE call.
        it('coerces an empty type to null so the property never POSTs type:""', () => {
            const {comp, modalService} = createComp({property: new PropertyModel()});
            comp.ngOnInit();
            expect(comp.form.get('type').value).toBe(''); // empty-option parity from Task 2
            comp.save().subscribe();
            const ctx = modalService.save.mock.calls[0][0];
            expect(ctx.property.type).toBeNull();

            // PropertyModel.toJSON() runs angular.copy(this) (angular is the AngularJS global, absent under Jest)
            // and does NOT touch type — so it faithfully forwards whatever type the property holds. Stub copy
            // with a shallow clone to prove the wire payload carries null, not "".
            (global as any).angular = {copy: (o: any) => Object.assign(Object.create(Object.getPrototypeOf(o)), o)};
            try {
                expect(ctx.property.toJSON().type).toBeNull();
            } finally {
                delete (global as any).angular;
            }
        });

        it('leaves a real type untouched (no coercion for a populated type)', () => {
            const {comp, modalService} = createComp({property: makeProperty({name: 'n', type: 'string'})});
            comp.ngOnInit();
            comp.save().subscribe();
            const ctx = modalService.save.mock.calls[0][0];
            expect(ctx.property.type).toBe('string');
        });
    });

    // Task 5: the recursive value-editing region. Complex types render <dynamic-property> against a
    // PropertyFEModel built by PropertiesUtils; edits are serialized back to a JSON string for save().
    describe('complex value region (<dynamic-property>) build + serialize', () => {

        it('isComplexType mirrors the old isComplexType (SIMPLE_TYPES.indexOf(type) === -1)', () => {
            const {comp: strComp} = createComp({property: makeProperty({type: 'string'})});
            strComp.ngOnInit();
            expect(strComp.isComplexType).toBe(false);

            const {comp: mapComp} = createComp({property: makeProperty({type: 'map', schema: {property: {type: 'string'}} as any})});
            mapComp.ngOnInit();
            expect(mapComp.isComplexType).toBe(true);

            const {comp: dtComp} = createComp({property: makeProperty({type: 'org.openecomp.datatypes.Foo'})});
            dtComp.ngOnInit();
            expect(dtComp.isComplexType).toBe(true);
        });

        it('does NOT build a PropertyFEModel for a simple (scalar) type', () => {
            const {comp, propertiesUtils} = createComp({property: makeProperty({type: 'string', value: 'x'})});
            comp.ngOnInit();
            expect(propertiesUtils.convertAddPropertyBAToPropertyFE).not.toHaveBeenCalled();
            expect(comp.propertyFEModel).toBeUndefined();
        });

        it('builds a PropertyFEModel for a map property and serializes the edited valueObj back to JSON on change', () => {
            const {comp, propertiesUtils, cdr} = createComp({
                property: makeProperty({type: 'map', schema: {property: {type: 'string'}} as any, value: '{"a":"1"}'})
            });
            comp.ngOnInit();
            // BUILD: convertAddPropertyBAToPropertyFE was called with the working property.
            expect(propertiesUtils.convertAddPropertyBAToPropertyFE).toHaveBeenCalledTimes(1);
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);

            // SERIALIZE: a subsequent edit to the FEModel's valueObj is flattened→string via stringifyValueObj
            // (the properties-assignment getJSONValue idiom) and stored on the save seam fields.
            comp.propertyFEModel.valueObj = {a: '2', b: '3'};
            comp.propertyFEModel.valueObjIsValid = true;
            comp.onValueChanged();
            expect(comp.serializedValue).toBe('{"a":"2","b":"3"}');
            expect(comp.valueObj).toEqual({a: '2', b: '3'});
            expect(comp.valueValid).toBe(true);
            // OnPush: an explicit detectChanges keeps the reused editor painted.
            expect(cdr.detectChanges).toHaveBeenCalled();
        });

        it('seeds an empty map value ({"":null}) before building when the property has no value', () => {
            const {comp} = createComp({property: makeProperty({type: 'map', schema: {property: {type: 'string'}} as any, value: undefined, defaultValue: undefined})});
            comp.ngOnInit();
            // Old initEmptyComplexValue: map → {'': null}. The FEModel is built from that seed (one empty row).
            expect(JSON.parse(comp.property.value)).toEqual({'': null});
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);
        });

        it('seeds an empty list value ([""]) before building when the property has no value', () => {
            const {comp} = createComp({property: makeProperty({type: 'list', schema: {property: {type: 'string'}} as any, value: undefined, defaultValue: undefined})});
            comp.ngOnInit();
            expect(JSON.parse(comp.property.value)).toEqual(['']);
        });

        it('seeds an empty object ({}) for a non-map/list complex datatype with no value', () => {
            const {comp} = createComp({property: makeProperty({type: 'org.openecomp.datatypes.Foo', value: undefined, defaultValue: undefined})});
            comp.ngOnInit();
            expect(JSON.parse(comp.property.value)).toEqual({});
        });

        it('rebuilds the FEModel with an empty complex seed when the type changes scalar→map (onTypeChange re-init)', () => {
            const {comp, propertiesUtils} = createComp({property: makeProperty({type: 'string', value: 'x'})});
            comp.ngOnInit();
            expect(comp.propertyFEModel).toBeUndefined();
            expect(propertiesUtils.convertAddPropertyBAToPropertyFE).not.toHaveBeenCalled();

            comp.form.get('type').setValue('map');
            comp.onTypeChange();
            // switching to a complex type seeds the empty value and (re)builds the FEModel → empty editor.
            expect(comp.isComplexType).toBe(true);
            expect(JSON.parse(comp.property.value)).toEqual({'': null});
            expect(propertiesUtils.convertAddPropertyBAToPropertyFE).toHaveBeenCalledTimes(1);
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);
        });

        it('resets the FEModel value to {} (map) on onSchemaTypeChange', () => {
            const {comp} = createComp({property: makeProperty({type: 'map', schema: {property: {type: 'string'}} as any, value: '{"a":"1"}'})});
            comp.ngOnInit();
            comp.form.get('schemaType').setValue('integer');
            comp.onSchemaTypeChange();
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);
            expect(JSON.parse(comp.property.value)).toEqual({});
        });

        it('save() ships the serialized complex value for a value-owner map property', () => {
            const {comp, modalService} = createComp({
                property: makeProperty({name: 'm', type: 'map', schema: {property: {type: 'string'}} as any, value: '{"a":"1"}'}),
                inputOverrides: {isPropertyValueOwner: true}
            });
            comp.ngOnInit();
            comp.propertyFEModel.valueObj = {a: '9'};
            comp.propertyFEModel.valueObjIsValid = true;
            comp.onValueChanged();
            comp.save().subscribe();
            const ctx = modalService.save.mock.calls[0][0];
            expect(ctx.property.value).toBe('{"a":"9"}');
        });

        // Fix wave 1 (reviewer Important): seed the save-seam fields from the freshly-built FEModel on
        // init. dynamic-property only emits (propertyChanged) on USER edits, never on init — so opening an
        // EXISTING complex property and clicking Save WITHOUT touching the editor must still ship the
        // ORIGINAL stored value, matching the old VM (initForNotSimpleType pre-populated myValue at open).
        describe('unedited-save seam seed on init (reviewer Important)', () => {

            // PropertiesUtils stub that mirrors production: convertAddPropertyBAToPropertyFE builds a real
            // FEModel AND populates valueObj by parsing the stored value (what initValueObjectRef does),
            // so a build-then-serialize (with NO onValueChanged) reflects the existing value.
            function makePopulatingPropertiesUtils(): any {
                return {
                    convertAddPropertyBAToPropertyFE: jest.fn((property: PropertyBEModel) => {
                        const fe = new PropertyFEModel(property);
                        fe.valueObj = fe.value != null ? JSON.parse(fe.value) : null;
                        fe.valueObjIsValid = true;
                        return fe;
                    })
                };
            }

            it('seeds serializedValue/valueObj from the built FEModel so an UNEDITED save ships the original value', () => {
                const {comp, modalService} = createComp({
                    property: makeProperty({name: 'm', type: 'map', schema: {property: {type: 'string'}} as any, value: '{"a":"1"}'}),
                    propertiesUtils: makePopulatingPropertiesUtils(),
                    inputOverrides: {isPropertyValueOwner: true}
                });
                comp.ngOnInit();
                // NO onValueChanged() — the user never touched the editor.
                expect(comp.serializedValue).toBe('{"a":"1"}');
                expect(comp.valueObj).toEqual({a: '1'});
                expect(comp.valueValid).toBe(true);
                // The unedited save must therefore ship the ORIGINAL value, not undefined/null.
                comp.save().subscribe();
                const ctx = modalService.save.mock.calls[0][0];
                expect(ctx.property.value).toBe('{"a":"1"}');
            });

            it('an edit still wins over the init seed (edited save ships the NEW value)', () => {
                const {comp, modalService} = createComp({
                    property: makeProperty({name: 'm', type: 'map', schema: {property: {type: 'string'}} as any, value: '{"a":"1"}'}),
                    propertiesUtils: makePopulatingPropertiesUtils(),
                    inputOverrides: {isPropertyValueOwner: true}
                });
                comp.ngOnInit();
                expect(comp.serializedValue).toBe('{"a":"1"}'); // seeded from the built model
                comp.propertyFEModel.valueObj = {a: '9'};
                comp.propertyFEModel.valueObjIsValid = true;
                comp.onValueChanged();
                comp.save().subscribe();
                const ctx = modalService.save.mock.calls[0][0];
                expect(ctx.property.value).toBe('{"a":"9"}'); // the edit overrides the seed
            });

            it('re-seeds the seam on onSchemaTypeChange so an unedited save ships the reset value', () => {
                const {comp, modalService} = createComp({
                    property: makeProperty({name: 'm', type: 'map', schema: {property: {type: 'string'}} as any, value: '{"a":"1"}'}),
                    propertiesUtils: makePopulatingPropertiesUtils(),
                    inputOverrides: {isPropertyValueOwner: true}
                });
                comp.ngOnInit();
                expect(comp.serializedValue).toBe('{"a":"1"}'); // original seed
                comp.form.get('schemaType').setValue('integer');
                comp.onSchemaTypeChange();
                // schema-type change resets the map value to {}; the seam must follow (not stay '{"a":"1"}').
                expect(comp.serializedValue).toBe('{}');
                comp.save().subscribe();
                const ctx = modalService.save.mock.calls[0][0];
                expect(ctx.property.value).toBe('{}');
            });
        });
    });

    // Task 6: the default-value TYPE radio (Value/Entries vs TOSCA Function). The old template's
    // hasGetFunctionValue toggle (property-form-view.html:146-164) hides the value editor and shows
    // <tosca-function> when the TOSCA-function branch is selected.
    describe('tosca-function radio (hasGetFunctionValue)', () => {

        it('initialises hasGetFunctionValue from the property (false for a plain scalar property)', () => {
            const {comp} = createComp({property: makeProperty({type: 'string', value: 'v'})});
            comp.ngOnInit();
            expect(comp.hasGetFunctionValue).toBe(false);
        });

        it('initialises hasGetFunctionValue=true when the property already holds a tosca function', () => {
            const {comp} = createComp({property: makeProperty({type: 'string', toscaFunction: {type: 'get_input'} as any})});
            comp.ngOnInit();
            expect(comp.hasGetFunctionValue).toBe(true);
        });

        it('builds the componentInstanceMap from CompositionService.componentInstances', () => {
            const {comp} = createComp({
                property: makeProperty({type: 'string'}),
                componentInstances: [{uniqueId: 'ci-1', name: 'Inst One'}, {uniqueId: 'ci-2', name: 'Inst Two'}]
            });
            comp.ngOnInit();
            expect(comp.componentInstanceMap.size).toBe(2);
            expect(comp.componentInstanceMap.get('ci-1').name).toBe('Inst One');
            expect(comp.componentInstanceMap.get('ci-2').name).toBe('Inst Two');
        });

        it('loads customToscaFunctions from TopologyTemplateService.getDefaultCustomFunction', () => {
            const {comp, topologyTemplateService} = createComp({
                property: makeProperty({type: 'string'}),
                customToscaFunctions: [{name: 'my_fn', type: 'custom'}]
            });
            comp.ngOnInit();
            expect(topologyTemplateService.getDefaultCustomFunction).toHaveBeenCalled();
            expect(comp.customToscaFunctions.length).toBe(1);
            expect(comp.customToscaFunctions[0].name).toBe('my_fn');
        });

        it('onValueTypeChange to TOSCA function clears any prior toscaFunction validity (invalid until a function is chosen)', () => {
            const {comp} = createComp({property: makeProperty({type: 'string'})});
            comp.ngOnInit();
            comp.hasGetFunctionValue = true;
            comp.onValueTypeChange();
            expect(comp.isGetFunctionValid).toBeUndefined();
        });

        it('onValueTypeChange back to Value clears the toscaFunction and restores validity', () => {
            const {comp} = createComp({property: makeProperty({type: 'string', toscaFunction: {type: 'get_input'} as any})});
            comp.ngOnInit();
            comp.hasGetFunctionValue = false;
            comp.onValueTypeChange();
            expect(comp.property.toscaFunction).toBeUndefined();
            expect(comp.isGetFunctionValid).toBe(true);
        });

        it('onGetFunctionValidFunction stores the emitted tosca function on the property', () => {
            const {comp} = createComp({property: makeProperty({type: 'string'})});
            comp.ngOnInit();
            const fn: any = {type: 'get_input'};
            comp.onGetFunctionValidFunction(fn);
            expect(comp.property.toscaFunction).toBe(fn);
        });

        it('onToscaFunctionValidityChange sets isGetFunctionValid true when valid, undefined when invalid', () => {
            const {comp} = createComp({property: makeProperty({type: 'string'})});
            comp.ngOnInit();
            comp.onToscaFunctionValidityChange({isValid: true} as any);
            expect(comp.isGetFunctionValid).toBe(true);
            comp.onToscaFunctionValidityChange({isValid: false} as any);
            expect(comp.isGetFunctionValid).toBeUndefined();
        });
    });

    // Task 6 / Task-5 seam: when the TOSCA-function radio is selected the value editor (scalar input,
    // boolean select, and <dynamic-property>) must hide (old template's data-ng-if="!hasGetFunctionValue").
    describe('value-region hides when hasGetFunctionValue (Task-5 seam)', () => {

        it('isComplexType-driven value editor is suppressed while a tosca function is selected', () => {
            const {comp} = createComp({property: makeProperty({type: 'map', schema: {property: {type: 'string'}} as any, value: '{"a":"1"}'})});
            comp.ngOnInit();
            expect(comp.isComplexType).toBe(true);
            expect(comp.showValueEditor).toBe(true);
            comp.hasGetFunctionValue = true;
            expect(comp.showValueEditor).toBe(false);
        });
    });

    // Task 6: constraints + metadata handlers (old onConstraintChange/onPropertyMetadataChange).
    describe('constraints + metadata change handlers', () => {

        it('onConstraintChange serializes constraints onto propertyConstraints + constraints', () => {
            const {comp} = createComp({property: makeProperty({type: 'string'})});
            comp.ngOnInit();
            const constraints = [{equal: 'x'}];
            comp.onConstraintChange({constraints, valid: true});
            expect(comp.property.constraints).toBe(constraints);
            expect(comp.property.propertyConstraints).toEqual([JSON.stringify({equal: 'x'})]);
        });

        it('onConstraintChange with an empty list nulls both constraint fields', () => {
            const {comp} = createComp({property: makeProperty({type: 'string', propertyConstraints: [JSON.stringify({equal: 'x'})]})});
            comp.ngOnInit();
            comp.onConstraintChange({constraints: [], valid: true});
            expect(comp.property.constraints).toBeNull();
            expect(comp.property.propertyConstraints).toBeNull();
        });

        it('onConstraintChange marks the form invalid for save when constraints are invalid', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'string', value: 'v'})});
            comp.ngOnInit();
            expect(comp.isValid()).toBe(true);
            comp.onConstraintChange({constraints: [{equal: 'x'}], valid: false});
            expect(comp.isValid()).toBe(false);
        });

        it('onPropertyMetadataChange stores metadata (and nulls it when empty)', () => {
            const {comp} = createComp({property: makeProperty({type: 'string'})});
            comp.ngOnInit();
            const metadata = {owner: 'me'};
            comp.onPropertyMetadataChange({metadata, valid: true});
            expect(comp.property.metadata).toBe(metadata);
            comp.onPropertyMetadataChange({metadata: {}, valid: true});
            expect(comp.property.metadata).toBeNull();
        });

        it('onPropertyMetadataChange marks the form invalid for save when metadata is invalid', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'string', value: 'v'})});
            comp.ngOnInit();
            expect(comp.isValid()).toBe(true);
            comp.onPropertyMetadataChange({metadata: {owner: 'me'}, valid: false});
            expect(comp.isValid()).toBe(false);
        });
    });

    // Task 6 / Step 4: complete isValid() — folds in the tosca-function branch.
    describe('isValid() with tosca-function gating', () => {

        it('requires a valid tosca function when hasGetFunctionValue', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'string', value: 'v'})});
            comp.ngOnInit();
            comp.hasGetFunctionValue = true;
            // no toscaFunction chosen yet, validity undefined
            comp.property.toscaFunction = undefined;
            comp.isGetFunctionValid = undefined;
            expect(comp.isValid()).toBe(false);

            // a valid tosca function makes the form valid again
            comp.property.toscaFunction = {type: 'get_input'} as any;
            comp.isGetFunctionValid = true;
            expect(comp.isValid()).toBe(true);
        });

        it('is invalid when hasGetFunctionValue and the function is set but validity is false', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'string', value: 'v'})});
            comp.ngOnInit();
            comp.hasGetFunctionValue = true;
            comp.property.toscaFunction = {type: 'get_input'} as any;
            comp.isGetFunctionValid = undefined;
            expect(comp.isValid()).toBe(false);
        });
    });

    // Task 6 / Step 3: prev/next navigation through filteredProperties (old getPrev/getNext).
    describe('getNext / getPrev navigation', () => {

        function threeProps(): PropertyModel[] {
            return [
                makeProperty({uniqueId: 'p1', name: 'first', type: 'string', value: 'a'}),
                makeProperty({uniqueId: 'p2', name: 'second', type: 'string', value: 'b'}),
                makeProperty({uniqueId: 'p3', name: 'third', type: 'string', value: 'c'})
            ];
        }

        it('getNext moves forward through filteredProperties and re-inits the form', () => {
            const filtered = threeProps();
            const {comp} = createComp({property: filtered[0], inputOverrides: {filteredProperties: filtered}});
            comp.ngOnInit();
            expect(comp.form.get('name').value).toBe('first');
            comp.getNext();
            expect(comp.property.name).toBe('second');
            expect(comp.form.get('name').value).toBe('second');
            expect(comp.form.get('value').value).toBe('b');
        });

        it('getPrev moves backward through filteredProperties and re-inits the form', () => {
            const filtered = threeProps();
            const {comp} = createComp({property: filtered[2], inputOverrides: {filteredProperties: filtered}});
            comp.ngOnInit();
            expect(comp.form.get('name').value).toBe('third');
            comp.getPrev();
            expect(comp.property.name).toBe('second');
            expect(comp.form.get('name').value).toBe('second');
        });

        it('isFirstProperty / isLastProperty gate the prev/next buttons', () => {
            const filtered = threeProps();
            const {comp} = createComp({property: filtered[0], inputOverrides: {filteredProperties: filtered}});
            comp.ngOnInit();
            expect(comp.isFirstProperty()).toBe(true);
            expect(comp.isLastProperty()).toBe(false);
            comp.getNext();
            expect(comp.isFirstProperty()).toBe(false);
            expect(comp.isLastProperty()).toBe(false);
            comp.getNext();
            expect(comp.isLastProperty()).toBe(true);
        });
    });

    // Task 6 / Step 3 + Task-4 obligation: delete opens a confirm modal → service.deleteProperty →
    // splice the deleted property out of filteredProperties (old VM _.remove, property-form-view-model.ts:641).
    describe('deleteCurrent (confirm + filteredProperties splice)', () => {

        it('opens the SdcUi confirm modal with the translated title/text', () => {
            const filtered = [makeProperty({uniqueId: 'p1', name: 'first', type: 'string'})];
            const {comp, sdcUiModalService, translateService} = createComp({property: filtered[0], inputOverrides: {filteredProperties: filtered}});
            comp.ngOnInit();
            comp.deleteCurrent();
            expect(translateService.translate).toHaveBeenCalledWith('PROPERTY_VIEW_DELETE_MODAL_TITLE');
            expect(sdcUiModalService.openInfoModal).toHaveBeenCalled();
            const args = sdcUiModalService.openInfoModal.mock.calls[0];
            expect(args[2]).toBe('delete-modal');
        });

        it('removes the deleted property from filteredProperties on confirm (Task-4 obligation)', (done) => {
            const filtered = [
                makeProperty({uniqueId: 'p1', name: 'first', type: 'string'}),
                makeProperty({uniqueId: 'p2', name: 'second', type: 'string'})
            ];
            const {comp, modalService, sdcUiModalService} = createComp({property: filtered[0], inputOverrides: {filteredProperties: filtered}});
            comp.ngOnInit();
            // stub the modal-close callback the component passes so we can assert the splice AFTER delete.
            comp.closeModal = jest.fn(() => {
                expect(modalService.deleteProperty).toHaveBeenCalled();
                // the deleted row (p1) is gone; only p2 remains.
                expect(filtered.length).toBe(1);
                expect(filtered[0].uniqueId).toBe('p2');
                done();
            }) as any;
            comp.deleteCurrent();
            // drive the OK button callback captured by openInfoModal.
            const okButton = sdcUiModalService.openInfoModal.mock.calls[0][3][0];
            okButton.callback();
        });

        // Fix wave 1 (BUG 2): the delete-success path must resolve the outer ModalsHandler deferred so the
        // caller's `.then(reloadProperties)` runs and the deleted row disappears without a full reload. The
        // component cannot own the deferred, so the ModalsHandler sets `deleteCallback` at launch; the modal
        // invokes it on delete-success (falling back to closeModal() when unset so old wiring keeps working).
        it('invokes the wired deleteCallback (resolve+close) on delete-success, not closeModal', (done) => {
            const filtered = [makeProperty({uniqueId: 'p1', name: 'first', type: 'string'})];
            const {comp, sdcUiModalService} = createComp({property: filtered[0], inputOverrides: {filteredProperties: filtered}});
            comp.ngOnInit();
            const closeSpy = jest.fn();
            comp.closeModal = closeSpy as any;
            comp.deleteCallback = jest.fn(() => {
                // the wired callback (which resolves the deferred + closes the modal) fired; closeModal did not.
                expect(comp.deleteCallback).toHaveBeenCalledTimes(1);
                expect(closeSpy).not.toHaveBeenCalled();
                done();
            });
            comp.deleteCurrent();
            const okButton = sdcUiModalService.openInfoModal.mock.calls[0][3][0];
            okButton.callback();
        });

        it('falls back to closeModal() on delete-success when no deleteCallback is wired', (done) => {
            const filtered = [makeProperty({uniqueId: 'p1', name: 'first', type: 'string'})];
            const {comp, sdcUiModalService} = createComp({property: filtered[0], inputOverrides: {filteredProperties: filtered}});
            comp.ngOnInit();
            comp.deleteCallback = undefined; // no ModalsHandler wiring
            comp.closeModal = jest.fn(() => {
                done();
            }) as any;
            comp.deleteCurrent();
            const okButton = sdcUiModalService.openInfoModal.mock.calls[0][3][0];
            okButton.callback();
        });
    });

    // Fix wave 1 (BUG 1): creating a NEW complex property (no name yet) must NOT crash. At create time the
    // name lives only in the reactive form; onTypeChange/onSchemaTypeChange rebuild the FEModel via
    // convertAddPropertyBAToPropertyFE, which calls updateExpandedChildPropertyId(name) → name.lastIndexOf('#').
    // Without syncing property.name from the form first, name is undefined and lastIndexOf throws.
    describe('create-new complex property does not crash (BUG 1)', () => {

        // A PropertiesUtils stub that mirrors production closely enough to reproduce the crash: it builds a
        // real PropertyFEModel and calls the real updateExpandedChildPropertyId(name), which throws on an
        // undefined name exactly like production properties.utils.ts:97.
        function makeCrashingPropertiesUtils(): any {
            return {
                convertAddPropertyBAToPropertyFE: jest.fn((property: PropertyBEModel) => {
                    const fe = new PropertyFEModel(property);
                    fe.updateExpandedChildPropertyId(fe.name); // throws if fe.name is undefined (production parity)
                    return fe;
                })
            };
        }

        it('onTypeChange to map on a nameless CREATE property does not throw and builds an empty-map FEModel', () => {
            const {comp} = createComp({
                property: new PropertyModel(), // brand-new: no name, no type
                propertiesUtils: makeCrashingPropertiesUtils()
            });
            comp.ngOnInit();
            comp.form.get('type').setValue('map');
            expect(() => comp.onTypeChange()).not.toThrow();
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);
            // an empty map seed → one empty row ({'': null}).
            expect(JSON.parse(comp.property.value)).toEqual({'': null});
        });

        it('onTypeChange to list on a nameless CREATE property does not throw', () => {
            const {comp} = createComp({
                property: new PropertyModel(),
                propertiesUtils: makeCrashingPropertiesUtils()
            });
            comp.ngOnInit();
            comp.form.get('type').setValue('list');
            expect(() => comp.onTypeChange()).not.toThrow();
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);
            expect(JSON.parse(comp.property.value)).toEqual(['']);
        });

        it('onTypeChange to a datatype on a nameless CREATE property does not throw', () => {
            const {comp} = createComp({
                property: new PropertyModel(),
                propertiesUtils: makeCrashingPropertiesUtils()
            });
            comp.ngOnInit();
            comp.form.get('type').setValue('org.openecomp.datatypes.Foo');
            expect(() => comp.onTypeChange()).not.toThrow();
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);
        });

        it('onSchemaTypeChange on a nameless CREATE map property does not throw', () => {
            const {comp} = createComp({
                property: new PropertyModel({type: 'map', schema: {property: {type: ''}}} as any),
                propertiesUtils: makeCrashingPropertiesUtils()
            });
            comp.ngOnInit();
            comp.form.get('schemaType').setValue('integer');
            expect(() => comp.onSchemaTypeChange()).not.toThrow();
            expect(comp.propertyFEModel).toBeInstanceOf(PropertyFEModel);
        });

        it('syncs property.name from the form into the built FEModel (empty string, never undefined)', () => {
            const {comp} = createComp({
                property: new PropertyModel(),
                propertiesUtils: makeCrashingPropertiesUtils()
            });
            comp.ngOnInit();
            comp.form.get('name').setValue('newMap');
            comp.form.get('type').setValue('map');
            comp.onTypeChange();
            // the working property picked up the form name, so the FEModel is built with a real name.
            expect(comp.property.name).toBe('newMap');
        });
    });

    // Fix wave 1 (BUG 3): scalar value validation (pattern + int-range + json) must gate the value control,
    // mirroring the old template's ng-pattern="getValidationPattern(simpleType||type)" + per-type
    // $setValidity('pattern', validateJson/validateIntRange(...)). A bad value must make the control invalid
    // (Save disabled), never round-trip to a BE 400.
    describe('scalar value validation (BUG 3)', () => {

        it('marks an integer value invalid for a non-integer input (isValid() false)', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'integer', value: ''})});
            comp.ngOnInit();
            comp.form.get('value').setValue('abc');
            expect(comp.form.get('value').invalid).toBe(true);
            expect(comp.isValid()).toBe(false);
        });

        it('accepts a valid integer value (control valid, isValid() true)', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'integer', value: ''})});
            comp.ngOnInit();
            comp.form.get('value').setValue('42');
            expect(comp.form.get('value').valid).toBe(true);
            expect(comp.isValid()).toBe(true);
        });

        it('marks a json value invalid for malformed json', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'json', value: ''})});
            comp.ngOnInit();
            comp.form.get('value').setValue('{not valid json');
            expect(comp.form.get('value').invalid).toBe(true);
            expect(comp.isValid()).toBe(false);
        });

        it('accepts a valid json value', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'json', value: ''})});
            comp.ngOnInit();
            comp.form.get('value').setValue('{"a":1}');
            expect(comp.form.get('value').valid).toBe(true);
            expect(comp.isValid()).toBe(true);
        });

        it('accepts a string value that matches the string pattern', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'string', value: ''})});
            comp.ngOnInit();
            comp.form.get('value').setValue('hello-world');
            expect(comp.form.get('value').valid).toBe(true);
            expect(comp.isValid()).toBe(true);
        });

        it('re-applies the value validator after onTypeChange (string→integer) so a bad value is caught', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'string', value: 'x'})});
            comp.ngOnInit();
            comp.form.get('type').setValue('integer');
            comp.onTypeChange();
            comp.form.get('value').setValue('abc');
            expect(comp.form.get('value').invalid).toBe(true);
            comp.form.get('value').setValue('7');
            expect(comp.form.get('value').valid).toBe(true);
        });

        it('leaves an empty value valid (no value entered yet)', () => {
            const {comp} = createComp({property: makeProperty({name: 'n', type: 'integer', value: ''})});
            comp.ngOnInit();
            comp.form.get('value').setValue('');
            expect(comp.form.get('value').valid).toBe(true);
        });
    });
});
