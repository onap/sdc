import unittest
import os
import tempfile
import ConfigParser
import action_library_client as ALC


class D(dict):

    def __init__(self, *args, **kwargs):
        super(D, self).__init__(*args, **kwargs)
        self.__dict__ = self


class UnitTest(unittest.TestCase):

    def __write_config_file(self, map):
        with tempfile.NamedTemporaryFile(mode="w", delete=False) as tmp:
            config = ConfigParser.ConfigParser()
            config.add_section("action_library_client")
            for k, v in map.items():
                section = config.set("action_library_client", k, v)
            config.write(tmp)
            tmp.flush()
            return tmp.name

    def test_argument_parser(self):
        # nothing = ALC.ArgumentParser().parse_args([])
        # self.assertEquals(nothing.help, None)
        # self.assertEquals(nothing.version, None)
        # self.assertEquals(nothing.verbose, None)
        #
        # help = ALC.ArgumentParser().parse_args(["--help"])
        # self.assertEquals(help.help, True)

        uuidx = ALC.ArgumentParser().parse_args(["--uuid", "abc"])
        self.assertEquals(uuidx.uuid, "abc")


    def test_settings_get(self):

        os.environ["a"] = "aa"
        os.environ["b"] = "WILL_BE_OVERRIDDEN"

        section = dict()
        section['ALC_HTTP_USER'] = "batman"
        section['ECOMP_INSTANCE_ID'] = "acdc"
        section['b'] = "bb"
        filename = self.__write_config_file(section)

        # with tempfile.NamedTemporaryFile(mode="w", delete=False) as tmp:
        #     config = configparser.ConfigParser()
        #     config.add_section("action_library_client")
        #     section = config["action_library_client"]
        #     config.write(tmp)
        #     tmp.flush()

        settings = ALC.Settings(ALC.Runner.parse_args(["--config", filename]))
        self.assertEquals("aa", settings.get("a"))
        self.assertEquals("bb", settings.get("b"))
        self.assertEquals("batman", settings.get("ALC_HTTP_USER"))
        self.assertEquals("batman", settings.get(ALC.Constants.ENV_HTTP_USER))
        self.assertEquals("ALC_ECOMP_INSTANCE_ID", settings.get("c", ALC.Constants.ENV_ECOMP_INSTANCE_ID))

        os.remove(filename)

    def test_parse_args(self):
        c1 = ALC.Runner.parse_args(["--version"])
        with tempfile.NamedTemporaryFile(mode="w", delete=False) as tmp:
            config = ConfigParser.ConfigParser()
            config.add_section("action_library_client")
            config.set("action_library_client", "ALC_HTTP_USER", "batman")
            config.write(tmp)
            tmp.flush()
        self.assertEquals(c1.version, True)

    def test_get_http_insecure(self):
        c = ALC.DryRunRESTClient(ALC.Runner.parse_args([]))
        self.assertEquals(False, c.get_http_insecure())

    def test_get_http_cafile(self):
        c1 = ALC.DryRunRESTClient(ALC.Runner.parse_args([]))
        self.assertEquals(False, c1.get_http_insecure())
        self.assertIsNone(c1.get_http_cafile())

        filename = self.__write_config_file({"ALC_HTTP_CAFILE": "/tmp/x"})
        c2 = ALC.DryRunRESTClient(ALC.Runner.parse_args(["--config", filename]))
        self.assertEquals(False, c2.get_http_insecure())
        self.assertEquals("/tmp/x", c2.get_http_cafile())

    def test_get_timeout_seconds(self):
        args = ALC.Runner.parse_args(["--version"])
        self.assertEquals(30, ALC.DryRunRESTClient(args).get_timeout_seconds())

    def test_get_basic_credentials(self):
        try:
            saved_user = os.environ["ALC_HTTP_USER"]
            saved_pass = os.environ["ALC_HTTP_PASS"]
        except KeyError:
            saved_user = ""
            saved_pass = ""
        try:
            os.environ["ALC_HTTP_USER"] = "AUTH-DELETE"
            os.environ["ALC_HTTP_PASS"] = "test"
            c = ALC.DryRunRESTClient(ALC.Runner.parse_args([]))
            c1 = c.get_basic_credentials()
            self.assertEqual(c1, "QVVUSC1ERUxFVEU6dGVzdA==")
            os.environ["ALC_HTTP_USER"] = "AUTH-DELETE"
            os.environ["ALC_HTTP_PASS"] = "death"
            c2 = c.get_basic_credentials()
            self.assertNotEqual(c2, "QVVUSC1ERUxFVEU6dGVzdA==")
        finally:
            os.environ["ALC_HTTP_USER"] = saved_user
            os.environ["ALC_HTTP_PASS"] = saved_pass

    def test_get_rest_client(self):
        uuid = ALC.IRESTClient.new_uuid()
        c1 = ALC.Runner.get_rest_client(ALC.Runner.parse_args(["--dryrun"]))
        self.assertTrue(isinstance(c1, ALC.DryRunRESTClient))
        c2 = ALC.Runner.get_rest_client(ALC.Runner.parse_args(["--curl"]))
        self.assertTrue(isinstance(c2, ALC.CURLRESTClient))
        c3 = ALC.Runner.get_rest_client(ALC.Runner.parse_args(["--uuid", uuid]))
        self.assertTrue(isinstance(c3, ALC.NativeRESTClient))

    def test_get_logger(self):
        logger = ALC.Runner.get_logger()
        logger.info("idotlogger")

    def test_new_uuid(self):
        uuid = ALC.IRESTClient.new_uuid()
        self.assertEqual(len(uuid), 36)

    def test_make_service_url(self):
        uuid = ALC.IRESTClient.new_uuid()

        args1 = ALC.Runner.parse_args(["--url", "http://banana"])
        client1 = ALC.DryRunRESTClient(args1)
        self.assertEqual(client1.make_service_url(),
                         "http://banana/onboarding-api/workflow/v1.0/actions")

        args2 = ALC.Runner.parse_args(["--url", "http://banana/"])
        client2 = ALC.DryRunRESTClient(args2)
        self.assertEqual(client2.make_service_url(),
                         "http://banana/onboarding-api/workflow/v1.0/actions")

        args3 = ["--url", "http://banana/onboarding-api/workflow/v1.1/actions", "--uuid", uuid]
        client3 = ALC.DryRunRESTClient(ALC.Runner.parse_args(args3))
        self.assertEqual(client3.make_service_url(),
                         "http://banana/onboarding-api/workflow/v1.1/actions/{}".format(uuid))

    def test_debug_curl_cmd(self):
        cmd = ["curl", "--header", "banana", "http://something/somewhere"]
        debug = ALC.CURLRESTClient.debug_curl_cmd(cmd)
        self.assertEqual("curl --header \"banana\" \"http://something/somewhere\" ", debug)