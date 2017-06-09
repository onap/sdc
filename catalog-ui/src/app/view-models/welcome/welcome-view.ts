'use strict';

export interface IWelcomeViewMode {
    onCloseButtonClick():void;
}

export class WelcomeViewModel {

    firstLoad:boolean = true;
    alreadyAnimated:Array<number> = [];

    static '$inject' = [
        '$scope',
        '$state'
    ];

    constructor(private $scope:IWelcomeViewMode,
                private $state:ng.ui.IStateService
               ) {
        this.init();
        this.initScope();
        window.setTimeout(():void => {
            this.loadImages(():void=> {
                window.setTimeout(():void =>{
                    $(".sdc-welcome-new-page").addClass("animated fadeIn");
                },1000);
            });
        },0);
    }

    private initScope = ():void => {
        this.$scope.onCloseButtonClick = ():void => {
            this.$state.go("dashboard", {});
        };
    };

    private init = ():void => {
        let viewModelsHtmlBasePath:string = 'src/app/view-models/';
        $('body').keyup((e):void=> {
            if (e.keyCode == 27) { // escape key maps to keycode `27`
                this.$state.go('dashboard');
            }
        });
    };

    private loadImages = (callback:Function):void => {
        let src = $('.sdc-welcome-wrapper').css('background-image');
        let url = src.match(/\((.*?)\)/)[1].replace(/('|")/g,'');

        let img = new Image();
        img.onload = function() {
            callback();
        };
        img.src = url;
    };
    
}
