import { Injectable } from '@angular/core';
import {Designer, DesignersConfiguration} from "app/models";

@Injectable()
export class DesignersService {

    constructor() {
    }

    public getDesignerByStateUrl = (stateUrl: string) => {
        let designerKey: any = _.findKey(DesignersConfiguration.designers, (designerConfig: Designer) =>{
            return designerConfig.designerStateUrl ===  stateUrl;
        });

        return DesignersConfiguration.designers[designerKey];
    }
}
