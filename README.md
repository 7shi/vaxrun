# User Mode VAX interpreter

This distribution is forked from:
https://github.com/kusabanachi/VaxInterpreter

## Install

```
$ make
$ make install
```

## Usage

```
$ cat hello.c
#include <stdio.h>

main() {
    printf("hello\n");
    return 0;
}
$ vaxcc hello.c
$ vaxrun a.out
```
