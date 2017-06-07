/**
 * Created by obarda on 2/23/2016.
 */
'use strict';
import {ComponentType} from "../utils/constants";

interface IAvailableIconsService {
    getIcons(componentType:ComponentType):Array<string>;
}

export class AvailableIconsService implements IAvailableIconsService {
    constructor() {
    }

    public getIcons = (componentType:string):Array<string> => {

        let icons:Array<string>;

        switch (componentType) {
            case ComponentType.SERVICE:
                icons = [
                    'call_controll',
                    'mobility',
                    'network_l_1-3',
                    'network_l_4'
                ];
                break;

            case ComponentType.RESOURCE:
                icons = [
                    'router',
                    'database',
                    'network',
                    'objectStorage',
                    'connector',
                    'brocade',
                    'cisco',
                    'ericsson',
                    'tropo',
                    'fortinet',
                    'att',
                    'broadsoft',
                    'alcatelLucent',
                    'metaswitch',
                    'aricent',
                    'mySql',
                    'oracle',
                    'nokia_siemens',
                    'juniper',
                    'call_controll',
                    'borderElement',
                    'applicationServer',
                    'server',
                    'port',
                    'loadBalancer',
                    'compute',
                    'gateway',
                    'cp',
                    'vl',
                    'vfw',
                    'firewall'
                ];
                break;

            case ComponentType.PRODUCT:
                icons = [
                    'vfw',
                    'network',
                    'security',
                    'cloud',
                    'setting',
                    'orphan',
                    'wanx',
                    'vrouter',
                    'ucpe',
                    'mobility'

                ];
                break;

        }
        return icons;
    }
}



