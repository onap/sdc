#!/usr/bin/python

##############################################################################
#
# action_library_client.py
#
# A command-line client for the SDC Action Library.
#
#
# Usage:
#
#  Usage: action_library_client.py [--help] [--url <url>] [--in <filename>]
#                                  [--out <filename>] [--config <filename>]
#                                  [--log <filename>] [--uuid <uuid>]
#                                  [--curl] [--dryrun] [--verbose] [--version]
#                                  [--list | --create | --update= | --delete |
#                                   --checkout | --undocheckout | --checkin | --submit]
#
#  Optional arguments:
#    --help                Show this help message and exit
#    --url <url>           REST endpoint URL
#    --in <filename>       Path to JSON input file (else STDIN)
#    --out <filename>      Path to JSON output file (else STDOUT or logfile)
#    --config <filename>   Path to configuration file
#    --log <filename>      Path to logfile (else STDOUT)
#    --uuid <uuid>         Action UUID, (=='actionInvariantUUID')
#    --curl                Use curl transport impl
#    --dryrun              Describe what will happen, execute nothing
#    --verbose             Verbose diagnostic output
#    --version             Print script version and exit
#    --list                List actions
#    --create              Create new action (requires --in)
#    --update              Update existing action (requires --uuid, --in)
#    --delete              Delete existing action (requires --uuid)
#    --checkout            Create minor version candidate (requires --uuid)
#    --undocheckout        Discard minor version candidate (requires --uuid)
#    --checkin             Create minor version from candidate (requires --uuid)
#    --submit              Create next major version (requires --uuid)
#
# For example:
#
#    ./action_library_client.py --url http://10.147.97.199:8080 --list
#
# Output:
#   - Return values:
#      - 0 - OK
#      - 1 - GENERAL_ERROR
#      - 2 - ARGUMENTS_ERROR
#      - 3 - HTTP_FORBIDDEN_ERROR
#      - 4 - HTTP_BAD_REQUEST_ERROR
#      - 5 - HTTP_GENERAL_ERROR
#      - 6 - PROCESS_ERROR
#   - JSON - to stdout:
#      - Delimited by "----------"
#      - Delimiter overrideable with ALC_JSON_DELIMITER setting.
#
# Configuration/env settings:
#   - ALC_HTTP_USER - HTTP BASIC username
#   - ALC_HTTP_PASS - HTTP BASIC password
#   - ALC_HTTP_INSECURE - allow untrusted SSL (server) connections.
#   - ALC_TIMEOUT_SECONDS - invocation (e.g. HTTP) timeout in seconds.
#   - ALC_JSON_DELIMITER - JSON delimiter in ouput.
#   - ALC_ECOMP_INSTANCE_ID - X-ECOMP-InstanceID header
#
# Configuration by 0600-mode INI file (section "action_library_client") is preferred.
#
# See:
#    http://10.147.97.199:8080/api-docs/ - REST API Swagger docs
#    https://www.python.org/dev/peps/pep-0008/ - style guide
#    ../doc/SDC_Action_Lib_API_AID_1610_13.pdf - REST API dev guide
#
# Version history:
# - 1.0.0 November 28th 2016, LP, initial impl.
# - 1.0.1 November 29th 2016, LP, constants, documentation, add --version.
# - 1.0.2 November 29th 2016, LP, logging to files, stream-handling.
# - 1.0.3 November 30th 2016, LP, optionally read config from env or config file.
# - 1.1.0 December 3rd 2016, LP, backport from Python 3.4.2 to 2.6.6(!).
#
##############################################################################


import sys
import os
import logging
import base64
import tempfile
import uuid
import json
import ssl
import urllib2
import subprocess
import ConfigParser
from abc import abstractmethod


###############################################################################


class Constants(object):
    """Common constants, for want of a better language feature..."""
    # Values.
    VERSION = "1.1.0"
    APPLICATION = "action_library_client"
    ACTIONS_URI = "onboarding-api/workflow/v1.0/actions"
    ECOMP_INSTANCE_ID = "sdc_alc"
    TIMEOUT_SECONDS_DEFAULT = 30
    JSON_DELIMITER_DEFAULT = "----------"
    LOG_FORMAT = "%(name)s\t%(levelname)s\t%(asctime)s\t%(message)s"
    # Env variable names.
    ENV_HTTP_USER = "ALC_HTTP_USER"
    ENV_HTTP_PASS = "ALC_HTTP_PASS"
    ENV_HTTP_INSECURE = "ALC_HTTP_INSECURE"
    ENV_HTTP_CAFILE = "ALC_HTTP_CAFILE"
    ENV_TIMEOUT_SECONDS = "ALC_TIMEOUT_SECONDS"
    ENV_JSON_DELIMITER = "ALC_JSON_DELIMITER"
    ENV_ECOMP_INSTANCE_ID = "ALC_ECOMP_INSTANCE_ID"


###############################################################################


class ResponseCodes(object):
    """Responses returned by IRESTClient impls."""
    OK = 0
    GENERAL_ERROR = 1
    ARGUMENTS_ERROR = 2
    HTTP_NOT_FOUND_ERROR = 3
    HTTP_FORBIDDEN_ERROR = 4
    HTTP_BAD_REQUEST_ERROR = 5
    HTTP_GENERAL_ERROR = 6
    PROCESS_GENERAL_ERROR = 9


###############################################################################


class FinalizeStatus(object):
    """Finalization operations."""
    Checkout = "Checkout"
    UndoCheckout = "Undo_Checkout"
    CheckIn = "Checkin"
    Submit = "Submit"


###############################################################################


class ArgsDict(dict):
    """A dict which makes attributes accessible as properties."""
    def __getattr__(self, attr):
        return self[attr]

    def __setattr__(self, attr, value):
        self[attr] = value


###############################################################################


class ArgumentParser(object):
    """A minimal reimpl of the argparse library, core in later Python releases"""
    ACTIONS = ["list", "create", "update", "delete", "checkout", "undocheckout", "checkin", "submit"]
    PARMS = ["url", "in", "out", "config", "log", "uuid"]
    OTHER = ["curl", "dryrun", "verbose", "version", "help"]

    def parse_args(self, clargs):
        """Parse command-line args, returning a dict that exposes everything as properties."""
        args = ArgsDict()
        args.action = None
        for arg in self.ACTIONS + self.PARMS + self.OTHER:
            args[arg] = None
        skip = False
        try:
            for i, clarg in enumerate(clargs):
                if skip:
                    skip = False
                    continue
                if not clarg.startswith("--"):
                    raise Exception("Invalid argument: {0}".format(clarg))
                arg = str(clarg[2:])
                if arg in self.ACTIONS:
                    if args.action:
                        raise Exception("Duplicate actions: --{0}, {1}".format(args.action, clarg))
                    args.action = arg
                elif arg in self.PARMS:
                    try:
                        args[arg] = clargs[i + 1]
                        skip = True
                    except IndexError:
                        raise Exception("Option {0} requires an argument".format(clarg))
                elif arg in self.OTHER:
                    args[arg] = True
                else:
                    raise Exception("Invalid argument: {0}".format(clarg))

            # Check action args.

            if args.action:
                if not args.url:
                    raise Exception("--url required for every action")
                if not args.uuid:
                    if args.action not in ["create", "list"]:
                        raise Exception("--uuid required for every action EXCEPT --list/--create")

            # Read from file or stdin, and replace the problematic "in"
            # property with "infile".

            if args.action in ["create", "update"]:
                if args["in"]:
                    args.infile = open(args["in"], mode="r")
                else:
                    args.infile = sys.stdin

        except Exception as e:
            print(e)
            ArgumentParser.usage()
            sys.exit(ResponseCodes.ARGUMENTS_ERROR)
        return args

    @staticmethod
    def usage():
        """Print usage message."""
        print("" +
            "Usage: action_library_client.py [--help] [--url <url>] [--in <filename>]\n" +
            "                                 [--out <filename>] [--config <filename>]\n" +
            "                                 [--log <filename>] [--uuid <uuid>]\n" +
            "                                 [--curl] [--dryrun] [--verbose] [--version]\n" +
            "                                 [--list | --create | --update= | --delete |\n" +
            "                                  --checkout | --undocheckout | --checkin | --submit]\n" +
            "\n" +
            "Optional arguments:\n" +
            "  --help                Show this help message and exit\n" +
            "  --url <url>           REST endpoint URL\n" +
            "  --in <filename>       Path to JSON input file (else STDIN)\n" +
            "  --out <filename>      Path to JSON output file (else STDOUT or logfile)\n" +
            "  --config <filename>   Path to configuration file\n" +
            "  --log <filename>      Path to logfile (else STDOUT)\n" +
            "  --uuid <uuid>         Action UUID, (=='actionInvariantUUID')\n" +
            "  --curl                Use curl transport impl\n" +
            "  --dryrun              Describe what will happen, execute nothing\n" +
            "  --verbose             Verbose diagnostic output\n" +
            "  --version             Print script version and exit\n" +
            "  --list                List actions\n" +
            "  --create              Create new action (requires --in)\n" +
            "  --update              Update existing action (requires --uuid, --in)\n" +
            "  --delete              Delete existing action (requires --uuid)\n" +
            "  --checkout            Create minor version candidate (requires --uuid)\n" +
            "  --undocheckout        Discard minor version candidate (requires --uuid)\n" +
            "  --checkin             Create minor version from candidate (requires --uuid)\n" +
            "  --submit              Create next major version (requires --uuid)")


###############################################################################


class Settings(object):
    """Settings read from (optional) configfile, or environment."""

    def __init__(self, args):
        """Construct for command-line args."""
        self.config = ConfigParser.ConfigParser()
        if args.config:
            self.config.read(args.config)

    def get(self, name, default_value=None):
        """Get setting from configfile or environment"""
        try:
            return self.config.get(Constants.APPLICATION, name)
        except (KeyError, ConfigParser.NoSectionError, ConfigParser.NoOptionError):
            try:
                return os.environ[name]
            except KeyError:
                return default_value


###############################################################################


# Python3: metaclass=ABCMeta
class IRESTClient(object):
    """Base class for local, proxy and dryrun impls."""

    def __init__(self, args):
        self.args = args
        self.logger = Runner.get_logger()
        self.settings = Settings(args)

    @abstractmethod
    def list(self):
        """Abstract list operation."""
        pass

    @abstractmethod
    def create(self):
        """Abstract list operation."""
        pass

    @abstractmethod
    def update(self):
        """Abstract list operation."""
        pass

    @abstractmethod
    def delete(self):
        """Abstract list operation."""
        pass

    @abstractmethod
    def version(self, status):
        """Abstract list operation."""
        pass

    @staticmethod
    def new_uuid():
        """Generate UUID."""
        return str(uuid.uuid4())

    def get_timeout_seconds(self):
        """Get request timeout in seconds."""
        return self.settings.get(Constants.ENV_TIMEOUT_SECONDS,
                                 Constants.TIMEOUT_SECONDS_DEFAULT)

    def get_http_insecure(self):
        """Get whether SSL certificate checks are (inadvisably) disabled."""
        return True if self.settings.get(Constants.ENV_HTTP_INSECURE) else False

    def get_http_cafile(self):
        """Get optional CA file for SSL server cert validation"""
        if not self.get_http_insecure():
            return self.settings.get(Constants.ENV_HTTP_CAFILE)

    def get_basic_credentials(self):
        """Generate Authorization: header."""
        usr = self.settings.get(Constants.ENV_HTTP_USER)
        pwd = self.settings.get(Constants.ENV_HTTP_PASS)
        if usr and pwd:
            return base64.b64encode(bytes("{0}:{1}".format(usr, pwd))).decode("ascii")
        else:
            raise Exception("REST service credentials not found")

    def make_service_url(self):
        """Generate service URL based on command-line arguments."""
        url = self.args.url
        if "/onboarding-api/" not in url:
            separator = "" if url.endswith("/") else "/"
            url = "{0}{1}{2}".format(url, separator, str(Constants.ACTIONS_URI))
        if self.args.uuid:
            separator = "" if url.endswith("/") else "/"
            url = "{0}{1}{2}".format(url, separator, self.args.uuid)
        return url

    def log_json_response(self, method, json_dict):
        """Log JSON response regardless of transport."""
        json_str = json.dumps(json_dict, indent=4)
        delimiter = self.settings.get(Constants.ENV_JSON_DELIMITER, Constants.JSON_DELIMITER_DEFAULT)
        self.logger.info("HTTP {0} JSON response:\n{1}\n{2}\n{3}\n".format(method, delimiter, json_str, delimiter))
        if self.args.out:
            with open(self.args.out, "w") as tmp:
                tmp.write(json_str)
                tmp.flush()
        elif self.args.log:
            # Directly to stdout if logging is sent to a file.
            print(json_str)

    def log_action(self, action, status=None):
        """Debug action before invocation."""
        url = self.make_service_url()
        name = status if status else self.__get_name()
        self.logger.debug("{0}::{1}({2})".format(name, action, url))

    @staticmethod
    def _get_result_from_http_response(code):
        """Get script returncode from HTTP error."""
        if code == 400:
            return ResponseCodes.HTTP_BAD_REQUEST_ERROR
        elif code == 403:
            return ResponseCodes.HTTP_FORBIDDEN_ERROR
        elif code == 404:
            return ResponseCodes.HTTP_NOT_FOUND_ERROR
        return ResponseCodes.HTTP_GENERAL_ERROR

    def __get_name(self):
        """Get classname for diags"""
        return type(self).__name__


###############################################################################


class NativeRESTClient(IRESTClient):
    """In-process IRESTClient impl."""

    def list(self):
        """In-process list impl."""
        self.log_action("list")
        return self.__exec(method="GET", expect_json=True)

    def create(self):
        """In-process create impl."""
        self.log_action("create")
        json_bytes = bytes(self.args.infile.read())
        return self.__exec(method="POST", json_bytes=json_bytes, expect_json=True)

    def update(self):
        """In-process update impl."""
        self.log_action("update")
        json_bytes = bytes(self.args.infile.read())
        return self.__exec(method="PUT", json_bytes=json_bytes, expect_json=True)

    def delete(self):
        """In-process delete impl."""
        self.log_action("delete")
        return self.__exec(method="DELETE")

    def version(self, status):
        """In-process version impl."""
        self.log_action("version", status)
        json_bytes = bytes(json.dumps({"status": status}))
        return self.__exec(method="POST", json_bytes=json_bytes, expect_json=True)

    def __exec(self, method, json_bytes=None, expect_json=None):
        """Build command, execute it, validate and return response."""
        try:
            url = self.make_service_url()
            timeout = float(self.get_timeout_seconds())
            cafile = self.get_http_cafile()
            headers = {
                "Content-Type": "application/json",
                "Accept": "application/json",
                "Authorization": "Basic {0}".format(self.get_basic_credentials()),
                "X-ECOMP-InstanceID": Constants.ECOMP_INSTANCE_ID,
                "X-ECOMP-RequestID": IRESTClient.new_uuid()
            }

            handler = urllib2.HTTPHandler
            if hasattr(ssl, 'create_default_context'):
                ctx = ssl.create_default_context(cafile=cafile)
                if self.get_http_insecure():
                    ctx.check_hostname = False
                    ctx.verify_mode = ssl.CERT_NONE
                handler = urllib2.HTTPSHandler(context=ctx) if url.lower().startswith("https") else urllib2.HTTPHandler

            self.logger.debug("URL {0} {1}: {2}".format(url, method, json_bytes))

            opener = urllib2.build_opener(handler)
            request = urllib2.Request(url, data=json_bytes, headers=headers)
            request.get_method = lambda: method

            f = None
            try:
                f = opener.open(request, timeout=timeout)
                return self.__handle_response(f, method, expect_json)
            finally:
                if f:
                    f.close()

        except urllib2.HTTPError as err:
            self.logger.exception(err)
            return IRESTClient._get_result_from_http_response(err.getcode())
        except urllib2.URLError as err:
            self.logger.exception(err)
            return ResponseCodes.HTTP_GENERAL_ERROR

    def __handle_response(self, f, method, expect_json):
        """Devolve response handling because of the """
        self.logger.debug("HTTP {0} status {1}, reason:\n{2}".format(method, f.getcode(), f.info()))
        if expect_json:
            # JSON responses get "returned", but actually it's the logging that
            # most callers will be looking for.
            json_body = json.loads(f.read().decode("utf-8"))
            self.log_json_response(method, json_body)
            return json_body
        # Not JSON, but the operation succeeded, so return True.
        return ResponseCodes.OK


###############################################################################


class CURLRESTClient(IRESTClient):
    """Remote/curl IRESTClient impl."""

    def list(self):
        """curl list impl"""
        self.log_action("list")
        return self._exec(method="GET", expect_json=True)

    def create(self):
        """curl create impl"""
        self.log_action("create")
        data_args = ["--data", "@{0}".format(self.args.infile.name)]
        return self._exec(method="POST", extra_args=data_args, expect_json=True)

    def update(self):
        """curl update impl"""
        self.log_action("update")
        data_args = ["--data", "@{0}".format(self.args.infile.name)]
        return self._exec(method="PUT", extra_args=data_args, expect_json=True)

    def delete(self):
        """curl delete impl"""
        self.log_action("delete")
        return self._exec(method="DELETE", expect_json=False)

    def version(self, status):
        """curl version impl"""
        self.log_action("version", status)
        with tempfile.NamedTemporaryFile(mode="w", delete=False) as tmp:
            tmp.write(json.dumps({"status": status}))
            tmp.flush()
        data_args = ["--data", "@{0}".format(tmp.name)]
        return self._exec(method="POST", extra_args=data_args, expect_json=True)

    def make_curl_cmd(self, method, url, extra_args):
        """Build curl command without executing."""
        cmd = ["curl", "-i", "-s", "-X", method]
        if self.get_http_insecure():
            cmd.append("-k")
        cmd.extend(["--connect-timeout", str(self.get_timeout_seconds())])
        cmd.extend(["--header", "Accept: application/json"])
        cmd.extend(["--header", "Content-Type: application/json"])
        cmd.extend(["--header", "Authorization: Basic {0}".format(self.get_basic_credentials())])
        cmd.extend(["--header", "X-ECOMP-InstanceID: {0}".format(Constants.ECOMP_INSTANCE_ID)])
        cmd.extend(["--header", "X-ECOMP-RequestID: {0}".format(IRESTClient.new_uuid())])
        if extra_args:
            for extra_arg in extra_args:
                cmd.append(extra_arg)
        cmd.append("{0}".format(url))
        return cmd

    @staticmethod
    def debug_curl_cmd(cmd):
        """Debug curl command, for diags and dryrun."""
        buf = ""
        for token in cmd:
            if token is "curl" or token.startswith("-"):
                buf = "{0}{1} ".format(buf, token)
            else:
                buf = "{0}\"{1}\" ".format(buf, token)
        return buf

    def _exec(self, method, extra_args=None, expect_json=None):
        """Execute action.

        Build command, invoke curl, validate and return response.
        Overridden by DryRunRESTClient.
        """
        url = self.make_service_url()
        cmd = self.make_curl_cmd(method, url, extra_args)
        self.logger.info("Executing: {0}".format(CURLRESTClient.debug_curl_cmd(cmd)))

        try:
            output = subprocess.check_output(cmd, stderr=subprocess.STDOUT).decode()
            if not expect_json:
                return ResponseCodes.OK
            try:
                separator = output.index("\r\n\r\n{")
                self.logger.debug("HTTP preamble:\n{0}".format(output[:separator]))
                json_body = json.loads(output[(separator+4):])
                self.log_json_response(method, json_body)
                return json_body
            except ValueError:
                self.logger.warning("Couldn't find HTTP separator in curl output:\n{}".format(output))
            code = CURLRESTClient.__get_http_code(output)
            return IRESTClient._get_result_from_http_response(code)
        except subprocess.CalledProcessError as err:
            self.logger.exception(err)
            return ResponseCodes.PROCESS_GENERAL_ERROR

    @staticmethod
    def __get_http_code(output):
        """Attempt to guess HTTP result from (error) output."""
        for line in output.splitlines():
            if line.startswith("HTTP"):
                tokens = line.split()
                if len(tokens) > 2:
                    try:
                        return int(tokens[1])
                    except ValueError:
                        pass
        return ResponseCodes.HTTP_GENERAL_ERROR


###############################################################################


class DryRunRESTClient(CURLRESTClient):
    """Neutered IRESTClient impl; only logs."""

    def _exec(self, method, extra_args=None, expect_json=None):
        """Override."""
        url = self.make_service_url()
        cmd = self.make_curl_cmd(method, url, extra_args)
        self.logger.info("[DryRun] {0}".format(CURLRESTClient.debug_curl_cmd(cmd)))


###############################################################################


class Runner(object):
    """A bunch of static housekeeping supporting the launcher."""

    @staticmethod
    def get_logger():
        """Get logger instance."""
        return logging.getLogger(Constants.APPLICATION)

    @staticmethod
    def get_rest_client(args):
        """Get the configured REST client impl, local, remote or dryrun."""
        if args.dryrun:
            return DryRunRESTClient(args)
        elif args.curl:
            return CURLRESTClient(args)
        else:
            return NativeRESTClient(args)

    @staticmethod
    def execute(args):
        """Execute the requested action."""
        client = Runner.get_rest_client(args)
        if args.version:
            print(Constants.VERSION)
        elif args.help:
            ArgumentParser.usage()
        elif args.action == "list":
            return client.list()
        elif args.action == "create":
            return client.create()
        elif args.action == "update":
            return client.update()
        elif args.action == "delete":
            return client.delete()
        elif args.action == "checkout":
            return client.version(FinalizeStatus.Checkout)
        elif args.action == "checkin":
            return client.version(FinalizeStatus.CheckIn)
        elif args.action == "undocheckout":
            return client.version(FinalizeStatus.UndoCheckout)
        elif args.action == "submit":
            return client.version(FinalizeStatus.Submit)
        else:
            logger = Runner.get_logger()
            logger.info("No action specified. Try --help.")

    @staticmethod
    def parse_args(raw):
        """Parse command-line args, returning dict."""
        return ArgumentParser().parse_args(raw)


###############################################################################


def execute(raw):
    """Delegate which executes minus error-handling, exposed for unit-testing."""

    # Intercept Python 2.X.

    if not (sys.version_info[0] == 2 and sys.version_info[1] >= 6):
        raise EnvironmentError("Python 2.6/2.7 required")

    # Parse command-line args.

    args = Runner.parse_args(raw)

    # Redirect logging to a file (freeing up STDIN) if directed.

    logging.basicConfig(level=logging.INFO, filename=args.log, format=Constants.LOG_FORMAT)

    # Set loglevel.

    logger = Runner.get_logger()
    if args.verbose:
        logger.setLevel(logging.DEBUG)
    logger.debug("Parsed arguments: {0}".format(args))

    # Execute request.

    return Runner.execute(args)


###############################################################################


def main(raw):
    """Execute for command-line arguments."""

    logger = Runner.get_logger()
    try:
        result = execute(raw)
        result_code = result if isinstance(result, int) else ResponseCodes.OK
        logger.debug("Execution complete. Returning result {0} ({1})".format(result, result_code))
        sys.exit(result_code)
    except Exception as err:
        logger.exception(err)
        sys.exit(ResponseCodes.GENERAL_ERROR)


###############################################################################


if __name__ == "__main__":
    main(sys.argv[1:])
