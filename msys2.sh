bindir=$1
javadir="/c/Program Files/Java"
if [ ! -d "$javadir" ]
then
    echo "can not find Java"
    exit 1
fi
cd "$javadir"
for i in `echo jdk* | sort`
do
    jdk="$i"
done
if [ "$jdk" = "jdk*" ]
then
    echo "can not find JDK"
    exit 1
fi
jdkbin="$javadir/$jdk/bin"
cd $bindir
echo "#!/bin/sh" > java
cp java javac
cp java jar
echo "\"$jdkbin/java\" \$@" >> java
echo "winpty \"$jdkbin/javac\" \$@" >> javac
echo "winpty \"$jdkbin/jar\" \$@" >> jar
