root = File.absolute_path(File.dirname(__FILE__))
file_cache_path root
cookbook_path root + '/cookbooks'
json_attribs root + '/solo.json'
checksum_path root + '/checksums'
data_bag_path root + '/data_bags'
environment_path root + '/environments'
file_backup_path root + '/backup'
file_cache_path root + '/cache'
log_level :info
log_location STDOUT
rest_timeout 300
role_path root + '/roles'
syntax_check_cache_path
umask 0022
verbose_logging nil
