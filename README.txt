================================================================================
=                      OpenIDM Development Snapshot                            =
================================================================================

OpenIDM is an open standards based Identity Management, Provisioning and
Compliance solution. OpenIDM is a Java application running as a set of
components based on web services integrated together by a service bus (ESB).
OpenIDM is using OpenESB as a service bus implementation and GlassFish as an
application server (for now). Although OpenIDM is a new product, it is based on
older stable software components. OpenIDM takes advantage of the Identity
Connector Framework.

The funcionality of OpenIDM is quite limited now. The features include:
* Most of the architectural structure is in place
* Basic authentication and authorization
* User administration
* Web-based administration GUI based on JSF2 and IceFaces
* Basic, manual account administration
* Smart, dynamic account detail GUI forms
* Basic provisioning
* Synchronous BPEL business processes
* Identity Connector Framework integration
* SLF4J logging

Please check the road map to find out more about what you can expect from
OpenIDM today and what is planned for the future
(https://wikis.forgerock.org/confluence/display/openidm/OpenIDM+Roadmap)

OpenIDM is distributed under the terms of the CDDL license. See "legal"
directory in the source tree for full text of the license.

All relative paths in this document are assumed to be relative to the svn
trunk directory (https://svn.forgerock.org/openidm/trunk).

Pre-requisites
==============

Java SE Development Kit 6
-------------------------

The development environment requires JDK 6.
Recommended is JDK 6 update 11 or above. Update 4 contains the JAX-WS 2.1 but 
there are some issues, so we suggest something after update 11 or the latest
which you can download at http://java.sun.com/javase/downloads/widget/jdk6.jsp

JAVA_HOME environment variable should point to the JDK 6 installation path.

Subversion
----------

The OpenIDM source is available from a Subversion Server (SVN).
If you need an SVN client, it is available
at http://www.open.collab.net/downloads/subversion.html
For windows users a nice graphical client is available at http://tortoisesvn.tigris.org

Note: open.collab.net svn client provides command line svn client that is required by 
maven build. However Tortoisesvn does not support standard command line svn client interface.

Maven
-----

Download and install Maven2:
http://maven.apache.org/download.html
Alternatively the maven plugin of Netbeans can be used.

M2_HOME environment variable should point to the Maven installation path.
This is needed to set especially if Netbeans are used.

If using Maven on MacOS X, setting the M2_HOME variable pointing to the 
installation path allows Netbeans to detect the Maven environment.

Add the following line to /etc/launchd.conf

setenv M2_HOME <path_to_maven>

e.g.

setenv M2_HOME /usr/local/apache-maven-2.2.1

The path is usually /usr/share/maven2 on Linux if you are using maven2
from the stock package.


Get the OpenIDM software
------------------------

Get the latest source from our SVN repository using the URL:
https://svn.forgerock.org/openidm/trunk

The source tree has the following directory layout:
  legal: licenses and license templates
  maven: maven-based build tree for most components
  OpenIDM-assembly: service assembly project for Netbeans
  openidm-integration-tests: integration test scripts
  platform: libraries needed for the build
  samples: sample files, sample data imports, etc.

Browsing the source code tree is also available here: http://sources.forgerock.org/changelog/openidm/trunk/OpenIDM


Development Tools and Environment
=================================

OpenIDM needs an application server and OpenESB enterprise bus implementation. 
Maven can be used to build most of the  project outside the development
environment. However, the Netbeans IDE is required to finish the build (create the
service assembly). An IDE is also recommended for development and customization of
OpenIDM. Currently we support the NetBeans IDE and Glassfish, but others will
follow. Feel free to use environments of your choice and let us know about
your experience.

Install Netbeans, GlassFish and OpenESB
---------------------------------------

It is easiest to download all three components together from
https://open-esb.dev.java.net/Downloads.html Choose the "Full install" which
includes GlassFish 2.1.1 and NetBeans 6.7.1. If you already have GlassFish 2.1.1
and NetBeans 6.7.1 then you can chose the "Components only" path.

The Glassfish installation needs to be patched. The patch includes newest
version of JAXB implementation (v2.2.3). Patch the glassfish by copying the
jaxb-api.jar and jaxb-impl.jar files from
https://svn.forgerock.org/openidm/trunk/platform/patch to
<GF_HOME>/lib/endorsed directory, which is usually
/opt/GlassFishESBv22/glassfish/lib/endorsed on Unix machines. Restart Glassfish.

N.B As Oracle is currently migrating all java.net projects to their new platform, GlassFish ESB
is unavailable from the normal download site. In the meantime please refer to:

http://dlc.sun.com/jbi/binaries/glassfishesb/v2.2/promoted/rc3-1/

JVM settings for Glassfish:
Set parameter -XX:MaxPermSize=256m

Parameters could be changed in domain configuration file located in ${GL_HOME}/domains/domain1/config/domain.xml
in section <jvm-options>.

Install the Database
--------------------

Currently OpenIDM supports only MySQL database. More databases will be
supported in later releases. MySQL can by downloaded from
http://www.mysql.com/downloads/mysql/5.1.html
Optionally you can download also MySQL workbench, a GUI interfaces for databases
from http://dev.mysql.com/downloads/workbench/5.2.html

Execute the script (e.g. from Workbench) create_openidm_tables_mysql.sql which is located
in maven/OpenIDM-database/src/main/resources/scripts. You can use the mysql
command-line tool like:

mysql -u root < create_openidm_tables_mysql.sql

This will create the OpenIDM database schema, the OPENIDM_PROXY user and all necessary
objects. Use -f switch in case you get drop errors. Use -v switch to watch
the progress.

OpenIDM connects to the database using a Java Datasource. The datasource
should be defined in the application server. The project contains a default 
datasource definition that gets compiled into the project and is applied to
GlassFish during deployment. If you have non-default database setup either
update the resource file before building the project or adjust the
datasource using the GlassFish administration tools after deployment. The
resource file is located at
maven/OpenIDM-repository/src/main/resources/META-INF/sun-resources.xml

You can also create the resources manually using the GlassFish command-line
administration tool asadmin:
<GF_HOME>/bin/asadmin add-resources maven/OpenIDM-repository/src/main/resources/META-INF/sun-resources.xml

Additionally GlassFish needs a MySQL java connector: http://mvnrepository.com/artifact/mysql/mysql-connector-java/5.1.13
The jar file must be available in the classpath of GlassFish.

E.g: $GLASSFISH/domains/domain1/lib/mysql-connector-java-5.1.13.jar

Build The Components
--------------------

Most of the project is built using maven, except for the final step of
service assembly creation. OpenIDM components may be built either using
command-line of IDE. The service assembly must be created in IDE for now.

COMMAND-LINE BUILD

Switch to the "maven" directory and execute maven:

mvn install

Maven will download all the dependencies and build the components. The
components will be installed to a maven local repository.

IDE BUILD

Open the "maven" project located in trunk in the IDE. It will open as
"OpenIDM parent pom" in Netbeans. Build the project by right clicking on it
and selecting Build. It will build also all the sub-projects.

Build Service Assembly
----------------------

Service assembly is a "composite application" that contains components
compiled in the previous step. It can be only done in the Netbeans IDE for now.

Open the "openidm-assembly" project in the OpenIDM-assembly directory in
Netbeans IDE (File->Open Project). Build the project by right-clicking on
the project and selecting "Build". It will internally execute maven, therefore
make sure you have the environment variable M2_HOME set correctly.

Deploy OpenIDM Service Assembly
-------------------------------

In NetBeans click right button on project openidm-assembly and choose Deploy.
It will try to deploy the project to the application server instance
selected for the project, which is most likely the GlassFish instance with
OpenESB.

If Server Instance not found warning is shown then click ok and choose available
server instance from the list.

Testing OpenIDM
===============

Administration Interface
------------------------

OpenIDM administration user interface will be available most likely on the
following URL:

http://localhost:8080/idm/

The data repository will be empty after the deployment. You can create users
but unless you define one or more resources the system will not be very
useful. See following example to define to define OpenDJ resource and modify
it to define more resources. The GUI for convenient resource definition is
planned for later OpenIDM versions.

OpenDJ Resource (optional)
--------------------------

We recommend to use OpenDJ directory server as a testing resource for
OpenIDM. OpenDJ can be downloaded at
http://forgerock.com/opendj.html

Get the zip file, unzip it at any convenient location, run the "setup"
utility and configure following recommended parameters:
LDAP Listener Port: 1389
Administration Connector Port: 4444
LDAP Secure Access: disabled
Root User DN: cn=Directory Manager
Password: secret
Stand-alone server
Directory Base DN: dc=example,dc=com
Import data from LDIF file: openidm/trunk/samples/example-base-only.ldif
leave all other values to default

Make sure that the OpenDJ instance is started. If it is not, use the
start-ds sript in the OpenDJ bin directory to start it.

Log in to the OpenIDM administration console using the following URL:

http://localhost:8080/idm/

Navigate to Configuration->Import Page. Copy&paste the content of
samples/opendj-localhost-resource-simple.xml file into the text area. Press the
"Add" button. You should see green message "Added objects: Localhost OpenDJ".

Now you can create users and them account on OpenDJ. Enjoy.

Short OpenIDM Walkthrough
-------------------------

This walkthrogh assumes that all the instalation steps were executed,
including the optional OpenDJ installation and configuration.

Log in to the OpenIDM administration console using the following URL:

http://localhost:8080/idm/

Username: administrator
Password: secret

A home page of the OpenIDM console should be displayed. This is
a pretty dynamic web application using AJAX for better user interaction.
The look&feel is quite minimalistic now, we are working on an
improvement just now.

Select User->Create User from the menu. Fill out the details for
a user. Especially make sure to fill out "Name", this will be
the user's login name. This will create a user in OpenIDM repository.
This is a "master" user record for the provisioning system. No 
resources will be modified yet.

After you submit the from you should see the user in a table. Feel
free to crete more users. You can return to this table anytime by
selecting User->List Users from the menu.

Select a specific user by clicking on the table row. The color of
the row should change. Now you can click on the buttons above.

Click on "User Details" button now. The page that describes the
user details and lists user accounts should be displayed. You would
not be able to see any user accounts there, as the user does not
have any.

Click on "New Account" button to create a new account. A list of
resources should be displayed, including "Localhost OpenDJ". Click on
"Localhost OpenDJ" and then click on "Add" button. A form describing the
details of a new account should be displyed with some values already
filled-in. This form is dynamically generated from. The fields are based on
the XSD resource schema defined in the "Localhost OpenDJ" XML object.
If you want to adapt the fields displayed here, you just need to
change the XSD schema in the "Localhost OpenDJ" XML object (e.g.
by using debug pages). This form is dynamically generated from the
resource schema (see "Adapting OpenIDM" section below).

Fill in the remaining values into the form fields. Make sure that
all the mandatory values (marked by asterisk) are filled in.
Click on "Submit" to create an account. The account now should be listed in
the user details page.


OpenIDM Customization
---------------------

OpenIDM is built from the very beginnig to allow extensive (almost
extreme) customization. In fact, OpenIDM is mostly just a set of
reusable components that are designed to work together well. OpenIDM
is based on ESB/JBI principles to allow such a great degree of
customization.

The OpenIDM administration/user interface contains only the very basic
functionality now. Most of the customization needs to be done by
modifying the XML files in the repository. The files may be modified
using a debug pages (selecting Configuration->Debug pages from the
menu). The well-documented schemas for the XML files are located in
the source tree:

trunk/maven/OpenIDM-xml/OpenIDM-schemas/src/main/resources/META-INF/wsdl/xml/ns/public/

Most serious customizations can be done by modifying the service assembly
(composite application) in netbeans IDE. Custom components can be inserted,
stock components can be replaced, etc. Maybe the most usefull modification
would be to insert a BPEL process between GUI (webapp) and IDM Model
implementation. This can support syncronous business processes. Netbeans
IDE contains editor for composite applications (CASA), BPEL Editor and
BPEL debugger.

OpenIDM is integrated with Identity Connector Framework (ICF), therefore
it should support provisioning to any system that has an ICF connector.
However, there is no administration interface for that yet. Such connectors
can be configured using XML files similarly to the "Localhost OpenDJ"
connector used as an example above.

The "new account" form is dynamically generated from. The fields are 
based on the XSD resource schema defined in the "Localhost OpenDJ"
XML object. If you want to adapt the fields displayed here, you just
need to change the XSD schema in the "Localhost OpenDJ" XML object (e.g.
by using debug pages).


Notes
=====

The opendj-resource-simple.xml file in the samples directory is a basic,
readable and understandable definition of an LDAP resource. The
opendj-resource-full.xml is a full-featured definition, but it is quite
complex and may be difficult to understand.

WARNING! Repository unit tests use the same database as is configured for
deployed OpenIDM instance. Running the unit tests will delete the content
of OpenIDM database.
