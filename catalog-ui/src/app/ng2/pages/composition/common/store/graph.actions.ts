export enum SelectedComponentType {
    COMPONENT_INSTANCE = "COMPONENT_INSTANCE",
    GROUP = "GROUP",
    POLICY = "POLICY",
    TOPOLOGY_TEMPLATE = "TOPOLOGY_TEMPLATE"
}

export class UpdateSelectedComponentAction {
    static readonly type = '[COMPOSITION] UpdateSelectedComponent';

        constructor(public payload: {uniqueId?: string, type?: string}) {
    }
}
    
export class SetSelectedComponentAction {
    static readonly type = '[COMPOSITION] SetSelectedComponent';

    constructor(public payload: {component?: any, type?: SelectedComponentType}) {
        }
}

export class OnSidebarOpenOrCloseAction {
    static readonly type = '[COMPOSITION] OnSidebarOpenOrCloseAction';

    constructor() {
    }
}

export class TogglePanelLoadingAction {
    static readonly type = '[COMPOSITION] TogglePanelLoading';
    constructor(public payload: { isLoading: boolean}) {
    }
}
