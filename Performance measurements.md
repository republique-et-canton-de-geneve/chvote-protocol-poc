## Performance measurements

### Introduction

This page will contain performance measurements, including all relevant context
information.
Newest measurements will always appear at the top of the page.

All measurements should include the current date and the hash of the HEAD commit
at the moment they were taken.

Unless otherwise specified, measurements were taken on an HP EliteBook, with an
 quad-core Intel i5 CPU @2.4GHz and 8GB of RAM, on Windows.

### Measurements

#### With more paralellism

The measurements in this section were taken on an HP z440, with an octo-core 
Intel Xeon @3.5GHz abd 16GB of RAM, on Windows

- Date: December 20th, 2016
- Head:  protocol-poc-back/c104f195c76c255bc7c1595a4a838ceec65cd2e1

##### Performance statistics @ 1024-bit

- using LibGMP: true
- length of p: 1024
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |             464 |
|          creating election set |              21 |
|          publishing parameters |               0 |
|                 key generation |               6 |
|            public key building |               5 |
|           publish election set |               0 |
|      generating electoral data |             296 |
|       build public credentials |              10 |
|           printing code sheets |              20 |
|                   voting phase |           3'799 |
|                         mixing |          13'204 |
|                     decryption |           8'839 |
|                       tallying |             820 |
|          total simulation time |          27'521 |

##### Performance statistics @ 2048-bit

- using LibGMP: true
- length of p: 2048
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |           1'244 |
|          creating election set |              12 |
|          publishing parameters |               0 |
|                 key generation |              32 |
|            public key building |               5 |
|           publish election set |               0 |
|      generating electoral data |             591 |
|       build public credentials |              13 |
|           printing code sheets |              44 |
|                   voting phase |          21'315 |
|                         mixing |          55'277 |
|                     decryption |          36'397 |
|                       tallying |           4'679 |
|          total simulation time |         119'675 |

##### Observations

- The increased CPU frequency and number of available cores lead to a further 50% 
reduction in execution time, which bodes well for the scalability of the system
in a more distributed environment.
- The improvement is most efficient for the voting phase and the decryption 
phase.
- The voting phase is less relevant, since the client side computation will be 
distributed anyway, and will have to be implemented in javascript.
- Most of the time spent in the decryption phase serves to verify the proofs of
the shuffles.

#### Using parallel streams

- Date December 19, 2016
- Head: protocol-poc-back/3aa3f7c73710ad5c02821addfe25f16e784c1653

##### Performance statistics @ 1024-bit

- using LibGMP: true
- length of p: 1024
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |           2 334 |
|          creating election set |              24 |
|          publishing parameters |               0 |
|                 key generation |              23 |
|            public key building |              11 |
|           publish election set |               0 |
|      generating electoral data |             747 |
|       build public credentials |              21 |
|           printing code sheets |              58 |
|                   voting phase |          14 418 |
|                         mixing |          17 771 |
|                     decryption |          19 136 |
|                       tallying |           1 144 |
|          total simulation time |          55 741 |

##### Performance statistics @ 2048-bit

- using LibGMP: true
- length of p: 2048
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |           3 854 |
|          creating election set |              25 |
|          publishing parameters |               0 |
|                 key generation |              48 |
|            public key building |              12 |
|           publish election set |               0 |
|      generating electoral data |           1 648 |
|       build public credentials |              41 |
|           printing code sheets |              71 |
|                   voting phase |          68 168 |
|                         mixing |          73 577 |
|                     decryption |          83 899 |
|                       tallying |           6 146 |
|          total simulation time |         237 544 |

#### LibGMP integration

- Date: December 16, 2016
- Head: protocol-poc-back/16da02afd7e5c2d1895669ec2b2af69bd01cc51d

##### Performance statistics @ 1024-bit

- using LibGMP: true
- length of p: 1024
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |           2 291 |
|          creating election set |              23 |
|          publishing parameters |               0 |
|                 key generation |              14 |
|            public key building |               9 |
|           publish election set |               0 |
|      generating electoral data |           1 063 |
|       build public credentials |              21 |
|           printing code sheets |              59 |
|                   voting phase |          23 623 |
|                         mixing |          17 508 |
|                     decryption |          50 218 |
|                       tallying |           1 114 |
|          total simulation time |          95 989 |

##### Performance statistics @ 2048-bit

- using LibGMP: true
- length of p: 2048
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |           4 020 |
|          creating election set |              24 |
|          publishing parameters |               0 |
|                 key generation |              67 |
|            public key building |               6 |
|           publish election set |               0 |
|      generating electoral data |           3 161 |
|       build public credentials |              56 |
|           printing code sheets |              86 |
|                   voting phase |         166 966 |
|                         mixing |          87 194 |
|                     decryption |         216 645 |
|                       tallying |           8 831 |
|          total simulation time |         487 119 |

#### Initial measurements

- Date: December 16, 2016
- Head: protocol-poc-back/108c150bd444d5de7da59888687b1ff8464b64b5

##### Performance statistics @ 1024-bit

- length of p: 1024
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |           2 317 |
|          creating election set |              26 |
|          publishing parameters |               0 |
|                 key generation |              47 |
|            public key building |              15 |
|           publish election set |               0 |
|      generating electoral data |           1 350 |
|       build public credentials |              24 |
|           printing code sheets |              73 |
|                   voting phase |          41 925 |
|                         mixing |          24 351 |
|                     decryption |          66 914 |
|                       tallying |            2204 |
|          total simulation time |         139 294 |

##### Performance statistics @ 2048-bit


- length of p: 2048
- number of voters: 100
- number of candidates per election: 3, 3, 10

|                      Step name | Time taken (ms) |
| ------------------------------ | --------------: |
|     creating public parameters |           3 192 |
|          creating election set |              39 |
|          publishing parameters |               0 |
|                 key generation |             112 |
|            public key building |               5 |
|           publish election set |               0 |
|      generating electoral data |           4 269 |
|       build public credentials |              24 |
|           printing code sheets |              53 |
|                   voting phase |         297 367 |
|                         mixing |         127 765 |
|                     decryption |         311 581 |
|                       tallying |          12 249 |
|          total simulation time |         756 722 |
