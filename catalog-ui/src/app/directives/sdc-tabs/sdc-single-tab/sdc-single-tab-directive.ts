'use strict';

export class SdcSingleTabDirective implements ng.IDirective {

    constructor(private $compile:ng.ICompileService, private $parse:ng.IParseService) {
    }

    restrict = 'E';

    link = (scope, elem:any, attrs:any, ctrl:any) => {
        if (!elem.attr('inner-sdc-single-tab')) {
            let name = this.$parse(elem.attr('ctrl'))(scope);
            elem = elem.removeAttr('ctrl');
            elem.attr('inner-sdc-single-tab', name);
            this.$compile(elem)(scope);
        }
    };

    public static factory = ($compile:ng.ICompileService, $parse:ng.IParseService)=> {
        return new SdcSingleTabDirective($compile, $parse);
    };
}

export class InnerSdcSingleTabDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        singleTab: "=",
        isViewOnly: "="
    };

    replace = true;
    restrict = 'A';
    controller = '@';
    template = '<div ng-include src="singleTab.templateUrl"></div>';

    public static factory = ()=> {
        return new InnerSdcSingleTabDirective();
    };
}

SdcSingleTabDirective.factory.$inject = ['$compile', '$parse'];
InnerSdcSingleTabDirective.factory.$inject = [];

