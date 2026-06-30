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
import {NetworkCallFlowTabComponent} from '../network-call-flow-tab.component';

function makeRegistry() {
    return {
        loadOnBoarding: jest.fn((cb: () => void) => cb()),   // synchronous (cached-bundle path)
        render: jest.fn(),
        unmount: jest.fn()
    };
}

const INSTANCES = [
    {uniqueId: 'inst-1', name: 'vf-one', originType: 'VF'},
    {uniqueId: 'inst-2', name: 'vl-one', originType: 'VL'},
    {uniqueId: 'inst-3', name: 'vf-two', originType: 'VF'}
];

function makeComponent(overrides: any = {}) {
    return Object.assign({
        uniqueId: 'service-id-1',
        lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
        lastUpdaterUserId: 'cs0008',
        isService: () => true,
        isResource: () => false,
        artifacts: {filteredByType: jest.fn(() => ['ncf-artifact'])},
        componentInstances: INSTANCES
    }, overrides);
}

function createComp(opts: any = {}) {
    const hostEl: any = {tagName: 'DIV'};
    const component = 'component' in opts ? opts.component : makeComponent();
    const workspaceService: any = {component, getComponentMode: jest.fn(() => WorkspaceMode.EDIT)};
    const cacheService: any = {get: jest.fn((k: string) => k === 'user' ? {userId: 'cs0008'} : null)};
    const componentService: any = {
        getComponentInformationalArtifactsAndInstances: jest.fn(() => of({
            artifacts: {filteredByType: jest.fn(() => ['ncf-artifact'])},
            componentInstances: INSTANCES
        }))
    };
    const cdr: any = {detectChanges: jest.fn()};
    const el: any = {nativeElement: {querySelector: jest.fn(() => hostEl)}};
    const $injector: any = {get: jest.fn(() => ({generate: () => 'req-uuid'}))};
    const sdcConfig: any = {api: {root: '/sdc2/rest/v1/'}, cookie: {userIdSuffix: 'USER_ID', userFirstName: 'FN', userLastName: 'LN', userEmail: 'EM'}};

    const comp = new NetworkCallFlowTabComponent(
        workspaceService, cacheService, componentService, cdr, el, $injector, sdcConfig);
    return {comp, workspaceService, cacheService, componentService, cdr, el, hostEl};
}

describe('NetworkCallFlowTabComponent', () => {
    let registry: any;

    beforeEach(() => {
        registry = makeRegistry();
        (window as any).PunchOutRegistry = registry;
    });

    afterEach(() => {
        delete (window as any).PunchOutRegistry;
    });

    it('mounts the sequence-diagram punch-out with the NETWORK_CALL_FLOW model', () => {
        const {comp, hostEl} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();

        expect(registry.loadOnBoarding).toHaveBeenCalledTimes(1);
        expect(registry.render).toHaveBeenCalledTimes(1);
        const [props, el] = registry.render.mock.calls[0];
        expect(props.name).toBe('sequence-diagram');
        expect(props.options.data.diagramType).toBe('NETWORK_CALL_FLOW');
        expect(props.options.data.serviceID).toBe('service-id-1');
        expect(el).toBe(hostEl);
    });

    it('derives one participant lane per VF component instance', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();
        const participants = registry.render.mock.calls[0][0].options.data.participants;
        expect(participants).toHaveLength(2);   // 2 VF, the VL is excluded
        expect(participants.map((p: any) => p.id)).toEqual(['inst-1', 'inst-3']);
        expect(participants.map((p: any) => p.name)).toEqual(['vf-one', 'vf-two']);
    });

    it('fetches artifacts AND instances only when either is missing', () => {
        const {comp, componentService} = createComp({component: makeComponent({componentInstances: undefined})});
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(componentService.getComponentInformationalArtifactsAndInstances).toHaveBeenCalledTimes(1);
        expect(registry.render).toHaveBeenCalledTimes(1);
        expect(registry.render.mock.calls[0][0].options.data.diagramType).toBe('NETWORK_CALL_FLOW');
    });

    it('does NOT fetch when artifacts and instances are both present', () => {
        const {comp, componentService} = createComp();
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(componentService.getComponentInformationalArtifactsAndInstances).not.toHaveBeenCalled();
        expect(registry.render).toHaveBeenCalledTimes(1);
    });

    it('renders editable (readonly=false) in EDIT mode', () => {
        const {comp} = createComp();   // default mock returns WorkspaceMode.EDIT
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(registry.render.mock.calls[0][0].options.data.readonly).toBe(false);
    });

    it('renders read-only (readonly=true) when the workspace mode is not EDIT', () => {
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

    it('does nothing when there is no working component', () => {
        const {comp, componentService} = createComp({component: null});
        comp.ngOnInit();
        comp.ngAfterViewInit();
        expect(componentService.getComponentInformationalArtifactsAndInstances).not.toHaveBeenCalled();
        expect(registry.render).not.toHaveBeenCalled();
    });
});
