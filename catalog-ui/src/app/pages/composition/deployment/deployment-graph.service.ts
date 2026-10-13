import { Injectable } from '@angular/core';
import 'rxjs/add/observable/forkJoin';
import { Module } from '../../../models/modules/base-module';
import { CommonGraphDataService } from '../common/common-graph-data.service';
@Injectable()
export class DeploymentGraphService extends CommonGraphDataService {
    public modules: Module[];
}
