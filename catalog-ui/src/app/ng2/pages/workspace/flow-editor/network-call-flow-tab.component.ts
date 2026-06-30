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
import * as _ from 'lodash';
import {ArtifactType, ResourceType} from 'app/utils';
import {ComponentInstance} from 'app/models';
import {CacheService} from 'app/services-ng2';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {ComponentGenericResponse} from 'app/ng2/services/responses/component-generic-response';
import {SdcConfigToken} from 'app/ng2/config/sdc-config.config';
import {WorkspaceService} from '../workspace.service';
import {FlowEditorTabBase} from './flow-editor-tab.base';
import {VendorModel, participant} from './vendor-model';

/**
 * Network Call Flow workspace tab — renders the internal React "sequence-diagram" flows editor for the
 * service's NETWORK_CALL_FLOW artifacts, with one lane per VF component instance. Migrated from the
 * AngularJS NetworkCallFlowViewModel (Phase 5).
 */
@Component({
    selector: 'network-call-flow-tab',
    templateUrl: './flow-editor-tab.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NetworkCallFlowTabComponent extends FlowEditorTabBase {

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
        if (!this.component.artifacts || !this.component.componentInstances) {
            this.isLoading = true;
            this.componentService.getComponentInformationalArtifactsAndInstances(this.component).subscribe(
                (response: ComponentGenericResponse) => {
                    this.component.artifacts = response.artifacts;
                    this.component.componentInstances = response.componentInstances;
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
            this.component.artifacts.filteredByType(ArtifactType.THIRD_PARTY_RESERVED_TYPES.NETWORK_CALL_FLOW),
            this.component.uniqueId,
            this.readonly,
            this.userId,
            this.generateRequestId(),
            ArtifactType.THIRD_PARTY_RESERVED_TYPES.NETWORK_CALL_FLOW,
            this.getVFParticipantsFromInstances(this.component.componentInstances)
        );
    }

    /** One participant lane per VF component instance (ported from the old
     *  NetworkCallFlowViewModel.getVFParticipantsFromInstances). */
    private getVFParticipantsFromInstances(instances: Array<ComponentInstance>): Array<participant> {
        const participants: Array<participant> = [];
        _.forEach(instances, (instance: ComponentInstance) => {
            if (ResourceType.VF === instance.originType) {
                participants.push(new participant(instance));
            }
        });
        return participants;
    }
}
