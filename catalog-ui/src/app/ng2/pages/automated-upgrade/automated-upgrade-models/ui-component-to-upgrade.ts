import {ComponentState} from "../../../../utils/constants";
import {IDependenciesServerResponse} from "../../../services/responses/dependencies-server-response";
import {UiBaseObject} from "../../../../models/ui-models/ui-base-object";

/**
 * Created by ob0695 on 5/1/2018.
 */
export enum AutomatedUpgradeInstanceType {
    VF, SERVICE_PROXY, ALLOTTED_RESOURCE
}
export class ServiceContainerToUpgradeUiObject extends UiBaseObject {

    icon:string;
    version:string;
    isLock:boolean; // true if service is in check-out or ceritification-in-progress
    vspInstances:Array<VspInstanceUiObject>; // list of instances of the vsp contain in the service - intances can be vf, proxy or allotted
    isAlreadyUpgrade:boolean; // true if all instances is in latest version

    constructor(componentToUpgrade:IDependenciesServerResponse) {
        super(componentToUpgrade.uniqueId, componentToUpgrade.type, componentToUpgrade.name);
        this.icon = componentToUpgrade.icon;
        this.version = componentToUpgrade.version;
        this.isAlreadyUpgrade = true;
        this.isLock = componentToUpgrade.state === ComponentState.NOT_CERTIFIED_CHECKOUT;
        this.vspInstances = [];
    }

    public addVfInstance = (vsp: IDependenciesServerResponse, latestVersion:string):void => {
        let isNeededUpgrade = parseInt(vsp.version) < parseInt(latestVersion);
        this.vspInstances.push(new VspInstanceUiObject(vsp.uniqueId, vsp.name, vsp.version, vsp.icon));
        if (isNeededUpgrade) {
            this.isAlreadyUpgrade = false;
        }
    }

    public addProxyInstance = (vsp: IDependenciesServerResponse, isNeededUpgrade:boolean, instanceName:string):void => {
        this.vspInstances.push(new ProxyVspInstanceUiObject(vsp.uniqueId, vsp.name, vsp.version, vsp.icon, instanceName));
        if (isNeededUpgrade) {
            this.isAlreadyUpgrade = false;
        }
    }

    public addAllottedResourceInstance = (vsp: IDependenciesServerResponse, isNeededUpgrade:boolean, instanceName:string, vfName:string, vfId:string):void => {
        this.vspInstances.push(new AllottedResourceInstanceUiObject(vsp.uniqueId, vsp.name, vsp.version, vsp.icon, instanceName, vfName, vfId));
        if (isNeededUpgrade) {
            this.isAlreadyUpgrade = false;
        }
    }

    public addMultipleInstances = (vsp: IDependenciesServerResponse, vspLatestVersion:string, instancesNames:Array<string>, allottedOriginVf: IDependenciesServerResponse):void => {
        _.forEach(instancesNames, (instanceName:string) => {
            let isNeededUpgrade = parseInt(vsp.version) < parseInt(vspLatestVersion);
            if (allottedOriginVf) {
                this.addAllottedResourceInstance(vsp, isNeededUpgrade, instanceName, allottedOriginVf.name, allottedOriginVf.uniqueId);
            } else {
                this.addProxyInstance(vsp, isNeededUpgrade, instanceName);
            }
        })
    }
}

export class VspInstanceUiObject {

    vspName:string;
    vspVersion:string;
    vspId:string;
    icon:string;
    instanceType:AutomatedUpgradeInstanceType;

    constructor(uniqueId:string, vspName:string, vspVersion:string, icon:string) {
        this.vspId = uniqueId;
        this.vspName = vspName;
        this.vspVersion = vspVersion;
        this.icon = icon;
        this.instanceType = AutomatedUpgradeInstanceType.VF;
    }
}

export class ProxyVspInstanceUiObject extends VspInstanceUiObject {

    instanceName:string;

    constructor(uniqueId:string, vspName:string, vspVersion:string, icon:string, instanceName: string) {
        super(uniqueId, vspName, vspVersion, icon);
        this.instanceName = instanceName;
        this.instanceType = AutomatedUpgradeInstanceType.SERVICE_PROXY;
    }
}

export class AllottedResourceInstanceUiObject extends VspInstanceUiObject {

    instanceName:string;
    originVfName:string;
    originVfId:string;

    constructor(uniqueId:string, vspName:string, vspVersion:string, icon:string, instanceName:string, originVfName:string, originVfId:string) {
        super(uniqueId, vspName, vspVersion, icon)
        this.instanceName = instanceName;
        this.originVfId = originVfId;
        this.originVfName = originVfName;
        this.instanceType = AutomatedUpgradeInstanceType.ALLOTTED_RESOURCE;
    }
}
