USE dox;
ALTER TABLE vsp_information ADD questionnaire_data text;
ALTER TABLE vsp_information ADD vlm_version frozen<version>;
ALTER TABLE vsp_process ADD type text;
alter table entitlement_pool ADD version_uuid text;
alter table license_key_group ADD version_uuid text;
delete from application_config where namespace='vsp.monitoring' and key = 'component.ceilometer';
