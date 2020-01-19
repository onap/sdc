import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {ISdcConfig, SdcConfigToken} from "../config/sdc-config.config";
import {DisplayModule, Module} from "../../models/modules/base-module";
import {Observable} from "rxjs/Observable";
import {ServerTypeUrl} from "../../utils/constants";

@Injectable()
export class ModulesService {

    protected baseUrl;

    constructor(private http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    getComponentInstanceModule = (topologyTemplateType: string, topologyTemplateId: string, componentInstanceId: string, moduleId: string):Observable<DisplayModule> => {
        return this.http.get<DisplayModule>(this.baseUrl + ServerTypeUrl.toServerTypeUrl(topologyTemplateType) + "/" + topologyTemplateId + "/resourceInstance/" + componentInstanceId + "/groupInstance/" + moduleId)
            .map((response) => {
                return new DisplayModule(response);
            })
    };

    getModuleForDisplay = (topologyTemplateType: string, topologyTemplateId: string, moduleId: string):Observable<DisplayModule> => {
        return this.http.get<DisplayModule>(this.baseUrl + ServerTypeUrl.toServerTypeUrl(topologyTemplateType) + "/" + topologyTemplateId + "/groups/" + moduleId)
            .map((response) => {
                return new DisplayModule(response);
            })
    };

    public updateModuleMetadata = (topologyTemplateType: string, topologyTemplateId: string, module: Module):Observable<Module> => {
        return this.http.put<Module>(this.baseUrl + ServerTypeUrl.toServerTypeUrl(topologyTemplateType) + "/" + topologyTemplateId + "/groups/" + module.uniqueId + "/metadata", module)
    }
}