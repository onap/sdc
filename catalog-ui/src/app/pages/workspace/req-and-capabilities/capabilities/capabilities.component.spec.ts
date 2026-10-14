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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiServices } from 'onap-ui-angular';
import { DatatableA11yModule } from '../../../../components/ui/datatable-a11y/datatable-a11y.module';
import { TopologyTemplateService } from '../../../../services/component-services/topology-template.service';
import { EventListenerService } from '../../../../services/event-listener.service';
import { WorkspaceService } from '../../workspace.service';
import { ReqAndCapabilitiesService } from '../req-and-capabilities.service';
import { CapabilitiesComponent } from './capabilities.component';

describe('CapabilitiesComponent', () => {
    let fixture: ComponentFixture<CapabilitiesComponent>;

    const name = (text: string): HTMLElement =>
        Array.from(fixture.nativeElement.querySelectorAll('[data-tests-id="row.name"]'))
            .find((el: HTMLElement) => el.textContent.trim() === text) as HTMLElement;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [NgxDatatableModule, DatatableA11yModule],
            declarations: [CapabilitiesComponent],
            schemas: [NO_ERRORS_SCHEMA],
            providers: [
                {provide: WorkspaceService, useValue: {}},
                {provide: SdcUiServices.LoaderService, useValue: {}},
                {provide: TopologyTemplateService, useValue: {}},
                {provide: ReqAndCapabilitiesService, useValue: {}},
                {provide: SdcUiServices.ModalService, useValue: {}},
                {provide: EventListenerService, useValue: {}}
            ]
        });
        fixture = TestBed.createComponent(CapabilitiesComponent);
        fixture.componentInstance.capabilities = [
            {name: 'imported', isCreatedManually: false, properties: []},
            {name: 'manual', isCreatedManually: true, properties: []}
        ] as any;
        spyOn(fixture.componentInstance, 'editCapability');
        fixture.detectChanges();
    });

    it('renders the name of a manually created capability as an edit button', () => {
        const manual = name('manual');
        expect(manual.tagName).toBe('BUTTON');
        expect(manual.getAttribute('type')).toBe('button');
        expect(manual.getAttribute('aria-label')).toBe('Edit manual');
        manual.click();
        expect(fixture.componentInstance.editCapability).toHaveBeenCalledWith(fixture.componentInstance.capabilities[1]);
    });

    // The AngularJS template opened the editor only for `capability.isCreatedManually`.
    it('renders the name of an imported capability as plain text that does not open the editor', () => {
        const imported = name('imported');
        expect(imported.tagName).toBe('SPAN');
        expect(imported.hasAttribute('role')).toBe(false);
        expect(imported.hasAttribute('tabindex')).toBe(false);
        imported.click();
        expect(fixture.componentInstance.editCapability).not.toHaveBeenCalled();
    });
});

describe('capability properties template', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./capabilities-properties/capabilities-properties.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    it('renders the property name as a native button, not an href-less link', () => {
        const nameCell = doc.querySelector('[data-tests-id="row[column.prop]"][\\(click\\)]');
        expect(nameCell.tagName).toBe('BUTTON');
        expect(nameCell.getAttribute('type')).toBe('button');
        expect(nameCell.getAttribute('(click)')).toBe('updateProperty(row)');
        expect(doc.querySelectorAll('a').length).toBe(0);
    });
});
