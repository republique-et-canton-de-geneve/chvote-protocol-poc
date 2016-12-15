# Prototype of an e-Voting protocol

## Project context

This prototype is being developed as a Proof of Concept for the protocol the eVoting group
of the BFH (Berner Fachhochschule) is working on, in partnership with Canton Geneva.

The detailed specifications of the prototype will be made public along with this code.

## Overview

### Components

This prototype consists of:

- a _backend_ (this repository), containing both
    - the full implementation of the protocol's algorithms
    - a simulation program, for an estimation of the performance of the protocol, and as an early
    verification proof of concept for the protocol
    - the required REST services to operate the frontend
- a _frontend_ (currenty not published), with the following features
    - system initialization (creation of a set of authorities and their respective keys)
    - election administration (creation of an election operation)
    - a simulation of a printing authority (for the display of the code sheet to the voter)
    - electronic voting interface (used by the voters)
    - vote casting finalization, including decryption and tallying

### Project progress

The table below contains information on the progress of the project for the _backend_.

| Step                                    | Status               |
| --------------------------------------- | -------------------- |
| **_Implementation of the protocol_**    | **_done_**        |
| - Initialization                        | done                 |
| - Key generation                        | done                 |
| - Electoral data                        | done                 |
| - Code sheet printing                   | done                 |
| - Vote casting                          | done                 |
| - Mixing                                | done                 |
| - Decryption                            | done                 |
| - Tallying                              | done                 |
| **_Simulation program_**                | **_in progress_**    |
| - Initialization                        | done                 |
| - Key generation                        | done                 |
| - Electoral data                        | done                 |
| - Code sheet printing                   | done                 |
| - Vote casting                          | done                 |
| - Mixing                                | todo                 |
| - Decryption                            | todo                 |
| - Tallying                              | todo                 |
| **_Rest services_**                     | **_todo_**           |
| - Scaffolding                           | done                 |
| - Initialization                        | todo                 |
| - Election administration               | todo                 |
| - Code sheet display                    | todo                 |
| - Vote casting                          | todo                 |
| - Decryption                            | todo                 |
| - Tallying                              | todo                 |

### Sequence diagrams

This section introduces the sequence diagrams of the simulation process and will need to be adjusted for
real context usage.

#### Initialization

![Initialization sequence diagram](src/main/java/ch/ge/ve/protopoc/service/simulation/Initialization%20-%20sequence%20diagram.png)

#### Code sheet printing

![Code sheet printing sequence diagram](src/main/java/ch/ge/ve/protopoc/service/simulation/Print%20Code%20Sheets%20-%20sequence%20diagram.png)

#### Vote casting phase

![Vote casting sequence diagram](src/main/java/ch/ge/ve/protopoc/service/simulation/Voting%20Phase%20-%20sequence%20diagram.png)

#### Mixing

![Mixing sequence diagram](src/main/java/ch/ge/ve/protopoc/service/simulation/Mixing%20-%20sequence%20diagram.png)

#### Decryption

![Decryption sequence diagram](src/main/java/ch/ge/ve/protopoc/service/simulation/Decryption%20-%20sequence%20diagram.png)

#### Tallying

![Tallying sequence diagram](src/main/java/ch/ge/ve/protopoc/service/simulation/Tallying%20-%20sequence%20diagram.png)

### Code structure

#### Protocol

All the elements required by the protocol can be found in the [service](src/main/java/ch/ge/ve/protopoc/service) 
package.
- The [algorithm](src/main/java/ch/ge/ve/protopoc/service/algorithm) package covers the algorithms defined in section 5 
of the specification
- The [support](src/main/java/ch/ge/ve/protopoc/service/support) package covers the prerequisites discussed in section 2
- The [service/model](src/main/java/ch/ge/ve/protopoc/service/model) package holds the model classes used to represent 
the tuples and values returned by the algorithms

#### Simulation

The simulation can be run by executing the 
[Simulation](src/main/java/ch/ge/ve/protopoc/service/simulation/Simulation.java) class.