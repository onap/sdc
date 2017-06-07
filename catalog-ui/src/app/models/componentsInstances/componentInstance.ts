/**
 * Created by obarda on 2/4/2016.
 */
'use strict';
import {ArtifactGroupModel, CapabilitiesGroup,RequirementsGroup, PropertyModel, InputModel, Module} from "../../models";
import {ResourceType} from "../../utils/constants";

export class ComponentInstance {

    public componentUid:string;
    public componentName:string;
    public posX:number;
    public posY:number;
    public componentVersion:string;
    public description:string;
    public icon:string;
    public name:string;
    public normalizedName:string;
    public originType:string;
    public deploymentArtifacts:ArtifactGroupModel;
    public artifacts:ArtifactGroupModel;
    public propertyValueCounter:number;
    public uniqueId:string;
    public creationTime:number;
    public modificationTime:number;
    public capabilities:CapabilitiesGroup;
    public requirements:RequirementsGroup;
    public customizationUUID:string;
    //custom properties
    public certified:boolean;
    public iconSprite:string;
    public inputs:Array<InputModel>;
    public properties:Array<PropertyModel>;
    public groupInstances:Array<Module>;

    constructor(componentInstance?:ComponentInstance) {

        if (componentInstance) {
            this.componentUid = componentInstance.componentUid;
            this.componentName = componentInstance.componentName;

            this.componentVersion = componentInstance.componentVersion;
            this.description = componentInstance.description;
            this.icon = componentInstance.icon;
            this.name = componentInstance.name;
            this.normalizedName = componentInstance.normalizedName;
            this.originType = componentInstance.originType;
            this.deploymentArtifacts = new ArtifactGroupModel(componentInstance.deploymentArtifacts);
            this.artifacts = new ArtifactGroupModel(componentInstance.artifacts);
            this.uniqueId = componentInstance.uniqueId;
            this.creationTime = componentInstance.creationTime;
            this.modificationTime = componentInstance.modificationTime;
            this.propertyValueCounter = componentInstance.propertyValueCounter;
            this.capabilities = new CapabilitiesGroup(componentInstance.capabilities);
            this.requirements = new RequirementsGroup(componentInstance.requirements);
            this.certified = componentInstance.certified;
            this.customizationUUID = componentInstance.customizationUUID;
            this.updatePosition(componentInstance.posX, componentInstance.posY);
            this.groupInstances = componentInstance.groupInstances;
        }
    }

    public isUcpe = ():boolean => {
        if (this.originType === 'VF' && this.capabilities && this.capabilities['tosca.capabilities.Container'] && this.name.toLowerCase().indexOf('ucpe') > -1) {
            return true;
        }
        return false;
    };

    public isVl = ():boolean => {
        return this.originType === 'VL';
    };

    public isComplex = () : boolean => {
        return this.originType === ResourceType.VF;
    }

    public setInstanceRC = ():void=> {
        _.forEach(this.requirements, (requirementValue:Array<any>, requirementKey)=> {
            _.forEach(requirementValue, (requirement)=> {
                if (!requirement.ownerName) {
                    requirement['ownerId'] = this.uniqueId;
                    requirement['ownerName'] = this.name;
                }
            });
        });
        _.forEach(this.capabilities, (capabilityValue:Array<any>, capabilityKey)=> {
            _.forEach(capabilityValue, (capability)=> {
                if (!capability.ownerName) {
                    capability['ownerId'] = this.uniqueId;
                    capability['ownerName'] = this.name;
                }
            });
        });
    };

    public updatePosition(posX:number, posY:number) {
        this.posX = posX;
        this.posY = posY;
    }

    public toJSON = ():any => {
        let temp = angular.copy(this);
        temp.certified = undefined;
        temp.iconSprite = undefined;
        temp.inputs = undefined;
        temp.groupInstances = undefined;
        temp.properties = undefined;
        temp.requirements = undefined;
        temp.capabilities = undefined;
        return temp;
    };
}
