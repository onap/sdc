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
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/of';
import {Observable} from 'rxjs/Observable';
import {WorkspaceMode} from 'app/utils/constants';
import {InterfaceOperationComponent} from './interface-operation.page.component';

function makeComponent(overrides: any = {}) {
    return Object.assign({
        uniqueId: 'abc',
        componentType: 'SERVICE'
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
    const translateService: any = {
        languageChangedObservable: Observable.of('en_US'),
        translate: jest.fn((k: string) => k)
    };
    const pluginsService: any = {getPluginByStateUrl: jest.fn(() => undefined)};
    const componentService: any = {
        getInterfaceOperations: jest.fn(() => Observable.of({interfaces: []})),
        getComponentInputs: jest.fn(() => Observable.of({inputs: []})),
        getInterfaceTypes: jest.fn(() => Observable.of({})),
        getCapabilitiesAndRequirements: jest.fn(() => Observable.of({capabilities: {}}))
    };
    const workflowService: any = {getWorkflows: jest.fn(() => Observable.of([]))};
    const modalService: any = {};
    const modalServiceSdcUI: any = {};

    const comp = new InterfaceOperationComponent(
        sdcConfig, navigationService, translateService, pluginsService, componentService,
        workflowService, modalService, modalServiceSdcUI, workspaceService);
    return {comp, component, workspaceService, componentService};
}

describe('InterfaceOperationComponent', () => {

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

describe('InterfaceOperationComponent template', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./interface-operation.page.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    // The global `.disabled` class only stops the mouse; a native disabled button also stops the
    // keyboard, and axe exempts it from color-contrast as an inactive control.
    it('renders both Add Operation controls as native buttons disabled when read-only', () => {
        const adds = Array.from(doc.querySelectorAll('[data-tests-id="add-operation"]'));
        expect(adds.map((el) => el.className)).toEqual(['top-add-btn add-btn', 'empty-list-add-btn']);
        adds.forEach((el) => {
            expect(el.tagName).toBe('BUTTON');
            expect(el.getAttribute('type')).toBe('button');
            expect(el.getAttribute('[disabled]')).toBe('readonly');
            expect(el.getAttribute('[class.disabled]')).toBe('readonly');
        });
    });

    // A button may hold phrasing content only; the svg-icon host renders a div.
    it('keeps the plus icon a decorative inline svg inside the empty-list button', () => {
        const add = doc.querySelector('.empty-list-add-btn');
        expect(add.querySelectorAll('div, svg-icon').length).toBe(0);
        const icon = add.querySelector('.empty-list-add-icon');
        expect(icon.tagName).toBe('SPAN');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
        expect(icon.querySelector('svg path')).not.toBeNull();
        expect(add.querySelector('.button-text').tagName).toBe('SPAN');
    });

    it('keeps the accordion row an exact-class div whose name sits in a toggle button', () => {
        const accordion = doc.querySelector('.interface-accordion');
        expect(accordion.tagName).toBe('DIV');
        expect(accordion.getAttribute('class')).toBe('interface-accordion');
        const toggle = accordion.querySelector('button');
        expect(toggle.getAttribute('[attr.aria-expanded]')).toBe('!interface.isCollapsed');
        expect(toggle.querySelector('span.interface-name')).not.toBeNull();
    });

    it('gives each operation row a name button and native expand, collapse and More/Less buttons', () => {
        expect(doc.querySelector('.data-row .field-name > button').getAttribute('type')).toBe('button');
        expect(doc.querySelector('[data-tests-id="expand-all"]').tagName).toBe('BUTTON');
        expect(doc.querySelector('[data-tests-id="collapse-all"]').tagName).toBe('BUTTON');
        expect(doc.querySelector('.more-or-less').tagName).toBe('BUTTON');
        expect(doc.querySelector('.delete-action').hasAttribute('(click)')).toBe(false);
        expect(doc.querySelector('.delete-action svg-icon').getAttribute('(click)')).toBe('onRemoveOperation($event, operation)');
    });
});
