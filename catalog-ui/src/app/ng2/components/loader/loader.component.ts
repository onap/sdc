/**
 * Created by rc2122 on 6/6/2017.
 */
import {Component, Input, ElementRef, Renderer, SimpleChanges} from "@angular/core";
@Component({
    selector: 'loader',
    templateUrl: './loader.component.html',
    styleUrls: ['./loader.component.less']
})
export class LoaderComponent {

    @Input() display:boolean;
    @Input() size:string;// small || medium || large
    @Input() relative: boolean;
    @Input() elementSelector: string; // optional. If is relative is set to true, option to pass in element that loader should be relative to. Otherwise, will be relative to parent element.
    

    constructor (private el: ElementRef, private renderer: Renderer){
    }

    ngOnInit() {
        if (!this.size) {
            this.size = 'large';
        }
        if (this.display === true) {
            this.changeLoaderDisplay(true);
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if(changes.display){
            if (this.display) {
                this.changeLoaderDisplay(false); //display is set to true, so loader will appear unless we explicitly tell it not to.
                window.setTimeout((): void => {
                    this.display && this.changeLoaderDisplay(true); //only show loader if we still need to display it.
                }, 500);
            } else {
                window.setTimeout(():void => {
                    this.changeLoaderDisplay(false);
                }, 0);
            }
        }
    }

    changeLoaderDisplay = (display: boolean): void => {
        if (display) {
            this.calculateLoaderPosition();
            this.renderer.setElementStyle(this.el.nativeElement, 'display', 'block');
        } else {
            this.renderer.setElementStyle(this.el.nativeElement, 'display', 'none');
        }
    }

    calculateLoaderPosition = () => {
        if (this.relative === true) { // Can change the parent position to relative without causing style issues.
            let parent = (this.elementSelector) ? angular.element(this.elementSelector).get(0) : this.el.nativeElement.parentElement;
            this.renderer.setElementStyle(parent, 'position', 'relative');
            this.setLoaderPosition(0, 0); //will be relative to parent and appear over specified element
            //TODO: DONT force parent to have position relative; set inner div's style instead of outer element
            // let parentPos: ClientRect = this.el.nativeElement.parentElement.getBoundingClientRect();
            // this.setLoaderPosition(parentPos.top, parentPos.left, parentPos.width, parentPos.height);
        } else {
            this.setLoaderPosition(0, 0); //will appear over whole page
        }
    }

    setLoaderPosition = (top:number, left:number, width?:number, height?:number): void => {
        this.renderer.setElementStyle(this.el.nativeElement, 'position', 'absolute');
        this.renderer.setElementStyle(this.el.nativeElement, 'top', top? top.toString() : "0");
        this.renderer.setElementStyle(this.el.nativeElement, 'left', left? left.toString() : "0");
        this.renderer.setElementStyle(this.el.nativeElement, 'width', width? width.toString() : "100%");
        this.renderer.setElementStyle(this.el.nativeElement, 'height', height? height.toString() : "100%");
    };
}
