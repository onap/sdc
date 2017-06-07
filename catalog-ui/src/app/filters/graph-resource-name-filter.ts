export class GraphResourceNameFilter {

    constructor() {
        let filter = <GraphResourceNameFilter>( (name:string) => {
            let context = document.createElement("canvas").getContext("2d");
            context.font = "13px Arial";

            if (67 < context.measureText(name).width) {
                let newLen = name.length - 3;
                let newName = name.substring(0, newLen);

                while (59 < (context.measureText(newName).width)) {
                    newName = newName.substring(0, (--newLen));
                }
                return newName + '...';
            }

            return name;
        });
        return filter;
    }
}
