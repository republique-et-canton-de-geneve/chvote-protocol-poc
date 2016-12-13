## MixingAuthorityTests

This document outlines the data and the steps of computations used in the more complex unit tests for 
the [MixingAuthorityAlgorithmsTest](MixingAuthorityAlgorithmsTest.groovy).
All random draws in the algorithms have been picked by hand, since the validity of the tests does not
depend on the quality of the randomness.

### Relevant public parameters

- `p = 11`
- `q = 5`
- `g = 3`
- `h = 4`

### genShuffle

#### Input

- `bold_e = ([5, 1], [3, 4], [5, 9])`
- `pk = 3`

#### Algorithm execution

1. `psy = (0, 1, 2)` _(simulated randomness)_
2. `i = 0` -> GenReEncryption
    1. `r'_0 = 1` _(simulated randomness)_
    2. `a'_0 = a_0 * pk ^ r'_0 mod 11 = 5 * 3 ^ 1 mod 11 = 15 mod 11 = 4`
    3. `b'_0 = b_0 * g ^ r'_0 mod 11 = 1 * 3 ^ 1 mod 11 = 6 mod 11 = 3`
3. `i = 1` -> GenReEncryption
    1. `r'_1 = 4` _(simulated randomness)_
    2. `a'_1 = a_1 * pk ^ r'_1 mod 11 = 3 * 3 ^ 4 mod 11 = 243 mod 11 = 1`
    3. `b'_1 = b_1 * g ^ r'_1 mod 11 = 4 * 3 ^ 4 mod 11 = 4 * 81 mod 11 = 5`
4. `i = 2` -> GenReEncryption
    1. `r'_2 = 2` _(simulated randomness)_
    2. `a'_2 = a_2 * pk ^ r'_2 mod 11 = 5 * 3 ^ 2 mod 11 = 45 mod 11 = 1`
    3. `b'_2 = b_2 * g ^ r'_2 mod 11 = 9 * 3 ^ 2 mod 11 = 81 mod 11 = 4`
5. `bold_e' = (e'_j_0, e'_j_1, e'_j_2) = ([1, 5], [4, 3], [1, 4])`
6. `bold_r' = (1, 4, 2)`

### genPermutationCommitment

#### Input

- `psy = (1, 0, 2)`
- `bold_h = (4, 3, 5)`

#### Algorithm execution

1. `i = 0`
    1. `j_0 = 1` 
    2. `r_1 = 2` _(simulated randomness)_
    3. `c_1 = g ^ r_1 * h_0 mod p = 3 ^ 2 * 4 mod 11 = 3`
2. `i = 1`
    1. `j_1 = 0` 
    2. `r_0 = 1` _(simulated randomness)_
    3. `c_0 = g ^ r_1 * h_1 mod p = 3 ^ 1 * 3 mod 11 = 9`
3. `i = 2`
    1. `j_2 = 2` 
    2. `r_2 = 3` _(simulated randomness)_
    3. `c_2 = g ^ r_2 * h_2 mod p = 3 ^ 3 * 5 mod 11 = 3`
4. `bold_c = (c_0, c_1, c_2) = (9, 3, 3)`
5. `bold_r = (r_0, r_1, r_2) = (1, 2, 3)`

### genCommitmentChain

#### Input

- `c_{-1} = 4` _(since list indices are 0-based, prepending an item makes its index `-1`)_
- `bold_u' = (4, 2, 3)`

#### Algorithm execution

1.  `i = 0`
    1. `r_0 = 4` _(simulated randomness)_
    2. `c_0 = g ^ r_0 * c_{-1} ^ u'_0 mod p = 3 ^ 4 * 4 ^ 4 mod 11 = 1`
2.  `i = 1`
    1. `r_1 = 0` _(simulated randomness)_
    2. `c_1 = g ^ r_1 * c_0 ^ u'_1 mod p = 3 ^ 0 * 1 ^ 2 mod 11 = 1`
3.  `i = 2`
    1. `r_2 = 1` _(simulated randomness)_
    2. `c_2 = g ^ r_2 * c_1 ^ u'_2 mod p = 3 ^ 1 * 1 ^ 3 mod 11 = 3`
4. `bold_c = (1, 1, 3)`
5. `bold_r = (4, 0, 1)`
    

### genShuffleProof

We use the shuffle, the permutation commitment and the commitment chain from the tests above.

#### Input

- `bold_e = ([5, 1], [3, 4], [5, 9])`
- `bold_e' = ([1, 5], [4, 3], [1, 4])`
- `bold_r' = (1, 4, 2)`
- `psy = (1, 0, 2)`
- `pk = 3`

#### Algorithm execution

1. `bold_h = (4, 3, 5)` _(simulated call)_
2. `(bold_c, bold_r) = ((9, 3, 3), (1, 2, 3))` _(from the genPermutationCommitment test above)_
3. `bold_u = (2, 4, 3)` _(simulated call)_
4. `bold_u' = (u_j_0, u_j_1, u_j_2) = (u_1, u_0, u_2) = (4, 2, 3)`
5. `(bold_c_circ, bold_r_circ) = ((1, 1, 3), (4, 0, 1))` _(from the genCommitmentChain test above)_
6. generate omega
    1. `omega_1 = 1` _(simulated randomness)_ 
    2. `omega_2 = 2` _(simulated randomness)_
    3. `omega_3 = 3` _(simulated randomness)_ 
    4. `omega_4 = 4` _(simulated randomness)_ 
7. generate `omega_circ` and `omega'` _(indices 0 based, so that usage of 'i' is coherent below)_
    1. `omega_circ_0 = 2`, `omega'_0 = 3` _(simulated randomness)_ 
    2. `omega_circ_1 = 4`, `omega'_1 = 0` _(simulated randomness)_
    3. `omega_circ_2 = 1`, `omega'_2 = 1` _(simulated randomness)_ 
8. `t_1 = g ^ omega_1 mod p = 3 ^ 1 mod 11 = 3`
8. `t_2 = g ^ omega_2 mod p = 3 ^ 2 mod 11 = 9`
8. `t_3 = g ^ omega_3 * PI{i=0, N-1}(h_i ^ omega'_i) mod p ` <br>
   `t_3 = 3 ^ 3 * 4 ^ 3 * 3 ^ 0 * 5 ^ 1' mod 11 = 27 * 64 * 1 * 5 mod 11 = 5`
9. `t_{4,1} = pk ^ -omega_4 * PI{i=0, N-1}( (a'_i) ^ omega'_i ) mod p` <br>
   `t_{4,1} = 3 ^ -4 * 1 ^ 3 * 4 ^ 0 * 1 ^ 1 mod 11 = 4 ^ 4 * 1 * 1 * 1 mod 11 = 3`
9. `t_{4,2} = g ^ -omega_4 * PI{i=0, N-1}( (b'_i) ^ omega'_i ) mod p` <br>
   `t_{4,2} = 3 ^ -4 * 5 ^ 3 * 3 ^ 0 * 4 ^ 1 mod 11 = 4 ^ 4 * 125 * 1 * 4 mod 11 = 4`
10. `c_circ_{-1} = h = 4`
11. `i = 0`
    1. `t_circ_0 = g ^ omega_circ_0 * c_circ_{-1} ^ omega'_0 mod p = 3 ^ 2 * 4 ^ 3 mod 11 = 4` 
    2. `t_circ_1 = g ^ omega_circ_1 * c_circ_0 ^ omega'_1 mod p = 3 ^ 4 * 1 ^ 0 mod 11 = 4` 
    3. `t_circ_2 = g ^ omega_circ_2 * c_circ_1 ^ omega'_2 mod p = 3 ^ 1 * 1 ^ 1 mod 11 = 3`
12. `c = GetNIZKPChallenge(...) = 4` _(simulated call)_
13. `r_bar = SIGMA{i=0, N-1}(r_i) mod q = 1 + 2 + 3 mod 5 = 1`
13. `s_1 = omega_1 + c * r_bar mod q = 1 + 4 * 1 mod 5 = 0`
14. `v_2 = 1`
    1. `i = 1; v_1 = u'_2 * v_2 mod q = 3 * 1 mod 5 = 3`
    1. `i = 0; v_0 = u'_1 * v_1 mod q = 2 * 3 mod 5 = 1`
15. `r_circ = SIGMA{i=0, N-1}(r_circ_i*v_i) mod q = 4 * 1 + 0 * 3 + 1 * 1 mod 5 = 0`
15. `s_2 = omega_2 + c * r_circ mod q = 2 + 4 * 0 mod 5 = 2`
16. `r_tilde = SIGMA{i=0, N-1}(r_i*u_i) mod q = 1 * 2 + 2 * 4 + 3 * 3 mod 5 = 4`
16. `s_3 = omega_3 + c * r_tilde mod q = 3 + 4 * 4 mod 5 = 4`
17. `r' = SIGMA{i=0, N-1}(r'_i*u_i) mod q = 1 * 2 + 4 * 4 + 2 * 3 mod 5 = 4`
17. `s_4 = omega_4 + c * r' mod q = 4 + 4 * 4 mod 5 = 0`
18. compute `s_circ` and `s'`
    1. `s_circ_0 = omega_circ_0 + c * r_circ_0 mod q = 2 + 4 * 4 mod 5 = 3`
    1. `s'_0 = omega'_0 + c * u'_0 mod q = 3 + 4 * 4 mod 5 = 4`
    2. `s_circ_1 = omega_circ_1 + c * r_circ_1 mod q = 4 + 4 * 0 mod 5 = 4`
    2. `s'_1 = omega'_1 + c * u'_1 mod q = 0 + 4 * 2 mod 5 = 3`
    3. `s_circ_2 = omega_circ_2 + c * r_circ_2 mod q = 1 + 4 * 1 mod 5 = 0`
    3. `s'_2 = omega'_2 + c * u'_2 mod q = 1 + 4 * 3 mod 5 = 3`

### checkShuffleProof
 
We use the input and the proof given in the test above

#### Input

- `pi = (t, s, bold_c, bold_c_circ)`
    - `t = (t_1, t_2, t_3, (t_{4,1}, t_{4,2}), (t_circ_0, ..., t_circ_{N-1}))`
        - `t_1 = 3`
        - `t_2 = 9`
        - `t_3 = 5`
        - `(t_{4,1}, t_{4,2}) = (3, 4)`
        - `(t_circ_0, ..., t_circ_{N-1}) = (4, 4, 3)`
    - `s = (s_1, s_2, s_3, s_4, (s_circ_0, ..., s_circ_{N-1}), (s'_0, ..., s'_{N-1}))`
        - `s_1 = 0`
        - `s_2 = 2`
        - `s_3 = 4`
        - `s_4 = 0`
    - `bold_c = (9, 3, 3)`
    - `bold_c_circ = (1, 1, 3)`
- `bold_e = ([5, 1], [3, 4], [5, 9])`
- `bold_e' = ([1, 5], [4, 4], [1, 3])`
- `pk = 3`

#### Algorithm execution

1. `bold_h = (4, 3, 5)` _(simulated call)_
2. `bold_u = (2, 4, 3)` _(simulated call)_
3. `c = GetNIZKPChallenge(...) = 4` _(simulated call)_
4. `c_bar = PI{i=0, N-1}(c_i) / PI{i=0, N-1}(h_i) mod p = (9 * 3 * 3) / (4 * 3 * 5) mod 11` <br>
   `c_bar = 4 * 5 ^ (-1) mod 11 = 4 * 9 mod 11 = 3`
5. `u = PI{i=0, N-1}(u_i) mod q = 2 * 4 * 3 mod 5 = 4`
6. `c_circ = c_circ_{N-1} / (h ^ u) mod p = 3 / (4 ^ 4) mod 11 = 3 * 3 ^ 4 mod 11 = 1`
7. `c_tilde = PI{i=0, N-1}(c_i ^ u_i) mod p = 9 ^ 2 * 3 ^ 4 * 3 ^ 3 mod 11 = 3`
8. `e'_1 = PI{i=0, N-1}(a_i ^ u_i) mod p = 5 ^ 2 * 3 ^ 4 * 5 ^ 3 mod 11 = 4`
8. `e'_2 = PI{i=0, N-1}(b_i ^ u_i) mod p = 1 ^ 2 * 4 ^ 4 * 9 ^ 3 mod 11 = 9`
9. `t'_1 = c_bar ^ (-c) * g ^ s_1 mod p = 3 ^ (-4) * 3 ^ 0 mod 11 = 4 ^ 4 * 1 mod 11 = 3`
10. `t'_2 = c_circ ^ (-c) * g ^ s_2 mod p = 1 ^ (-4) * 3 ^ 2 mod 11 = 9`
11. `t'_3 = c_tilde ^ (-c) * g ^ s_3 * PI{i=0, N-1}(h_i ^ s'_i) mod p` <br>
    `t'_3 = 3 ^ (-4) * 3 ^ 4 * 4 ^ 4 * 3 ^ 3 * 5 ^ 3 mod 11 = 5`
12. `t'_{4,1} = e'_1 ^ (-c) * pk ^ (-s_4) * PI{i=0, N-1}(a'_i ^ s'_i) mod p` <br>
    `t'_{4,1} = 4 ^(-4) * 3 ^ (-0) * 1 ^ 4 * 4 ^ 3 * 1 ^ 3 mod 11 = 3`
12. `t'_{4,2} = e'_2 ^ (-c) * g ^ (-s_4) * PI{i=0, N-1}(b'_i ^ s'_i) mod p` <br>
    `t'_{4,2} = 9 ^(-4) * 3 ^ (-0) * 5 ^ 4 * 4 ^ 3 * 3 ^ 3 mod 11 = 4`
13. compute `t_circ'`
    1. `t_circ'_0 = c_circ_0 ^ (-c) * g ^ s_circ_0 * c_circ_{-1} ^ s'_0 mod p` <br>
       `t_circ'_0 = 1 ^ (-4) * 3 ^ 3 * 4 ^ 4 mod 11 = 4` 
    2. `t_circ'_1 = c_circ_1 ^ (-c) * g ^ s_circ_1 * c_circ_0 ^ s'_1 mod p` <br>
       `t_circ'_1 = 1 ^ (-4) * 3 ^ 4 * 1 ^ 3 mod 11 = 4` 
    3. `t_circ'_2 = c_circ_2 ^ (-c) * g ^ s_circ_2 * c_circ_1 ^ s'_2 mod p` <br>
       `t_circ'_2 = 3 ^ (-4) * 3 ^ 0 * 1 ^ 3 mod 11 = 3` 
