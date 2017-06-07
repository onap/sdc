'use strict';
export interface IExpandCollapseScope extends ng.IScope {
    toggle():void;
    collapsed:boolean;
    expandedSelector:string;
    content:string;
    isCloseOnInit:boolean;
    loadDataFunction:Function;
    isLoadingData:boolean;
}

export class ExpandCollapseDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        expandedSelector: '@',
        loadDataFunction: '&?',
        isCloseOnInit: '=?'
    };

    public replace = false;
    public restrict = 'AE';
    public transclude = true;

    template = ():string => {
        return require('./expand-collapse.html');
    };

    link = (scope:IExpandCollapseScope, $elem:any) => {
        scope.collapsed = false;
        scope.isLoadingData = false;
        $elem.addClass('expanded');


        if (scope.isCloseOnInit) {
            window.setTimeout(function () {
                toggle();
            }, 0);
        }
        //
        // $elem.click(function () {
        //     toggle();
        // });
        $elem.bind('click', function() {
            toggle();
        })
        let expand = ():void => {
            $elem.addClass('expanded');
            scope.collapsed = false;

            let element = $(scope.expandedSelector)[0];
            let prevWidth = element.style.height;
            element.style.height = 'auto';
            let endWidth = getComputedStyle(element).height;
            element.style.height = prevWidth;
            element.offsetHeight; // force repaint
            element.style.transition = 'height .3s ease-in-out';
            element.style.height = endWidth;
            element.hidden = false;
            element.addEventListener('transitionend', function transitionEnd(event) {
                if (event['propertyName'] == 'height') {
                    element.style.transition = '';
                    element.style.height = 'auto';
                    element.removeEventListener('transitionend', transitionEnd, false);
                }
            }, false)
        };

        let collapse = ():void => {
            $elem.removeClass('expanded');
            scope.collapsed = true;

            let element = $(scope.expandedSelector)[0];
            element.style.height = getComputedStyle(element).height;
            element.style.transition = 'height .5s ease-in-out';
            element.offsetHeight; // force repaint
            element.style.height = '0px';
            element.hidden = true;
        };

        let toggle = ():void => {
            if (scope.collapsed === true) {
                if (scope.loadDataFunction) {
                    scope.isLoadingData = true;
                    let onSuccess = () => {
                        window.setTimeout(function () {
                            expand();
                            scope.isLoadingData = false;
                        }, 0);
                    };
                    scope.loadDataFunction().then(onSuccess);
                }
                else {
                    if (scope.isLoadingData === false) {
                        expand();
                    }
                }

            } else {
                if (scope.isLoadingData === false) {
                    collapse();
                }
            }
        }

    };

    public static factory = ()=> {
        return new ExpandCollapseDirective();
    };
}

ExpandCollapseDirective.factory.$inject = [];
