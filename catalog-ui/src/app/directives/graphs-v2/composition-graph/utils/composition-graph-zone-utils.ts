import {PolicyInstance} from "app/models/graph/zones/policy-instance";
import {ZoneInstance, ZoneInstanceType, ZoneInstanceAssignmentType} from "app/models/graph/zones/zone-instance";
import {Zone} from "app/models/graph/zones/zone";
import {DynamicComponentService} from "app/ng2/services/dynamic-component.service";
import {PaletteAnimationComponent} from "app/ng2/components/ui/palette-animation/palette-animation.component";
import {Point, LeftPaletteMetadataTypes, Component} from "../../../../models";
import {CanvasHandleTypes} from "app/utils";
import {PoliciesService} from "../../../../ng2/services/policies.service";
import {Observable} from "rxjs";
import {GroupsService} from "../../../../ng2/services/groups.service";
import {GroupInstance} from "app/models/graph/zones/group-instance";


export class CompositionGraphZoneUtils {

    constructor(private dynamicComponentService:DynamicComponentService,
                private policiesService:PoliciesService,
                private groupsService:GroupsService) {
    }


    public createCompositionZones = ():Array<Zone> => {
        let zones:Array<Zone> = [];

        zones[ZoneInstanceType.POLICY] = new Zone('Policies', 'P', ZoneInstanceType.POLICY);
        zones[ZoneInstanceType.GROUP] = new Zone('Groups', 'G', ZoneInstanceType.GROUP);

        return zones;
    }

    public showZone = (zone:Zone):void => {
        zone.visible = true;
        zone.minimized = false;
    }

    public getZoneTypeForPaletteComponent = (componentCategory:LeftPaletteMetadataTypes) => {
        if (componentCategory == LeftPaletteMetadataTypes.Group) {
            return ZoneInstanceType.GROUP;
        } else if (componentCategory == LeftPaletteMetadataTypes.Policy) {
            return ZoneInstanceType.POLICY;
        }
    };

    public initZoneInstances(zones:Array<Zone>, component:Component) {
        if (component.groupInstances && component.groupInstances.length) {
            this.showZone(zones[ZoneInstanceType.GROUP]);
            _.forEach(component.groupInstances, (group:GroupInstance) => {
                let newInstance = new ZoneInstance(group, component);
                this.addInstanceToZone(zones[ZoneInstanceType.GROUP], newInstance);
            });
        }

        if (component.policies && component.policies.length) {
            this.showZone(zones[ZoneInstanceType.POLICY]);
            _.forEach(component.policies, (policy:PolicyInstance) => {
                let newInstance = new ZoneInstance(policy, component);
                this.addInstanceToZone(zones[ZoneInstanceType.POLICY], newInstance);

            });
        }
    }

    public findAndUpdateZoneInstanceData (zones: Array<Zone>, instanceData:PolicyInstance | GroupInstance) {
        _.forEach(zones, (zone:Zone) => {
            _.forEach(zone.instances, (zoneInstance:ZoneInstance) => {
                if(zoneInstance.instanceData.uniqueId === instanceData.uniqueId){
                    zoneInstance.updateInstanceData(instanceData);
                }
            });
        });
    }

    public updateTargetsOrMembersOnCanvasDelete = (canvasNodeID:string, zones:Array<Zone>, type:ZoneInstanceAssignmentType):void => {
        _.forEach(zones, (zone) => {
            _.forEach(zone.instances, (zoneInstance:ZoneInstance) => {
                if (zoneInstance.isAlreadyAssigned(canvasNodeID)) {
                    zoneInstance.addOrRemoveAssignment(canvasNodeID, type);
                    //remove it from our list of BE targets and members as well (so that it will not be sent in future calls to BE).
                    zoneInstance.instanceData.setSavedAssignments(zoneInstance.assignments);
                }
            });
        });
    };

    public createZoneInstanceFromLeftPalette = (zoneType:ZoneInstanceType, component:Component, paletteComponentType:string):Observable<ZoneInstance> => {
        if (zoneType === ZoneInstanceType.POLICY) {
            return this.policiesService.createPolicyInstance(component.componentType, component.uniqueId, paletteComponentType).map(response => {
                let newInstance = new PolicyInstance(response);
                component.policies.push(newInstance);
                return new ZoneInstance(newInstance, component);
            });
        } else if (zoneType === ZoneInstanceType.GROUP) {
            return this.groupsService.createGroupInstance(component.componentType, component.uniqueId, paletteComponentType).map(response => {
                let newInstance = new GroupInstance(response);
                component.groupInstances.push(newInstance);
                return new ZoneInstance(newInstance, component);
            });
        }
    }

    public addInstanceToZone(zone:Zone, instance:ZoneInstance, hide?:boolean) {
        if(hide){
            instance.hidden = true;
        }
        zone.instances.push(instance);

    };

    private findZoneCoordinates(zoneType):Point {
        let point:Point = new Point(0, 0);
        let zone = angular.element(document.querySelector('.' + zoneType + '-zone'));
        let wrapperZone = zone.offsetParent();
        point.x = zone.prop('offsetLeft') + wrapperZone.prop('offsetLeft');
        point.y = zone.prop('offsetTop') + wrapperZone.prop('offsetTop');
        return point;
    }

    public createPaletteToZoneAnimation = (startPoint:Point, zoneType:ZoneInstanceType, newInstance:ZoneInstance) => {
        let zoneTypeName = ZoneInstanceType[zoneType].toLowerCase();
        let paletteToZoneAnimation = this.dynamicComponentService.createDynamicComponent(PaletteAnimationComponent);
        paletteToZoneAnimation.instance.from = startPoint;
        paletteToZoneAnimation.instance.type = zoneType;
        paletteToZoneAnimation.instance.to = this.findZoneCoordinates(zoneTypeName);
        paletteToZoneAnimation.instance.zoneInstance = newInstance;
        paletteToZoneAnimation.instance.iconName = zoneTypeName;
        paletteToZoneAnimation.instance.runAnimation();
    }

    public startCyTagMode = (cy:Cy.Instance) => {
        cy.autolock(true);
        cy.nodes().unselectify();
        cy.emit('tagstart'); //dont need to show handles because they're already visible bcz of hover event

    };

    public endCyTagMode = (cy:Cy.Instance) => {
        cy.emit('tagend');
        cy.nodes().selectify();
        cy.autolock(false);
    };

    public handleTagClick = (cy:Cy.Instance, zoneInstance:ZoneInstance, nodeId:string) => {
        zoneInstance.addOrRemoveAssignment(nodeId, ZoneInstanceAssignmentType.COMPONENT_INSTANCES);
        this.showZoneTagIndicationForNode(nodeId, zoneInstance, cy);
    };

    public showGroupZoneIndications = (groupInstances:Array<ZoneInstance>, policyInstance:ZoneInstance) => {
        groupInstances.forEach((groupInstance:ZoneInstance)=> {
            let handle:string = this.getCorrectHandleForNode(groupInstance.instanceData.uniqueId, policyInstance);
            groupInstance.showHandle(handle);
        })
    };

    public hideGroupZoneIndications = (instances:Array<ZoneInstance>) => {
        instances.forEach((instance) => {
            instance.hideHandle();
        })
    }

    public showZoneTagIndications = (cy:Cy.Instance, zoneInstance:ZoneInstance) => {

        cy.nodes().forEach(node => {
            let handleType:string = this.getCorrectHandleForNode(node.id(), zoneInstance);
            cy.emit('showhandle', [node, handleType]);
        });
    };

    public showZoneTagIndicationForNode = (nodeId:string, zoneInstance:ZoneInstance, cy:Cy.Instance) => {
        let node = cy.getElementById(nodeId);
        let handleType:string = this.getCorrectHandleForNode(nodeId, zoneInstance);
        cy.emit('showhandle', [node, handleType]);
    }

    public hideZoneTagIndications = (cy:Cy.Instance) => {
        cy.emit('hidehandles');
    };

    public getCorrectHandleForNode = (nodeId:string, zoneInstance:ZoneInstance):string => {
        if (zoneInstance.isAlreadyAssigned(nodeId)) {
            if (zoneInstance.type == ZoneInstanceType.POLICY) {
                return CanvasHandleTypes.TAGGED_POLICY;
            } else {
                return CanvasHandleTypes.TAGGED_GROUP;
            }
        } else {
            return CanvasHandleTypes.TAG_AVAILABLE;
        }
    };
}

CompositionGraphZoneUtils.$inject = [
    'DynamicComponentService',
    'PoliciesServiceNg2',
    'GroupsServiceNg2'
];