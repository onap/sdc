import {IZoneInstanceAssignment} from '../../models/graph/zones/zone-instance';
import {Injectable, Inject} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";
import {GroupInstance} from '../../models/graph/zones/group-instance';
import {UiBaseObject} from "../../models/ui-models/ui-base-object";
import {IZoneService} from "../../models/graph/zones/zone";
import { HttpClient } from '@angular/common/http';

@Injectable()
export class GroupsService implements IZoneService {

    protected baseUrl;

    private mapApiDirections = {
        'RESOURCE': 'resources',
        'SERVICE': 'services'
    }

    constructor(private http: HttpClient, @Inject(SdcConfigToken) sdcConfig:ISdcConfig) {
        this.baseUrl = sdcConfig.api.root;
    }

    public createGroupInstance(componentType:string, componentUniqueId:string, groupType:string): Observable<GroupInstance>{
        return this.http.post<GroupInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[componentType.toUpperCase()] + '/' + componentUniqueId + '/groups/' + groupType, {}).map(resp => {
            return new GroupInstance(resp);
        });
    };

    public addGroupMember(topologyTemplateType:string, topologyTemplateId:string, group:GroupInstance, memberId:string) {
        let members:Array<string> = Object.assign({}, group.members);
        members.push(memberId);
        return this.updateGroupMembers(topologyTemplateType, topologyTemplateId, group.uniqueId, members);
    }

    public deleteGroupMember(topologyTemplateType:string, topologyTemplateId:string, group:GroupInstance, memberId:string) {
        let _members:Array<string> = angular.copy(group.members);
        _members =_.without(_members, memberId);
        return this.updateGroupMembers(topologyTemplateType, topologyTemplateId, group.uniqueId, _members);
    }

    public updateGroupMembers(topologyTemplateType:string, topologyTemplateId:string, groupId:string, members:Array<string>):Observable<Array<string>> {
        return this.http.post<Array<string>>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/groups/' + groupId + '/members', members);
    }

    public updateMembers(topologyTemplateType:string, topologyTemplateId:string, groupId:string, members:Array<UiBaseObject>):Observable<Array<string>> {
        let membersIds:Array<string> = members.map(member => member.uniqueId);
        return this.updateGroupMembers(topologyTemplateType, topologyTemplateId, groupId, membersIds);
    }

    public getSpecificGroup(topologyTemplateType:string, topologyTemplateId:string, groupId:string):Observable<GroupInstance> {
        return this.http.get<GroupInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/groups/' + groupId)
            .map(res => {
                return new GroupInstance(res);
            });
    }

    public updateName(topologyTemplateType:string, topologyTemplateId:string, groupId:string, newName:string):Observable<GroupInstance> {
        return this.http.put<GroupInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/groups/' + groupId, {name: newName});
    };

    public deleteGroup(topologyTemplateType:string, topologyTemplateId:string, groupId:string) {
        return this.http.delete<GroupInstance>(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[topologyTemplateType.toUpperCase()] + '/' + topologyTemplateId + '/groups/' + groupId);
    };

    public updateZoneInstanceAssignments(topologyTemplateType:string, topologyTemplateId:string, policyId:string, members:Array<IZoneInstanceAssignment>):Observable<any> {
        return this.updateMembers(topologyTemplateType, topologyTemplateId, policyId, members);
    };

    public deleteZoneInstance(topologyTemplateType:string, topologyTemplateId:string, policyId:string):Observable<any> {
        return this.deleteGroup(topologyTemplateType, topologyTemplateId, policyId);
    };


}
