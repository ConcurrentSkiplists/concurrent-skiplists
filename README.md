# concurrent-skiplists

Compare fine-grained locked skiplists and lock-free skiplists.

## Directories

* `FineGrained`, `FineGrainedImproved`, etc.: Implementations of skiplists.
  Improved versions include our get() optimization.
* `benchmarks`: Source that generates benchmark data.
* `graph`: Graph serving / plotting.
* `paper`: TeX source and generated PDF for our paper.

Makefiles are provided. Most directories have the following targets:

```
make
```

and

```
make test
```

[![Build Status](https://travis-ci.org/ConcurrentSkiplists/concurrent-skiplists.svg?branch=master)](https://travis-ci.org/ConcurrentSkiplists/concurrent-skiplists)
