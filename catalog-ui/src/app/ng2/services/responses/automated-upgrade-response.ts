/**
 * Created by ob0695 on 4/29/2018.
 */

export class AutomatedUpgradeStatusResponse {
    name:string;
    status:string;
    uniqueId:string;
    version:string;
}

export class AutomatedUpgradeGenericResponse {
    error:string;
    status:string;
    componentToUpgradeStatus:Array<AutomatedUpgradeStatusResponse>;
}