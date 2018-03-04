import { Component, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import { ZoneInstanceConfig, ZoneInstanceMode } from 'app/models/graph/zones/zone-child';

@Component({
    selector: 'zone-instance',
    templateUrl: './zone-instance.component.html',
    styleUrls: ['./zone-instance.component.less'],
    encapsulation: ViewEncapsulation.None
})
export class ZoneInstanceComponent {

    @Input() config:ZoneInstanceConfig;
    @Input() defaultIconText:string;
    @Input() isActive:boolean;
    @Input() activeInstanceMode: ZoneInstanceMode;
    @Output() modeChange: EventEmitter<any> = new EventEmitter<any>();
    private MODE = ZoneInstanceMode;

    private setMode = (mode:ZoneInstanceMode, event?:any):void => {
        if(!this.isActive || this.isActive && mode == ZoneInstanceMode.TAG){ //when active, do not allow hover/select mode toggling
            this.modeChange.emit({newMode: mode, instance: this.config});
        }
        if(event){
            event.stopPropagation();
        }
    }

}