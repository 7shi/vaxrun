PREFIX=~/.vaxrun
BINDIR=/usr/local/bin

all:
	mkdir -p build/classes
	javac -d build/classes `find src -name "*.java"`
	mkdir -p dist
	jar cfm dist/VFSViewer.jar VFSViewer.mf -C build/classes/ .
	$(MAKE) $@ -C cmd

install:
	mkdir -p $(PREFIX)
	install -c -m 755 bin/* $(BINDIR)
	cd build && tar cvf - classes | tar xf - -C $(PREFIX)
	tar cvf - root | tar xf - -C $(PREFIX)
	cd cmd && cp nm strip $(PREFIX)/root/bin
	chmod 755 $(PREFIX)/root/bin/*
	cd $(PREFIX)/root/lib && chmod 755 c2 ccom cpp f1

clean:
	rm -rf build dist root/tmp/*
	$(MAKE) $@ -C cmd
