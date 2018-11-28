import { Injectable, Inject } from "@angular/core";
import { Response } from "@angular/http";
import { Observable } from "rxjs/Observable";
import { HttpService } from "./http.service";
import { SdcConfigToken, ISdcConfig } from "../config/sdc-config.config";
import { Component } from "app/models";

@Injectable()
export class WorkflowServiceNg2 {

    protected baseUrl;
    protected catalogBaseUrl;

    WF_STATE_ACTIVE = 'ACTIVE';
    WF_STATE_ARCHIVED = 'ARCHIVED';
    VERSION_STATE_CERTIFIED = 'CERTIFIED';

    constructor(private http: HttpService, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.workflow_root;
        this.catalogBaseUrl = sdcConfig.api.POST_workflow_artifact;
    }

    public associateWorkflowArtifact(component: Component, operationId: string, workflowId: string, workflowVersionId: string, artifactUuid: string): Observable<any> {
        return this.http.post(this.baseUrl + '/workflows/' + workflowId + '/versions/' + workflowVersionId + '/artifact-deliveries', {
                endpoint: this.catalogBaseUrl + '/' + component.getTypeUrl() + component.uuid + '/interfaces/' + operationId + '/artifacts/' + artifactUuid,
                method: 'POST'
            })
            .map((res:Response) => {
                return res.json();
            });
    }

    public getWorkflows(filterCertified: boolean = true): Observable<any> {
        return this.http.get(this.baseUrl + '/workflows' + (filterCertified ? '?versionState=' + this.VERSION_STATE_CERTIFIED : ''))
            .map((res:Response) => {
                return res.json().items;
            });
    }

    public getWorkflowVersions(workflowId: string, filterCertified: boolean = true): Observable<any> {
        return this.http.get(this.baseUrl + '/workflows/' + workflowId + '/versions' + (filterCertified ? '?state=' + this.VERSION_STATE_CERTIFIED : ''))
            .map((res:Response) => {
                return res.json().items;
            });
    }

    public updateWorkflowVersion(workflowId: string, versionId: string, payload: any): Observable<any> {
        return this.http.put(this.baseUrl + '/workflows/' + workflowId + '/versions/' + versionId, payload)
            .map((res:Response) => {
                return res.json();
            });
    }

}
