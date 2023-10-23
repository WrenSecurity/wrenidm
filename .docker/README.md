<p align="center">
  <img src="https://user-images.githubusercontent.com/13997406/99670197-73a79b80-2a70-11eb-945d-a421a4d3d6a2.png">
</p>

# Wren:IDM

[![Organization Website](https://img.shields.io/badge/organization-Wren_Security-c12233)](https://wrensecurity.org)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/WrenSecurity)
[![License](https://img.shields.io/badge/license-CDDL-blue.svg)](https://github.com/WrenSecurity/wrenidm/blob/main/LICENSE)
[![Source Code](https://img.shields.io/badge/source_code-GitHub-6e40c9)](https://github.com/WrenSecurity/wrenidm)
[![Contributing Guide](https://img.shields.io/badge/contributions-guide-green.svg)](https://github.com/WrenSecurity/wrensec-docs/wiki/Contributor-Guidelines)

Wren:IDM is a community‐developed identity management system with a flexible data model, multiple extension points
and scripting support, including JavaScript and Groovy. It can connect to and manage a wide range of systems through
the Identity Connector Framework (Wren:ICF).

Wren:IDM is one of the projects in the Wren Security Suite, a community initiative that adopted open‐source projects
formerly developed by ForgeRock, which has its own roots in Sun Microsystems’ products.

Wren:IDM itself is focused on identity management processes and it also provides a powerful framework for implementing
IAG and a portion of IAM processes as well. Although the project is based on OpenIDM code, it is not affiliated with
ForgeRock in any way. It is based on the very latest code available under a CDDL license (not‐yet‐released OpenIDM 5.x).

The features of Wren:IDM include:

  * A complete platform for building IDM and IG solutions using the concepts described below – roles, mappings,
  synchronizations, workflows, policies, etc.
  * ICF Connector Servers – services that allow connectors to be run outside of the IDM itself. Useful when a connector
  needs a specific client environment to talk to the integrated system. Also facilitates security. .NET and Java Connector
  Servers are available.
  * Administration GUI – an interface for making changes to data models and configuration using a point‐and‐click
  interface rather than Wren:IDM's REST interface.
  * Self‐service GUI – an interface for end‐users to update their profile information, passwords, and preferences.

Both the Administration GUI and Self‐Service GUI are web‐based, single‐page applications that can be turned off in
deployments that do not desire to use them [[1]](#Bibliogprahy).

# How to use this image

## Create a `Dockerfile` in your project

    FROM wrensecurity/wrenidm:latest

    COPY --chown=wrenidm:root project /opt/wrenidm/project

    EXPOSE 8080

Then, run the commands to build and run the Docker image:

    docker build -t wrenidm-image .
    docker run --rm --name wrenidm-project -p 8080:8080 wrenidm-image -p project

Then you can hit http://localhost:8080/admin in your browser.

## Without a `Dockerfile`

If you don't want to include a `Dockerfile` in your project, you can run Wren:IDM through this command:

    docker run --rm --name wrenidm-test -p 8080:8080 wrensecurity/wrenidm:latest

Then you can hit http://localhost:8080/admin in your browser.

# Acknowledgments

Large portions of the source code are based on the open-source projects
previously released by:
* Sun Microsystems
* ForgeRock

We'd like to thank them for supporting the idea of open-source software.

# Disclaimer

Please note that the acknowledged parties are not affiliated with this project.
Their trade names, product names and trademarks should not be used to refer to
the Wren Security products, as it might be considered an unfair commercial
practice.

Wren Security is open source and always will be.

# Bibliogprahy

[1] SCHWARTZ, Michael, Maciej MACHULAK. Securing the Perimeter: Deploying Identity and Access Management with Free Open Source Software. Apress, 2018. ISBN 978-1-4842-2601-8.

[contribute]: https://github.com/WrenSecurity/wrensec-docs/wiki/Contributor-Guidelines
