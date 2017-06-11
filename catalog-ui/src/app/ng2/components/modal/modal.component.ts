/**
 * Created by rc2122 on 6/1/2017.
 */
import { Component, ElementRef, Input, OnInit, OnDestroy } from '@angular/core';
import * as $ from 'jquery';
import {ButtonsModelMap} from "app/models/button";

@Component({
    selector: 'modal',
    templateUrl: './modal.component.html',
    styleUrls:['modal.component.less']
})

export class ModalComponent implements OnInit, OnDestroy {
    @Input() size: string; 'xl|l|md|sm|xsm'
    @Input() title: string;
    @Input() public buttons:ButtonsModelMap;
    private modalElement: JQuery;
    private buttonsNames:Array<string>;

    constructor( el: ElementRef ) {
        this.modalElement = $(el.nativeElement);
    }

    ngOnInit(): void {
        let modal = this;
        this.modalElement.appendTo('body');
        if(this.buttons){
            this.buttonsNames = Object.keys(this.buttons);
        }
    }

    ngOnDestroy(): void {
        this.modalElement.remove();
    }

    open(): void {
        this.modalElement.show();
        $('body').addClass('modal-open');
    }

    close(): void {
        this.modalElement.hide();
        $('body').removeClass('modal-open');
    }
}
