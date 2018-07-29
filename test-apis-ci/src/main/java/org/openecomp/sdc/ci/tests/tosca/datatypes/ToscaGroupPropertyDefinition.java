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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToscaGroupPropertyDefinition that = (ToscaGroupPropertyDefinition) o;

        if (min_vf_module_instances != null ? !min_vf_module_instances.equals(that.min_vf_module_instances) : that.min_vf_module_instances != null)
            return false;
        if (max_vf_module_instances != null ? !max_vf_module_instances.equals(that.max_vf_module_instances) : that.max_vf_module_instances != null)
            return false;
        if (vf_module_label != null ? !vf_module_label.equals(that.vf_module_label) : that.vf_module_label != null)
            return false;
        if (vfc_list != null ? !vfc_list.equals(that.vfc_list) : that.vfc_list != null) return false;
        if (vf_module_type != null ? !vf_module_type.equals(that.vf_module_type) : that.vf_module_type != null)
            return false;
        if (vf_module_description != null ? !vf_module_description.equals(that.vf_module_description) : that.vf_module_description != null)
            return false;
        if (initial_count != null ? !initial_count.equals(that.initial_count) : that.initial_count != null)
            return false;
        if (volume_group != null ? !volume_group.equals(that.volume_group) : that.volume_group != null) return false;
        return availability_zone_count != null ? availability_zone_count.equals(that.availability_zone_count) : that.availability_zone_count == null;
    }

    @Override
    public int hashCode() {
        int result = min_vf_module_instances != null ? min_vf_module_instances.hashCode() : 0;
        result = 31 * result + (max_vf_module_instances != null ? max_vf_module_instances.hashCode() : 0);
        result = 31 * result + (vf_module_label != null ? vf_module_label.hashCode() : 0);
        result = 31 * result + (vfc_list != null ? vfc_list.hashCode() : 0);
        result = 31 * result + (vf_module_type != null ? vf_module_type.hashCode() : 0);
        result = 31 * result + (vf_module_description != null ? vf_module_description.hashCode() : 0);
        result = 31 * result + (initial_count != null ? initial_count.hashCode() : 0);
        result = 31 * result + (volume_group != null ? volume_group.hashCode() : 0);
        result = 31 * result + (availability_zone_count != null ? availability_zone_count.hashCode() : 0);
        return result;
    }
}
