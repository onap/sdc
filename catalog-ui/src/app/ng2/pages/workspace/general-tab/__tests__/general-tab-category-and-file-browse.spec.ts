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
 * Two regressions found by running the deployed master UI against a 1.16.2 cluster. Both were green
 * in Jest, AOT and Selenium, so they are pinned here.
 *
 * A. Category dropdown emptied on every RESOURCE that has a model.
 *    The port of GeneralViewModel.initModel dropped its `componentType === SERVICE` guard around
 *    filterCategoriesByModel. Only SERVICE categories carry a `models` array; resource categories
 *    have models === null, so filtering a resource whose component.model is set removed every
 *    option and lost the stored selection (live: 10 cached categories -> 1 rendered option).
 *
 * B. Raw unstyled <input type="file"> on the General tab.
 *    The same commit replaced the styled <file-upload> directive with a bare native input gated on
 *    `!isVspImport()` — true for nearly every component — where the old form defaulted
 *    isShowFileBrowse to FALSE and enabled it in only four narrow cases. The native picker also
 *    never wrote component.importedFile, so a selected file could not reach the create payload.
 */
import {of} from 'rxjs';
import {Subject} from 'rxjs/Subject';
import {ComponentMetadataService} from '../component-metadata.service';
import {GeneralFormService} from '../general-form.service';
import {GeneralTabComponent} from '../general-tab.component';
import {NavigationService} from '../../../../services/navigation.service';

const RESOURCE_CATEGORIES = [
    {name: 'Generic', models: null, subcategories: [{name: 'Abstract'}, {name: 'Database'}]},
    {name: 'Network L2-3', models: null, subcategories: [{name: 'Router'}]},
    {name: 'Infrastructure', models: null, subcategories: [{name: 'Compute'}]}
];

const SERVICE_CATEGORIES = [
    {name: 'Network Service', models: ['SDC AID DEFAULT MODEL'], subcategories: []},
    {name: 'ETSI NFV Network Service', models: ['ETSI SOL001 v2.5.1'], subcategories: []}
];

function makeResource(overrides: any = {}) {
    return Object.assign({
        name: 'MyVFC', description: 'desc', vendorName: 'v', vendorRelease: '1',
        contactId: 'cs0008', tags: [], selectedCategory: 'Infrastructure',
        lifecycleState: 'NOT_CERTIFIED_CHECKOUT', lastUpdaterUserId: 'cs0008',
        uniqueId: 'id-1', componentType: 'RESOURCE', resourceType: 'VFC',
        isService: () => false, isResource: () => true,
        getComponentSubType: () => 'VFC', isAlreadyCertified: () => false,
        componentMetadata: {}, categorySpecificMetadata: {}
    }, overrides);
}

function makeServiceComponent(overrides: any = {}) {
    return Object.assign(makeResource(), {
        name: 'MySvc', componentType: 'SERVICE', resourceType: undefined,
        isService: () => true, isResource: () => false,
        getComponentSubType: () => 'SERVICE',
        selectedCategory: 'Network Service',
        categories: [{name: 'Network Service', subcategories: []}],
        instantiationType: '', environmentContext: undefined, serviceType: 'Service'
    }, overrides);
}

function makeNavigationService(stateParams: any = {id: 'id-1'}) {
    const router: any = {
        url: '/dashboard/workspace' + (stateParams.id ? '/' + stateParams.id : '') + '/resource/general',
        events: new Subject<any>(),
        navigateByUrl: jest.fn(() => Promise.resolve(true))
    };
    const route: any = {snapshot: {params: stateParams, queryParams: {}, firstChild: null}};
    return new NavigationService(router, route, {unsavedChanges: false} as any);
}

function createComp(opts: any = {}) {
    const component = opts.component || makeResource();
    const cache: any = opts.cache || {};
    const workspaceService: any = {component, isValidForm: true, setComponent: jest.fn()};
    const cacheService: any = {get: jest.fn((k: string) => {
        if (k === 'user') { return {userId: 'cs0008', role: 'DESIGNER'}; }
        if (cache[k] !== undefined) { return cache[k]; }
        return null;
    }), set: jest.fn(), remove: jest.fn(), contains: jest.fn(() => false)};
    const comp = new GeneralTabComponent(
        new GeneralFormService(), new ComponentMetadataService(),
        workspaceService, cacheService,
        {registerObserverCallback: jest.fn(), unRegisterObserver: jest.fn(), notifyObservers: jest.fn()} as any,
        {base64toBlob: jest.fn()} as any,
        {openErrorDetailModal: jest.fn()} as any,
        {translate: (k: string) => k} as any,
        {push: jest.fn()} as any,
        {detectChanges: jest.fn()} as any,
        opts.navigationService || makeNavigationService(opts.stateParams),
        {toscaFileExtension: 'yaml,yml', csarFileExtension: 'csar'} as any,
        {component_workspace_menu_option: {VFC: [{hiddenCategories: []}], SERVICE: [{hiddenCategories: []}]}} as any,
        {createComponent: (c: any) => Object.assign({}, c)} as any,
        {} as any,
        {getModels: jest.fn(() => of([])), getModelsOfType: jest.fn(() => of([]))} as any,
        {getCategoryBaseTypes: jest.fn(() => of({required: false, baseTypes: []}))} as any,
        {} as any
    );
    return {comp, component, workspaceService};
}

// ─── A. Category dropdown must survive a RESOURCE that carries a model ────────────────────────
describe('GeneralTabComponent - category list vs component.model', () => {

    it('keeps every resource category when the resource has a model', () => {
        const {comp} = createComp({
            component: makeResource({model: 'ETSI SOL001 v2.5.1'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        // Without the SERVICE guard around filterCategoriesByModel this is 0: resource categories
        // all carry models === null, so the model-name branch of the predicate rejects them all.
        expect(comp.categories.length).toBe(3);
        expect(comp.categories.map((c: any) => c.name).sort())
            .toEqual(['Generic', 'Infrastructure', 'Network L2-3']);
    });

    it('keeps the stored category selectable on a resource with a model', () => {
        const {comp} = createComp({
            component: makeResource({model: 'ETSI SOL001 v2.5.1', selectedCategory: 'Infrastructure'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        expect(comp.form.get('category').value).toBe('Infrastructure');
        expect(comp.categories.some((c: any) => c.name === 'Infrastructure')).toBe(true);
    });

    it('keeps every resource category when the resource has NO model', () => {
        const {comp} = createComp({cache: {resourceCategories: RESOURCE_CATEGORIES}});
        comp.ngOnInit();
        expect(comp.categories.length).toBe(3);
    });

    it('still filters SERVICE categories by model (the behaviour the guard protects)', () => {
        const {comp} = createComp({
            component: makeServiceComponent({model: 'ETSI SOL001 v2.5.1', selectedCategory: 'ETSI NFV Network Service'}),
            cache: {serviceCategories: SERVICE_CATEGORIES}
        });
        comp.ngOnInit();
        expect(comp.categories.map((c: any) => c.name)).toEqual(['ETSI NFV Network Service']);
    });
});

// ─── B. The file-browse widget's visibility and capture ───────────────────────────────────────
describe('GeneralTabComponent - file browse visibility', () => {

    it('hides the file picker on a plain VFC with no imported file', () => {
        const {comp} = createComp({cache: {resourceCategories: RESOURCE_CATEGORIES}});
        comp.ngOnInit();
        // The regression showed a native picker here — on every VFC, including certified/view-mode.
        expect(comp.isShowFileBrowse).toBe(false);
    });

    it('shows it for a resource that arrived with an imported file', () => {
        const {comp} = createComp({
            component: makeResource({importedFile: {filename: 'x.yaml', base64: ''}}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        expect(comp.isShowFileBrowse).toBe(true);
        expect(comp.importedFileExtensionsWithDot).toBe('.yaml,.yml');
    });

    it('shows it for a VF with no VSP behind it, with the CSAR extension list', () => {
        const {comp} = createComp({
            component: makeResource({resourceType: 'VF', getComponentSubType: () => 'VF'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        expect(comp.isShowFileBrowse).toBe(true);
        expect(comp.browseFileLabel).toBe('Upload File:');
    });

    it('hides it for a VF that DOES have a VSP behind it', () => {
        const {comp} = createComp({
            component: makeResource({resourceType: 'VF', csarUUID: 'vsp-1', getComponentSubType: () => 'VF'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        expect(comp.isShowFileBrowse).toBe(false);
    });

    it('labels a VFC upload "Upload VFC:"', () => {
        const {comp} = createComp({
            component: makeResource({importedFile: {filename: 'x.yaml', base64: ''}}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        expect(comp.browseFileLabel).toBe('Upload VFC:');
    });
});

describe('GeneralTabComponent - file browse capture', () => {

    function fileEvent(name: string, size: number) {
        return {target: {files: [{name, size, type: 'application/octet-stream'}], value: name}};
    }

    beforeEach(() => {
        // jsdom has no FileReader.readAsBinaryString worth driving; stub it to resolve immediately.
        (global as any).FileReader = class {
            public onload: any;
            public result: any;
            public readAsBinaryString(_f: any) { this.result = 'RAW'; this.onload(); }
        };
    });

    it('writes the picked file onto component.importedFile so the create payload can carry it', () => {
        const {comp, component} = createComp({
            component: makeResource({resourceType: 'VF', getComponentSubType: () => 'VF'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        comp.onImportFileChange(fileEvent('vf.csar', 2048));
        // The raw <input> the regression shipped left importedFile undefined, so payloadData was
        // never populated and the upload silently did nothing.
        expect(component.importedFile).toBeDefined();
        expect(component.importedFile.filename).toBe('vf.csar');
        expect(component.importedFile.filesize).toBe(2048);
        expect(component.importedFile.base64).toBe(btoa('RAW'));
    });

    it('rejects an empty file', () => {
        const {comp, component} = createComp({
            component: makeResource({resourceType: 'VF', getComponentSubType: () => 'VF'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        comp.onImportFileChange(fileEvent('vf.csar', 0));
        expect(comp.importedFileError).toBe('VALIDATION_ERROR_EMPTY_FILE');
        expect(component.importedFile).toBeUndefined();
    });

    it('rejects a file whose extension is not in the accepted list', () => {
        const {comp, component} = createComp({
            component: makeResource({resourceType: 'VF', getComponentSubType: () => 'VF'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        comp.onImportFileChange(fileEvent('vf.zip', 100));
        expect(comp.importedFileError).toBe('NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS');
        expect(component.importedFile).toBeUndefined();
    });

    it('rejects a file over the 10 MB limit', () => {
        const {comp, component} = createComp({
            component: makeResource({resourceType: 'VF', getComponentSubType: () => 'VF'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        comp.onImportFileChange(fileEvent('vf.csar', 10240 * 1024 + 1));
        expect(comp.importedFileError).toBe('VALIDATION_ERROR_MAX_FILE_SIZE');
        expect(component.importedFile).toBeUndefined();
    });

    it('clears the picked file and the error on the x button', () => {
        const {comp, component} = createComp({
            component: makeResource({resourceType: 'VF', getComponentSubType: () => 'VF'}),
            cache: {resourceCategories: RESOURCE_CATEGORIES}
        });
        comp.ngOnInit();
        comp.onImportFileChange(fileEvent('vf.csar', 2048));
        comp.clearImportedFile();
        expect(component.importedFile).toBeUndefined();
        expect(comp.importedFileError).toBeNull();
    });

    it('blanks input.value on click so re-picking the same file still fires change', () => {
        const {comp} = createComp({cache: {resourceCategories: RESOURCE_CATEGORIES}});
        comp.ngOnInit();
        const target: any = {value: 'vf.csar'};
        comp.onImportFileClick({target});
        expect(target.value).toBeNull();
    });
});
