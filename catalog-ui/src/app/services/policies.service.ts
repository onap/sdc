/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import * as _ from 'lodash';
import { Observable } from 'rxjs/Observable';
import { ISdcConfig, SdcConfigToken } from '../config/sdc-config.config';
import { PolicyInstance, PolicyTargetsRequest } from '../models/graph/zones/policy-instance';
import { IZoneService } from '../models/graph/zones/zone';
import { IZoneInstanceAssignment } from '../models/graph/zones/zone-instance';
import { TargetUiObject } from '../models/ui-models/ui-target-object';
import { TargetOrMemberType } from '../utils/constants';

@Injectable()
export class PoliciesService implements IZoneService {
    protected baseUrl;

    private mapApiDirections = {
        RESOURCE: 'resources',
        SERVICE: 'services'
    };

    constructor(private http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root;
    }

    public createPolicyInstance(topologyTemplateType: string, topologyTemplateId: string, policyType: string): Observable<PolicyInstance> {
        return this.http.post<PolicyInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/policies/' + policyType, {});
    }

    public addPolicyTarget(topologyTemplateType: string, topologyTemplateId: string, policy: PolicyInstance, targetId: string, targetType: TargetOrMemberType) {
        let _targets: string[];
        let _members: string[];

        if (targetType === TargetOrMemberType.COMPONENT_INSTANCES) {
            _targets = _.cloneDeep(policy.targets.COMPONENT_INSTANCES);
            _targets.push(targetId);
        } else if (targetType === TargetOrMemberType.GROUPS) {
            _members = _.cloneDeep(policy.targets.GROUPS);
            _members.push(targetId);
        }
        const policyTargetRequest: PolicyTargetsRequest = new PolicyTargetsRequest(_members, _targets);
        return this.updatePolicyTargets(topologyTemplateType, topologyTemplateId, policy.uniqueId, policyTargetRequest);
    }

    public deletePolicyTarget(topologyTemplateType: string, topologyTemplateId: string, policy: PolicyInstance, targetId: string, targetType: TargetOrMemberType): Observable<PolicyInstance> {
        let _targets: string[] = _.cloneDeep(policy.targets.COMPONENT_INSTANCES);
        let _members: string[] = _.cloneDeep(policy.targets.GROUPS);
        if (targetType === TargetOrMemberType.COMPONENT_INSTANCES) {
            _targets = _.without(_targets, targetId);
        } else if (targetType === TargetOrMemberType.GROUPS) {
            _members = _.without(_members, targetId);
        }
        const policyTargetRequest: PolicyTargetsRequest = new PolicyTargetsRequest(_members, _targets);
        return this.updatePolicyTargets(topologyTemplateType, topologyTemplateId, policy.uniqueId, policyTargetRequest);
    }

    public updatePolicyTargets(topologyTemplateType: string, topologyTemplateId: string, policyId: string, targets: PolicyTargetsRequest): Observable<PolicyInstance> {
        return this.http.post<PolicyInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/policies/' + policyId + '/targets', targets.requestItems)
            .map((response) => new PolicyInstance(response));
    }

    public updateTargets(topologyTemplateType: string, topologyTemplateId: string, policyId: string, targets: TargetUiObject[]): Observable<PolicyInstance> {
        const instances: string[] = _.filter(targets, (target: TargetUiObject) => {
            return target.type === TargetOrMemberType.COMPONENT_INSTANCES;
        }).map((target) => target.uniqueId);

        const groups: string[] = _.filter(targets, (target: TargetUiObject) => {
            return target.type === TargetOrMemberType.GROUPS;
        }).map((target) => target.uniqueId);

        const policyTargetRequest: PolicyTargetsRequest = new PolicyTargetsRequest(groups, instances);
        return this.updatePolicyTargets(topologyTemplateType, topologyTemplateId, policyId, policyTargetRequest);
    }

    public getSpecificPolicy(topologyTemplateType: string, topologyTemplateId: string, policyId: string): Observable<PolicyInstance> {
        return this.http.get<PolicyInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/policies/' + policyId)
            .map((res) => {
                return new PolicyInstance(res);
            });
    }

    public updateName(topologyTemplateType: string, topologyTemplateId: string, policyId: string, newName: string): Observable<any> {
        return this.http.put<PolicyInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/policies/' + policyId, {name: newName});
    }

    public deletePolicy(topologyTemplateType: string, topologyTemplateId: string, policyId: string) {
        return this.http.delete<PolicyInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/policies/' + policyId);
    }

    public updateZoneInstanceAssignments(topologyTemplateType: string, topologyTemplateId: string, policyId: string, targets: IZoneInstanceAssignment[]): Observable<PolicyInstance> {
        return this.updateTargets(topologyTemplateType, topologyTemplateId, policyId, targets);
    }

    public deleteZoneInstance(topologyTemplateType: string, topologyTemplateId: string, policyId: string): Observable<any> {
        return this.deletePolicy(topologyTemplateType, topologyTemplateId, policyId);
    }

}
