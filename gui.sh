if [ ! -e "./lib/antlr-4.5-complete.jar" ]; then
  mkdir lib
  curl https://www.antlr.org/download/antlr-4.5-complete.jar --output lib/antlr-4.5-complete.jar
  javac -classpath .:./lib/antlr-4.5-complete.jar src/*.java
fi
java -classpath src:./lib/antlr-4.5-complete.jar org.antlr.v4.runtime.misc.TestRig Java8 compilationUnit -gui $*
