.PHONY: build run test clean

INTERFACE = ../SkipListInterface/SkipListInterface.java
SRC = SkipListEntry.java SkipList.java

build:
	mkdir -p build
	javac $(INTERFACE) $(SRC) Main.java -d build -Xlint:unchecked
run: build
	java -cp build CoarseGrained.Main
clean:
	rm -r build
test:
	@mkdir -p build
	@javac -cp .:jars/junit-4.12.jar $(INTERFACE) $(SRC) SkipListTest.java -d build
	@java -cp .:jars/junit-4.12.jar:jars/hamcrest-core-1.3.jar:build org.junit.runner.JUnitCore CoarseGrained.SkipListTest
