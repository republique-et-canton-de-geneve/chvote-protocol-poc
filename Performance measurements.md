## Performance measurements

### Introduction

This page will contain performance measurements, including all relevant context
information.
Newest measurements will always appear at the top of the page.

All measurements should include the current date and the hash of the HEAD commit
at the moment they were taken.

Unless otherwise specified, measurements were taken on an HP EliteBook, with an
 Intel i5 CPU @2.4GHz and 8GB of RAM, on Windows.

### Measurements

#### LibGMP integration

- Date: December 16, 2016
- Head: 

##### Performance statistics @ 1024-bit

- using LibGMP: true
- length of p: 1024
- number of voters: 100
- number of candidates per election: 3,3,10

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

#### Initial measurements

- Date: December 16, 2016
- Head: 108c150bd444d5de7da59888687b1ff8464b64b5

##### Performance statistics @ 1024-bit

- length of p: 1024
- number of voters: 100
- number of candidates per election: 3,3,10

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
- number of candidates per election: 3,3,10

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
