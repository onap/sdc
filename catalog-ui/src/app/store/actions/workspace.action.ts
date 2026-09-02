/**
 * Created by ob0695 on 7/17/2018.
 */

export class UpdateIsViewOnly {
    static readonly type = '[WORKSPACE] UpdateIsViewOnly';

    constructor(public isViewOnly:boolean) {
    }
}

export class UpdateIsDesigner {
    static readonly type = '[WORKSPACE] UpdateIsDesigner';

    constructor(public isDesigner:boolean) {
    }
}
