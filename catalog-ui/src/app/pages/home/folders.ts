
export interface IItemMenu {

}

export interface IMenuItemProperties {
    text:string;
    group:string;
    state:string;
    dist:string;
    groupname:string;
    states:Array<any>;
}

export class FoldersMenu {
    private _folders:Array<FoldersItemsMenu> = [];

    constructor(folders:Array<IMenuItemProperties>) {
        let self = this;
        folders.forEach(function (folder:IMenuItemProperties) {
            if (folder.groupname) {
                self._folders.push(new FoldersItemsMenuGroup(folder));
            } else {
                self._folders.push(new FoldersItemsMenu(folder));
            }
        });
        self._folders[0].setSelected(true);
    }

    public getFolders():Array<FoldersItemsMenu> {
        return this._folders;
    }

    public getCurrentFolder():FoldersItemsMenu {
        let menuItem:FoldersItemsMenu = undefined;
        this.getFolders().forEach(function (tmpFolder:FoldersItemsMenu) {
            if (tmpFolder.isSelected()) {
                menuItem = tmpFolder;
            }
        });
        return menuItem;
    }

    public setSelected(folder:FoldersItemsMenu):void {
        this.getFolders().forEach(function (tmpFolder:FoldersItemsMenu) {
            tmpFolder.setSelected(false);
        });
        folder.setSelected(true);
    }
}

export class FoldersItemsMenu implements IItemMenu {
    public text:string;
    public group:string;
    public state:string;
    public dist:string;
    public states:Array<any>;

    private selected:boolean = false;

    constructor(menuProperties:IMenuItemProperties) {
        this.text = menuProperties.text;
        this.group = menuProperties.group;
        this.state = menuProperties.state;
        this.states = menuProperties.states;
        this.dist = menuProperties.dist;
    }

    public isSelected():boolean {
        return this.selected;
    }

    public setSelected(value:boolean):void {
        this.selected = value;
    }

    public isGroup():boolean {
        return false;
    }
}

export class FoldersItemsMenuGroup extends FoldersItemsMenu {
    public groupname:string;

    constructor(menuProperties:IMenuItemProperties) {
        super(menuProperties);
        this.groupname = menuProperties.groupname;
    }

    public isGroup():boolean {
        return true;
    }
}
