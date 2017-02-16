import sys
import os
import unittest
import uuid
import json
import tempfile
import action_library_client

class IntegrationTest(unittest.TestCase):

    HTTP = "http://10.147.97.199:8080"
    HTTPS = "https://10.147.97.199:8443"

    def setUp(self):
        os.environ["ALC_HTTP_USER"] = "AUTH-DELETE"
        os.environ["ALC_HTTP_PASS"] = "test"

    def tearDown(self):
        os.environ["ALC_HTTP_INSECURE"] = ""
        os.environ["ALC_HTTP_USER"] = ""
        os.environ["ALC_HTTP_PASS"] = ""

    @staticmethod
    def __prepare(testcase, name):
        with open(testcase, 'r') as fin:
            jsonk = json.loads(fin.read())
            jsonk['name'] = name
            with tempfile.NamedTemporaryFile(mode='w', delete=False) as temp:
                temp.write(json.dumps(jsonk))
                temp.flush()
                return temp.name

    @staticmethod
    def __get_sequence():
        with open(r'./seq.txt', 'r+') as f:
            value = int(f.read())
            f.seek(0)
            f.write(str(value + 1))
            return value

    def __print_separator(self):
        logger = action_library_client.Runner.get_logger()
        logger.info("==================================================")

    def __list(self, stdargs):
        logger = action_library_client.Runner.get_logger()
        list_response = action_library_client.execute(["--list"] + stdargs)
        logger.info("--list response: {}".format(list_response))
        self.assertTrue(isinstance(list_response, dict))
        return list_response

    def __get_action(self, list_response, ai_uuid):
        for action in list_response['actionList']:
            if action['actionInvariantUUID'] == ai_uuid:
                return action

    def __create_delete(self, extraargs):

        logger = action_library_client.Runner.get_logger()

        # Setup.

        seq = IntegrationTest.__get_sequence()
        name = "Backout{}".format(seq)
        path = IntegrationTest.__prepare("scenarios/Backout.json", name)
        stdargs = ["--url", self.HTTP, "--verbose"]
        if extraargs:
            stdargs.extend(extraargs)

        # List actions.

        self.__print_separator()
        list_response1 = self.__list(stdargs)
        self.assertTrue(isinstance(list_response1, dict))

        # CREATE action.

        self.__print_separator()
        create_response = action_library_client.execute(["--create", "--in", path] + stdargs)
        logger.info("--create response: {}".format(create_response))
        self.assertTrue(isinstance(create_response, dict))
        ai_uuid = create_response['actionInvariantUUID']
        self.assertTrue(ai_uuid)
        self.assertEquals(create_response['status'], 'Locked')
        self.assertEquals(create_response['version'], '0.1')

        # UPDATE action #1.

        self.__print_separator()
        update_response1 = action_library_client.execute(["--update", "--in", path, "--uuid", ai_uuid] + stdargs)
        logger.info("--update response: {}".format(update_response1))
        self.assertTrue(isinstance(update_response1, dict))

        # UPDATE action #2.

        self.__print_separator()
        update_response2 = action_library_client.execute(["--update", "--in", path, "--uuid", ai_uuid] + stdargs)
        logger.info("--update response: {}".format(update_response2))
        self.assertTrue(isinstance(update_response2, dict))

        # CHECKOUT action (usage unknown).

        self.__print_separator()
        try:
            action_library_client.execute(["--checkout", "--uuid", ai_uuid] + stdargs)
            self.fail("--checkout should fail")
        except Exception as err:
            print(err)

        # CHECKIN action.

        self.__print_separator()
        checkin_response = action_library_client.execute(["--checkin", "--in", path, "--uuid", ai_uuid] + stdargs)
        logger.info("--checkin response: {}".format(checkin_response))
        self.assertTrue(isinstance(checkin_response, dict))
        self.assertEquals(checkin_response['status'], 'Available')
        self.assertEquals(checkin_response['version'], '0.1')

        # SUBMIT action.

        self.__print_separator()
        submit_response = action_library_client.execute(["--submit", "--in", path, "--uuid", ai_uuid] + stdargs)
        logger.info("--submit response: {}".format(submit_response))
        self.assertTrue(isinstance(submit_response, dict))
        self.assertEquals(submit_response['status'], 'Final')
        self.assertEquals(submit_response['version'], '1.0')

        # LIST again

        self.__print_separator()
        list_response2 = self.__list(stdargs)
        action_found2 = self.__get_action(list_response2, ai_uuid)
        self.assertTrue(action_found2)

        # DELETE action.

        self.__print_separator()
        delete_response = action_library_client.execute(["--delete", "--uuid", ai_uuid] + stdargs)
        logger.info("--delete response: {}".format(delete_response))
        self.assertEqual(delete_response, action_library_client.ResponseCodes.OK)

        # LIST yet again

        self.__print_separator()
        list_response3 = self.__list(stdargs)
        action_found3 = self.__get_action(list_response3, ai_uuid)
        self.assertFalse(action_found3)

    def __create_undo(self, extraargs):

        # Setup

        logger = action_library_client.Runner.get_logger()
        seq = IntegrationTest.__get_sequence()
        name = "Backout{}".format(seq)
        path = IntegrationTest.__prepare("scenarios/Backout.json", name)
        stdargs = ["--url", self.HTTP, "--verbose"]

        # CREATE action.

        self.__print_separator()
        create_response = action_library_client.execute(["--create", "--in", path] + stdargs + extraargs)
        logger.info("--create response: {}".format(create_response))
        self.assertTrue(isinstance(create_response, dict))
        ai_uuid = create_response['actionInvariantUUID']
        self.assertTrue(ai_uuid)
        self.assertEquals(create_response['status'], 'Locked')
        self.assertEquals(create_response['version'], '0.1')

        # UNDOCHECKOUT action

        self.__print_separator()
        undocheckout_response = action_library_client.execute(["--undocheckout", "--uuid", ai_uuid] + stdargs + extraargs)
        self.assertTrue(isinstance(undocheckout_response, dict))

    def __create_list(self, extraargs):
        # Setup

        logger = action_library_client.Runner.get_logger()
        seq = IntegrationTest.__get_sequence()
        name = "Backout{}".format(seq)
        path = IntegrationTest.__prepare("scenarios/Backout.json", name)
        stdargs = ["--url", self.HTTP, "--verbose"]

        # CREATE action.

        self.__print_separator()
        create_response = action_library_client.execute(["--create", "--in", path] + stdargs + extraargs)
        logger.info("--create response: {}".format(create_response))
        self.assertTrue(isinstance(create_response, dict))
        ai_uuid = create_response['actionInvariantUUID']
        self.assertTrue(ai_uuid)
        self.assertEquals(create_response['status'], 'Locked')
        self.assertEquals(create_response['version'], '0.1')

        # CHECKIN action.

        self.__print_separator()
        checkin_response = action_library_client.execute(["--checkin", "--in", path, "--uuid", ai_uuid] +
                                                         stdargs + extraargs)
        logger.info("--checkin response: {}".format(checkin_response))
        self.assertTrue(isinstance(checkin_response, dict))
        self.assertEquals(checkin_response['status'], 'Available')
        self.assertEquals(checkin_response['version'], '0.1')

        try:
            # LIST.

            self.__print_separator()
            list_response1 = self.__list(stdargs + extraargs)
            action_found1 = self.__get_action(list_response1, ai_uuid)
            self.assertTrue(action_found1)

            # LIST with UUID.

            self.__print_separator()
            list_response2 = self.__list(stdargs + extraargs + ["--uuid", ai_uuid])
            self.assertFalse(hasattr(list_response2, 'actionList'))
            self.assertEquals(len(list_response2['versions']), 1)

            # LIST with bad UUID.

            self.__print_separator()
            list_response3 = action_library_client.execute(["--list"] + stdargs + extraargs +
                                                           ["--uuid", "where_the_wind_blows"])
            if isinstance(list_response3, int):
                self.assertEquals(action_library_client.ResponseCodes.HTTP_NOT_FOUND_ERROR, list_response3)
            else:
                self.assertEquals("ACT1045", list_response3["code"])

        finally:

            # DELETE action

            self.__print_separator()
            action_library_client.execute(["--delete", "--uuid", ai_uuid] + stdargs + extraargs)

    def __http_secure(self, extraargs):
        os.environ["ALC_HTTP_INSECURE"] = ""
        try:
            self.__list(["--url", self.HTTPS, "--verbose"] + extraargs)
            if not (sys.version_info[0] == 2 and sys.version_info[1] == 6):
                self.fail("Should fail (non-2.6) for TLS + secure")
        except Exception:
            pass

    def __http_insecure(self, extraargs):
        os.environ["ALC_HTTP_INSECURE"] = True
        self.__list(["--url", self.HTTPS, "--verbose"] + extraargs)

    def __no_credentials(self, extraargs):

        args = ["--url", self.HTTP] + extraargs
        self.__list(args)
        print("OK")

        os.environ["ALC_HTTP_USER"] = ""
        os.environ["ALC_HTTP_PASS"] = ""
        try:
            action_library_client.execute(["--list"] + args)
            self.fail("Should fail for missing credentials")
        except Exception as e:
            self.assertEquals("REST service credentials not found", e.message)

    def __bad_credentials(self, extraargs):

        args = ["--url", self.HTTP] + extraargs
        self.__list(args)

        os.environ["ALC_HTTP_USER"] = "wakey_wakey"
        os.environ["ALC_HTTP_PASS"] = "rise_and_shine"
        code = action_library_client.execute(["--list"] + args)
        self.assertEquals(action_library_client.ResponseCodes.HTTP_FORBIDDEN_ERROR, code)

    ################################################################################

    def test_https_insecure_local_fail(self):
        self.__http_secure([])

    def test_https_insecure_remote_fail(self):
        self.__http_secure(["--curl"])

    def test_https_native(self):
        self.__http_secure([])

    def test_https_curl(self):
        self.__http_secure(["--curl"])

    def test_undo_checkout_native(self):
        self.__create_undo([])

    def test_undo_checkout_curl(self):
        self.__create_undo(["--curl"])

    def test_create_delete_native(self):
        self.__create_delete([])

    def test_create_delete_curl(self):
        self.__create_delete(["--curl"])

    def test_create_list_native(self):
        self.__create_list([])

    def test_create_list_curl(self):
        self.__create_list(["--curl"])

    def test_bad_credentials_native(self):
        self.__bad_credentials([])

    def test_bad_credentials_curl(self):
        self.__bad_credentials(["--curl"])
    #
    def test_no_credentials_native(self):
        self.__no_credentials([])

    def test_no_credentials_curl(self):
        self.__no_credentials(["--curl"])

    def test_create_to_delete_dryrun(self):
        ai_uuid = str(uuid.uuid4())
        path = IntegrationTest.__prepare("scenarios/Backout.json", "Backout{}".format("001"))
        stdargs = ["--url", self.HTTP, "--verbose", "--dryrun"]
        action_library_client.execute(["--create", "--in", path] + stdargs)
        action_library_client.execute(["--update", "--in", path, "--uuid", ai_uuid] + stdargs)
        action_library_client.execute(["--checkout", "--uuid", ai_uuid] + stdargs)
        action_library_client.execute(["--undocheckout", "--uuid", ai_uuid] + stdargs)
        action_library_client.execute(["--checkin", "--uuid", ai_uuid] + stdargs)
        action_library_client.execute(["--submit", "--uuid", ai_uuid] + stdargs)
        action_library_client.execute(["--list"] + stdargs)
