package csc3202.Engine;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import csc3202.Engine.Intersector.IntersectException;

/**
 * A hitbox which maintains its position in the game-world,
 * and the max/min verticies for each axis
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class Hitbox {
	
	// Turn on render for all instances of hitbox
	// DEFINITELY not thread-safe
	public static boolean render = false;
	
	private static Intersector intersect = new Intersector();;
	
	private Vector3f pos;
	private Vector3f min;
	private Vector3f max;
	
	
	/**
	 * Create a hitbox with min/max x/y/z verticies
	 * 
	 * @param Vector3f max
	 * @param Vector3f min
	 */
	public Hitbox(Vector3f min, Vector3f max) {
		
		this.min = min;
		this.max = max;
		
		this.pos = new Vector3f(0,0,0);
	}
	
	
	
	/**
	 * Set the hitbox position in the game world
	 * 
	 * @param position
	 * @return
	 */
	public void setPosition(Vector3f position) {
		
		pos = position;
	}
	
	
	
	/**
	 * Render the hitbox
	 */
	public void render() {
		
		if(render) {

			// Cube verts for rendering
			float[] verts = {
				min.x, min.y, max.z,	min.x, max.y, max.z,	max.x, max.y, max.z,	max.x, min.y, max.z,	// Front
				min.x, min.y, min.z,	min.x, min.y, max.z,	max.x, min.y, max.z,	max.x, min.y, min.z,	// Base
				max.x, min.y, min.z,	max.x, max.y, min.z,	min.x, max.y, min.z,	min.x, min.y, min.z,	// Back
				min.x, max.y, max.z,	min.x, max.y, min.z,	max.x, max.y, min.z,	max.x, max.y, max.z		// Top
			};
			
			glPushMatrix();
				glTranslatef(pos.x, pos.y, pos.z);
				glColor3f(1f,1f,1f);
				
				for(int i=0; i<verts.length; i+=12) {
					glBegin(GL_LINE_LOOP);
						for(int j=0; j<12; j+=3) {
							glVertex3f(verts[i+j], verts[i+j+1], verts[i+j+2]);
						}
					glEnd();
				}

			glPopMatrix();
		}
	}
	
	
	/**
	 * Scale the hitbox by a scale factor
	 * @param scale
	 */
	public void setScale(float scale) {
		min = (Vector3f) min.scale(scale);
		max = (Vector3f) max.scale(scale);
	}
	
	
	/**
	 * Debug a collision between two hitboxes. 
	 * Usually need this when elements of min/max are ordered incorrectly
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean debugCollision(Hitbox a, Hitbox b) {

		// Debug collision

		if( (a.pos.x + a.min.x < b.pos.x + b.max.x) && (a.pos.x + a.max.x > b.pos.x + b.min.x))
			System.out.println("PASS HITLVL1_X "
					+ (a.pos.x + a.min.x) + " < " + (b.pos.x + b.max.x) + "  &&  "
					+ (a.pos.x + a.max.x) + " > " + (b.pos.x + b.min.x));
		else
			System.out.println("FAIL HITLVL1_X "
					+ (a.pos.x + a.min.x) + " < " + (b.pos.x + b.max.x) + "  &&  " 
					+ (a.pos.x + a.max.x) + " > " + (b.pos.x + b.min.x));
		
		if(	(a.pos.y + a.min.y < b.pos.y + b.max.y) && (a.pos.y + a.max.y > b.pos.y + b.min.y)) 
			System.out.println("PASS HITLVL2_Y "
					+ (a.pos.y + a.min.y) + " < " + (b.pos.y + b.max.y) + "  &&  " 
					+ (a.pos.y + a.max.y) + " > " + (b.pos.y + b.min.y));
		else 
			System.out.println("FAIL HITLVL2_Y "
					+ (a.pos.y + a.min.y) + " < " + (b.pos.y + b.max.y) + "  &&  " 
					+ (a.pos.y + a.max.y) + " > " + (b.pos.y + b.min.y)); 
		
		if(	(a.pos.z + a.min.z < b.pos.z + b.max.z) && (a.pos.z + a.max.z > b.pos.z + b.min.z))
			System.out.println("PASS HITLVL3_Z "
					+ (a.pos.z + a.min.z) + " < " + (b.pos.z + b.max.z) + "  &&  " 
					+ (a.pos.z + a.max.z) + " > " + (b.pos.z + b.min.z));
		else
			System.out.println("FAIL HITLVL3_Z "
					+ (a.pos.z + a.min.z) + " < " + (b.pos.z + b.max.z) + "  &&  " 
					+ (a.pos.z + a.max.z) + " > " + (b.pos.z + b.min.z));

		
		System.out.println();
		return checkCollision(a, b);
	}
	
	
	/**
	 * Check for 3D collision between two hitboxes
	 * 
	 * Useful link: http://www.euclideanspace.com/threed/animation/collisiondetect/index.htm
	 * 
	 * @param a
	 * @param b
	 * @return true/false
	 */
	
	public static boolean checkCollision(Hitbox a, Hitbox b) {
		
		if( ((a.pos.x + a.min.x < b.pos.x + b.max.x) && (a.pos.x + a.max.x > b.pos.x + b.min.x)) &&
			((a.pos.y + a.min.y < b.pos.y + b.max.y) && (a.pos.y + a.max.y > b.pos.y + b.min.y)) &&
			((a.pos.z + a.min.z < b.pos.z + b.max.z) && (a.pos.z + a.max.z > b.pos.z + b.min.z)) ) {
			
			return true;
		}
		return false;
	}
	
	
	/**
	 * Copy parameters into a new hitbox
	 * @param h
	 * @return
	 */
	public static Hitbox copy(Hitbox h) {
		return new Hitbox(new Vector3f(h.min.x, h.min.y, h.min.z), new Vector3f(h.max.x, h.max.y, h.max.z));
	}
	
	
	/**
	 * Check 2d line intersection with the side planes of a hitbox
	 * 
	 * Implemented in 2d plane ONLY
	 * @param hit
	 * @param Vector2f start
	 * @param Vector2f end
	 * @return true if lines intersect, false if they do not.
	 */
	public static boolean checkIntersection(Hitbox hit, Vector2f start, Vector2f end) {
		
		// Extract 4 lines for the edges of the hitbox. z==y because of screwy plane implementation
		Vector2f[][] sides = {
				{ new Vector2f(hit.min.x, hit.min.z), new Vector2f(hit.max.x, hit.min.z) },
				{ new Vector2f(hit.max.x, hit.min.z), new Vector2f(hit.max.x, hit.max.z) }, 
				{ new Vector2f(hit.max.x, hit.max.z), new Vector2f(hit.min.x, hit.max.z) }, 
				{ new Vector2f(hit.min.x, hit.max.z), new Vector2f(hit.min.x, hit.min.z) }
		};
		
		try {
			for (Vector2f[] side : sides) {
				Vector2f[] res = intersect.intersection(side[0], side[1], start, end);
				
//				System.out.println(
//						"Intersect test:\n\t a1" + side[0]
//						+ "\n\t a2"	+ side[1]
//						+ "\n\t b1" + start
//						+ "\n\t b2"	+ end
//						+ "\n\t " + ((res.length > 0) ? "Intersection" : ""));
				
				if(res.length > 0)	// Found an intersection and returned its point
					return true;
			}
		} catch (IntersectException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
