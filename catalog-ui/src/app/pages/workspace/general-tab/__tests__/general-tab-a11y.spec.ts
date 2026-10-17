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

import * as fs from 'fs';
import * as path from 'path';
import {ChangeDetectorRef, NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {EventListenerService} from 'app/services/event-listener.service';
import {CacheService} from 'app/services/cache.service';
import {SdcUiServices} from 'onap-ui-angular';
import {Subject} from 'rxjs/Subject';
import {ComponentFactory} from 'app/utils/component-factory';
import {ModalsHandler} from 'app/utils/modals-handler';
import {ConfigureFn, configureTests} from '../../../../../jest/test-config.helper';
import {ImportVSPService} from '../../../../components/modals/onboarding-modal/import-vsp.service';
import {SdcConfigToken} from '../../../../config/sdc-config.config';
import {SdcMenuToken} from '../../../../config/sdc-menu.config';
import {ElementService} from '../../../../services/element.service';
import {ModelService} from '../../../../services/model.service';
import {FileUtilsService} from '../../../../services/file-utils.service';
import {NavigationService} from '../../../../services/navigation.service';
import {TranslateService} from '../../../../shared/translator/translate.service';
import {WorkspaceService} from '../../workspace.service';
import {ComponentMetadataService} from '../component-metadata.service';
import {GeneralFormService} from '../general-form.service';
import {GeneralTabComponent} from '../general-tab.component';

const CATEGORY = 'Network L4+';

describe('GeneralTabComponent - required fields and errors for assistive technology', () => {

    let fixture: ComponentFixture<GeneralTabComponent>;
    let baseTypes$: Subject<any>;

    function serviceComponent() {
        return {
            name: 'MySvc', description: 'd', contactId: 'cs0008', tags: [],
            uniqueId: 'id-1', componentType: 'SERVICE', lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
            lastUpdaterUserId: 'cs0008', selectedCategory: CATEGORY,
            categories: [{name: CATEGORY, subcategories: []}],
            componentMetadata: {}, categorySpecificMetadata: {},
            isService: () => true, isResource: () => false,
            getComponentSubType: () => 'SERVICE', isAlreadyCertified: () => false
        };
    }

    async function render(component: any) {
        baseTypes$ = new Subject<any>();
        // A REAL NavigationService over a mocked Router/ActivatedRoute, so the route-data facade the
        // component depends on (getParam / setUnsavedChanges / navigate) behaves as it does at runtime.
        const router: any = {
            url: '/dashboard/workspace/id-1/service/general',
            events: new Subject<any>(),
            navigateByUrl: jest.fn(() => Promise.resolve(true))
        };
        const route: any = {snapshot: {params: {id: 'id-1'}, queryParams: {}, firstChild: null}};
        const navigationService = new NavigationService(router, route, {unsavedChanges: false} as any);

        const configure: ConfigureFn = (testBed) => {
            testBed.configureTestingModule({
                declarations: [GeneralTabComponent],
                // FormsModule for the standalone [(ngModel)] tag input, ReactiveFormsModule for the
                // [formGroup] the rest of the template sits in.
                imports: [FormsModule, ReactiveFormsModule],
                schemas: [NO_ERRORS_SCHEMA],
                providers: [
                    GeneralFormService,
                    ComponentMetadataService,
                    {provide: WorkspaceService, useValue: {component, isValidForm: true,
                                                           setComponent: jest.fn()}},
                    {provide: CacheService, useValue: {get: (k: string) => {
                        if (k === 'user') { return {userId: 'cs0008', role: 'DESIGNER'}; }
                        // ngOnInit -> onCategoryChange rebuilds component.categories from this list,
                        // and loadBaseTypesForSelectedCategory bails out if it comes back empty.
                        if (k === 'serviceCategories') { return [{name: CATEGORY, subcategories: []}]; }
                        return null;
                    }, set: jest.fn(), remove: jest.fn(), contains: () => false}},
                    {provide: EventListenerService, useValue: {registerObserverCallback: jest.fn(),
                                                               unRegisterObserver: jest.fn(),
                                                               notifyObservers: jest.fn()}},
                    {provide: FileUtilsService, useValue: {base64toBlob: jest.fn()}},
                    {provide: SdcUiServices.ModalService, useValue: {openErrorDetailModal: jest.fn()}},
                    {provide: TranslateService, useValue: {translate: (k: string) => k}},
                    {provide: SdcUiServices.NotificationsService, useValue: {push: jest.fn()}},
                    {provide: NavigationService, useValue: navigationService},
                    {provide: SdcConfigToken, useValue: {toscaFileExtension: 'yaml,yml', csarFileExtension: 'csar'}},
                    {provide: SdcMenuToken, useValue: {component_workspace_menu_option: {SERVICE: [{hiddenCategories: []}]}}},
                    {provide: ComponentFactory, useValue: {createComponent: (c: any) => Object.assign({}, c)}},
                    {provide: ImportVSPService, useValue: {}},
                    {provide: ModelService, useValue: {getModels: () => baseTypes$.asObservable().filter(() => false),
                                                      getModelsOfType: () => baseTypes$.asObservable().filter(() => false)}},
                    // Deferred on purpose — this is what makes the options arrive after the first CD pass.
                    {provide: ElementService, useValue: {getCategoryBaseTypes: () => baseTypes$.asObservable()}},
                    {provide: ModalsHandler, useValue: {}}
                ]
            });
        };
        const testBed = await configureTests(configure);
        fixture = testBed.createComponent(GeneralTabComponent);
        // First CD pass runs ngOnInit and writes [value] while `baseTypes` is still empty.
        fixture.detectChanges();
    }

    const control = (id: string): HTMLElement => fixture.nativeElement.querySelector('#' + id);
    // The component is OnPush, so the fixture's own detectChanges does not re-render it.
    const refresh = (): void => fixture.debugElement.injector.get(ChangeDetectorRef).detectChanges();

    beforeEach(async () => {
        await render(serviceComponent());
    });

    it('marks every field with a required label as aria-required', () => {
        const required = Array.from(fixture.nativeElement.querySelectorAll('label.required[for]'))
            .map((label: HTMLLabelElement) => label.htmlFor);
        expect(required).toEqual(['gt-name', 'gt-model', 'gt-category', 'gt-description', 'gt-contact-id']);
        required.forEach((id) => expect(control(id).getAttribute('aria-required')).toBe('true'));
        expect(control('gt-service-type').hasAttribute('aria-required')).toBe(false);
    });

    it('flags an invalid touched field and points it at its error message', () => {
        const name = fixture.componentInstance.form.get('name');
        expect(control('gt-name').hasAttribute('aria-invalid')).toBe(false);
        expect(control('gt-name').hasAttribute('aria-describedby')).toBe(false);

        name.setValue('');
        name.markAsTouched();
        refresh();
        expect(control('gt-name').getAttribute('aria-invalid')).toBe('true');
        const describedBy = control('gt-name').getAttribute('aria-describedby');
        expect(control(describedBy).textContent.trim()).toBe('Name is required.');

        name.setValue('MySvc2');
        refresh();
        expect(control('gt-name').hasAttribute('aria-invalid')).toBe(false);
        expect(control('gt-name').hasAttribute('aria-describedby')).toBe(false);
        expect(control(describedBy)).toBeNull();
    });

    it('gives every error message the id its field refers to', () => {
        const html = fs.readFileSync(path.join(__dirname, '..', 'general-tab.component.html'), 'utf8');
        const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
        const errors = Array.from(doc.querySelectorAll('.input-error'));
        expect(errors.length).toBeGreaterThan(0);
        errors.forEach((error) => {
            const field = doc.querySelector(`[\\[attr\\.aria-describedby\\]*="'${error.id}'"]`);
            expect(field).not.toBeNull();
            expect(error.getAttribute('*ngif')).toBe(
                field.getAttribute('[attr.aria-invalid]').replace(" ? 'true' : null", ''));
        });
    });
});

describe('required-field asterisk', () => {
    it('keeps the asterisk out of the accessible name with a fallback for older browsers', () => {
        const less = fs.readFileSync(path.join(__dirname, '../../../../../assets/styles/form-elements.less'), 'utf8');
        const rule = less.slice(less.indexOf('&.required::before'));
        const declarations = rule.slice(0, rule.indexOf('}')).match(/content:[^;]*;/g);
        expect(declarations).toEqual(["content: '*';", "content: '*' / '';"]);
    });
});
