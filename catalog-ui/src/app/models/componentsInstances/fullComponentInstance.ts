import { ComponentInstance, Component, ArtifactGroupModel, Service, Resource, IMainCategory, ArtifactModel, AttributeModel } from "app/models";
import { ComponentType } from '../../utils/constants';
import * as _ from 'lodash';


export class FullComponentInstance extends ComponentInstance {
    public contactId: string;
    public componentType: string;
    public interfaces:any;
    public tags:Array<string>;
    public version:string;
    public allVersions:any;
    public highestVersion:boolean;
    public categories:Array<IMainCategory>;
    public creationDate:number;
    public creatorFullName:string;
    public vendorName:string;
    public vendorRelease:string;
    public systemName:string;
    public uuid:string;
    public lifecycleState: string;
    public archived: boolean;

    public isServiceInstance: boolean;
    public isResourceInstance: boolean;
    public directives: string[];

    DIRECTIVES_TYPES = {
        SELECTABLE: 'selectable'
    };

    //service
    public serviceApiArtifacts:ArtifactGroupModel;
    public serviceType:string;
    public serviceRole:string;

    //resource
    public csarUUID:string;
    public isCsarComponent: boolean;
    public csarVersion:string;
    public csarPackageType:string;
    public packageId:string;
    public resourceType:string;
    public resourceVendorModelNumber:string;

    public attributes: Array<AttributeModel>;

    constructor(componentInstance:ComponentInstance, originComponent:Component) {
        super(componentInstance);

        this.componentType = originComponent.componentType;
        this.interfaces = originComponent.interfaces;
        this.tags = [];
        this.tags = _.clone(originComponent.tags);
        this.version = originComponent.version;
        this.allVersions = originComponent.allVersions;
        this.highestVersion = originComponent.highestVersion;
        this.categories = originComponent.categories;
        this.creationDate = originComponent.creationDate;
        this.creatorFullName = originComponent.creatorFullName;
        this.vendorName = originComponent.vendorName;
        this.vendorRelease = originComponent.vendorRelease;
        this.contactId = originComponent.contactId;
        this.description = originComponent.description;
        this.systemName = originComponent.systemName;
        this.uuid = originComponent.uuid;
        this.lifecycleState = originComponent.lifecycleState;
        this.archived = originComponent.archived;
        this.attributes = originComponent.attributes;
        this.directives = componentInstance.directives;


        if(originComponent.componentType === ComponentType.SERVICE || originComponent.componentType === ComponentType.SERVICE_PROXY){
            this.isServiceInstance = true;
            this.serviceApiArtifacts = (<Service>originComponent).serviceApiArtifacts;
            this.serviceType = (<Service>originComponent).serviceType;
            this.serviceRole = (<Service>originComponent).serviceRole;
        }
        if(originComponent.componentType === ComponentType.RESOURCE) {
            this.isResourceInstance = true;
            this.csarUUID = (<Resource>originComponent).csarUUID;
            this.isCsarComponent = !!this.csarUUID;
            this.resourceType = (<Resource>originComponent).resourceType;
            this.resourceVendorModelNumber = (<Resource>originComponent).resourceVendorModelNumber;
        }
    }

    public isResource = ():boolean => {
        return this.isResourceInstance;
    }

    public isService = ():boolean => {
        return this.isServiceInstance;
    }
    public isDependent = () : boolean => {
        return this.directives && this.directives.indexOf(this.DIRECTIVES_TYPES.SELECTABLE) !== -1;
    }

    public markAsDependent = () : void => {
        this.directives.push(this.DIRECTIVES_TYPES.SELECTABLE);
    }

    public unmarkAsDependent = () : void => {
        const index = this.directives.indexOf(this.DIRECTIVES_TYPES.SELECTABLE);
        if(index >= 0) {
            this.directives.splice(index, 1);
        }
    }

}