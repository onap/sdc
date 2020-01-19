import {
    Point,
    PolicyInstance,
    Zone,
    LeftPaletteMetadataTypes,
    ZoneInstance,
    ZoneInstanceType,
    ZoneInstanceAssignmentType
} from "app/models";
import {CanvasHandleTypes} from "app/utils";
import {Observable} from "rxjs";
import {GroupInstance} from "app/models/graph/zones/group-instance";
import {Injectable} from "@angular/core";
import {DynamicComponentService} from "app/ng2/services/dynamic-component.service";
import {PoliciesService} from "app/ng2/services/policies.service";
import {GroupsService} from "app/ng2/services/groups.service";
import {Store} from "@ngxs/store";
import {CompositionService} from "../../composition.service";
import {WorkspaceService} from "app/ng2/pages/workspace/workspace.service";
import { PaletteAnimationComponent } from "app/ng2/pages/composition/palette/palette-animation/palette-animation.component";

@Injectable()
export class CompositionGraphZoneUtils {

    constructor(private dynamicComponentService: DynamicComponentService,
                private policiesService: PoliciesService,
                private groupsService: GroupsService,
                private workspaceService: WorkspaceService,
                private compositionService: CompositionService) {
    }


    public createCompositionZones = (): Array<Zone> => {
        let zones: Array<Zone> = [];

        zones[ZoneInstanceType.POLICY] = new Zone('Policies', 'P', ZoneInstanceType.POLICY);
        zones[ZoneInstanceType.GROUP] = new Zone('Groups', 'G', ZoneInstanceType.GROUP);

        return zones;
    }

    public showZone = (zone: Zone): void => {
        zone.visible = true;
        zone.minimized = false;
    }

    public getZoneTypeForPaletteComponent = (componentCategory: LeftPaletteMetadataTypes) => {
        if (componentCategory == LeftPaletteMetadataTypes.Group) {
            return ZoneInstanceType.GROUP;
        } else if (componentCategory == LeftPaletteMetadataTypes.Policy) {
            return ZoneInstanceType.POLICY;
        }
    };

    public initZoneInstances(zones: Array<Zone>) {

        if (this.compositionService.groupInstances && this.compositionService.groupInstances.length) {
            this.showZone(zones[ZoneInstanceType.GROUP]);
            zones[ZoneInstanceType.GROUP].instances = [];
            _.forEach(this.compositionService.groupInstances, (group: GroupInstance) => {
                let newInstance = new ZoneInstance(group, this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId);
                this.addInstanceToZone(zones[ZoneInstanceType.GROUP], newInstance);
            });
        }

        if (this.compositionService.policies && this.compositionService.policies.length) {
            this.showZone(zones[ZoneInstanceType.POLICY]);
            zones[ZoneInstanceType.POLICY].instances = [];
            _.forEach(this.compositionService.policies, (policy: PolicyInstance) => {
                let newInstance = new ZoneInstance(policy, this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId);
                this.addInstanceToZone(zones[ZoneInstanceType.POLICY], newInstance);

            });
        }
    }

    public findAndUpdateZoneInstanceData(zones: Array<Zone>, instanceData: PolicyInstance | GroupInstance) {
        _.forEach(zones, (zone: Zone) => {
            _.forEach(zone.instances, (zoneInstance: ZoneInstance) => {
                if (zoneInstance.instanceData.uniqueId === instanceData.uniqueId) {
                    zoneInstance.updateInstanceData(instanceData);
                }
            });
        });
    }

    public updateTargetsOrMembersOnCanvasDelete = (canvasNodeID: string, zones: Array<Zone>, type: ZoneInstanceAssignmentType): void => {
        _.forEach(zones, (zone) => {
            _.forEach(zone.instances, (zoneInstance: ZoneInstance) => {
                if (zoneInstance.isAlreadyAssigned(canvasNodeID)) {
                    zoneInstance.addOrRemoveAssignment(canvasNodeID, type);
                    //remove it from our list of BE targets and members as well (so that it will not be sent in future calls to BE).
                    zoneInstance.instanceData.setSavedAssignments(zoneInstance.assignments);
                }
            });
        });
    };

    public createZoneInstanceFromLeftPalette = (zoneType: ZoneInstanceType, paletteComponentType: string): Observable<ZoneInstance> => {

        if (zoneType === ZoneInstanceType.POLICY) {
            return this.policiesService.createPolicyInstance(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, paletteComponentType).map(response => {
                let newInstance = new PolicyInstance(response);
                this.compositionService.addPolicyInstance(newInstance);
                return new ZoneInstance(newInstance, this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId);
            });
        } else if (zoneType === ZoneInstanceType.GROUP) {
            return this.groupsService.createGroupInstance(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, paletteComponentType).map(response => {
                let newInstance = new GroupInstance(response);
                this.compositionService.addGroupInstance(newInstance);
                return new ZoneInstance(newInstance, this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId);
            });
        }
    }

    public addInstanceToZone(zone: Zone, instance: ZoneInstance, hide?: boolean) {
        if (hide) {
            instance.hidden = true;
        }
        zone.instances.push(instance);

    };

    private findZoneCoordinates(zoneType): Point {
        let point: Point = new Point(0, 0);
        let zone = angular.element(document.querySelector('.' + zoneType + '-zone'));
        let wrapperZone = zone.offsetParent();
        point.x = zone.prop('offsetLeft') + wrapperZone.prop('offsetLeft');
        point.y = zone.prop('offsetTop') + wrapperZone.prop('offsetTop');
        return point;
    }

    public createPaletteToZoneAnimation = (startPoint: Point, zoneType: ZoneInstanceType, newInstance: ZoneInstance) => {
        let zoneTypeName = ZoneInstanceType[zoneType].toLowerCase();
        let paletteToZoneAnimation = this.dynamicComponentService.createDynamicComponent(PaletteAnimationComponent);
        paletteToZoneAnimation.instance.from = startPoint;
        paletteToZoneAnimation.instance.type = zoneType;
        paletteToZoneAnimation.instance.to = this.findZoneCoordinates(zoneTypeName);
        paletteToZoneAnimation.instance.zoneInstance = newInstance;
        paletteToZoneAnimation.instance.iconName = zoneTypeName;
        paletteToZoneAnimation.instance.runAnimation();
    }

    public startCyTagMode = (cy: Cy.Instance) => {
        cy.autolock(true);
        cy.nodes().unselectify();
        cy.emit('tagstart'); //dont need to show handles because they're already visible bcz of hover event

    };

    public endCyTagMode = (cy: Cy.Instance) => {
        cy.emit('tagend');
        cy.nodes().selectify();
        cy.autolock(false);
    };

    public handleTagClick = (cy: Cy.Instance, zoneInstance: ZoneInstance, nodeId: string) => {
        zoneInstance.addOrRemoveAssignment(nodeId, ZoneInstanceAssignmentType.COMPONENT_INSTANCES);
        this.showZoneTagIndicationForNode(nodeId, zoneInstance, cy);
    };

    public showGroupZoneIndications = (groupInstances: Array<ZoneInstance>, policyInstance: ZoneInstance) => {
        groupInstances.forEach((groupInstance: ZoneInstance) => {
            let handle: string = this.getCorrectHandleForNode(groupInstance.instanceData.uniqueId, policyInstance);
            groupInstance.showHandle(handle);
        })
    };

    public hideGroupZoneIndications = (instances: Array<ZoneInstance>) => {
        instances.forEach((instance) => {
            instance.hideHandle();
        })
    }

    public showZoneTagIndications = (cy: Cy.Instance, zoneInstance: ZoneInstance) => {

        cy.nodes().forEach(node => {
            let handleType: string = this.getCorrectHandleForNode(node.id(), zoneInstance);
            cy.emit('showhandle', [node, handleType]);
        });
    };

    public showZoneTagIndicationForNode = (nodeId: string, zoneInstance: ZoneInstance, cy: Cy.Instance) => {
        let node = cy.getElementById(nodeId);
        let handleType: string = this.getCorrectHandleForNode(nodeId, zoneInstance);
        cy.emit('showhandle', [node, handleType]);
    }

    public hideZoneTagIndications = (cy: Cy.Instance) => {
        cy.emit('hidehandles');
    };

    public getCorrectHandleForNode = (nodeId: string, zoneInstance: ZoneInstance): string => {
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
