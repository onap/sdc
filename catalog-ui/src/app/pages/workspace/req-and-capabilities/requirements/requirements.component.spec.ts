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
import { RequirementsComponent } from './requirements.component';

describe('RequirementsComponent', () => {
    let fixture: ComponentFixture<RequirementsComponent>;

    const names = (): HTMLElement[] =>
        Array.from(fixture.nativeElement.querySelectorAll('[data-tests-id="row.name"]'));

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [NgxDatatableModule, DatatableA11yModule],
            declarations: [RequirementsComponent],
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
        fixture = TestBed.createComponent(RequirementsComponent);
        fixture.componentInstance.requirements = [
            {name: 'imported', isCreatedManually: false},
            {name: 'manual', isCreatedManually: true}
        ] as any;
        spyOn(fixture.componentInstance, 'editRequirement');
        fixture.detectChanges();
    });

    it('renders the name of a manually created requirement as an edit button', () => {
        const manual = names()[0];
        expect(manual.tagName).toBe('BUTTON');
        expect(manual.getAttribute('type')).toBe('button');
        expect(manual.getAttribute('aria-label')).toBe('Edit manual');
        expect(manual.textContent.trim()).toBe('manual');
        manual.click();
        expect(fixture.componentInstance.editRequirement).toHaveBeenCalledWith(fixture.componentInstance.requirements[0]);
    });

    it('renders the name of an imported requirement as plain text that does not open the editor', () => {
        const imported = names()[1];
        expect(imported.tagName).toBe('SPAN');
        expect(imported.hasAttribute('role')).toBe(false);
        expect(imported.hasAttribute('tabindex')).toBe(false);
        expect(imported.textContent.trim()).toBe('imported');
        imported.click();
        expect(fixture.componentInstance.editRequirement).not.toHaveBeenCalled();
    });

    it('names the table body region', () => {
        expect(fixture.nativeElement.querySelector('ngx-datatable').getAttribute('sdcDatatableA11y')).toBe('Requirements');
    });
});
