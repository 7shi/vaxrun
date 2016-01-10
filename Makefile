PREFIX=~/.vaxrun
BINDIR=/usr/local/bin

all:
	mkdir -p class/vax_interpreter
	javac -d class vax_interpreter/*.java

install:
	mkdir -p $(PREFIX)
	tar cvf - root class | tar xf - -C $(PREFIX)
	install -c -m 755 bin/* $(BINDIR)

clean:
	rm -rf class
