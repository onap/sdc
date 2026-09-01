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

/**
 * RENDERING test for the Substitution Node Type (base type) selects.
 *
 * The old AngularJS general-view.html bound these two-way:
 *     data-ng-model="component.derivedFromGenericType"
 *     data-ng-options="type for type in baseTypes track by type"
 * ng-model re-syncs the DOM whenever either side changes, so an option list arriving late was
 * harmless. The migrated template binds ONE-way with [value], which the browser resolves against
 * the options that exist AT THAT MOMENT — and `baseTypes` is filled by an async
 * ElementService.getCategoryBaseTypes() call that resolves AFTER the first change detection.
 * The <select> therefore fell back to index 0 ("None") even though the model held the right
 * value: the user saw an empty Substitution Node Type on every existing Service and after every
 * Import Service (CSAR). The fix is [selected]="type === component?.derivedFromGenericType" on
 * the *ngFor option, which marks the option itself once it appears.
 *
 * This has to be a RENDERING test with a DEFERRED base-types response: the bug lives entirely in
 * the template's write-ordering and is invisible to AOT compilation and to the component-level
 * unit tests in this directory (which never render).
 */
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {EventListenerService} from 'app/ng2/services/event-listener.service';
import {CacheService} from 'app/ng2/services/cache.service';
import {SdcUiServices} from 'onap-ui-angular';
import {Subject} from 'rxjs/Subject';
import {ComponentFactory} from 'app/utils/component-factory';
import {ModalsHandler} from 'app/utils/modals-handler';
import {ConfigureFn, configureTests} from '../../../../../../jest/test-config.helper';
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

const NS = 'tosca.nodes.nfv.NS';
const CATEGORY = 'ETSI NFV Network Service';

describe('GeneralTabComponent - base type select rendering', () => {

    let fixture: ComponentFixture<GeneralTabComponent>;
    /** getCategoryBaseTypes response, pushed by the test AFTER the first change detection. */
    let baseTypes$: Subject<any>;

    function serviceComponent(overrides: any = {}) {
        return Object.assign({
            name: 'MySvc', description: 'd', contactId: 'cs0008', tags: [],
            uniqueId: 'id-1', componentType: 'SERVICE', lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
            lastUpdaterUserId: 'cs0008', selectedCategory: CATEGORY,
            categories: [{name: CATEGORY, subcategories: []}],
            derivedFromGenericType: NS, derivedFromGenericVersion: '1.0',
            componentMetadata: {}, categorySpecificMetadata: {},
            isService: () => true, isResource: () => false,
            getComponentSubType: () => 'SERVICE', isAlreadyCertified: () => false
        }, overrides);
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

    /** Emit the getCategoryBaseTypes response, i.e. fill the option lists. */
    function baseTypesArrive(payload: any = {required: false,
                                              baseTypes: [{toscaResourceName: NS, versions: ['1.0', '2.0']},
                                                          {toscaResourceName: 'tosca.nodes.nfv.PNF', versions: ['1.0']}]}) {
        baseTypes$.next(payload);
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

    /** Labels of the options carrying the `selected` property — i.e. what [selected] resolved to. */
    function markedOptions(testId: string): string[] {
        return Array.prototype.slice.call(select(testId).options)
            .filter((o: HTMLOptionElement) => o.selected)
            .map((o: HTMLOptionElement) => o.textContent.trim());
    }

    it('shows the stored base type once the async option list arrives (was falling back to "None")', async () => {
        await render(serviceComponent());
        // Before the response there is only the "None" option, so nothing else can be displayed.
        expect(select('selectBaseType').value).toBe('');

        baseTypesArrive();

        expect(fixture.componentInstance.baseTypes).toContain(NS);
        expect(select('selectBaseType').value).toBe(NS);
        // ...and the option the user actually sees is the right one, not the "None" fallback.
        expect(displayedOption('selectBaseType')).toBe(NS);
    });

    it('shows the stored base type version once its async option list arrives', async () => {
        await render(serviceComponent());

        baseTypesArrive();

        // Newest first (loadBaseTypes/initBaseTypes reverse the versions), so '1.0' is NOT index 0 —
        // without [selected] this select showed '2.0'.
        expect(fixture.componentInstance.baseTypeVersions).toEqual(['2.0', '1.0']);
        expect(select('selectBaseTypeVersion').value).toBe('1.0');
    });

    it('falls back to None when the component has no base type', async () => {
        await render(serviceComponent({derivedFromGenericType: undefined,
                                       derivedFromGenericVersion: undefined}));
        baseTypesArrive();

        // [selected] must not accidentally match a missing value: only the static empty-valued "None"
        // option may be marked, and the version select is not rendered at all in this state.
        expect(select('selectBaseType').value).toBe('');
        expect(markedOptions('selectBaseType')).toEqual(['None']);
        expect(displayedOption('selectBaseType')).toBe('None');
        expect(fixture.nativeElement.querySelector('[data-tests-id="selectBaseTypeVersion"]')).toBeNull();
    });
});
