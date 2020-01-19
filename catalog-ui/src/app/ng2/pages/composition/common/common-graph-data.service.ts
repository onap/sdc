import {Injectable} from "@angular/core";
import 'rxjs/add/observable/forkJoin';
import {ComponentInstance} from "../../../../models/componentsInstances/componentInstance";
import {SelectedComponentType} from "./store/graph.actions";
import {RelationshipModel} from "../../../../models/graph/relationship";

@Injectable()
export class CommonGraphDataService {

    public componentInstances: Array<ComponentInstance>;
    public componentInstancesRelations: RelationshipModel[];
    public selectedComponentType: SelectedComponentType;

    constructor() {
    }

    //------------------------ RELATIONS ---------------------------------//
    public setRelations = (componentInstancesRelations: RelationshipModel[]) => {
        this.componentInstancesRelations = this.componentInstancesRelations;
    }

    public getRelations = (): RelationshipModel[] => {
        return this.componentInstancesRelations;
    }

    public addRelation = (componentInstancesRelations: RelationshipModel) => {
        this.componentInstancesRelations.push(componentInstancesRelations);
    }

    public deleteRelation(relationToDelete: RelationshipModel) {
        this.componentInstancesRelations = _.filter(this.componentInstancesRelations, (relationship: RelationshipModel) => {
            return relationship.relationships[0].relation.id !== relationToDelete.relationships[0].relation.id;
        });
    }

    //---------------------------- COMPONENT INSTANCES ------------------------------------//
    public getComponentInstances = (): Array<ComponentInstance> => {
        return this.componentInstances;
    }

    public addComponentInstance = (instance: ComponentInstance) => {
        return this.componentInstances.push(instance);
    }

    public updateComponentInstances = (componentInstances: ComponentInstance[]) => {
        _.unionBy(this.componentInstances, componentInstances, 'uniqueId');
    }

    public updateInstance = (instance: ComponentInstance) => {
        this.componentInstances = this.componentInstances.map(componentInstance => instance.uniqueId === componentInstance.uniqueId? instance : componentInstance);
    }

    public deleteComponentInstance(instanceToDelete: string) {
        this.componentInstances = _.filter(this.componentInstances, (instance: ComponentInstance) => {
            return instance.uniqueId !== instanceToDelete;
        });
    }

    //----------------------------SELECTED COMPONENT -----------------------//

    public setSelectedComponentType = (selectedType: SelectedComponentType) => {
        this.selectedComponentType = selectedType;
    }
}
