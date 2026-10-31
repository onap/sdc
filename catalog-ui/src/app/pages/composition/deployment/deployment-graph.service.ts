import {Injectable} from "@angular/core";

import {CommonGraphDataService} from "../common/common-graph-data.service";
import {Module} from "../../../models/modules/base-module";
@Injectable()
export class DeploymentGraphService extends CommonGraphDataService {
    public modules:Array<Module>;
}
