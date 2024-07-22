

#include "random.h"


#include <stdio.h>
#include <string.h>

/* Period parameters */
#define N 624
#define M 397
#define MATRIX_A VL_UINT32_C(0x9908b0df)   /* constant vector a */
#define UPPER_MASK VL_UINT32_C(0x80000000) /* most asignificant w-r bits */
#define LOWER_MASK VL_UINT32_C(0x7fffffff) /* least significant r bits */

/* initializes mt[N] with a seed */

/** @brief Initialise random number generator
 ** @param self number generator.
 **/

void
vl_rand_init (VlRand * self)
{
  memset (self->mt, 0, sizeof(self->mt[0]) * N) ;
  self->mti = N + 1 ;
}

/** @brief Seed the state of the random number generator
 ** @param self random number generator.
 ** @param s seed.
 **/

void
vl_rand_seed (VlRand * self, vl_uint32 s)
{
#define mti self->mti
#define mt self->mt
  mt[0]= s & VL_UINT32_C(0xffffffff);
  for (mti=1; mti<N; mti++) {
    mt[mti] =
      (VL_UINT32_C(1812433253) * (mt[mti-1] ^ (mt[mti-1] >> 30)) + mti);
    /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
    /* In the previous versions, MSBs of the seed affect   */
    /* only MSBs of the array mt[].                        */
    /* 2002/01/09 modified by Makoto Matsumoto             */
    mt[mti] &= VL_UINT32_C(0xffffffff);
    /* for >32 bit machines */
  }
#undef mti
#undef mt
}

/** @brief Seed the state of the random number generator by an array
 ** @param self     random number generator.
 ** @param key      array of numbers.
 ** @param keySize  length of the array.
 **/

void
vl_rand_seed_by_array (VlRand * self, vl_uint32 const key [], vl_size keySize)
{
#define mti self->mti
#define mt self->mt
  int i, j, k;
  vl_rand_seed (self, VL_UINT32_C(19650218));
  i=1; j=0;
  k = (N > keySize ? N : (int)keySize);
  for (; k; k--) {
    mt[i] = (mt[i] ^ ((mt[i-1] ^ (mt[i-1] >> 30)) * VL_UINT32_C(1664525)))
      + key[j] + j; /* non linear */
    mt[i] &= VL_UINT32_C(0xffffffff); /* for WORDSIZE > 32 machines */
    i++; j++;
    if (i>=N) { mt[0] = mt[N-1]; i=1; }
    if (j>=(signed)keySize) j=0;
  }
  for (k=N-1; k; k--) {
    mt[i] = (mt[i] ^ ((mt[i-1] ^ (mt[i-1] >> 30)) * VL_UINT32_C(1566083941)))
      - i; /* non linear */
    mt[i] &= VL_UINT32_C(0xffffffff) ; /* for WORDSIZE > 32 machines */
    i++;
    if (i>=N) { mt[0] = mt[N-1]; i=1; }
  }

  mt[0] = VL_UINT32_C(0x80000000); /* MSB is 1; assuring non-zero initial array */
#undef mti
#undef mt
}

/** @brief Randomly permute and array of indexes.
 ** @param self random number generator.
 ** @param array array of indexes.
 ** @param size number of element in the array.
 **
 ** The function uses *Algorithm P*, also known as *Knuth shuffle*.
 **/

void
vl_rand_permute_indexes (VlRand *self, vl_index *array, vl_size size)
{
  vl_index i, j, tmp;
  for (i = size - 1 ; i > 0; i--) {
    /* Pick a random index j in the range 0, i + 1 and swap it with i */
    j = (vl_int) vl_rand_uindex (self, i + 1) ;
    tmp = array[i] ; array[i] = array[j] ; array[j] = tmp ;
  }
}


/** @brief Generate a random UINT32
 ** @param self random number generator.
 ** @return a random number in [0, 0xffffffff].
 **/

vl_uint32
vl_rand_uint32 (VlRand * self)
{
  vl_uint32 y;
  static vl_uint32 mag01[2]={VL_UINT32_C(0x0), MATRIX_A};
  /* mag01[x] = x * MATRIX_A  for x=0,1 */

#define mti self->mti
#define mt self->mt

  if (mti >= N) { /* generate N words at one time */
    int kk;

    if (mti == N+1)   /* if init_genrand() has not been called, */
      vl_rand_seed (self, VL_UINT32_C(5489)); /* a default initial seed is used */

    for (kk=0;kk<N-M;kk++) {
      y = (mt[kk]&UPPER_MASK)|(mt[kk+1]&LOWER_MASK);
      mt[kk] = mt[kk+M] ^ (y >> 1) ^ mag01[y & VL_UINT32_C(0x1)];
    }
    for (;kk<N-1;kk++) {
      y = (mt[kk]&UPPER_MASK)|(mt[kk+1]&LOWER_MASK);
      mt[kk] = mt[kk+(M-N)] ^ (y >> 1) ^ mag01[y & VL_UINT32_C(0x1)];
    }
    y = (mt[N-1]&UPPER_MASK)|(mt[0]&LOWER_MASK);
    mt[N-1] = mt[M-1] ^ (y >> 1) ^ mag01[y & VL_UINT32_C(0x1)];

    mti = 0;
  }

  y = mt[mti++];

  /* Tempering */
  y ^= (y >> 11);
  y ^= (y << 7) & VL_UINT32_C(0x9d2c5680);
  y ^= (y << 15) & VL_UINT32_C(0xefc60000);
  y ^= (y >> 18);

  return (vl_uint32)y;

#undef mti
#undef mt
}
