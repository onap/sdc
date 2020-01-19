import { Injectable, Inject } from "@angular/core";
import { Observable } from "rxjs/Observable";
import { SdcConfigToken, ISdcConfig } from "../config/sdc-config.config";
import { HttpClient } from "@angular/common/http";
import { Component, OperationModel } from "app/models";

interface WorkflowOutputParameter {
    name: string;
    type: string;
    mandatory: boolean;
}

interface WorkflowInputParameter extends WorkflowOutputParameter {
    property: string;
}

@Injectable()
export class WorkflowServiceNg2 {

    protected baseUrl;
    protected catalogBaseUrl;

    WF_STATE_ACTIVE = 'ACTIVE';
    WF_STATE_ARCHIVED = 'ARCHIVED';
    VERSION_STATE_CERTIFIED = 'CERTIFIED';

    constructor(private http: HttpClient, @Inject(SdcConfigToken) sdcConfig:ISdcConfig) {
        this.baseUrl = sdcConfig.api.workflow_root;
        this.catalogBaseUrl = sdcConfig.api.POST_workflow_artifact;
    }

    public associateWorkflowArtifact(component: Component, operation: OperationModel): Observable<any> {
        return this.http.post<any>(this.baseUrl + '/workflows/' + operation.workflowId + '/versions/' + operation.workflowVersionId + '/artifact-deliveries', {
                endpoint: this.catalogBaseUrl + '/' + component.getTypeUrl() + component.uuid + '/interfaces/' + operation.interfaceId + '/operations/'
                + operation.uniqueId + '/artifacts/' + operation.implementation.artifactUUID, method: 'POST'
            });
    }

    public getWorkflows(filterCertified: boolean = true): Observable<any> {
        return this.http.get<any>(this.baseUrl + '/workflows' + (filterCertified ? '?versionState=' + this.VERSION_STATE_CERTIFIED : ''));
    }

    public getWorkflowVersions(workflowId: string, filterCertified: boolean = true): Observable<any> {
        return this.http.get<any>(this.baseUrl + '/workflows/' + workflowId + '/versions' + (filterCertified ? '?state=' + this.VERSION_STATE_CERTIFIED : ''))
            .map((res) => {
                return res.items;
            });
    }

    public updateWorkflowVersion(workflowId: string, versionId: string, payload: any): Observable<any> {
        return this.http.put<any>(this.baseUrl + '/workflows/' + workflowId + '/versions/' + versionId, payload);
    }

}
