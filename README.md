Distribution of VAX Tools

## Included

* https://github.com/kusabanachi/VaxInterpreter
* https://github.com/hiro4669/ve3
* https://github.com/hiro4669/FileExtractor
* https://github.com/hiro4669/VFSViewer

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
hello
```
