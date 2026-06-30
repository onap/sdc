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
import {of} from 'rxjs';
import {WorkspaceMode} from 'app/utils/constants';
import {ManagementWorkflowTabComponent} from '../management-workflow-tab.component';

function makeRegistry() {
    return {
        loadOnBoarding: jest.fn((cb: () => void) => cb()),   // synchronous (cached-bundle path)
        render: jest.fn(),
        unmount: jest.fn()
    };
}

function makeComponent(overrides: any = {}) {
    return Object.assign({
        uniqueId: 'service-id-1',
        lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
        lastUpdaterUserId: 'cs0008',
        isService: () => true,
        isResource: () => false,
        artifacts: {filteredByType: jest.fn(() => ['wf-artifact'])}
    }, overrides);
}

function createComp(opts: any = {}) {
    const hostEl: any = {tagName: 'DIV'};
    const component = 'component' in opts ? opts.component : makeComponent();
    const workspaceService: any = {
        component,
        getComponentMode: jest.fn(() => WorkspaceMode.EDIT)
    };
    const cacheService: any = {get: jest.fn((k: string) => k === 'user' ? {userId: 'cs0008', firstName: 'C', lastName: 'S', email: 'cs@x'} : null)};
    const componentService: any = {
        getComponentInformationalArtifacts: jest.fn(() => of({artifacts: {filteredByType: jest.fn(() => ['wf-artifact'])}}))
    };
    const cdr: any = {detectChanges: jest.fn()};
    const el: any = {nativeElement: {querySelector: jest.fn(() => hostEl)}};
    const $injector: any = {get: jest.fn(() => ({generate: () => 'req-uuid'}))};
    const sdcConfig: any = {api: {root: '/sdc2/rest/v1/'}, cookie: {userIdSuffix: 'USER_ID', userFirstName: 'FN', userLastName: 'LN', userEmail: 'EM'}};

    const comp = new ManagementWorkflowTabComponent(
        workspaceService, cacheService, componentService, cdr, el, $injector, sdcConfig);
    return {comp, workspaceService, cacheService, componentService, cdr, el, hostEl};
}

describe('ManagementWorkflowTabComponent', () => {
    let registry: any;

    beforeEach(() => {
        registry = makeRegistry();
        (window as any).PunchOutRegistry = registry;
    });

    afterEach(() => {
        delete (window as any).PunchOutRegistry;
    });

    it('loads the onboarding bundle and mounts the sequence-diagram punch-out with the WORKFLOW model', () => {
        const {comp, hostEl} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();

        expect(registry.loadOnBoarding).toHaveBeenCalledTimes(1);
        expect(registry.render).toHaveBeenCalledTimes(1);
        const [props, el] = registry.render.mock.calls[0];
        expect(props.name).toBe('sequence-diagram');
        expect(props.options.data.diagramType).toBe('WORKFLOW');
        expect(props.options.data.serviceID).toBe('service-id-1');
        expect(props.options.apiRoot).toBe('/sdc2/rest/v1/');
        expect(el).toBe(hostEl);
    });

    it('uses the 11 hardcoded ONAP participant lanes', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();
        const participants = registry.render.mock.calls[0][0].options.data.participants;
        expect(participants).toHaveLength(11);
        expect(participants.map((p: any) => p.name)).toEqual(
            ['Customer', 'CCD', 'Infrastructure', 'MSO', 'SDN-C', 'A&AI', 'APP-C', 'Cloud', 'DCAE', 'ALTS', 'VF']);
    });

    it('fetches informational artifacts only when the component has none cached', () => {
        const {comp, componentService} = createComp({component: makeComponent({artifacts: undefined})});
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(componentService.getComponentInformationalArtifacts).toHaveBeenCalledTimes(1);
        expect(registry.render).toHaveBeenCalledTimes(1);
        expect(registry.render.mock.calls[0][0].options.data.diagramType).toBe('WORKFLOW');
    });

    it('does NOT fetch artifacts when they are already present', () => {
        const {comp, componentService} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(componentService.getComponentInformationalArtifacts).not.toHaveBeenCalled();
        expect(registry.render).toHaveBeenCalledTimes(1);
    });

    it('sets readonly=false when the workspace mode is EDIT', () => {
        const {comp} = createComp();   // default mock returns WorkspaceMode.EDIT
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(registry.render.mock.calls[0][0].options.data.readonly).toBe(false);
    });

    it('sets readonly=true when the workspace mode is not EDIT', () => {
        const {comp, workspaceService} = createComp();
        workspaceService.getComponentMode.mockReturnValue(WorkspaceMode.VIEW);
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(registry.render.mock.calls[0][0].options.data.readonly).toBe(true);
    });

    it('unmounts the punch-out on destroy', () => {
        const {comp, hostEl} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();
        comp.ngOnDestroy();
        expect(registry.unmount).toHaveBeenCalledWith(hostEl);
    });

    it('does nothing (and does not render) when there is no working component', () => {
        const {comp, componentService} = createComp({component: null});
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(componentService.getComponentInformationalArtifacts).not.toHaveBeenCalled();
        expect(registry.render).not.toHaveBeenCalled();
        expect(comp.isLoading).toBe(false);
    });

    it('does not unmount when nothing was ever mounted', () => {
        const {comp} = createComp({component: null});
        comp.ngOnInit();
        comp.ngAfterViewInit();
        comp.ngOnDestroy();
        expect(registry.unmount).not.toHaveBeenCalled();
    });
});
