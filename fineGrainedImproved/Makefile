.PHONY: build run test clean

build:
	mkdir -p build
	javac SkipListEntry.java SkipList.java Main.java -d build
run: build
	java -cp build fineGrainedImproved.Main
clean:
	rm -r build
test:
	@mkdir -p build
	@javac -cp .:jars/junit-4.12.jar SkipList.java SkipListEntry.java SkipListTest.java -d build
	@java -cp .:jars/junit-4.12.jar:jars/hamcrest-core-1.3.jar:build org.junit.runner.JUnitCore fineGrainedImproved.SkipListTest
