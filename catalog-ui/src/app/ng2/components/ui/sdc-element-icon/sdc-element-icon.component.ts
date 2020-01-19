import {Component, Input, OnInit} from "@angular/core";
import {ComponentType, SdcElementType, ResourceType} from "../../../../utils/constants";


export class ElementIcon {
    iconName: string;
    color: string;
    backgroundColor: string;
    type: string
    shape: string;
    size: string;

    constructor(name?: string, type?:string,  backgroundColor?:string, color?:string, shape?: string, size?:string) {
        this.iconName = name || 'default';
        this.type = type || 'resource_24';
        this.backgroundColor = backgroundColor || 'primary';
        this.color = color || "white";
        this.shape = shape || "circle";
        this.size = size || "x_large";
    }
}

@Component({
    selector: 'sdc-element-icon',
    templateUrl: './sdc-element-icon.component.html',
    styleUrls: ['./sdc-element-icon.component.less']
})
export class SdcElementIconComponent {

    @Input() iconName: string;
    @Input() elementType: string;
    @Input() uncertified: boolean = false;

    public elementIcon;

    private createIconForDisplay = () => {
        switch (this.elementType) {

            case ComponentType.SERVICE:
                this.elementIcon = new ElementIcon(this.iconName, "services_24", "lightBlue");
                break;
            case ComponentType.SERVICE_PROXY:
                this.elementIcon = new ElementIcon(this.iconName, "services_24", "white", "primary");
                break;
            case ResourceType.CONFIGURATION:
                this.elementIcon = new ElementIcon(this.iconName, "resources_24", "purple", "white", 'circle', "medium");
                break;
            case SdcElementType.GROUP:
                this.elementIcon = new ElementIcon("group", "resources_24", "blue", 'white', 'rectangle');
                break;
            case SdcElementType.POLICY:
                this.elementIcon = new ElementIcon("policy", "resources_24", "darkBlue2", 'white', 'rectangle');
                break;
            case ResourceType.CP:
            case ResourceType.VL:
                this.elementIcon = new ElementIcon(this.iconName, "resources_24", "purple", '', '', 'medium');
                break;
            default:
                this.elementIcon = new ElementIcon(this.iconName, "resources_24", "purple");
        }
    }

    ngOnChanges():void {
        this.createIconForDisplay();
    }
}



