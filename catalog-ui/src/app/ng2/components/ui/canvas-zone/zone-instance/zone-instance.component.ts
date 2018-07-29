import { Component, Input, Output, EventEmitter, ViewEncapsulation, OnInit, SimpleChange, ElementRef, ViewChild, SimpleChanges } from '@angular/core';
import {
    ZoneInstance, ZoneInstanceMode, ZoneInstanceType,
    IZoneInstanceAssignment
} from 'app/models/graph/zones/zone-instance';
import { PoliciesService } from '../../../../services/policies.service';
import { GroupsService } from '../../../../services/groups.service';
import { IZoneService } from "../../../../../models/graph/zones/zone";
import { EventListenerService } from 'app/services';
import { GRAPH_EVENTS } from '../../../../../utils';
import { Subject, Observable } from 'rxjs';

@Component({
    selector: 'zone-instance',
    templateUrl: './zone-instance.component.html',
    styleUrls: ['./zone-instance.component.less'],
    encapsulation: ViewEncapsulation.None
})
export class ZoneInstanceComponent implements OnInit {

    @Input() zoneInstance:ZoneInstance;
    @Input() defaultIconText:string;
    @Input() isActive:boolean;
    @Input() isViewOnly:boolean;
    @Input() activeInstanceMode: ZoneInstanceMode;
    @Input() hidden:boolean;
    @Input() forceSave:Subject<Function>;
    @Output() modeChange: EventEmitter<any> = new EventEmitter<any>();
    @Output() assignmentSaveStart: EventEmitter<void> = new EventEmitter<void>();
    @Output() assignmentSaveComplete: EventEmitter<boolean> = new EventEmitter<boolean>();
    @Output() tagHandleClick: EventEmitter<ZoneInstance> = new EventEmitter<ZoneInstance>();
    @ViewChild('currentComponent') currentComponent: ElementRef;
    private MODE = ZoneInstanceMode;
    private zoneService:IZoneService;

    constructor(private policiesService:PoliciesService, private groupsService:GroupsService, private eventListenerService:EventListenerService){}

    ngOnInit(){
        if(this.zoneInstance.type == ZoneInstanceType.POLICY){
            this.zoneService = this.policiesService;
        } else {
            this.zoneService = this.groupsService;
        }
        this.forceSave.subscribe((afterSaveFunction:Function) => {
            this.setMode(ZoneInstanceMode.TAG, null, afterSaveFunction);
        })
    }

    ngOnChanges(changes:SimpleChanges) {
        if(changes.hidden){
            this.currentComponent.nativeElement.scrollIntoView({behavior: "smooth", block: "nearest", inline:"end"});
        }
    }

    ngOnDestroy() {
        this.forceSave.unsubscribe();
    }

    private setMode = (mode:ZoneInstanceMode, event?:any, afterSaveCallback?:Function):void => {
        
        if(event){ //prevent event from handle and then repeat event from zone instance
            event.stopPropagation();
        }

        if(!this.isActive && this.activeInstanceMode === ZoneInstanceMode.TAG) {
            return; //someone else is tagging. No events allowed
        }

        if(this.isActive && this.zoneInstance.mode === ZoneInstanceMode.TAG){
            if(mode !== ZoneInstanceMode.TAG) {
                return; //ignore all other events. The only valid option is saving changes.
            }

            let oldAssignments:Array<IZoneInstanceAssignment> = this.zoneInstance.instanceData.getSavedAssignments();
            if(this.zoneInstance.isZoneAssignmentChanged(oldAssignments, this.zoneInstance.assignments)) {

                this.assignmentSaveStart.emit();
                
                this.zoneService.updateZoneInstanceAssignments(this.zoneInstance.parentComponentType, this.zoneInstance.parentComponentID, this.zoneInstance.instanceData.uniqueId, this.zoneInstance.assignments).subscribe(
                    (success) => {
                        this.zoneInstance.instanceData.setSavedAssignments(this.zoneInstance.assignments);
                        if(this.zoneInstance.type === ZoneInstanceType.POLICY){
                            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_POLICY_INSTANCE_UPDATE, this.zoneInstance.instanceData);
                        } else {
                            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_GROUP_INSTANCE_UPDATE, this.zoneInstance.instanceData);
                        }
                        this.assignmentSaveComplete.emit(true);
                        if(afterSaveCallback) afterSaveCallback();
                    }, (error) => {
                        this.zoneInstance.assignments = oldAssignments;
                        this.assignmentSaveComplete.emit(false);
                });
            } else {
                if(afterSaveCallback) afterSaveCallback();
            }
            this.modeChange.emit({newMode: ZoneInstanceMode.NONE, instance: this.zoneInstance});

        } else {
            this.modeChange.emit({newMode: mode, instance: this.zoneInstance});
        }         
        

    } 

    private tagHandleClicked = (event:Event) => {
        this.tagHandleClick.emit(this.zoneInstance);
        event.stopPropagation();
    };

}