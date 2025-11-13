<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://user-images.githubusercontent.com/13997406/203101588-348157bd-edc6-42a9-b8f9-e2333f6ec0e6.png">
    <source media="(prefers-color-scheme: light)" srcset="https://user-images.githubusercontent.com/13997406/203096721-d7617bff-70fe-402a-b04e-fd799273a3fd.png">
    <img alt="Wren:IDM logo" src="https://user-images.githubusercontent.com/13997406/203096721-d7617bff-70fe-402a-b04e-fd799273a3fd.png" width="70%">
  </picture>
</p>

# Wren:IDM

[![License](https://img.shields.io/badge/license-CDDL-blue.svg)](https://github.com/WrenSecurity/wrenidm/blob/main/LICENSE)
[![Gitter](https://img.shields.io/matrix/wrensecurity_lobby%3Agitter.im?server_fqdn=matrix.org)](https://matrix.to/#/#WrenSecurity_Lobby:gitter.im)

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

## Contributions

[![Contributing Guide](https://img.shields.io/badge/Contributions-guide-green.svg?style=flat)][contribute]
[![Contributors](https://img.shields.io/github/contributors/WrenSecurity/wrenidm)][contribute]
[![Pull Requests](https://img.shields.io/github/issues-pr/WrenSecurity/wrenidm)][contribute]
[![Last commit](https://img.shields.io/github/last-commit/WrenSecurity/Wrenidm.svg)](https://github.com/WrenSecurity/Wrenidm/commits/main)

## Getting the Wren:IDM application

You can get Wren:IDM application in couple of ways:

### Download binary release

The easiest way to get the Wren:IDM is to download the latest binary [release](https://github.com/WrenSecurity/wrenidm/releases).

### Build the source code

In order to build the project from the command line follow these steps:

**Prepare your Environment**

Following software is needed to build the project:

| Software  | Required Version |
| --------- | -------------    |
| OpenJDK   | 11 and above     |
| Git       | 2.0 and above    |
| Maven     | 3.0 and above    |

**Build the source code**

All project dependencies are hosted in JFrog repository and managed by Maven, so to build the project simply execute Maven *package* goal.

```
$ cd $GIT_REPOSITORIES/wrenidm
$ mvn clean package
```

Built binary can be found in `${GIT_REPOSITORIES}/wrenidm/openidm-zip/target/wrenidm-${VERSION}.zip`.

### Docker image

You can also run Wren:IDM in a Docker container. Official Wren:IDM Docker images can be found [here](https://hub.docker.com/r/wrensecurity/wrenidm).

## Documentation

Project documentation can be found in our documentation platform ([docs.wrensecurity.org](https://docs.wrensecurity.org/wrenidm/latest/index.html)).
Repository hosting cookbook with common use cases is available on [GitHub](https://github.com/WrenSecurity/wrenidm-cookbook).

## Acknowledgments

Large portions of the source code are based on the open-source projects
previously released by:
* Sun Microsystems
* ForgeRock

We'd like to thank them for supporting the idea of open-source software.

## Disclaimer

Please note that the acknowledged parties are not affiliated with this project.
Their trade names, product names and trademarks should not be used to refer to
the Wren Security products, as it might be considered an unfair commercial
practice.

Wren Security is open source and always will be.

## Bibliogprahy

[1] SCHWARTZ, Michael, Maciej MACHULAK. Securing the Perimeter: Deploying Identity and Access Management with Free Open Source Software. Apress, 2018. ISBN 978-1-4842-2601-8.

[contribute]: https://github.com/WrenSecurity/wrensec-docs/wiki/Contributor-Guidelines
