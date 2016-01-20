TARGET = nm strip
CC = sh vaxcc

all: $(TARGET)

nm: nm.c
	$(CC) -o $@ $<

strip: strip.c
	$(CC) -o $@ $<

clean:
	rm -f $(TARGET)
