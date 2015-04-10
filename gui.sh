J2SWIFT=~/Desktop/j2swift/
java -classpath $J2SWIFT/lib/antlr-4.5-complete.jar:$J2SWIFT/out/production/antlr/:$J2SWIFT/src org.antlr.v4.runtime.misc.TestRig Java8 compilationUnit -gui $*
