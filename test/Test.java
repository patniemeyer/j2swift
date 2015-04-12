package foo.bar;

import foo.bar.Blah;

/** Class comment */
public class Test extends Object
{
    public static Object lock = null; // static, null -> Swift 1.2 static class variable, nil
    public int x = 5;
    protected float f = 5.0f; // float literal
    public int a = 1, b = 2;
    float [] floatArray;
    Object [] objArray;
    volatile int testVolatile;

    // Constructor (to convenience constructor)
    public Test() {
        // this is the constructor body
        this(5.0, "foo"); // explicit constructor invocation
    }

    // Constructor with two args
    public Test( int a, Object b ) {  }

    protected void run()
    {
        // this is the run body
        int a = 42; // local var
        this.a = b; // this reference
        new Test(); // naked new instance creation
        Test test = new Test(); // new instance creation
        throw new RuntimeException("foo"); // throw exception
        float foo = (float)42.0;
        float [] floatArray;
        Object [] objArray;
        Map<String,String> stringMap = new HashMap<String,String>();
        List<String> stringList = new ArrayList<>();

        for( int i=0; i< 42; i++ ) { System.out.println("foo"); }
        for(int i=0; i< 42; i++) System.out.println("foo"); // missing braces
        int i = 42;
        while( i > 0 ) {
            System.out.println(i--);
        }
        List<Object> objectList;
        for( Object obj : objectList ) { println(obj); }

        String foo;
        if ( foo instanceof String ) { }
    }

    static float test( int a, Object b ) // static func two args
    {
        return 42.0f;
    }
    float [] test( int a, float [] fa )
    {
        return 42.0f;
    }
}

class Foo implements Bar, Gee { }

public interface MyInterface {
    void run();
}

