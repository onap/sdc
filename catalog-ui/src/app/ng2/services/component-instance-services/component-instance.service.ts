import {Injectable} from '@angular/core';
import {Response, RequestOptions, Headers} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import {HttpService} from "../http.service";
import {sdc2Config} from "../../../../main";
import {PropertyBEModel} from "app/models";
import {CommonUtils} from "app/utils";
import {Component, ComponentInstance, InputModel} from "app/models";

@Injectable()
export class ComponentInstanceServiceNg2 {

    protected baseUrl;

    constructor(private http: HttpService) {
        this.baseUrl = sdc2Config.api.root + sdc2Config.api.component_api_root;
    }

    getComponentInstanceProperties(component: Component, componentInstanceId: string): Observable<Array<PropertyBEModel>> {

        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstanceId + '/properties')
            .map((res: Response) => {
                return CommonUtils.initBeProperties(res.json());
        })
    }

    getComponentInstanceInputs(component: Component, componentInstance: ComponentInstance): Observable<Array<PropertyBEModel>> {
        return this.http.get(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/componentInstances/' + componentInstance.uniqueId + '/' + componentInstance.componentUid + '/inputs')
            .map((res: Response) => {
                return CommonUtils.initInputs(res.json());
            })
    }

    updateInstanceProperty(component: Component, componentInstanceId: string, property: PropertyBEModel): Observable<PropertyBEModel> {

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/property', property)
            .map((res: Response) => {
                return new PropertyBEModel(res.json());
        })
    }

    updateInstanceInput(component: Component, componentInstanceId: string, input: PropertyBEModel): Observable<PropertyBEModel> {

        return this.http.post(this.baseUrl + component.getTypeUrl() + component.uniqueId + '/resourceInstance/' + componentInstanceId + '/input', input)
            .map((res: Response) => {
                return new PropertyBEModel(res.json());
            })
    }


}
