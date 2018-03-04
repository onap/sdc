import { Component, Input, Output, ViewEncapsulation, EventEmitter } from '@angular/core';
import { EventListenerService } from 'app/services';
import { GRAPH_EVENTS } from 'app/utils';

@Component({
    selector: 'zone-container',
    templateUrl: './zone-container.component.html',
    styleUrls: ['./zone-container.component.less'],
    encapsulation: ViewEncapsulation.None
})

export class ZoneContainerComponent {
    @Input() title:string;
    @Input() class:string;
    @Input() count:number;
    @Input() showZone:boolean;
    @Input() minifyZone:boolean;
    constructor(private eventListenerService:EventListenerService) {}

    private unminifyZone = () => {
        this.minifyZone = !this.minifyZone;
        this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_ZONE_SIZE_CHANGE);
    }

}