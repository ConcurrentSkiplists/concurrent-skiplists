.PHONY: build

build:
	mkdir -p build
	javac *.java -d build
run: build
	java -cp build Main
clean:
	rm -r build
