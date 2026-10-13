/**
 * Created by ob0695 on 7/17/2018.
 */
import { Action, State, StateContext } from '@ngxs/store';
import { Selector } from '@ngxs/store';
import { UpdateIsDesigner, UpdateIsViewOnly } from '../actions/workspace.action';

export interface WorkspaceStateModel {
    isViewOnly: boolean;
    isDesigner: boolean;
}

@State<WorkspaceStateModel>({
    name: 'workspace',
    defaults: {
        isViewOnly: false,
        isDesigner: true
    }
})

export class WorkspaceState {

    constructor() {}

    @Selector() static isViewOnly(state: WorkspaceStateModel): boolean {
        return state.isViewOnly;
    }
    @Selector() static isDesigner(state: WorkspaceStateModel): boolean {
        return state.isDesigner;
    }

    @Action(UpdateIsViewOnly)
    updateIsViewOnly({getState, setState}: StateContext<WorkspaceStateModel>, action: UpdateIsViewOnly) {
        const state = getState();
        setState({
            ...state,
            isViewOnly: action.isViewOnly
        });
    }

    @Action(UpdateIsDesigner)
    updateIsDesigner({getState, patchState}: StateContext<WorkspaceStateModel>, action: UpdateIsDesigner) {
        const state = getState();
        patchState({
            isDesigner: action.isDesigner
        });
    }
}
