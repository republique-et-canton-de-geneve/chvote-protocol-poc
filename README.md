# Prototype of an e-Voting protocol

# Table of contents

- [Overview](#overview)
    - [Context](#context)
    - [Components](#components)
    - [Concept](#concept)
- [Project status](#project-status)
    - [Implementation status](#implementation-status)
    - [Performance measurements](#performance-measurements)
- [Compiling and running the simulation](#compiling-and-running-the-simulation)
    - [Prerequisites](#prerequisites)
    - [Compiling](#compiling)
    - [Running](#running)
- [Contributing](#contributing)
    - [Pull request policies](#pull-request-policies)
	- [Security](#security)
- [License](#license)
- [Future](#future)

# Overview

## Project context

This prototype is being developed as a Proof of Concept for the protocol the eVoting group
of the BFH (Berner Fachhochschule) is working on, in partnership with Canton Geneva.

The detailed specifications of the prototype are published along with this code.
They are available on the [Cryptology ePrint Archive](https://ia.cr/2017/325).

## Components

This prototype consists of:

- a _backend_ (this repository), containing both
    - the full implementation of the protocol's algorithms
    - a simulation program, for an estimation of the performance of the protocol, and as an early
    verification proof of concept for the protocol
    - the required REST services to operate the frontend
- a _frontend_ (currently not published), with the following features
    - system initialization (creation of a set of authorities and their respective keys)
    - election administration (creation of an election operation)
    - a simulation of a printing authority (for the display of the code sheet to the voter)
    - electronic voting interface (used by the voters)
    - vote casting finalization, including decryption and tallying

## Concept

See [Concept](doc/Concept.md).

# Project status

## Implementation status

See [Implementation](doc/Implementation.md)

## Performance measurements

Some performance measurements can be found on [Performance measurements](doc/Performance%20measurements.md).

# Compiling and running

## Preconditions
The following software needs to be installed to build and run the application:
- Java (JDK 8 or more recent)
- Gradle (optional, a wrapper is provided)

## Compiling

- With gradle installed:
    - `gradle build`

- Without gradle (Linux)
    - `./gradlew build`

- Without gradle (Windows)
    - `.\gradlew build`

## Running

The following instructions are to run the Simulation program, rather than the backend server, since the backend 
currently offers no interface for the core functionality.

The simulation program instantiates an election configuration, initializes the components and runs the whole protocol,
from generation of secrets to the tallying of the results, using simulated voters.

- With gradle installed:
    - `gradle simulation`

- Without gradle (Linux)
    - `./gradlew simulation`

- Without gradle (Windows)
    - `.\gradlew simulation`

The simulation takes its parameters from environment variables. The possible parameters are:
- `secLevel`
    - Allowed values: 0, 1 or 2. Those match the security levels described in chapter 8 of the specification
    - default: 1
- `electionType`
    - `SINGLE_VOTE`: simple 1-out-of-3 election corresponding to a referendum (Yes-No-Blank)
    - `SIMPLE_SAMPLE`: two simple 1-out-of-3 elections, one 2-out-of-10 election, the latter one being available to 10%
     of the voters
    - `GC_CE`: one 7-out-of-36 election and one 100-out-of-576 (**WARNING**: _not compatible with secLevel 1_)
    - default: SIMPLE_SAMPLE
- `votersCount`
    - The number of voters.
    - default: 100
    
For instance, to run a simulation on GC_CE with 100'000 voters (_not recommended unless you have quite some time to 
kill_), run the following command (or adapt it as explained above if you do not have gradle installed):

    gradle simulation -DelectionType=GC_CE -DvotersCount=100000

# Contributing
CHVote is opensourced with the main purpose of transparency to build confidence in the system.
 
Contributions are also welcomed, either using pull requests or by submitting issues in github. The CHVote community
manager will take care of those inputs, lead exchanges around them, and actions could take place according to their 
relevance, their criticality, and the CHVote development roadmap.

For this specific repository, since the code released will not be used _as-is_ in production, we particularly welcome
disscussion of the security of the protocol, so that the currently undergoing project can benefit from input at the
earliest possible stage.

## Pull request policies
We welcome pull requests on any branch of this project.

## Security
The code presented is not used in production, merely a Proof of Concept developed before the start of the main project.
Therefore, security issues related to the code itself are welcome on pull requests or issues.

However, should you find issue with an impact on the protocol, we would rather take your comments on 
security-chvote@etat.ge.ch, so that we can analyze impact before taking the discussion to the public.
We would appreciate getting two weeks notice to discuss the issue in-house and with our partners before taking the issue
to the public.

# License
CHVote components are released under [AGPL 3.0](https://www.gnu.org/licenses/agpl.txt).

# Future
The second generation of CHVote is under active development. It aims to provide end-to-end encryption with individual
and universal verifiabilities. Its source code will be published under AGPL 3.0 as well.