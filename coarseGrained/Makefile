.PHONY: build

SRC = SkipListEntry.java SkipList.java

build:
	mkdir -p build
	javac $(SRC) Main.java -d build -Xlint:unchecked
run: build
	java -cp build coarseGrained.Main
clean:
	rm -r build
