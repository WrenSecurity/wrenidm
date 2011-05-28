SETTING UP YOUR CA
-----------------------------------

Step 1.  Go to www.openssl.org and download the source code.  Even Windows
users need to build it, so you'll need access to a C compiler.  You may be
able to get hold of prebuilt binaries on the web and you can certainly get
hold of the GNU C compiler or you can use Borland and Microsoft compilers.
There are good build instructions included with the source distribution, so
I won't go into build details.

Step 2.  Set the SSLHOME environment variable and point to this ssl directory.
set SSLHOME={ROOT_FOLDER}/trunk/maven/OpenIDM-cryptography/ssl

Step 3.  Create a private key and certificate request for your own CA:
openssl req -config openssl.cnf -new -newkey rsa:1024 -pubkey -nodes -out private/ca.csr -keyout private/ca.key

Organizational Unit Name (eg, section) [OpenIDM Service Unit]:OpenIDM Demo Certification Authority
Common Name (eg, YOUR name) [openidm.forgerock.org]:OpenIDM Demo Top Certificate

Step 4.  Create your private key and CA's self-signed certificate (note lasts one year - increase the days setting to whatever you want):
openssl x509 -trustout -signkey private/ca.key -days 3653 -req -in private/ca.csr -out ca.pem

WINDOWS USERS:If you copy the ca.pem file to ca.crt and edit the file so
that the strings "TRUSTED CERTIFICATE" read "CERTIFICATE", you can import
your CA certificate into your trusted root certificates store.

Step 5.  Import the CA certificate into the JDK certificate authorities
keystore:
keytool -import -keystore $JAVA_JOME/jre/lib/security/cacerts -file ca.pem -alias OpenIDM-demo-ca

Windows users need to replace $JAVA_HOME with %JAVA_HOME%.

Create a custom JCEKS keystore and import the CA certificate
keytool -import -trustcacerts -alias "OpenIDM-demo-ca" -file ca.pem -keystore OpenIDM-keystore.jks -storepass "secret" -storetype JCEKS

Check the new keystore:
keytool -list -v -keystore OpenIDM-keystore.jks -storepass "secret" -storetype JCEKS


Step 6.  Create a file to hold your CA's serial numbers.  This file starts
with the number "2":
echo "02" > ca.srl


SETTING UP YOUR SERVICE UNITS
----------------------------------------------------

Step 7.  Create a keystore for your JBI server. (Can be the same or have another JKS type?)
keytool -genkey -alias OpenIDM-model -keyalg RSA -keysize 1024 -validity 360 -keystore OpenIDM-keystore.jks -storepass "secret" -storetype JCEKS

Step 8.  Create a certificate request for your web server.
keytool -certreq -keyalg RSA -alias OpenIDM-model -file OpenIDM-model.csr -keystore OpenIDM-keystore.jks -storepass "secret" -storetype JCEKS

You need to edit the certificate request file slightly.  Open it up in a
text editor and amend the text which reads "NEW CERTIFICATE REQUEST" to
"CERTIFICATE REQUEST"

Step 9.  Have your CA sign your certificate request:
openssl x509 -req -CA ca.pem -CAkey private/ca.key -CAserial ca.srl -in OpenIDM-model.csr -out certs/OpenIDM-model.crt -days 365
openssl x509 -req -CA ca.pem -CAkey private/ca.key -CAcreateserial 	-in OpenIDM-model.csr -out certs/OpenIDM-model.crt -days 365


Step 10.  Import your signed server certificate into your server keystore:
keytool -import -alias OpenIDM-model -trustcacerts -file certs/OpenIDM-model.crt -keystore OpenIDM-keystore.jks -storepass "secret" -storetype JCEKS
You should see a message "Certificate reply was installed in keystore".

Step 11.  Import your CA certificate into your server keystore:
keytool -import -trustcacerts -alias "OpenIDM-demo-ca" -file ca.pem -keystore OpenIDM-server.jks -storepass "secret"

Step 12. Set up an SSL connector for GlassFish.
find out, how to do this!! 

SETTING UP AN SSL CLIENT
-------------------------------------------

Step 13.  Create a client certificate request:
openssl req -config openssl.cnf -new -newkey rsa:512  -pubkey -nodes -out client/OpenIDM-client-AD.csr -keyout client/OpenIDM-client-AD.key

The common name of the client must match a ?????

Step 14.  Have your CA sign your client certificate.

openssl x509 -req -CA ca.pem -CAkey private/ca.key -CAserial ca.srl -in client/OpenIDM-client-AD.csr -out client/OpenIDM-client-AD.pem -days 365

Step 15.  Generate a PKCS12 file containing your server key and server certificate.

openssl pkcs12 -export -clcerts -in client/OpenIDM-client-AD.pem -inkey client/OpenIDM-client-AD.key -out client/OpenIDM-client-AD.p12 -name "OpenIDM-client-AD_certificate"

Step 16.  Import the PKCS12 file into your web browser to use as your client certificate and key.

Repeat steps 13-16 as often as required.

