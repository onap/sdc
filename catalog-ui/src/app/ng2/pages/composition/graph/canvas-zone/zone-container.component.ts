import { Component, Input, Output, ViewEncapsulation, EventEmitter, OnInit } from '@angular/core';
import { ZoneInstanceType } from 'app/models/graph/zones/zone-instance';

@Component({
    selector: 'zone-container',
    templateUrl: './zone-container.component.html',
    styleUrls: ['./zone-container.component.less'],
    encapsulation: ViewEncapsulation.None
})

export class ZoneContainerComponent implements OnInit {
    @Input() title:string;
    @Input() type:ZoneInstanceType;
    @Input() count:number;   
    @Input() visible:boolean;
    @Input() minimized:boolean;
    @Output() minimize: EventEmitter<any> = new EventEmitter<any>();
    @Output() backgroundClick: EventEmitter<void> = new EventEmitter<void>();
    private class:string;

    constructor() {}

    ngOnInit() {
        this.class = ZoneInstanceType[this.type].toLowerCase();
    }

    private unminifyZone = () => {
        this.minimize.emit();
    }

    private backgroundClicked = () => {
        this.backgroundClick.emit();
    }

}