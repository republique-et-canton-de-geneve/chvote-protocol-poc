# Implementation

## Implementation status

The table below contains information on the progress of the project for the _backend_.

| Step                                    | Status               |
| --------------------------------------- | -------------------- |
| **_Implementation of the protocol_**    | **_done_**           |
| - Initialization                        | done                 |
| - Key generation                        | done                 |
| - Electoral data                        | done                 |
| - Code sheet printing                   | done                 |
| - Vote casting                          | done                 |
| - Mixing                                | done                 |
| - Decryption                            | done                 |
| - Tallying                              | done                 |
| **_Simulation program_**                | **_done_**           |
| - Initialization                        | done                 |
| - Key generation                        | done                 |
| - Electoral data                        | done                 |
| - Code sheet printing                   | done                 |
| - Vote casting                          | done                 |
| - Mixing                                | done                 |
| - Decryption                            | done                 |
| - Tallying                              | done                 |
| - performance measurements              | done                 |
| **_Rest services_**                     | **_todo_**           |
| - Scaffolding                           | done                 |
| - Initialization                        | todo                 |
| - Election administration               | todo                 |
| - Code sheet display                    | todo                 |
| - Vote casting                          | todo                 |
| - Decryption                            | todo                 |
| - Tallying                              | todo                 |

# Code structure

## Protocol

All the elements required by the protocol can be found in the [service](src/main/java/ch/ge/ve/protopoc/service) 
package.
- The [algorithm](../src/main/java/ch/ge/ve/protopoc/service/algorithm) package covers the algorithms defined in section 5 
of the specification
- The [support](../src/main/java/ch/ge/ve/protopoc/service/support) package covers the prerequisites discussed in section 2
- The [service/model](../src/main/java/ch/ge/ve/protopoc/service/model) package holds the model classes used to represent 
the tuples and values returned by the algorithms

## Simulation

The simulation can be run by executing the 
[Simulation](../src/main/java/ch/ge/ve/protopoc/service/simulation/Simulation.java) class.
