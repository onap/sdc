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
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, Inject} from '@angular/core';
import {ArtifactType} from 'app/utils';
import {CacheService} from 'app/services-ng2';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {ComponentGenericResponse} from 'app/ng2/services/responses/component-generic-response';
import {SdcConfigToken} from 'app/ng2/config/sdc-config.config';
import {WorkspaceService} from '../workspace.service';
import {FlowEditorTabBase} from './flow-editor-tab.base';
import {VendorModel} from './vendor-model';

/**
 * Management Workflow workspace tab — renders the internal React "sequence-diagram" flows editor for
 * the service's WORKFLOW artifacts. Migrated from the AngularJS ManagementWorkflowViewModel (Phase 5).
 */
@Component({
    selector: 'management-workflow-tab',
    templateUrl: './flow-editor-tab.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ManagementWorkflowTabComponent extends FlowEditorTabBase {

    /** The fixed set of ONAP actor lanes shown for a management workflow (ported verbatim from the old
     *  ManagementWorkflowViewModel.getParticipants). Plain {id, name} objects (not VF instances). */
    private static readonly PARTICIPANTS: Array<{id: string, name: string}> = [
        {id: '1', name: 'Customer'},
        {id: '2', name: 'CCD'},
        {id: '3', name: 'Infrastructure'},
        {id: '4', name: 'MSO'},
        {id: '5', name: 'SDN-C'},
        {id: '6', name: 'A&AI'},
        {id: '7', name: 'APP-C'},
        {id: '8', name: 'Cloud'},
        {id: '9', name: 'DCAE'},
        {id: '10', name: 'ALTS'},
        {id: '11', name: 'VF'}
    ];

    constructor(workspaceService: WorkspaceService,
                cacheService: CacheService,
                componentService: ComponentServiceNg2,
                cdr: ChangeDetectorRef,
                el: ElementRef,
                @Inject('$injector') $injector: any,
                @Inject(SdcConfigToken) sdcConfig: any) {
        super(workspaceService, cacheService, componentService, cdr, el, $injector, sdcConfig);
    }

    protected loadAndMount(): void {
        if (!this.component.artifacts) {
            this.isLoading = true;
            this.componentService.getComponentInformationalArtifacts(this.component).subscribe(
                (response: ComponentGenericResponse) => {
                    this.component.artifacts = response.artifacts;
                    this.render(this.buildVendorModel());
                },
                () => { this.isLoading = false; this.detectChangesSafe(); }
            );
        } else {
            this.render(this.buildVendorModel());
        }
    }

    protected buildVendorModel(): VendorModel {
        return new VendorModel(
            this.component.artifacts.filteredByType(ArtifactType.THIRD_PARTY_RESERVED_TYPES.WORKFLOW),
            this.component.uniqueId,
            this.readonly,
            this.userId,
            this.generateRequestId(),
            ArtifactType.THIRD_PARTY_RESERVED_TYPES.WORKFLOW,
            ManagementWorkflowTabComponent.PARTICIPANTS
        );
    }
}
