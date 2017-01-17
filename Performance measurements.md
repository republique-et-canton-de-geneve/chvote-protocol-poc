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

#### Larger data sets

The reason for the improvement tested below, was related to tests with a larger
number of voters performing much worse than expected. The table below is merged
from two execution runs with the same parameters, for a clearer "before/after"
comparison.

The only change between the two runs is the commit 
[fa56b7ed382040574c339716d6ad6fc6766ed1a9](commit/fa56b7ed382040574c339716d6ad6fc6766ed1a9).

##### Performance comparison

- using LibGMP: true
- length of p: 2048
- number of voters: 20000
- elections: 1-out-of-3

| Step name                      | Before (in ms)  | After (in ms)   |
| ------------------------------ | --------------: | --------------: |
| creating public parameters     |           1'311 |           1'268 |
| creating election set          |              22 |              25 |
| publishing parameters          |               0 |               0 |
| key generation                 |              15 |              14 |
| public key building            |               5 |               4 |
| publish election set           |               0 |               0 |
| generating electoral data      |          43'413 |          43'366 |
| build public credentials       |           1'542 |           1'253 |
| printing code sheets           |           1'230 |           1'109 |
| voting phase                   |       1'623'692 |       1'620'094 |
| mixing                         |      39'159'200 |       2'441'770 |
| decryption                     |     111'789'477 |       2'575'944 |
| tallying                       |         802'362 |         787'555 |
| total simulation time          |     153'422'326 |       7'472'461 |


#### Reuse hashes

Algorithm 7.6, GetChallenges, calls the recursive hashing function multiple 
times with similar values. The first parameter is vector containing the 
encrypted ballots, the result of the current shuffle and a list of commitments,
the second parameter is an incrementing index.
Due to the recursive nature of the hash function definition, the process can be
improved by computing the hash for the first parameter only once.

The getChallenges algorithm gets a linear complexity instead of being quadratic
in the number of votes.

- Date: January 16th
- Head: protocol-poc-back/d00ce69

##### Performance statistics @2048-bit

- using LibGMP: true
- length of p: 2048
- number of voters: 100
- elections: 1-out-of-3, 1-out-of-3, 2-out-of-10

| Step name                      | Time taken (ms) |
| ------------------------------ | --------------: |
| creating public parameters     |           3 290 |
| creating election set          |              28 |
| publishing parameters          |               0 |
| key generation                 |              40 |
| public key building            |              14 |
| publish election set           |               0 |
| generating electoral data      |           1 279 |
| build public credentials       |              37 |
| printing code sheets           |              63 |
| voting phase                   |          64 169 |
| mixing                         |          22 214 |
| decryption                     |          38 212 |
| tallying                       |           5 344 |
| total simulation time          |         134 784 |

#### More complex elections

- Date: December 21st, 2016
- Head: protocol-poc-back/4e04f1105462ac6785e3a5a84f9243b85bc1a474

##### Performance statistics @ 2048-bit

- using LibGMP: true
- length of p: 2048
- number of voters: 10
- elections: 7-out-of-36, 100-out-of-576

| Step name                      | Time taken (ms) |
| ------------------------------ | --------------: |
| creating public parameters     |           3 405 |
| creating election set          |              72 |
| publishing parameters          |               0 |
| key generation                 |              46 |
| public key building            |              10 |
| publish election set           |               0 |
| generating electoral data      |           2 699 |
| build public credentials       |              11 |
| printing code sheets           |             111 |
| voting phase                   |         188 720 |
| mixing                         |           3 966 |
| decryption                     |           4 999 |
| tallying                       |             743 |
| total simulation time          |         205 314 |

#### Arithmetic improvements

Replaced calls to divAndRemainder() used in BigInteger to byte[] conversion,
now using shiftRight(8) and byteValue().

This was a major hot spot (most of the execution time - excluding the calls to
the native modexp library), identified through profiling.

- Date: December 21st, 2016
- Head: protocol-poc-back/6361984ec86aabeb0fdf46854f52858df0bc189c

##### Performance statistics @ 1024-bit

- using LibGMP: true
- length of p: 1024
- number of voters: 100
- number of candidates per election: 3, 3, 10

| Step name                      | Time taken (ms) |
| ------------------------------ | --------------: |
| creating public parameters     |           2 507 |
| creating election set          |              22 |
| publishing parameters          |               0 |
| key generation                 |              14 |
| public key building            |               8 |
| publish election set           |               0 |
| generating electoral data      |             683 |
| build public credentials       |              23 |
| printing code sheets           |              70 |
| voting phase                   |          14 122 |
| mixing                         |           9 470 |
| decryption                     |          11 972 |
| tallying                       |           1 041 |
| total simulation time          |          39 983 |

##### Performance statistics @ 2048-bit

- using LibGMP: true
- length of p: 2048
- number of voters: 100
- number of candidates per election: 3, 3, 10

| Step name                      | Time taken (ms) |
| ------------------------------ | --------------: |
| creating public parameters     |           3 247 |
| creating election set          |              19 |
| publishing parameters          |               0 |
| key generation                 |              46 |
| public key building            |               8 |
| publish election set           |               0 |
| generating electoral data      |           1 680 |
| build public credentials       |              30 |
| printing code sheets           |              59 |
| voting phase                   |          65 636 |
| mixing                         |          37 669 |
| decryption                     |          47 359 |
| tallying                       |           6 114 |
| total simulation time          |         161 918 |

#### With more parallelism

The measurements in this section were taken on an HP z440, with an octo-core 
Intel Xeon @3.5GHz and 16GB of RAM, on Windows

- Date: December 20th, 2016
- Head: protocol-poc-back/c104f195c76c255bc7c1595a4a838ceec65cd2e1

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
