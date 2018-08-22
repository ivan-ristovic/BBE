public class A 
{ 
    private static int fooint = 3;

    private static void foo() 
    {
        int i = 9;
        int jot, k, r = 0;
        ArrayList<Integer> al = new ArrayList<Integer>();
        
        fooint = 4;
        
        jot = 345;
        
        i += 5;
        
        jot -= 5;

        k = 19;
        
        k++;
        
        ++i;
        
        --k;
        
        r += i;
        
        int x = i + k;

        int m = 4 * 3;
        
        if (m < 3)
        	k++;
        
        foo();

        int a = 5, b = 6;
        return x;
    }
    
    int foo (int a, int b)
    {
    	int x = a + b;
    	
    	return x;
    }
}
