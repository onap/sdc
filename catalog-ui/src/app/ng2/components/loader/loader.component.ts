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
    @Input() elementSelector:string; // required if is relative true
    @Input() relative:boolean;

    interval;

    constructor (private el: ElementRef, private renderer: Renderer){
    }

    ngOnInit() {

        if (this.elementSelector) {
            let elemParent = angular.element(this.elementSelector);
            let positionStyle:string = elemParent.css('position');
            this.setStyle(positionStyle);
        }

        if (this.relative === true) {
            let positionStyle:string = this.el.nativeElement.parentElement.style.position;
            this.setStyle(positionStyle);
        }
        if (!this.size) {
            this.size = 'large';
        }
    }

    ngOnDestroy(){
        clearInterval(this.interval);
    }

    calculateSizesForFixPosition = (positionStyle:string):void => {
        // This is problematic, I do not want to change the parent position.
        // set the loader on all the screen
        let parentLeft = this.el.nativeElement.parentElement.offsetLeft;
        let parentTop = this.el.nativeElement.parentElement.offsetTop;
        let parentWidth = this.el.nativeElement.parentElement.offsetWidth;
        let parentHeight = this.el.nativeElement.parentElement.offsetHeight;
        this.renderer.setElementStyle(this.el.nativeElement, 'position', positionStyle);
        this.renderer.setElementStyle(this.el.nativeElement, 'top', parentTop);
        this.renderer.setElementStyle(this.el.nativeElement, 'left', parentLeft);
        this.renderer.setElementStyle(this.el.nativeElement, 'width', parentWidth);
        this.renderer.setElementStyle(this.el.nativeElement, 'height', parentHeight);
    };

    setStyle = (positionStyle:string):void => {

        switch (positionStyle) {
            case 'absolute':
            case 'fixed':
                // The parent size is not set yet, still loading, so need to use interval to update the size.
                this.interval = window.setInterval(()=> {
                    this.calculateSizesForFixPosition(positionStyle);
                }, 2000);
                break;
            default:
                // Can change the parent position to relative without causing style issues.
                this.renderer.setElementStyle(this.el.nativeElement.parentElement,'position', 'relative');
                break;
        }
    };

    ngOnChanges(changes: SimpleChanges) {
        if(changes.display){
            this.changeLoaderDisplay(false);
            if ( this.display ) {
                window.setTimeout(():void => {
                    this.changeLoaderDisplay(true);
                }, 500);
            } else {
                window.setTimeout(():void => {
                    this.changeLoaderDisplay(false);
                }, 0);
            }
        }
    }

    changeLoaderDisplay = (display:boolean):void => {
        this.renderer.setElementStyle(this.el.nativeElement, 'display', display ? 'block' : 'none');
    }
}
