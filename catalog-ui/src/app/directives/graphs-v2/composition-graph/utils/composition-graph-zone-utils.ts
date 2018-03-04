import { PolicyInstance } from "app/models/graph/zones/policy-instance";
import { ZoneConfig, ZoneInstanceConfig } from "app/models/graph/zones/zone-child";
import { DynamicComponentService } from "app/ng2/services/dynamic-component.service";
import { PaletteAnimationComponent } from "app/ng2/components/ui/palette-animation/palette-animation.component";
import { Point } from "../../../../models";

export class CompositionGraphZoneUtils {

    constructor(private dynamicComponentService: DynamicComponentService) {}

    public createCompositionZones(){
        let zones = {
            'policy': new ZoneConfig('Policies', 'P', 'policy', false),
            'group': new ZoneConfig('Groups', 'G', 'group', false)
        };
        return zones;
    }

    public initPolicyInstances(policyZone:ZoneConfig, policies:Array<PolicyInstance>) {
        if(policies && policies.length){
            policyZone.showZone = true;
        }
        _.forEach(policies, (policy:PolicyInstance) => {
            policyZone.instances.push(new ZoneInstanceConfig(policy));
        });
    }

    public addInstanceToZone(zone:ZoneConfig, instance:PolicyInstance){
        zone.instances.push(new ZoneInstanceConfig(instance));
    };

    private findZoneCoordinates(zoneType):Point{
        let point:Point = new Point(0,0);
        let zone = angular.element(document.querySelector('.' + zoneType + '-zone'));
        let wrapperZone = zone.offsetParent();  
        point.x = zone.prop('offsetLeft') + wrapperZone.prop('offsetLeft'); 
        point.y =  zone.prop('offsetTop') + wrapperZone.prop('offsetTop');        
        return point;
    }

    public showAnimationToZone = (startPoint:Point, zoneType:string) => {
        
        let paletteToZoneAnimation = this.dynamicComponentService.createDynamicComponent(PaletteAnimationComponent);
        paletteToZoneAnimation.instance.from = startPoint;
        paletteToZoneAnimation.instance.to = this.findZoneCoordinates(zoneType);
        paletteToZoneAnimation.instance.iconName = zoneType;
        paletteToZoneAnimation.instance.runAnimation();
    }

    
}

CompositionGraphZoneUtils.$inject = [
    'DynamicComponentService'
];