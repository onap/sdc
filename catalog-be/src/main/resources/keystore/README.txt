keytool -genkeypair -keystore catalogbe.jks -alias catalogbe -keypass Aa123456 -storepass Aa123456  -keyalg RSA -keysize 2048  -validity 3650 -dname "CN=Catalog BE, OU=Development, O=AT&T, L=TLV, C=IL"


3650 – 10 years validity
Eyal Sofer – creator 
Development – Organization unit
AT&T – Organization
TLV- City
IL – Country code


catalogbe.jks – name of keystore
Aa123456 - password

#In order to generate the password OBF:..., run the following command:
java -cp ../jetty-distribution-9.2.7.v20150116/lib/jetty-http-9.2.7.v20150116.jar:../jetty-distribution-9.2.7.v20150116/lib/jetty-util-9.2.7.v20150116.jar org.eclipse.jetty.util.security.Password Aa123456