#!/usr/bin/python

##############################################################################
###
### generate-manifest.py
###
### A Vendor utility to generate a valid heat zip manifest file for the AT&T onboarding. 
###
### Usage:
###
###    generate-manifest.py [-f|--folder] vendor-heat-directory [-n|--name] manifest-name [-d|--description] manifet-description
###
### For example:
###
###    ./generate-manifest.py --folder ./vota --name vOTA --description "HOT template to create vOTA server"
###
### Help:
### The script is doing the following:
###  1) Split the files into different types
###     a. .env files
###     b. Network files (anything containing the string network)
###     c. Volume files (anything containing the string volume)
###     d. Top level Heat files
###     e. Other types
###  2) Match env files to heat files – looking for same name ignoring suffix and extension
###  3) Match Network childs
###     a. Look for Top level heats which name is a substring of the name of the Network heat name.
###  4) Match Volume childs
###     a. Look for Top level heats which name is a substring of the name of the Volume heat name.
###  5)      Generate the JSON file from the above
###
###
### Author: Avi Ziv
### Version 1.4 for OPENECOMP 1.0
### Date: 13 July 2016 (c) OPENECOMP
###
##############################################################################

# import os,sys,getopt,json,re
import os, sys, getopt, re
from collections import OrderedDict
from json import JSONEncoder
import json

VERSION = "1.4"
ENV_EXT = ".env"
SHELL_EXT = ".sh"
YAML_EXT = [".yaml", ".yml"]
# VERSION_DELIMITER_PATTERN='_v\d{*}.\d{*}'
# VERSION_DELIMITER_PATTERN='_v*.*'
#v1.0
VERSION_DELIMITER_PATTERN = '_v\d+.\d+'
#07_12_2016
VERSION_DELIMITER_PATTERN2 = '_\d+-\d+-\d+'

# types
HEAT = "HEAT"
HEAT_BASE = "HEAT_BASE"
HEAT_NET = "HEAT_NET"
HEAT_VOL = "HEAT_VOL"
HEAT_ENV = "HEAT_ENV"
SHELL = "SHELL"
OTHER = "OTHER"

globalVolumeVal = "VOLUME"
globalNetworkVal = "NETWORK"
globalBaseVal = "BASE"


def version():
    return VERSION


def usage():
    print ('usage: ' + sys.argv[0] + ' [-f|--folder] vendor-heat-directory [-n|--name] manifest-name [-d|--description] manifet-description' )


def header():
    print ("\nASDC Vendor manifest file generator, version " + version() + "\n")


def getEnvVariables(value, defaultValue):
    try:
        eVal = os.environ[value]
        return eVal
    except KeyError:
        print ("Missing ${" + value + "} envirunment variable. Using default value: " + defaultValue)
    return defaultValue


def getF(listFiles):
    print ("Analyzing files ...")
    foundABase = False
    files = listFiles
    jsons = {}
    lOfEnvs = {}
    lOfVolumes = {}
    lOfNetworks = {}
    lOfHeats = {}
    lOfShels = {}
    lOfArtifacts = {}

    for f in files:
        fullFilename = f[1]
        fObj = ManifestFileInfo(fullFilename)
        if fObj.isEnv():
            lOfEnvs[fObj.file_name] = fObj
        elif fObj.isShell():
            lOfShels[fObj.file_name] = fObj
        elif fObj.isVolume():
            lOfVolumes[fObj.file_name] = fObj
        elif fObj.isNetwork():
            lOfNetworks[fObj.file_name] = fObj
        elif (fObj.isYaml() and not fObj.isBase()):
            lOfHeats[fObj.file_name] = fObj
        elif fObj.isArtifact():
            lOfArtifacts[fObj.file_name] = fObj
        elif (fObj.isBase() and fObj.isYaml()):
            foundABase = True
            lOfHeats[fObj.file_name] = fObj

    jsons['heats'] = lOfHeats
    jsons['envs'] = lOfEnvs
    jsons['shells'] = lOfShels
    jsons['volumes'] = lOfVolumes
    jsons['networks'] = lOfNetworks
    jsons['artifacts'] = lOfArtifacts

    if not foundABase:
        print (">>> Warning: No Base was found")
    return jsons

def loadFilesToList(folder):
    print ("Analyzing files in folder: << " + folder + " >>")
    files = os.listdir(folder)
    listOfFiles = []
    for f in files:
        if os.path.isdir(os.path.join(folder, f)):
            ConsoleLogger.warning("Sub folders are ignored by this script, you may want to remove it before archiving")
            continue

        filename, file_extension = os.path.splitext(f)
        if filename == 'MANIFEST':
            ConsoleLogger.warning("Your folder already contains a manifest file that will be overridden")
            continue
        listOfFiles.append([filename, f])
    return listOfFiles


def make(files):
    flist = []
    dEnvs = {}
    dEnvs = files['envs']
    dHeats = files['heats']
    dNetworks = files['networks']
    dVolumes = files['volumes']
    dArtifacts = files['artifacts']
    dShells = files['shells']

    env_items = dEnvs.items()
    for fileItem in env_items:
        env_name = fileItem[1].file_name
        env_base = fileItem[1].base_file_name
        if env_name in dHeats:
            dHeats[env_name].add(fileItem[1])
            continue

        if env_name in dNetworks.items():
            dNetworks[env_name].add(fileItem[1])
            continue

        if env_name in dVolumes.items():
            dVolumes[env_name[0]].add(env_name[1])
            continue

        for fName in dHeats:
            heat_base = dHeats[fName].base_file_name
            if env_base in heat_base:
                dHeats[fName].add(dEnvs[env_name])
                break
        else:
            for fName in dNetworks:
                net_base = dNetworks[fName].base_file_name
                if env_base in net_base:
                    dNetworks[fName].add(dEnvs[env_name])
                    break
            else:
                for fName in dVolumes:
                    vol_base = dVolumes[fName].base_file_name
                    if env_base in vol_base:
                        dVolumes[fName].add(dEnvs[env_name])
                        break

                else:
                    flist.append(dEnvs[env_name])

    for fName in dVolumes:
        vol_base = dVolumes[fName].base_file_name
        for hfName in dHeats:
            heat_base = dHeats[hfName].base_file_name
            if heat_base in vol_base:
                dHeats[hfName].add(dVolumes[fName])
                break
        else:
            flist.append(dVolumes[fName])

    for fName in dNetworks:
        net_base = dNetworks[fName].base_file_name
        for hfName in dHeats:
            heat_base = dHeats[hfName].base_file_name
            if heat_base in net_base:
                dHeats[hfName].add(dNetworks[fName])
                break
        else:
            flist.append(dNetworks[fName])

    for fName in dHeats:
        flist.append(dHeats[fName])
    for fName in dShells:
        flist.append(dShells[fName])
    for fName in dArtifacts:
        flist.append(dArtifacts[fName])

    print ("\n------------------------------------------------------------\n")
    return flist


def generate(folder, name, description):
    print ("Checking envirunment variables ...")
    global globalVolumeVal
    globalVolumeVal = getEnvVariables("VOLUME", globalVolumeVal)

    global globalNetworkVal
    globalNetworkVal = getEnvVariables("NETWORK", globalNetworkVal)

    global globalBaseVal
    globalBaseVal = getEnvVariables("BASE", globalBaseVal)

    YamlTabCleaner(folder).cleanYamlTabs()

    print ("Generating manifest file ...")
    jsons = getF(loadFilesToList(folder))

    lFiles = make(jsons)
    manifest = Manifest(name, description, '1.0', lFiles)
    output_json = json.dumps(manifest, default=jdefault, indent=4, sort_keys=False)

    f = open(os.path.join(folder, 'MANIFEST.json'), 'w')
    f.write(output_json)
    print("MANIFEST file created")


################

def jdefault(obj):
    if hasattr(obj, '__json__'):
        return obj.__json__()
    else:
        return obj.__dict__


class ManifestFileInfo(object):
    def __init__(self, filename):
        self.name = filename
        self.base = 'false'
        self.data = []
        self.file_name, self.file_extension = os.path.splitext(filename)
        self.base_file_name = re.sub(VERSION_DELIMITER_PATTERN, '', self.file_name)
        self.base_file_name = re.sub(VERSION_DELIMITER_PATTERN2, '', self.base_file_name)

        if self.isEnv():
            self.heat_type = Types.ENV
        elif self.isShell():
            self.heat_type = Types.SHELL
        elif self.isVolume():
            self.heat_type = Types.VOL
        elif self.isNetwork():
            self.heat_type = Types.NET
        elif self.isYaml() and not self.isBase():
            self.heat_type = Types.HEAT
        elif self.isArtifact():
            self.heat_type = Types.OTHER
        elif (self.isBase() and self.isYaml()):
            self.heat_type = Types.HEAT
            self.base = 'true'

    def set(self, data):
        self.data = data

    def add(self, item):
        self.data.append(item)

    def isYaml(self):
        return any(val in self.file_extension.lower() for val in YAML_EXT)

    def isEnv(self):
        return self.file_extension.lower() == ENV_EXT.lower()

    def isShell(self):
        return self.file_extension.lower() == SHELL_EXT.lower()

    def isVolume(self):
        res = globalVolumeVal.lower() in self.file_name.lower()
        return res

    def isNetwork(self):
        res = globalNetworkVal.lower() in self.file_name.lower()
        return res

    def isBase(self):
        res = globalBaseVal.lower() in self.file_name.lower()
        return res

    def isArtifact(self):
        return (not self.isBase() and not self.isVolume() and not self.isNetwork() and not self.isEnv())

    def isHEAT(self):
        return ((self.heat_type == Types.HEAT) | (self.heat_type == Types.BASE) | (self.heat_type == Types.NET) | (
            self.heat_type == Types.VOL))

    def __json__(self):
        dict = OrderedDict(
            [('file', self.name), ('type', self.heat_type)])
        if self.isHEAT():
            dict['isBase'] = self.base
            if self.data != []:
                dict['data'] = self.data

        return dict


class Manifest(object):
    def __init__(self, name, description, version, data):
        self.name = name
        self.description = description
        self.version = version
        self.data = data

    def add(self, data):
        self.data.append(data)

    def __json__(self):
        return OrderedDict([('name', self.name), ('description', self.description), ('data', self.data)])


class YamlTabCleaner(object):
    def __init__(self, folder):
        self.folder = folder

    def replaceTabs(self, sourceFile, targetFile):
        with open(sourceFile, "rt") as fin:
            if '\t' in fin.read():
                print("\'tab\' character was found in the file: " + sourceFile + "\na clean version of the file can be found under \'clean\' folder")
                target = os.path.dirname(targetFile)
                if not os.path.exists(target):
                    os.makedirs(target)
                fin.seek(0)
                with open(targetFile, "wt") as fout:
                    for line in fin:
                        fout.write(line.replace('\t', ' '))

    def cleanYamlTabs(self):
        included_extenstions = ['yml', 'yaml']
        files = [fn for fn in os.listdir(self.folder)
                 if any(fn.endswith(ext) for ext in included_extenstions)]
        target = os.path.join(self.folder, "clean")
        for file in files:
            self.replaceTabs(os.path.join(self.folder, file), os.path.join(target, file))

class ConsoleLogger(object):
    @classmethod
    def error(cls, message):
        print(">>> Error: " + message)

    @classmethod
    def warning(cls, message):
        print(">>> Warning: " + message)


    @classmethod
    def info(cls, message):
        print(">>> Info: " + message)


def enum(**named_values):
    return type('Enum', (), named_values)


################

def main(argv):
    action = ''
    folderName = '.'
    name = ''
    description = ''
    version = ''

    try:
        opts, args = getopt.getopt(argv, "h:f:n:d", ["folder=", "name=", "description=", ])
    except getopt.GetoptError as err:
        # print help information and exit:
        print ('>>>>' + str(err))
        usage()
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            usage()
            sys.exit()
        elif opt in ('-f', '--folder'):
            action = 'generate'
            if not arg:
                print ("Error: missing heat files directory")
                usage()
                sys.exit(2)
            else:
                folderName = arg
        elif opt in ('-n', '--name'):
            name = arg
        elif opt in ('-d', '--description'):
            description = arg
        else:
            usage()

    if action == 'generate':
        generate(folderName, name, description)
        sys.exit()
    else:
        usage()


if __name__ == "__main__":
    header()
    Types = enum(HEAT='HEAT', BASE='HEAT_BASE', NET='HEAT_NET', VOL='HEAT_VOL', ENV='HEAT_ENV', SHELL='SHELL',
                 OTHER='OTHER')
    main(sys.argv[1:])
