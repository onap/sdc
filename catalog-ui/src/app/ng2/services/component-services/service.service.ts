import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { Response } from '@angular/http';
import {Service} from "app/models";
import { downgradeInjectable } from '@angular/upgrade/static';
import {sdc2Config} from "../../../../main";
import {InterceptorService} from "ng2-interceptors/index";


@Injectable()
export class ServiceServiceNg2 {

    protected baseUrl = "";

    constructor(private http: InterceptorService) {
        this.baseUrl = sdc2Config.api.root + sdc2Config.api.component_api_root;
    }

    validateConformanceLevel(service: Service): Observable<boolean> {

        return this.http.get(this.baseUrl + service.getTypeUrl() + service.uuid + '/conformanceLevelValidation')
            .map((res: Response) => {
                return res.json();
            });
    }

}

angular.module('Sdc.Services').factory('ServiceServiceNg2', downgradeInjectable(ServiceServiceNg2)); // This is in order to use the service in angular1 till we finish remove all angular1 code
