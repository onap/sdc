package org.openecomp.sdc.ci.tests.tosca.datatypes;

import org.yaml.snakeyaml.TypeDescription;

public class ToscaGroupPropertyDefinition {

    public String min_vf_module_instances;
    public String max_vf_module_instances;
    public String vf_module_label;
    public String vfc_list;
    public String vf_module_type;
    public String vf_module_description;
    public String initial_count;
    public String volume_group;
    public String availability_zone_count;

    public ToscaGroupPropertyDefinition() {
    }

    public String getMin_vf_module_instances() {
        return min_vf_module_instances;
    }

    public void setMin_vf_module_instances(String min_vf_module_instances) {
        this.min_vf_module_instances = min_vf_module_instances;
    }

    public String getMax_vf_module_instances() {
        return max_vf_module_instances;
    }

    public void setMax_vf_module_instances(String max_vf_module_instances) {
        this.max_vf_module_instances = max_vf_module_instances;
    }

    public String getVf_module_label() {
        return vf_module_label;
    }

    public void setVf_module_label(String vf_module_label) {
        this.vf_module_label = vf_module_label;
    }

    public String getVfc_list() {
        return vfc_list;
    }

    public void setVfc_list(String vfc_list) {
        this.vfc_list = vfc_list;
    }

    public String getVf_module_type() {
        return vf_module_type;
    }

    public void setVf_module_type(String vf_module_type) {
        this.vf_module_type = vf_module_type;
    }

    public String getVf_module_description() {
        return vf_module_description;
    }

    public void setVf_module_description(String vf_module_description) {
        this.vf_module_description = vf_module_description;
    }

    public String getInitial_count() {
        return initial_count;
    }

    public void setInitial_count(String initial_count) {
        this.initial_count = initial_count;
    }

    public String getVolume_group() {
        return volume_group;
    }

    public void setVolume_group(String volume_group) {
        this.volume_group = volume_group;
    }

    public String getAvailability_zone_count() {
        return availability_zone_count;
    }

    public void setAvailability_zone_count(String availability_zone_count) {
        this.availability_zone_count = availability_zone_count;
    }

    @Override
    public String toString() {
        return "ToscaGroupPropertyDefinition{" +
                ", min_vf_module_instances='" + min_vf_module_instances + '\'' +
                ", max_vf_module_instances='" + max_vf_module_instances + '\'' +
                ", vf_module_label='" + vf_module_label + '\'' +
                ", vfc_list='" + vfc_list + '\'' +
                ", vf_module_type='" + vf_module_type + '\'' +
                ", vf_module_description='" + vf_module_description + '\'' +
                ", initial_count='" + initial_count + '\'' +
                ", volume_group='" + volume_group + '\'' +
                ", availability_zone_count='" + availability_zone_count + '\'' +
                '}';
    }

    //gets Type description for Yaml snake
    public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaGroupPropertyDefinition.class);
        return typeDescription;
    }
}
