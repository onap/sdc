package org.openecomp.sdc.asdctool.impl.migration;

public enum MigrationMsg {
    RENMAE_KEY_PROPERTIES_1707("renaming key properties"),
    KEY_PROPERTY_NOT_EXIST("key propery %s not exist"),
    RENAME_KEY_PROPERTY_FAILED("failed to rename key property %s"),
    FAILED_TO_RETRIEVE_GRAPH("failed to get graph %s"),
    PROPERTY_KEY_NOT_EXIST("property key %s not found."),
    FAILED_TO_RETRIEVE_NODES("failed to retrieve nodes from graph. error status: %s"),
    FAILED_TO_GET_NODE_FROM_GRAPH("failed to retrieve node from graph. error status : %s"),
    FAILED_TO_CREATE_NODE("failed to create node of type %s. reason: %s"),
    FAILED_TO_RETRIEVE_CATEGORIES("failed to retrieve categories. error status: %s"),
    FAILED_TO_RETRIEVE_CATEGORY("failed to retrieve category %s. error status: %s"),
    FAILED_TO_CREATE_SUB_CATEGORY("failed to create sub category %s of category %s. error status: %s"),
    FAILED_TO_CREATE_CATEGORY("failed to create category %s. error status: %s"),
    FAILED_TO_RETRIEVE_USER_STATES("failed to retrieve user %s states. error status: %s"),
    FAILED_TO_RETRIEVE_MIGRATION_USER_STATES("failed to retrieve migrating user %s states for deletion. error status: %s"),
    FAILED_TO_RETRIEVE_MIGRATION_USER("failed to retrieve migration user %s. error status: %s"),
    FAILED_TO_RETRIEVE_VERSION_RELATION("failed to retrieve version relation from component with id %s to component with id %s. error status: %s"),
    FAILED_TO_RETRIEVE_REQ_CAP("failed to retrieve fulfilled requirements or capabilities for instance %s. error status: %s"),
    FAILED_TO_RETRIEVE_VERTEX("failed to retrieve vertex with id: %s. error status: %s"),
    FAILED_TO_RETRIEVE_CAP_REQ_VERTEX("failed to retrieve capabilities or requirements vertex for component %s. error status: %s"),
    FAILED_TO_ASSOCIATE_CAP_REQ("failed to associate fulfilled capabilities or requirements for components %s. error status: %s"),
    FAILED_TO_RETRIEVE_TOSCA_DEF("failed to retrieve tosca definition for requirement or capability %s. error status %s"),
    ;

    private String message;

    MigrationMsg(String migrationDescription) {
        this.message = migrationDescription;
    }

    public String getMessage(String ... msgProperties) {
        return String.format(this.message, msgProperties);
    }
}
