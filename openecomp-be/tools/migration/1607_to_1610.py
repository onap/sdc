#!/usr/bin/python
import os
import sys,json,datetime,time,types,httplib,re
import mimetypes

DEFAULT_HOST = "127.0.0.1"
OPENECOMP_BE = "127.0.0.1"

HOST = DEFAULT_HOST
DEFAULT_PORT = "8080"
DEFAULT_USERNAME = "cs0008"
DEFAULT_PASSWORD = "cs0008"

ONBOARD_BASE_PATH = "/onboarding-api/v1.0"
VSP_LIST_PATH = "{0}/vendor-software-products".format(ONBOARD_BASE_PATH)
VSP_ACTIONS_PATH =  "{0}/vendor-software-products/{{vspId}}/actions".format(ONBOARD_BASE_PATH)
VSP_UPLOAD_PATH = "{0}/vendor-software-products/{{vspId}}/upload".format(ONBOARD_BASE_PATH)
VSP_DOWNLOAD_PATH = "{0}/vendor-software-products/{{vspId}}/downloadHeat".format(ONBOARD_BASE_PATH)
VSP_GET_URL = "{0}/vendor-software-products/{{vspId}}".format(ONBOARD_BASE_PATH)

def main(argv):
    username=DEFAULT_USERNAME
    password=DEFAULT_PASSWORD
    host=DEFAULT_HOST

    if not argv:
        print "Going to use default values"
    else:
    	if argv[0].lower() == 'h' or argv[0].lower() == '-h':
        	printHelp()
        	return

       	if argv[0] == '-ip':
                host=argv[1]
	else:
		if argv[0].lower() == '-a' and '/' not in argv[1]:
        		print '\n>>> Error: Credentials required (username/password)\n'
			printHelp()
        		return

		else:
                        creds = argv[1].split('/')
                        username = creds[0]
                        password = creds[1] # not used

               	try:
                       	cmdIp=argv[2]
                       	host=argv[3]
               	except IndexError:
                       	host=DEFAULT_HOST
	print "Going to use user defined values"
    Service.server(host)

    webHandler=WebHandler(host=host, port=DEFAULT_PORT)
    response, headers = webHandler.rest(url=VSP_LIST_PATH, method='GET', data=None, attuid=username)
    jResult = json.loads(response)
    jSrvices = jResult["results"]
    reportFileName = 'upgradereport.csv'  #datetime.now()
    reportFile = open(reportFileName, 'w')
    reportFile.write(Service.header())

    for jService in jSrvices:
        serviceName = jService["name"]
        vendorName = jService["vendorName"]
        vspId = jService["id"]
        status = jService["status"]
        if status != "Locked":
            lockingUser = "None"
        else:
            lockingUser = jService["lockingUser"]

        service = Service(serviceName=serviceName, vspId=vspId, vendorName=vendorName, lockingUser=lockingUser )
        print service
	# Will try to GET the service
        res = service.Get()
        if res == 500:
           serviceMigration(service, status, username)
        else:
           print "Service {0} was tested and does not need a migration".format(serviceName)

        reportFile.write(service.line())
    reportFile.close()


def serviceMigration(service, serviceStatus, username):
        print "Service {0} was tested and it needs a migration".format(service.serviceName)
        print "Service {0} - Migration start"
        if serviceStatus == "Locked":
           print "Service {0} is locked - forcing checkin".format(service.serviceName)
           service.Checkin()
        print "Doing new checkout"
        service.Checkout(username)

        zipName = service.DownloadHeat()
        if not zipName:
            print "no heat found"
            service.uploadStatus = "no heat found"
        else:
            uploadResponse = service.UploadHeat(zipName)
            uploadResults = json.loads(uploadResponse)
            if uploadResults['status'] == 'Success' and uploadResults['errors'].__len__() == 0:
                service.uploadStatus = "Heat uploaded successfully"
            else:
                service.uploadStatus = "Heat uploaded with errors"
        print "Doing new checkin"
        service.Checkin()

        print "Service {0} - Migration end"


def  printHelp():
    print("Upgrade script Help:")
    print("==================================")
    print("1607_to_1610 -h                            --> get help")
    print("1607_to_1610 -a <username>/<password> [-ip {ip}]")
    print("Example: 1607_to_1610 -a root/secret")

class Service(object):
    def __init__(self, serviceName, vspId ,vendorName, lockingUser):
        self.serviceName = serviceName
        self.vspId = vspId
        self.vendorName = vendorName
        self.lockingUser = lockingUser
        self.webHandler = WebHandler(host=Service.serveraddress, port=DEFAULT_PORT) # Schema?
        self.uploadStatus = "not started"

    def __repr__(self):
        return 'Name: {0}, Id: {1}, Vendor: {2}, locked by: {3}, status {4}'.format(self.serviceName, self.vspId ,self.vendorName, self.lockingUser, self.uploadStatus)
    @classmethod
    def header(cls):
        return 'Name,Id,Vendor,locked-by,status\n'

    @classmethod
    def server(cls, address):
        cls.serveraddress=address

    def line(self):
        return '{0},{1},{2},{3},{4}\n'.format(self.serviceName, self.vspId ,self.vendorName, self.lockingUser, self.uploadStatus)

    def Checkout(self, attuid):
        # /v1.0/vendor-software-products/{vspId}/actions
        urlpath=VSP_ACTIONS_PATH.format(vspId=self.vspId)
        response, headers = self.webHandler.rest( url=urlpath, method='PUT', data={"action": "Checkout"}, attuid=attuid)
        self.lockingUser=attuid #we will later use this user to checkin
        return response

    def Checkin(self):
        # /v1.0/vendor-software-products/{vspId}/actions
        urlpath = VSP_ACTIONS_PATH.format(vspId=self.vspId)
        response, headers = self.webHandler.rest(url=urlpath, method='PUT', data={"action": "Checkin"}, attuid=self.lockingUser)
        return response

    def Get(self):
        # /v1.0/vendor-software-products/{vspId}
        urlpath = VSP_GET_URL.format(vspId=self.vspId)
        try:
            response, headers = self.webHandler.rest(url=urlpath, method='GET', data=None, attuid=self.lockingUser)
        except HttpError as e:
            print e.message
            response = e.status
        return response

    def UploadHeat(self, zipName):
            #/v1.0/vendor-software-products/{vspId}/upload
            urlpath = VSP_UPLOAD_PATH.format(vspId=self.vspId)
            try:
                fields = []
                with open(zipName, 'rb') as fin:
                    buffer = fin.read()
                    fin.close()
                files = [('upload', 'heatfile.zip', buffer)]
                response = self.webHandler.post_multipart('HTTP', urlpath, fields, files, self.lockingUser)

                return response
            finally:
                print "done upload"

    def DownloadHeat(self):
        urlpath=VSP_DOWNLOAD_PATH.format(vspId=self.vspId)
        try:
            response, headers = self.webHandler.rest(url=urlpath, method='Get', data=None, attuid=self.lockingUser, accept='application/octet-stream')
        except HttpError as e:
            if e.status == 404:
                return ""

        for (key, value) in headers:
            if key.lower() == "content-disposition":
                file_name = value[value.index('=')+1:]
                break
        heatsDir= os.path.join(os.path.dirname(__file__), 'heats')
        if not os.path.exists(heatsDir):
            os.makedirs(heatsDir)
        file_name = os.path.join(heatsDir, file_name)
        with open(file_name, "wb") as fout:
            fout.write(response)
            fout.close()

        return file_name



class WebHandler(object):
    def __init__(self, host, port):
        self.host = host
        self.port = port

    def rest(self, url, method, data, attuid, accept='application/json', content_type='application/json'):
        connection = httplib.HTTPConnection(host=self.host, port=self.port)

        try:
            headers = {'Content-Type':content_type ,'Accept':accept}
            headers['USER_ID'] = attuid

            connection.request(method=method, headers=headers, body=json.dumps(data), url=url)
            response = connection.getresponse()
            if response.status not in range(200, 300):
                raise HttpError(status= response.status, message=response.reason)

            return response.read(), response.getheaders()
        finally:
            connection.close()

    def post_multipart(self, scheme, selector, fields, files, attuid):
        """
        Post fields and files to an http host as multipart/form-data.
        fields is a sequence of (name, value) elements for regular form fields.
        files is a sequence of (name, filename, value) elements for data to be uploaded as files
        Return the server's response page.
        """
        content_type, body = self.encode_multipart_form_data(fields, files)
        if scheme and scheme.lower() == "http":
            h = httplib.HTTP(self.host, self.port)
        else:
            h = httplib.HTTPS(self.host, self.port)
        h.putrequest('POST', selector)
        h.putheader('content-type', content_type)
        h.putheader('content-length', str(len(body)))
        h.putheader('Accept', 'application/json')
        h.putheader('USER_ID', attuid)

        h.endheaders()
        h.send(body)
        errcode, errmsg, headers = h.getreply()
        print errcode, errmsg, headers
        return h.file.read()

    def encode_multipart_form_data(self, fields, files):
        LIMIT = '----------lImIt_of_THE_fIle_eW_$'
        CRLF = '\r\n'
        L = []
        for (key, value) in fields:
            L.append('--' + LIMIT)
            L.append('Content-Disposition: form-data; name="%s"' % key)
            L.append('')
            L.append(value)
        for (key, filename, value) in files:
            L.append('--' + LIMIT)
            L.append('Content-Disposition: form-data; name="%s"; filename="%s"' % (key, filename))
            L.append('Content-Type: %s' % self.get_content_type(filename))
            L.append('')
            L.append(value)
        L.append('--' + LIMIT + '--')
        L.append('')
        body = CRLF.join(L)
        content_type = 'multipart/form-data; boundary=%s' % LIMIT
        return content_type, body

    def get_content_type(self, filename):
        return mimetypes.guess_type(filename)[0] or 'application/octet-stream'

class HttpError(Exception):
    def __init__(self, status, message):
        self.status = status
        self.message=message
    def __str__(self):
        return repr(self.value, self.message)

if __name__ == "__main__":
    main(sys.argv[1:])


