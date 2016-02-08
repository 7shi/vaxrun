Distribution of VAX Tools

## Included

* https://github.com/kusabanachi/VaxInterpreter
* https://github.com/hiro4669/ve3
* https://github.com/hiro4669/FileExtractor
* https://github.com/hiro4669/VFSViewer
* https://bitbucket.org/7shi/vax

## Install

#### UNIX

```
$ hg clone https://bitbucket.org/7shi/vaxrun
$ cd vaxrun
$ make
$ sudo make install
```

#### MSYS2

```
$ hg clone https://bitbucket.org/7shi/vaxrun
$ cd vaxrun
$ make install-msys2
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
hello
```

## Build UNIX/32V Kernel

```
$ cd sys/sys
$ make
```
