#!/usr/bin/python

##############################################################################
###
### parse-json.py
###
### A utility to parse a cassnadra-commands file and return the commands per type
### An Example for a json file:
###     {
###         "create":{
###                 "choice_or_other":"CREATE TYPE IF NOT EXISTS choice_or_other (results text)",
###                 "vendor_license_model": "CREATE TABLE IF NOT EXISTS vendor_license_model (vlm_id text PRIMARY KEY, name text, description text, icon text)",
###                 "license_agreement": "CREATE TABLE IF NOT EXISTS license_agreement (vlm_id text, la_id text, name text, description text, type text, contract text, req_const text, fg_ids set<text>, PRIMARY KEY (vlm_id, la_id))",
###                 "feature_group": "CREATE TABLE IF NOT EXISTS feature_group (vlm_id text, fg_id text, name text, description text, ep_ids set<text>, lkg_ids set<text>, refd_by_las set<text>, PRIMARY KEY (vlm_id, fg_id))",
###                 "license_key_group": "CREATE TABLE IF NOT EXISTS license_key_group (vlm_id text,lkg_id text,name text,description text, type text, operational_scope text, ref_fgs set<text>,PRIMARY KEY (vlm_id, lkg_id))",
###         }
###     }
###
### The return for "create" will be:
###                 CREATE TYPE IF NOT EXISTS choice_or_other (results text)
###                 CREATE TABLE IF NOT EXISTS vendor_license_model (vlm_id text PRIMARY KEY, name text, description text, icon text)
###                 CREATE TABLE IF NOT EXISTS license_agreement (vlm_id text, la_id text, name text, description text, type text, contract text, req_const text, fg_ids set<text>, PRIMARY KEY (vlm_id, la_id))
###                 CREATE TABLE IF NOT EXISTS feature_group (vlm_id text, fg_id text, name text, description text, ep_ids set<text>, lkg_ids set<text>, refd_by_las set<text>, PRIMARY KEY (vlm_id, fg_id))
###                 CREATE TABLE IF NOT EXISTS license_key_group (vlm_id text,lkg_id text,name text,description text, type text, operational_scope text, ref_fgs set<text>,PRIMARY KEY (vlm_id, lkg_id))
### Usage:
###
###    parse-json.py -t create -f cassandra-commands.json
###
### For example:
###
###
### Author: Avi Ziv
### Version 1.0
### Date: 3 May 2016
###
##############################################################################

import sys, getopt
import json as json
from collections import OrderedDict


def readJsonFile(file, type):
  with open(file, 'r') as f:
     data = json.load(f, object_pairs_hook=OrderedDict)
     return data[type]

def printJsonTypeEntries(jsonData):
        for i in jsonData.keys():
                print jsonData[i] + ';'


def usage():
    print 'parseJsonFile.py [-f <json-file> & -t <cql-type: drop|create|insert|update|select]'

def main(argv):
    action = ''

    try:
        opts, args = getopt.getopt(argv, "h:f:t:")
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            usage()
            sys.exit()
        elif opt == '-f':
            jsonFile = arg
            action = 'file'
        elif opt == '-t':
            type = arg

    if action == 'file':
        sJson = readJsonFile(jsonFile, type)
        printJsonTypeEntries(sJson)
        sys.exit()
    else:
        usage()


if __name__ == "__main__":
    main(sys.argv[1:])
