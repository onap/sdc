/**
 * Created by obarda on 12/21/2016.
 */
declare module Cy {

    interface Instance {

        expandCollapse(options?: ExpandCollapseOptions);
        collapseAll(options?: ExpandCollapseOptions);
    }



    interface CollectionNodes {

        qtip(tooltipOptions: TooltipOption);
    }


    interface TooltipOption {
        content?: string | Function;
        position?: any;
        style?: any;
        show?:any;
        hide?:any;
        includeLabels?: boolean;
    }

    interface ExpandCollapseOptions {
        layoutBy:any;
        fisheye: boolean;
        undoable: boolean;
        expandCueImage: string;
        collapseCueImage: string;
        expandCollapseCueSize: number;
        expandCollapseCueSensitivity: number;
        cueOffset: number;
      //  expandCollapseCuePosition: string;
    }
}