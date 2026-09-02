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
import * as _ from 'lodash';
// The component under test calls `_.isUndefined` / `_.forEach` off the bare global that webpack
// supplies via script-loader (lodash.min.js); Jest has no such global.
(window as any)._ = _;

import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import {Observable} from 'rxjs/Observable';
import {WorkspaceMode} from 'app/utils/constants';
import {InterfaceDefinitionComponent} from './interface-definition.page.component';

function makeComponent(overrides: any = {}) {
    return Object.assign({
        uniqueId: 'abc',
        componentType: 'SERVICE',
        model: null,
        isService: () => true
    }, overrides);
}

function createComp(opts: any = {}) {
    const component = 'component' in opts ? opts.component : makeComponent();
    const workspaceService: any = {
        component,
        getComponentMode: jest.fn(() => opts.mode || WorkspaceMode.EDIT)
    };
    const sdcConfig: any = {enableWorkflowAssociation: false};
    const navigationService: any = {navigate: jest.fn()};
    const notificationsService: any = {push: jest.fn()};
    const translateService: any = {
        languageChangedObservable: Observable.of('en_US'),
        translate: jest.fn((k: string) => k)
    };
    const componentService: any = {
        getInterfaceOperations: jest.fn(() => Observable.of({interfaces: []})),
        getComponentInputs: jest.fn(() => Observable.of({inputs: []})),
        getInterfaceTypes: jest.fn(() => Observable.of({})),
        getCapabilitiesAndRequirements: jest.fn(() => Observable.of({capabilities: {}})),
        getComponentResourcePropertiesData: jest.fn(() => Observable.of({componentInstances: []}))
    };
    const modalService: any = {};
    const modalServiceSdcUI: any = {};
    const topologyTemplateService: any = {};
    const toscaArtifactService: any = {getToscaArtifacts: jest.fn(() => Observable.of(null))};
    const workflowService: any = {getWorkflows: jest.fn(() => Observable.of([]))};
    const pluginsService: any = {getPluginByStateUrl: jest.fn(() => undefined)};

    const comp = new InterfaceDefinitionComponent(
        sdcConfig, navigationService, notificationsService, translateService, componentService,
        modalService, modalServiceSdcUI, topologyTemplateService, toscaArtifactService,
        componentService, workflowService, modalServiceSdcUI, pluginsService, workspaceService);
    return {comp, component, workspaceService, componentService};
}

describe('InterfaceDefinitionComponent', () => {

    it('derives readonly from the workspace mode', () => {
        const {comp} = createComp({mode: WorkspaceMode.VIEW});
        comp.ngOnInit();
        expect(comp.readonly).toBe(true);
    });

    it('is editable for a designer in EDIT mode', () => {
        const {comp} = createComp({mode: WorkspaceMode.EDIT});
        comp.ngOnInit();
        expect(comp.readonly).toBe(false);
    });

    it('reads the component from WorkspaceService, not an @Input', () => {
        const {comp, component} = createComp();
        comp.ngOnInit();
        expect(comp.component).toBe(component);
        expect(comp.component.uniqueId).toBe('abc');
    });
});
