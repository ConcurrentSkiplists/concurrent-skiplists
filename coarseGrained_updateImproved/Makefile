.PHONY: build run test clean

SRC = SkipListEntry.java SkipList.java

build:
	mkdir -p build
	javac $(SRC) Main.java -d build -Xlint:unchecked
run: build
	java -cp build coarseGrained_updateImproved.Main
clean:
	rm -r build
test:
	@mkdir -p build
	@javac -cp .:jars/junit-4.12.jar $(SRC) SkipListTest.java -d build
	@java -cp .:jars/junit-4.12.jar:jars/hamcrest-core-1.3.jar:build org.junit.runner.JUnitCore coarseGrained_updateImproved.SkipListTest
