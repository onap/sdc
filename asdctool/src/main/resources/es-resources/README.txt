ASDC elasticsearch tool
========================

This tool purpose is to ease and allow updating elasticsearch indices.

In order to use the scripts, you need to verify Python is installed and to install the elasticsearc-py library:
	Verify pip is installed:		$command -v pip
	if not installed:	
		Download https://bootstrap.pypa.io/get-pip.py
		$python get-pip.py  (see instruction: https://pip.pypa.io/en/latest/installing/#installing-with-get-pip-py)
	$pip install elasticsearch


Tool contains:
	- index_ops.py
	  This script includes operations on elasticsearch index:
	  
	  create index:
		$python index_ops.py -o create -a <elasticsearch hostname> -n <indexName> -f <index mapping file>
		
	  delete index:
		$python index_ops.py -o delete -a <elasticsearch hostname> -n <indexName>
	  
	  copy index (assumes destination index already exists):
	    $python index_ops.py -o move -a <elasticsearch hostname> -n <indexName> -t <toIndex>
		
		
	- file_utils.py
	  This script includes operations on files 
	  
	- audit_migration_1602.py
	  This script run full flow to migrate audit information from previous versions to ASDC 1602
	  It has 2 inputs:
	   1. config_properties.py - this file holds configuration (hostname, index name, index mapping file etc.)
	   2. folder of fields mapping per elasticsearch type (map old field to new field)
	  The flow of this script is as follow:
	   * create temp index with correct index mapping
	   * scan the audit index to get all records
	   * manipulate fields data and insert it to temp index
	   * delete audit index 
	   * create audit index with correct mapping
	   * copy from temp index to newly created audit index
	   * delete temp index