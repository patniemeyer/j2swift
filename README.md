# j2swift
@author Pat Niemeyer (pat@pat.net)

This is a very basic Java 8 to Swift 1.2 syntax converter.  It handles things like flipping type arguments around and rewriting method signatures and constructors. This is by no means a complete Java to Swift translator as it does not make API updates other than in trivial cases.

The converter preserves whitespace and should leave the input untouched in places that it doesn't update, so it is somewhat useful even when it leaves work to be done.  For simplistic Java code (e.g. business logic) I'd say this gets you 80% of the way there.

See test/Test.java and test/Test.swift for an idea of what is currently handled.

A few things it does
--------------------
- Rewrites static and instance field declarations
- Rewrites method and init argument lists
- Rewrites constructors and constructor invocations, including attempting to note convenience intializers 
- Rewrites simple array types
- Rewrites 'this' references as 'self'
- Rewrites for, enhanced for, and while loops
- Maps float, int, long and boolean types
- Maps null to nil
- Comments out throws statements and replaces them with a placeholder method invocation throwException()
- Maps interface to protocol
- Maps System.out.println() to println()


A few things it could do in the future
--------------------------------------

- Final vars should become "let" vars.
- Method and fields with the same name should be disambiguated.
- Static method invocations should be qualified.


Usage
--------
gradle convertDirectory -Pdir={directory}
gradle convertDirectory -Pfile={filepath}

Notes
________

The ANTLR grammar for Java is provided for reference but you do not have to build the parser to make changes.  All updates really happen in the J2SwiftListener class.

J2Swift takes a filename or stding as input and outputs to stdout.

Contributions
[Runtime Converter (Online PHP to Java Conversions)](http://www.runtimeconverter.com)

