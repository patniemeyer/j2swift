// package foo.bar;

// import foo.bar.Blah;

/** Class comment */
public class Test  :  Object
{
    public static var lock : Object = nil; // static, null -> Swift 1.2 static class variable, nil
    public var x : Int = 5;
    internal var f : Float = 5.0; // float literal
    public var a : Int = 1, b : Int = 2;
    var floatArray : [Float];
    var objArray : [Object];
    /*volatile*/ var testVolatile : Int;

    // Constructor (to convenience constructor)
    public convenience init() {
        // this is the constructor body
        self.init(5.0, "foo"); // explicit constructor invocation
    }

    // Constructor with two args
    public init( _ a : Int, _ b : Object ) {  }

    internal func run()
    {
        // this is the run body
        var a : Int = 42; // local var
        self.a = b; // this reference
        Test(); // naked new instance creation
        var test : Test = Test(); // new instance creation
        throwException() /* throw RuntimeException("foo"); */ // throw exception
        var foo : Float = (Float)42.0;
        var floatArray : [Float];
        var objArray : [Object];
        var stringMap : Dictionary<String,String> = Dictionary<String,String>();
        var stringList : Array<String> = Array<>();

        for  var i : Int=0; i< 42; i++  { println("foo"); }
        for var i : Int=0; i< 42; i++ { println("foo"); } // missing braces
        var i : Int = 42;
        while i > 0  {
            println(i--);
        }
        var objectList : Array<Object>;
        for obj : Object in objectList { println(obj); }

        var foo : String;
        if ( foo is String ) { }
    }

    class func test( a : Int, _ b : Object ) -> Float // static func two args
    {
        return 42.0;
    }
    func test( a : Int, _ fa : [Float] ) -> [Float]
    {
        return 42.0;
    }
}

class Foo  :  Bar, Gee { }

public protocol MyInterface {
    func run();
}


