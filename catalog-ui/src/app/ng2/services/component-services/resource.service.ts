import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { Http, Response, Headers, RequestOptions } from '@angular/http';

@Injectable()
export class ResourceServiceNg2 {

    protected baseUrl = "";

    constructor(private http: Http) {

    }




}
