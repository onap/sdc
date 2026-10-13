import { Component, Input } from '@angular/core';
import { Store } from '@ngxs/store';
import { Component as TopologyTemplate } from 'app/models/components/component';
import { Relationship, RelationshipModel } from 'app/models/graph/relationship';
import { Requirement } from 'app/models/requirement';
import { CompositionService } from 'app/pages/composition/composition.service';
import { WorkspaceService } from 'app/pages/workspace/workspace.service';
import { ResourceNamePipe } from 'app/pipes/resource-name.pipe';
import { ComponentInstanceService } from 'app/services/component-instance-services/component-instance.service';
import { EventListenerService } from 'app/services/event-listener.service';
import { GRAPH_EVENTS } from 'app/utils/constants';
import { TogglePanelLoadingAction } from '../../../../common/store/graph.actions';

@Component({
    selector: 'requirement-list',
    templateUrl: './requirement-list.component.html'
})
export class RequirementListComponent  {
    @Input() component: TopologyTemplate;
    @Input() requirements: Requirement[];
    @Input() isInstanceSelected: boolean;
    @Input() isViewOnly: boolean;
    readonly: boolean;

    constructor(private compositionService: CompositionService,
                private workspaceService: WorkspaceService,
                private componentInstanceService: ComponentInstanceService,
                private store: Store,
                private eventListenerService: EventListenerService) {}

    public getRelation = (requirement: any): any => {
        if (this.isInstanceSelected && this.component.componentInstancesRelations) {
            const relationItem: RelationshipModel[] = _.filter(this.component.componentInstancesRelations, (relation: RelationshipModel) => {
                return relation.fromNode === this.component.uniqueId &&
                    _.filter(relation.relationships, (relationship: Relationship) => {
                        return relationship.relation.requirement == requirement.name && relationship.relation.requirementOwnerId == requirement.ownerId;
                    }).length;
            });

            if (relationItem && relationItem.length) {
                return {
                    type: requirement.relationship.split('.').pop(),
                    requirementName: ResourceNamePipe.getDisplayName(this.compositionService.componentInstances[_.map
                    (this.compositionService.componentInstances, 'uniqueId').indexOf(relationItem[0].toNode)].name)
                };
            }
        }
        return null;
    }

    onMarkAsExternal(requirement: Requirement) {
       this.store.dispatch(new TogglePanelLoadingAction({isLoading: true}));
       requirement.external = !requirement.external;
       this.componentInstanceService.updateInstanceRequirement(this.workspaceService.metadata.getTypeUrl(), this.workspaceService.metadata.uniqueId, this.component.uniqueId, requirement)
           .subscribe(() => {
               this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_COMPONENT_INSTANCE_REQUIREMENT_EXTERNAL_CHANGED, this.component.uniqueId, requirement);
               this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
           } , (error) => { console.log('An error has occured setting external: ', error);
                            requirement.external = !requirement.external;
                            this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
           });
    }

}
