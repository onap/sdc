import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/Rx';
import {Response, Headers, RequestOptions, Http} from '@angular/http';
import { COMPONENT_INSTANCE_RESPONSE,COMPONENT_INPUT_RESPONSE,COMPONENT_PROPERTIES_RESPONSE } from './mocks/properties.mock';
import { HttpService } from './http.service';
import { sdc2Config } from './../../../main';
import {IAppConfigurtaion} from "../../models/app-config";

@Injectable()
export class PostsService {

    private base;

    constructor(private http: HttpService) {
        this.base = sdc2Config.api.root;
    }

    getAppVersion(): Observable<JSON> {
        return this.http
            .get(this.base + sdc2Config.api.GET_SDC_Version)
            .map((res: Response) => res.json());
    }

    // getProperties(id:string): Observable<any> {
    //    return this.http
    //         .get(this.base + sdc2Config.api.GET_SDC_Version)
    //         .map((res: Response) => res.json());
    // }

    getProperties(): Observable<any> {
       return Observable.create(observer => {
            observer.next(COMPONENT_PROPERTIES_RESPONSE);
            observer.complete();
        });
    }

    getInstance(): Observable<any> {
        return Observable.create(observer => {
            observer.next(COMPONENT_INSTANCE_RESPONSE);
            observer.complete();
        });
    }

    getInputs(): Observable<any> {
        return Observable.create(observer => {
            observer.next(COMPONENT_INPUT_RESPONSE);
            observer.complete();
        });
    }

}
