import {Component, OnInit} from '@angular/core';
import {GRAPH_EVENTS, SdcElementType} from "app/utils";
import {LeftPaletteComponent, Point} from "app/models";
import {EventListenerService} from "app/services";
import {LeftPaletteMetadataTypes} from "app/models/components/displayComponent";

@Component({
    selector: 'app-palette-popup-panel',
    templateUrl: './palette-popup-panel.component.html',
    styleUrls: [ './palette-popup-panel.component.less' ],
})
export class PalettePopupPanelComponent implements OnInit {

    public panelTitle: string;
    public isShowPanel: boolean;
    private component: Component;
    private displayComponent: LeftPaletteComponent;
    private popupPanelPosition:Point = new Point(0,0);

    constructor(private eventListenerService: EventListenerService) {
        this.isShowPanel = false;
    }

    ngOnInit() {
        this.registerObserverCallbacks();
    }

    public onMouseEnter() {
        this.isShowPanel = true;
    }

    public getMenuItems = () => {
        return [{
            text: 'Delete',
            iconName: 'trash-o',
            iconType: 'common',
            iconMode: 'secondary',
            iconSize: 'small',
            type: '',
            action: () => this.addZoneInstance()
        }];
    }

    public onMouseLeave() {
        this.isShowPanel = false;
    }

    public addZoneInstance(): void {
        if(this.displayComponent) {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_ADD_ZONE_INSTANCE_FROM_PALETTE, this.component, this.displayComponent, this.popupPanelPosition);
            this.hidePopupPanel();
        }
    }

    private registerObserverCallbacks() {

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_SHOW_POPUP_PANEL,
            (displayComponent: LeftPaletteComponent, sectionElem: HTMLElement) => {
                this.showPopupPanel(displayComponent, sectionElem);
            });

        this.eventListenerService.registerObserverCallback(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HIDE_POPUP_PANEL, () => this.hidePopupPanel());
    }

    private getPopupPanelPosition (sectionElem: HTMLElement):Point {
        let pos: ClientRect = sectionElem.getBoundingClientRect();
        let offsetX: number = -30;
        const offsetY: number = pos.height / 2; 
        return new Point((pos.right + offsetX), (pos.top - offsetY + window.pageYOffset));
    };

    private setPopupPanelTitle(component: LeftPaletteComponent): void {
        if (component.componentSubType === SdcElementType.GROUP) {
            this.panelTitle = "Add Group";
            return;
        }

        if (component.componentSubType === SdcElementType.POLICY) {
            this.panelTitle = "Add Policy";
            return;
        }
    }

    private showPopupPanel(displayComponent:LeftPaletteComponent, sectionElem: HTMLElement) {
        if(!this.isShowPanel){
            this.displayComponent = displayComponent;
            this.setPopupPanelTitle(displayComponent);
            this.popupPanelPosition = this.getPopupPanelPosition(sectionElem);
            this.isShowPanel = true;
        }
    };
    
    private hidePopupPanel() {
        if(this.isShowPanel){
            this.isShowPanel = false;
        }
    };
}
