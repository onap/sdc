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

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {HttpErrorResponse, HttpRequest, HttpResponse} from '@angular/common/http';
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
import {ToscaTypesService} from '../../../../services/tosca-types.service';
import {FileUtilsService} from '../../../../services/file-utils.service';
import {NavigationService} from '../../../../services/navigation.service';
import {TranslateService} from '../../../../shared/translator/translate.service';
import {WorkspaceService} from '../../workspace.service';
import {ComponentMetadataService} from '../component-metadata.service';
import {GeneralFormService} from '../general-form.service';
import {GeneralTabComponent} from '../general-tab.component';

const GENERIC_VF = 'org.openecomp.resource.abstract.nodes.VF';
const GENERIC_CR = 'org.openecomp.resource.abstract.nodes.CR';

describe('GeneralTabComponent - CR base type select', () => {

    let fixture: ComponentFixture<GeneralTabComponent>;
    let baseTypes$: Subject<any>;
    let elementService: any;
    let toscaTypesService: any;
    let requests: Array<{request: HttpRequest<any>, model: string, resolve: (types: any) => void, reject: (error: any) => void}>;

    function crComponent(overrides: any = {}) {
        return Object.assign({
            name: 'MyResource', description: 'd', contactId: 'cs0008', tags: [], vendorName: 'v', vendorRelease: '1',
            componentType: 'RESOURCE', resourceType: 'CR', lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
            lastUpdaterUserId: 'cs0008', selectedCategory: 'Generic_#_Abstract',
            categories: [{name: 'Generic', subcategories: [{name: 'Abstract'}]}],
            componentMetadata: {}, categorySpecificMetadata: {},
            isService: () => false, isResource: () => true, isCsarComponent: () => false,
            getComponentSubType: () => 'CR', isAlreadyCertified: () => false
        }, overrides);
    }

    async function render(component: any, {create = true} = {}) {
        baseTypes$ = new Subject<any>();
        elementService = {getCategoryBaseTypes: jest.fn(() => baseTypes$.asObservable())};
        requests = [];
        const backend: any = {handle: (request: HttpRequest<any>) => {
            const response = new Subject<HttpResponse<any>>();
            requests.push({request, model: request.params.get('model'),
                resolve: (body) => {
                    response.next(new HttpResponse({body}));
                    response.complete();
                },
                reject: (error) => response.error(error)});
            return response.asObservable();
        }};
        toscaTypesService = new ToscaTypesService(null,
            {api: {root: '/sdc1/feProxy/rest', component_api_root: '/v1/catalog/'}} as any,
            backend, {getUserIdSuffix: () => 'USER_ID', getUserId: () => 'cs0008'} as any);
        // A REAL NavigationService over a mocked Router/ActivatedRoute, so the route-data facade the
        // component depends on (getParam / setUnsavedChanges / navigate) behaves as it does at runtime.
        const router: any = {
            url: '/dashboard/workspace/resource/general',
            events: new Subject<any>(),
            navigateByUrl: jest.fn(() => Promise.resolve(true))
        };
        const route: any = {snapshot: {params: create ? {} : {id: 'id-1'}, queryParams: {}, firstChild: null}};
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
                        if (k === 'resourceCategories') { return [{name: 'Generic', subcategories: [{name: 'Abstract'}, {name: 'Network Elements'}]}]; }
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
                    {provide: SdcMenuToken, useValue: {component_workspace_menu_option: {SERVICE: [{hiddenCategories: []}], RESOURCE: [{hiddenCategories: []}]}}},
                    {provide: ComponentFactory, useValue: {createComponent: (c: any) => Object.assign({}, c)}},
                    {provide: ImportVSPService, useValue: {}},
                    {provide: ModelService, useValue: {getModels: () => baseTypes$.asObservable().filter(() => false),
                                                      getModelsOfType: () => baseTypes$.asObservable().filter(() => false)}},
                    {provide: ElementService, useValue: elementService},
                    {provide: ToscaTypesService, useValue: toscaTypesService},
                    {provide: ModalsHandler, useValue: {}}
                ]
            });
        };
        const testBed = await configureTests(configure);
        fixture = testBed.createComponent(GeneralTabComponent);
        // First CD pass runs ngOnInit and writes [value] while `baseTypes` is still empty.
        fixture.detectChanges();
    }

    const crInput = () => fixture.nativeElement.querySelector('[data-tests-id="selectCrBaseType"]') as HTMLInputElement;
    const optionValues = () => Array.from(fixture.nativeElement.querySelectorAll('#gt-cr-base-types option'))
        .map((option: HTMLOptionElement) => option.value);

    async function flush() {
        await fixture.whenStable();
        fixture.detectChanges();
    }

    function enterType(value: string) {
        crInput().value = value;
        crInput().dispatchEvent(new Event('input'));
        fixture.detectChanges();
    }

    it('offers runtime TOSCA names without requiring configuration or selecting a default', async () => {
        await render(crComponent());
        expect(crInput()).toBeTruthy();
        expect(crInput().disabled).toBe(false);
        expect(requests.length).toBe(1);
        expect(requests[0].model).toBeFalsy();
        expect(requests[0].request.url).toBe('/sdc1/feProxy/rest/v1/catalog/nodeTypes');
        expect(requests[0].request.headers.get('USER_ID')).toBe('cs0008');
        requests[0].resolve({[GENERIC_VF]: {componentMetadataDefinition: {}}, [GENERIC_CR]: {componentMetadataDefinition: {}}});
        await flush();
        expect(optionValues()).toEqual([GENERIC_CR, GENERIC_VF]);
        expect(crInput().value).toBe('');
        expect(fixture.componentInstance.component.derivedFromGenericType).toBeUndefined();
    });

    it('accepts a name absent from suggestions and clears its version, with empty input restoring default', async () => {
        await render(crComponent({derivedFromGenericVersion: '1.0'}));
        enterType('example.nodes.CustomBase');
        expect(fixture.componentInstance.component.derivedFromGenericType).toBe('example.nodes.CustomBase');
        expect(fixture.componentInstance.component.derivedFromGenericVersion).toBeUndefined();
        enterType('');
        expect(fixture.componentInstance.component.derivedFromGenericType).toBeUndefined();
    });

    it('preserves an explicit base when the category changes or is cleared', async () => {
        await render(crComponent({derivedFromGenericType: GENERIC_VF}));
        expect(crInput().value).toBe(GENERIC_VF);
        for (const category of ['Generic_#_Network Elements', '']) {
            fixture.componentInstance.form.get('category').setValue(category);
            fixture.componentInstance.onCategoryChange();
            fixture.detectChanges();
            expect(crInput().value).toBe(GENERIC_VF);
            expect(fixture.componentInstance.component.derivedFromGenericType).toBe(GENERIC_VF);
        }
    });

    it('refreshes for the model and ignores an older response without clearing the entered name', async () => {
        await render(crComponent({model: 'Model A'}));
        expect(requests[0].model).toBe('Model A');
        enterType(GENERIC_VF);
        fixture.componentInstance.form.get('model').setValue('Model B');
        fixture.componentInstance.onModelChange();
        expect(requests[1].model).toBe('Model B');
        requests[1].resolve({[GENERIC_CR]: {}});
        await flush();
        requests[0].resolve({[GENERIC_VF]: {}});
        await flush();
        expect(optionValues()).toEqual([GENERIC_CR]);
        expect(crInput().value).toBe(GENERIC_VF);
        fixture.componentInstance.form.get('model').setValue('');
        fixture.componentInstance.onModelChange();
        fixture.detectChanges();
        expect(requests[2].model).toBeFalsy();
        expect(optionValues()).toEqual([]);
    });

    it('allows manual entry after discovery fails and retries discovery', async () => {
        await render(crComponent());
        requests[0].reject(new HttpErrorResponse({status: 503}));
        await flush();
        expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
        enterType(GENERIC_VF);
        expect(fixture.componentInstance.component.derivedFromGenericType).toBe(GENERIC_VF);
        fixture.nativeElement.querySelector('[data-tests-id="retryCrBaseTypes"]').click();
        expect(requests.length).toBe(2);
        requests[1].resolve({[GENERIC_VF]: {}});
        await flush();
        expect(optionValues()).toEqual([GENERIC_VF]);
        expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
    });

    it('ignores an outstanding response after destruction', async () => {
        await render(crComponent());
        fixture.destroy();
        requests[0].resolve({[GENERIC_VF]: {}});
        await Promise.resolve();
        expect(fixture.componentInstance.crBaseTypeOptions()).toEqual([]);
    });

    it('never offers a base type on a VF', async () => {
        await render(crComponent({resourceType: 'VF', getComponentSubType: () => 'VF'}));
        expect(crInput()).toBeNull();
        expect(requests.length).toBe(0);
    });

    it('does not ask for Service base types for a CR', async () => {
        await render(crComponent());
        expect(elementService.getCategoryBaseTypes).not.toHaveBeenCalled();
    });

    it('shows no base type on an existing CR', async () => {
        await render(crComponent({uniqueId: 'id-1', derivedFromGenericType: GENERIC_CR}), {create: false});
        expect(crInput()).toBeNull();
        expect(requests.length).toBe(0);
    });
});
