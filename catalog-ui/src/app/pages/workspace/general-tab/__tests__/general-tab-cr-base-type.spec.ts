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
import {ComponentFixture} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {EventListenerService} from 'app/services/event-listener.service';
import {CacheService} from 'app/services/cache.service';
import {SdcUiServices} from 'onap-ui-angular';
import {Subject} from 'rxjs';
import {filter} from 'rxjs/operators';
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

const RAN = 'org.openecomp.resource.abstract.nodes.RANTemplateExecutionTask';
const DAY0 = 'org.openecomp.resource.abstract.nodes.DayZeroGenerationTask';
const GENERIC_CR = 'org.openecomp.resource.abstract.nodes.CR';

describe('GeneralTabComponent - CR base type select', () => {

    let fixture: ComponentFixture<GeneralTabComponent>;
    let baseTypes$: Subject<any>;
    let elementService: any;

    function crComponent(overrides: any = {}) {
        return Object.assign({
            name: 'MyTask', description: 'd', contactId: 'cs0008', tags: [], vendorName: 'v', vendorRelease: '1',
            componentType: 'RESOURCE', resourceType: 'CR', lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
            lastUpdaterUserId: 'cs0008', selectedCategory: 'Task_#_RANTemplateExecutionTask',
            categories: [{name: 'Task', subcategories: [{name: 'RANTemplateExecutionTask'}]}],
            componentMetadata: {}, categorySpecificMetadata: {},
            isService: () => false, isResource: () => true, isCsarComponent: () => false,
            getComponentSubType: () => 'CR', isAlreadyCertified: () => false
        }, overrides);
    }

    async function render(component: any, {create = true, crBaseTypes = {Task: [RAN, DAY0]} as any} = {}) {
        baseTypes$ = new Subject<any>();
        elementService = {getCategoryBaseTypes: jest.fn(() => baseTypes$.asObservable())};
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
                        if (k === 'resourceCategories') { return [{name: 'Task', subcategories: [{name: 'RANTemplateExecutionTask'}]},
                                                           {name: 'Generic', subcategories: [{name: 'Abstract'}]}]; }
                        if (k === 'crBaseTypes') { return crBaseTypes; }
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
                    {provide: ModelService, useValue: {getModels: () => baseTypes$.asObservable().pipe(filter(() => false)),
                                                      getModelsOfType: () => baseTypes$.asObservable().pipe(filter(() => false))}},
                    {provide: ElementService, useValue: elementService},
                    {provide: ModalsHandler, useValue: {}}
                ]
            });
        };
        const testBed = await configureTests(configure);
        fixture = testBed.createComponent(GeneralTabComponent);
        // First CD pass runs ngOnInit and writes [value] while `baseTypes` is still empty.
        fixture.detectChanges();
    }

    function select(testId: string): HTMLSelectElement {
        const el: HTMLSelectElement = fixture.nativeElement.querySelector(`[data-tests-id="${testId}"]`);
        expect(el).toBeTruthy();
        return el;
    }

    /** The label the user actually reads in the closed <select>. */
    function displayedOption(testId: string): string {
        const el = select(testId);
        // jsdom does not implement selectedOptions, so resolve via selectedIndex.
        const option = el.options[el.selectedIndex];
        return option ? option.textContent.trim() : null;
    }

    const crSelect = () => fixture.nativeElement.querySelector('[data-tests-id="selectCrBaseType"]') as HTMLSelectElement;
    const optionValues = () => Array.prototype.map.call(crSelect().options, (o: HTMLOptionElement) => o.value);

    it('offers the configured types for the CR category', async () => {
        await render(crComponent());
        expect(crSelect().disabled).toBe(false);
        expect(optionValues()).toEqual(['', RAN, DAY0]);
        expect(displayedOption('selectCrBaseType')).toBe('Default');
    });

    it('puts the chosen type, without a version, on the component', async () => {
        await render(crComponent());
        fixture.componentInstance.onCrBaseTypeChange(RAN);
        expect(fixture.componentInstance.component.derivedFromGenericType).toBe(RAN);
        expect(fixture.componentInstance.component.derivedFromGenericVersion).toBeUndefined();
        fixture.componentInstance.onCrBaseTypeChange('');
        expect(fixture.componentInstance.component.derivedFromGenericType).toBeUndefined();
    });

    it('shows nothing without configuration', async () => {
        await render(crComponent(), {crBaseTypes: {}});
        expect(crSelect()).toBeNull();
        expect(fixture.componentInstance.component.derivedFromGenericType).toBeUndefined();
    });

    it('shows nothing for an unlisted category', async () => {
        await render(crComponent({selectedCategory: 'Generic_#_Abstract',
                                  categories: [{name: 'Generic', subcategories: [{name: 'Abstract'}]}]}));
        expect(crSelect()).toBeNull();
    });

    it('drops the chosen type when the category changes to an unlisted one', async () => {
        await render(crComponent());
        fixture.componentInstance.onCrBaseTypeChange(RAN);
        fixture.componentInstance.form.get('category').setValue('Generic_#_Abstract');
        fixture.componentInstance.onCategoryChange();
        fixture.detectChanges();
        expect(fixture.componentInstance.component.derivedFromGenericType).toBeUndefined();
        expect(crSelect()).toBeNull();
    });

    it('keeps the chosen type when the new category lists it too', async () => {
        await render(crComponent());
        fixture.componentInstance.onCrBaseTypeChange(RAN);
        fixture.componentInstance.onCategoryChange();
        expect(fixture.componentInstance.component.derivedFromGenericType).toBe(RAN);
    });

    it('never offers a base type on a VF', async () => {
        await render(crComponent({resourceType: 'VF', getComponentSubType: () => 'VF'}));
        expect(crSelect()).toBeNull();
    });

    it('does not ask for Service base types for a CR', async () => {
        await render(crComponent());
        expect(elementService.getCategoryBaseTypes).not.toHaveBeenCalled();
    });

    it('shows no base type on an existing CR', async () => {
        await render(crComponent({uniqueId: 'id-1', derivedFromGenericType: GENERIC_CR}), {create: false});
        expect(crSelect()).toBeNull();
    });
});
