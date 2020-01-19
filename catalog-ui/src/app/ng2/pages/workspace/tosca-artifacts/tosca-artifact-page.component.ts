import {Component, OnInit, ViewChild} from "@angular/core";
import {WorkspaceService} from "../workspace.service";
import {SdcUiServices} from "onap-ui-angular";
import {ArtifactModel} from "../../../../models";
import {Select, Store} from "@ngxs/store";
import {WorkspaceState} from "../../../store/states/workspace.state";
import * as _ from "lodash";
import {ArtifactGroupType, COMPONENT_FIELDS} from "../../../../utils";
import {GetArtifactsByTypeAction} from "../../../store/actions/artifacts.action";
import {Observable} from "rxjs/index";
import {ArtifactsState} from "../../../store/states/artifacts.state";
import {ArtifactType} from "../../../../utils/constants";
import {map} from "rxjs/operators";

@Component({
    selector: 'tosca-artifact-page',

    templateUrl: './tosca-artifact-page.component.html',
    styleUrls: ['./tosca-artifact-page.component.less', '../../../../../assets/styles/table-style.less']
})
export class ToscaArtifactPageComponent implements OnInit {

    @Select(WorkspaceState.isViewOnly) isViewOnly$: boolean;
    @ViewChild('toscaArtifactsTable') table: any;
    public toscaArtifacts$: Observable<ArtifactModel[]>;
    public componentId: string;
    public componentType:string;

    constructor(private serviceLoader: SdcUiServices.LoaderService, private workspaceService: WorkspaceService, private store: Store) {
    }


    ngOnInit(): void {
        this.componentId = this.workspaceService.metadata.uniqueId;
        this.componentType = this.workspaceService.metadata.componentType;

        this.store.dispatch(new GetArtifactsByTypeAction({componentType:this.componentType, componentId:this.componentId, artifactType:ArtifactGroupType.TOSCA}));
        this.toscaArtifacts$ = this.store.select(ArtifactsState.getArtifactsByType).pipe(map(filterFn => filterFn(ArtifactGroupType.TOSCA)));
    }

    onActivate(event) {
        if(event.type === 'click'){
            this.table.rowDetail.toggleExpandRow(event.row);
        }
    }
}