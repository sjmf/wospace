package csc3202.Engine;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

/**
 * Port of a stackOverflow question answer on line intersection from JS to Java via C#
 * 
 * Used for 2-dimensional hitbox look-ahead collision 
 * 
 * @author sam
 * 
 * port of this JavaScript code with some changes:
 * http://www.kevlindev.com/gui/math/intersection/Intersection.js found here:
 * http://stackoverflow.com/questions/563198
 * http://stackoverflow.com/questions/2255842
 */
 
public class Intersector
{
	static double MyEpsilon = 0.00001;
	
	public class IntersectException extends Exception {
		private static final long serialVersionUID = 3928207473341941035L;

		public IntersectException(String message) {
	        super(message);
	    }
	}
		
	private float[] overlapIntervals(float ub1, float ub2)
	{
	    float l = Math.min(ub1, ub2);
	    float r = Math.max(ub1, ub2);
	    float A = Math.max(0, l);
	    float B = Math.min(1, r);
	    if (A > B) // no intersection
	        return new float[] { };
	    else if (A == B)
	        return new float[] { A };
	    else // if (A < B)
	        return new float[] { A, B };
	}
	
	// IMPORTANT: a1 and a2 cannot be the same, e.g. a1--a2 is a true segment, not a point
	// b1/b2 may be the same (b1--b2 is a point)
	private Vector2f[] oneD_Intersection(Vector2f a1, Vector2f a2, Vector2f b1, Vector2f b2)
	{
	    //float ua1 = 0.0f; // by definition
	    //float ua2 = 1.0f; // by definition
	    float ub1, ub2;
	
	    float denomx = a2.x - a1.x;
	    float denomy = a2.y - a1.y;
	
	    if (Math.abs(denomx) > Math.abs(denomy))
	    {
	        ub1 = (b1.x - a1.x) / denomx;
	        ub2 = (b2.x - a1.x) / denomx;
	    }
	    else
	    {
	        ub1 = (b1.y - a1.y) / denomy;
	        ub2 = (b2.y - a1.y) / denomy;
	    }
	
	    List<Vector2f> ret = new ArrayList<Vector2f>();
	    float[] interval = overlapIntervals(ub1, ub2);
	    for(float f : interval)
	    {
	        float x = a2.x * f + a1.x * (1.0f - f);
	        float y = a2.y * f + a1.y * (1.0f - f);
	        Vector2f p = new Vector2f(x, y);
	        ret.add(p);
	    }
	    return ret.toArray(new Vector2f[0]);
	}
	
	private boolean vector2fOnLine(Vector2f p, Vector2f a1, Vector2f a2) throws IntersectException
	{
	    float dummyU = 0.0f;
	    double d = distFromSeg(p, a1, a2, MyEpsilon, dummyU);
	    return d < MyEpsilon;
	}
	
	private double distFromSeg(Vector2f p, Vector2f q0, Vector2f q1, double radius, float u) throws IntersectException
	{
	    // formula here:
	    //http://mathworld.wolfram.com/Vector2f-LineDistance2-Dimensional.html
	    // where x0,y0 = p
	    //       x1,y1 = q0
	    //       x2,y2 = q1
	    double dx21 = q1.x - q0.x;
	    double dy21 = q1.y - q0.y;
	    double dx10 = q0.x - p.x;
	    double dy10 = q0.y - p.y;
	    double segLength = Math.sqrt(dx21 * dx21 + dy21 * dy21);
	    if (segLength < MyEpsilon)
	        throw new IntersectException("Expected line segment, not point.");
	    double num = Math.abs(dx21 * dy10 - dx10 * dy21);
	    double d = num / segLength;
	    return d;
	}
	
	// this is the general case. Really really general
	public Vector2f[] intersection(Vector2f a1, Vector2f a2, Vector2f b1, Vector2f b2) throws IntersectException
	{
	    if (a1.equals(a2) && b1.equals(b2))
	    {
	        // both "segments" are points, return either point
	        if (a1.equals(b1))
	            return new Vector2f[] { a1 };
	        else // both "segments" are different points, return empty set
	            return new Vector2f[] { };
	    }
	    else if (b1.equals(b2)) // b is a point, a is a segment
	    {
	        if (vector2fOnLine(b1, a1, a2))
	            return new Vector2f[] { b1 };
	        else
	            return new Vector2f[] { };
	    }
	    else if (a1.equals(a2)) // a is a point, b is a segment
	    {
	        if (vector2fOnLine(a1, b1, b2))
	            return new Vector2f[] { a1 };
	        else
	            return new Vector2f[] { };
	    }
	
	    // at this point we know both a and b are actual segments
	
	    float ua_t = (b2.x - b1.x) * (a1.y - b1.y) - (b2.y - b1.y) * (a1.x - b1.x);
	    float ub_t = (a2.x - a1.x) * (a1.y - b1.y) - (a2.y - a1.y) * (a1.x - b1.x);
	    float u_b = (b2.y - b1.y) * (a2.x - a1.x) - (b2.x - b1.x) * (a2.y - a1.y);
	
	    // Infinite lines intersect somewhere
	    if (!(-MyEpsilon < u_b && u_b < MyEpsilon))   // e.g. u_b != 0.0
	    {
	        float ua = ua_t / u_b;
			float ub = ub_t / u_b;
			if (0.0f <= ua && ua <= 1.0f && 0.0f <= ub && ub <= 1.0f) {
				// Intersection
				return new Vector2f[] { new Vector2f(
						(a1.x + ua * (a2.x - a1.x)),
						(a1.y + ua * (a2.y - a1.y))) };
			} else {
				// No Intersection
				return new Vector2f[] {};
			}
		}
	    else // lines (not just segments) are parallel or the same line
	    {
	        // Coincident
	        // find the common overlapping section of the lines
	        // first find the distance (squared) from one point (a1) to each point
	        if ((-MyEpsilon < ua_t && ua_t < MyEpsilon)
	           || (-MyEpsilon < ub_t && ub_t < MyEpsilon))
	        {
	            if (a1.equals(a2)) // danger!
	                return oneD_Intersection(b1, b2, a1, a2);
	            else // safe
	                return oneD_Intersection(a1, a2, b1, b2);
	        }
	        else
	        {
	            // Parallel
	            return new Vector2f[] { };
	        }
	    }
	}
	

	/**
	 * Unit test for Intersector class
	 * @author http://stackoverflow.com/questions/2255842
	 */
	private static class IntersectTest
	{
		Intersector intersect;
		
		public IntersectTest() {
			intersect = new Intersector();
		}
		
	    public void printVector2fs(Vector2f[] pf)
	    {
	        if (pf == null || pf.length < 1)
	            System.out.println("Doesn't intersect");
	        else if (pf.length == 1)
	        {
	            System.out.println(pf[0]);
	        }
	        else if (pf.length == 2)
	        {
	            System.out.println(pf[0] + " -- " + pf[1]);
	        }
	    }
	
	    public Vector2f[] testIntersect(Vector2f a1, Vector2f a2, Vector2f b1, Vector2f b2) throws IntersectException
	    {
	        System.out.println("----------------------------------------------------------");
	        System.out.println("Does      " + a1 + " -- " + a2);
	        System.out.println("intersect " + b1 + " -- " + b2 + " and if so, where?");
	        System.out.println("");
	        Vector2f[] result = intersect.intersection(a1, a2, b1, b2);
	        printVector2fs(result);
	        return result;
	    }
	
	    public void runTest() throws IntersectException
	    {
	    	Vector2f[] res;
	        System.out.println("----------------------------------------------------------");
	        System.out.println(" Line segments intersect");
	        res = testIntersect(new Vector2f(0, 0),
	                      new Vector2f(100, 100),
	                      new Vector2f(100, 0),
	                      new Vector2f(0, 100));
	        System.out.println("Expected {X=50, Y=50}");
			if (res[0].x == 50 && res[0].y == 50)	System.out.println("PASS");
			else 									System.out.println("FAIL");
	        
	        res = testIntersect(new Vector2f(5, 17),
	                      new Vector2f(100, 100),
	                      new Vector2f(100, 29),
	                      new Vector2f(8, 100));
	        // C# run of this test gives different results. See http://stackoverflow.com/questions/6683059
	        System.out.println("Expected {X=56.85001, Y=62.30054}");
			if (res[0].x == 56.85001 && res[0].y == 62.30054)	System.out.println("PASS");
			else 												System.out.println("FAIL");
			
	        System.out.println("----------------------------------------------------------\n\n");
	        
	
	        
	        System.out.println("----------------------------------------------------------");
	        System.out.println(" Just touching points and lines cross");
	        res = testIntersect(new Vector2f(0, 0),
	                      new Vector2f(25, 25),
	                      new Vector2f(25, 25),
	                      new Vector2f(100, 75));
	        System.out.println("Expected {X=25, Y=25}");
			if (res[0].x == 25 && res[0].y == 25)	System.out.println("PASS");
			else 									System.out.println("FAIL");
			
	        System.out.println("----------------------------------------------------------\n\n");
	        
	        
	
	        System.out.println("----------------------------------------------------------");
	        System.out.println(" Parallel");
	        res = testIntersect(new Vector2f(0, 0),
	                      new Vector2f(0, 100),
	                      new Vector2f(100, 0),
	                      new Vector2f(100, 100));
	        System.out.println("Expected 'Doesn't intersect'");
	        if(res.length == 0)		System.out.println("PASS");
			else 					System.out.println("FAIL");
	        
	        System.out.println("----------------------------------------------------------\n\n");
	        
	        
	
	        System.out.println("----------------------------------------------------------");
	        System.out.println(" Lines cross but segments don't intersect");
	        res = testIntersect(new Vector2f(50, 50),
	                      new Vector2f(100, 100),
	                      new Vector2f(0, 25),
	                      new Vector2f(25, 0));
	        System.out.println("Expected 'Doesn't intersect'");
	        if(res.length == 0)		System.out.println("PASS");
			else 					System.out.println("FAIL");
	        
	        System.out.println("----------------------------------------------------------\n\n");
	        
	        
	
	        System.out.println("----------------------------------------------------------");
	        System.out.println(" Coincident but do not overlap!");
	        res = testIntersect(new Vector2f(0, 0),
	                      new Vector2f(25, 25),
	                      new Vector2f(75, 75),
	                      new Vector2f(100, 100));
	        System.out.println("Expected 'Doesn't intersect'");
	        if(res.length == 0)		System.out.println("PASS");
			else 					System.out.println("FAIL");
	        
	        System.out.println("----------------------------------------------------------\n\n");
	        
	        
	
	        System.out.println("----------------------------------------------------------");
	        System.out.println(" Touching points and coincident!");
	        res = testIntersect(new Vector2f(0, 0),
	                      new Vector2f(25, 25),
	                      new Vector2f(25, 25),
	                      new Vector2f(100, 100));
	        System.out.println("Expected {X=25, Y=25}");
			if (res[0].x == 25 && res[0].y == 25)	System.out.println("PASS");
			else 									System.out.println("FAIL");
			
	        System.out.println("----------------------------------------------------------\n\n");
	       
	
	        
	        System.out.println("----------------------------------------------------------");
	        System.out.println(" Overlap/coincident");
	        res = testIntersect(new Vector2f(0, 0),
	                      new Vector2f(75, 75),
	                      new Vector2f(25, 25),
	                      new Vector2f(100, 100));
	        System.out.println("Expected {X=25, Y=25} -- {X=75, Y=75}");
			if (res[0].x == 25 && res[0].y == 25 &&
				res[1].x == 75 && res[1].y == 75)	System.out.println("PASS");
			else 									System.out.println("FAIL");
			
	        res = testIntersect(new Vector2f(0, 0),
	                      new Vector2f(100, 100),
	                      new Vector2f(0, 0),
	                      new Vector2f(100, 100));
	        System.out.println("Expected {X=0, Y=0} -- {X=100, Y=100}");
			if (res[0].x == 0 && res[0].y == 0 &&
				res[1].x == 100 && res[1].y == 100)	System.out.println("PASS");
			else 									System.out.println("FAIL");
			
	        System.out.println("----------------------------------------------------------\n");
	
	    }
	} 

	public static void main(String[] args) throws IntersectException
	{
		new Intersector.IntersectTest().runTest();
	}
}