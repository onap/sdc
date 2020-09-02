import { Component, Input } from '@angular/core';
import { Component as TopologyTemplate, RelationshipModel, Relationship, Requirement } from "app/models";
import { CompositionService } from "app/ng2/pages/composition/composition.service";
import { ResourceNamePipe } from "app/ng2/pipes/resource-name.pipe";
import { ComponentInstanceServiceNg2 } from "app/ng2/services/component-instance-services/component-instance.service";
import { WorkspaceService } from "app/ng2/pages/workspace/workspace.service";

@Component({
    selector: 'requirement-list',
    templateUrl: './requirement-list.component.html'
})
export class RequirementListComponent  {
    @Input() component: TopologyTemplate;
    @Input() requirements: Array<Requirement>;
    @Input() isInstanceSelected:boolean;
    @Input() isViewOnly: boolean;
    readonly:boolean;
    
    constructor(private compositionService: CompositionService,
                private workspaceService: WorkspaceService,
                private componentInstanceServiceNg2: ComponentInstanceServiceNg2) {}

    public getRelation = (requirement:any):any => {
        if (this.isInstanceSelected && this.component.componentInstancesRelations) {
            let relationItem:Array<RelationshipModel> = _.filter(this.component.componentInstancesRelations, (relation:RelationshipModel) => {
                return relation.fromNode === this.component.uniqueId &&
                    _.filter(relation.relationships, (relationship:Relationship) => {
                        return relationship.relation.requirement == requirement.name && relationship.relation.requirementOwnerId == requirement.ownerId;
                    }).length;
            });

            if (relationItem && relationItem.length) {
                return {
                    type: requirement.relationship.split('.').pop(),
                    requirementName: ResourceNamePipe.getDisplayName(this.compositionService.componentInstances[_.map
                    (this.compositionService.componentInstances, "uniqueId").indexOf(relationItem[0].toNode)].name)
                };
            }
        }
        return null;
    };

    onMarkAsExternal(requirement:Requirement) {
       if (requirement.external){
	        requirement.external = false;
       } else {
	        requirement.external = true;
       }
       this.componentInstanceServiceNg2.updateInstanceRequirement(this.workspaceService.metadata.getTypeUrl(), this.workspaceService.metadata.uniqueId, this.component.uniqueId, requirement).subscribe((response:any) => {
        }, (error) => { console.log("An error has occured setting external: ", error);  });;

    }

};

