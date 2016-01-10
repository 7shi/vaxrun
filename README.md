# User Mode VAX Interpreter

This distribution is forked from:
https://github.com/kusabanachi/VaxInterpreter

## Install

```
$ hg clone https://bitbucket.org/7shi/vaxrun
$ cd vaxrun
$ make
$ sudo make install
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
