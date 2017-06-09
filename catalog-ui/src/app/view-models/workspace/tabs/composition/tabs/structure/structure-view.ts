'use strict';
import {ICompositionViewModelScope} from "../../composition-view-model";

interface IStructureViewModel extends ICompositionViewModelScope {
}

export class StructureViewModel {
    static '$inject' = [
        '$scope'
    ];

    constructor(private $scope:IStructureViewModel) {
    }
}
