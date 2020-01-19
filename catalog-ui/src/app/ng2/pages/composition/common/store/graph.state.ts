import { Action, Selector, State, StateContext} from '@ngxs/store';
import {
    OnSidebarOpenOrCloseAction,
    SelectedComponentType,
    SetSelectedComponentAction,
    TogglePanelLoadingAction
} from "./graph.actions";
import { PolicyInstance, GroupInstance, Component as TopologyTemplate, ComponentInstance, LeftPaletteComponent, FullComponentInstance} from "app/models";
import { TopologyTemplateService } from "app/ng2/services/component-services/topology-template.service";
import { tap } from "rxjs/operators";
import { CompositionService } from "app/ng2/pages/composition/composition.service";
import {GroupsService} from "../../../../services/groups.service";
import {PoliciesService} from "../../../../services/policies.service";
import {WorkspaceService} from "../../../workspace/workspace.service";

export class CompositionStateModel {

    isViewOnly?: boolean;
    panelLoading?: boolean;
    selectedComponentType?: SelectedComponentType;
    selectedComponent?: PolicyInstance | GroupInstance | TopologyTemplate | ComponentInstance;
    withSidebar?: boolean;
}

@State<CompositionStateModel>({
    name: 'composition',
    defaults: {
         withSidebar: true
    }
})
export class GraphState {

    constructor(private topologyTemplateService: TopologyTemplateService,
                private compositionService: CompositionService,
         private policiesService:PoliciesService, private groupsService:GroupsService,
                 private workspaceService: WorkspaceService) {}

    @Action(SetSelectedComponentAction)
    setSelectedComponent({dispatch, getState, patchState}:StateContext<CompositionStateModel>, action: SetSelectedComponentAction) {
        
        const state:CompositionStateModel = getState();
        
        patchState({ panelLoading: true });

        if(action.payload.component instanceof ComponentInstance){
            let originComponent = this.compositionService.getOriginComponentById(action.payload.component.getComponentUid());
            if(!originComponent) {
                return this.topologyTemplateService.getFullComponent(action.payload.component.originType, action.payload.component.getComponentUid())
                .pipe(tap(resp => {
                    this.compositionService.addOriginComponent(resp);
                    this.compositionService.setSelectedComponentType(SelectedComponentType.COMPONENT_INSTANCE);
                    patchState({
                        selectedComponent: new FullComponentInstance(action.payload.component, resp), 
                        selectedComponentType: action.payload.type,
                        panelLoading: false
                    });
                    }, err => {
                        patchState({
                            panelLoading: false
                        })
                    }
                ));
            } else {
                patchState({
                    selectedComponent: new FullComponentInstance(action.payload.component, originComponent), 
                    selectedComponentType: action.payload.type,
                    panelLoading: false
                });
            }
        } else if (action.payload.component instanceof PolicyInstance) {
            let topologyTemplate = this.workspaceService.metadata;
            return this.policiesService.getSpecificPolicy(topologyTemplate.componentType, topologyTemplate.uniqueId, action.payload.component.uniqueId).pipe(tap(resp => 
                {
                    this.compositionService.updatePolicy(resp);
                    patchState({
                        selectedComponent: resp, 
                        selectedComponentType: action.payload.type,
                        panelLoading: false
                    })
                }, err => {
                    patchState({
                        panelLoading: false
                    })
                 }
            ));
        
        } else if (action.payload.component instanceof GroupInstance) {
            let topologyTemplate = this.workspaceService.metadata;
            return this.groupsService.getSpecificGroup(topologyTemplate.componentType, topologyTemplate.uniqueId, action.payload.component.uniqueId).pipe(tap(resp => {
                this.compositionService.updateGroup(resp);
                patchState({
                    selectedComponent: resp,
                    selectedComponentType: action.payload.type,
                    panelLoading: false
                });
            }, err => {
                patchState({
                    panelLoading: false
                })
             }
            ));
        } else { //TopologyTemplate
            patchState({
                selectedComponent: action.payload.component,
                selectedComponentType: action.payload.type,
                panelLoading: false
            })
        }
    }


    // @Action(UpdateSelectedComponentNameAction)
    // UpdateSelectedComponentNameAction({patchState}:StateContext<CompositionStateModel>, action: UpdateSelectedComponentNameAction) {

    //     switch(action.payload.type){
    //         case SelectedComponentType.COMPONENT_INSTANCE:
    //             this.store.dispatch(new UpdateComponentInstancesAction([action.payload.component]));
    //             break;
    //         case SelectedComponentType.POLICY:
    //             this.store.dispatch(new UpdatePolicyNameAction(action.payload.uniqueId, action.payload.newName));
    //             break;
    //         case SelectedComponentType.GROUP:
    //             this.store.dispatch(new UpdateGroupInstancesAction)

    //     }
    //     if(action.payload.type === SelectedComponentType.COMPONENT_INSTANCE){
            
    //     } 
    
    // }

    @Selector() 
    static getSelectedComponent(state:CompositionStateModel) {
        return state.selectedComponent;
    }

    @Selector() 
    static getSelectedComponentId(state:CompositionStateModel) {
        return state.selectedComponent.uniqueId;
    }

    @Selector() 
    static getSelectedComponentType(state:CompositionStateModel) {
        return state.selectedComponentType;
    }


    @Action(OnSidebarOpenOrCloseAction)
    onSidebarOpenOrCloseAction({getState, setState}:StateContext<CompositionStateModel>) {
        const state:CompositionStateModel = getState();

        setState({
            ...state,
            withSidebar: !state.withSidebar
        });
    }
    
    @Action(TogglePanelLoadingAction)
    TogglePanelLoading({patchState}:StateContext<CompositionStateModel>, action: TogglePanelLoadingAction) {

        patchState({
            panelLoading: action.payload.isLoading
        });
    }

    @Selector() static withSidebar(state):boolean {
        return state.withSidebar;
    }

}