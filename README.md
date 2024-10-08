# Jenkins Status Overview Plugin

[![ci](https://github.com/jhnc-oss/jenkins-status-overview/actions/workflows/ci.yml/badge.svg)](https://github.com/jhnc-oss/jenkins-status-overview/actions/workflows/ci.yml)
[![GitHub release](https://img.shields.io/github/release/jhnc-oss/jenkins-status-overview.svg)](https://github.com/jhnc-oss/jenkins-status-overview/releases)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/java-17-green.svg)

REST API providing basic status overview data.

#### Available endpoints

- **`status-overview/master`:** Master status information
- **`status-overview/agents`:** Agent status information (master node not included)
- **`status-overview/plugins`:** Plugins status information

#### Permissions

Accessing the API requires `Status Overview` Permission (implied by `Administer`).
