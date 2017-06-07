export class ResourceNameFilter {


    constructor() {
        let filter = <ResourceNameFilter>( (name:string) => {
            if (name) {
                //let newName:string =  _.last(name.split('.'));
                let newName =
                    _.last(_.last(_.last(_.last(_.last(_.last(_.last(_.last(name.split('tosca.nodes.'))
                        .split('network.')).split('relationships.')).split('org.openecomp.')).split('resource.nfv.'))
                        .split('nodes.module.')).split('cp.')).split('vl.'));
                if (newName) {
                    return newName;
                }
                return name;
            }
        });

        return filter;
    }
}
