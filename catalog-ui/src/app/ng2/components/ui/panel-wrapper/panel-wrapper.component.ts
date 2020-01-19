import {Component} from "@angular/core";
import {Select, Store} from "@ngxs/store";
import {Subscription} from "rxjs/Subscription";
import {GraphState} from "../../../pages/composition/common/store/graph.state";
import {OnSidebarOpenOrCloseAction} from "../../../pages/composition/common/store/graph.actions";

@Component({
    selector: 'panel-wrapper-component',
    templateUrl: './panel-wrapper.component.html',
    styleUrls: ['./panel-wrapper.component.less']
})
export class PanelWrapperComponent {
    @Select(GraphState.withSidebar) withSidebar$: boolean;

    tabs: Array<any>;
    subscription: Subscription;

    constructor(public store: Store) {
    }

    private toggleSidebarDisplay = () => {
        // this.withSidebar = !this.withSidebar;
        this.store.dispatch(new OnSidebarOpenOrCloseAction());
    }
}
