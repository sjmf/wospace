package csc3202.Engine;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

/**
 * A simple first-person camera, with a little help from
 * http://www.lloydgoodall.com/tutorials/first-person-camera-control-with-lwjgl/
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class Camera {

	private final float MAX_PITCH_UP = 90;
	private final float MAX_PITCH_DOWN = -90;
	
	private final float MOVEMENT_SPEED = 0.05f;
	private final float SENSITIVITY = 0.05f;

	private Vector3f xyz_pos = null;		// xyz coordinate in 3d space
	private Vector3f pyr_pos = null;		// pitch/yaw/roll of the camera

	private boolean key_left;
	private boolean key_right;
	private boolean key_up;
	private boolean key_down;
	private boolean key_rise;
	private boolean key_fall;
	
	private boolean lock_camera = true;

	
	/**
	 * Constructor
	 */
	public Camera() {
		xyz_pos = new Vector3f(0, -10, 0);
	}

	
	
	/**
	 * Update the camera movement speed delta
	 * @param delta
	 */
	public void update(long delta) {
		
		if(lock_camera)
			return;
		
//		if( (System.currentTimeMillis() / 500) % 2 == 0 ) {
//			
//			System.out.println(xyz_pos.x + "/" + xyz_pos.y + "/" + xyz_pos.z 
//										+ "/" + pyr_pos.x + "/" + pyr_pos.y);
//		}
		
		if(key_left)
			left(MOVEMENT_SPEED * delta);
		if(key_right)
			right(MOVEMENT_SPEED * delta);
		if(key_up)
			forward(MOVEMENT_SPEED * delta);
		if(key_down)
			backward(MOVEMENT_SPEED * delta);
		if(key_rise)
			up(MOVEMENT_SPEED * delta);
		if(key_fall)
			down(MOVEMENT_SPEED * delta);
	}
	
	

	/** 
	 * "Move" the camera
	 * (Technically, just applying a big translation
	 *  to everything in the Modelview Matrix)
	 */
	public void updateView() {
		
		glRotatef(pyr_pos.x, 1.0f, 0.0f, 0.0f);  			// Rotate the pitch around the X axis							                                                    	
		glRotatef(pyr_pos.y, 0.0f, 1.0f, 0.0f);    			// Rotate the yaw around the Y axis 						                                                    	
		glRotatef(pyr_pos.z, 0.0f, 0.0f, 1.0f);    			// Rotate the roll around the Z axis        							                                                    	
		glTranslatef(xyz_pos.x, xyz_pos.y, xyz_pos.z);   	// translate to the position vector's location
	}
	
	

	/** 
	 * Handle keyboard input passed down from the Engine 
	 */
	public void keyInput(int key) {

		if (Keyboard.getEventKeyState()) {
			switch (key) {
			case Keyboard.KEY_LEFT:
			case Keyboard.KEY_A:
				key_left = true;
				break;
			case Keyboard.KEY_RIGHT:
			case Keyboard.KEY_D:
				key_right = true;
				break;
			case Keyboard.KEY_UP:
			case Keyboard.KEY_W:
				key_up = true;
				break;
			case Keyboard.KEY_DOWN:
			case Keyboard.KEY_S:
				key_down = true;
				break;
			case Keyboard.KEY_LSHIFT:
			case Keyboard.KEY_RSHIFT:
				key_rise = true;
				break;
			case Keyboard.KEY_LCONTROL:
			case Keyboard.KEY_RCONTROL:
				key_fall = true;
				break;
			default:
				break;
			}
		} else {	// Key-up handling
			switch (key) {
				case Keyboard.KEY_LEFT:
				case Keyboard.KEY_A:
					key_left = false;
					break;
				case Keyboard.KEY_RIGHT:
				case Keyboard.KEY_D:
					key_right = false;
					break;
				case Keyboard.KEY_UP:
				case Keyboard.KEY_W:
					key_up = false;
					break;
				case Keyboard.KEY_DOWN:
				case Keyboard.KEY_S:
					key_down = false;
					break;
				case Keyboard.KEY_LSHIFT:
				case Keyboard.KEY_RSHIFT:
					key_rise = false;
					break;
				case Keyboard.KEY_LCONTROL:
				case Keyboard.KEY_RCONTROL:
					key_fall = false;
					break;
				default:
					break;
			}
		}
	}

	
	
	/**
	 * Handle mouse input passed down from the Engine 
	 */
	public void mouseInput() {

		if(lock_camera)
			return;

		pyr_pos.x -=  Mouse.getDY() * SENSITIVITY;
		pyr_pos.x %= 360;
		
		pyr_pos.y += Mouse.getDX() * SENSITIVITY;
		pyr_pos.y %= 360;

		if (pyr_pos.x > MAX_PITCH_UP)
			pyr_pos.x = MAX_PITCH_UP;
		else if (pyr_pos.x < MAX_PITCH_DOWN)
			pyr_pos.x = MAX_PITCH_DOWN;
	}

	
	
	/**
	 * @return the pos
	 */
	public Vector3f getPos() {
		return xyz_pos;
	}
	
	
	
	/**
	 * Toggle camera locked/free
	 */
	public void toggleLock() {
		lock_camera = !lock_camera;
	}
	
	
	
	public void lock() {
		lock_camera = true;
	}

	public void unlock() {
		lock_camera = false;
	}
	
	
	/**
	 * @return the pos
	 * @param xyz Vector3f - the xyz position of the camera
	 * @param pyr Vector3f - pitch/yaw/roll of the camera 
	 */
	public void setCameraPos(Vector3f xyz, Vector3f pyr) {
		this.xyz_pos = xyz;
		this.pyr_pos = pyr;
	}

	
	
	/** Move the camera forward relative to its current rotation (yaw) **/
	public void forward(float distance) {
		xyz_pos.x -= distance * (float) Math.sin(Math.toRadians(pyr_pos.y));
		xyz_pos.z += distance * (float) Math.cos(Math.toRadians(pyr_pos.y));
	}

	
	/** Move the camera backward relative to its current rotation (yaw) **/
	public void backward(float distance) {
		xyz_pos.x += distance * (float) Math.sin(Math.toRadians(pyr_pos.y));
		xyz_pos.z -= distance * (float) Math.cos(Math.toRadians(pyr_pos.y));
	}
	
	

	/** Strafe the camera left relative to its current rotation (yaw) **/
	public void left(float distance) {
		xyz_pos.x -= distance * (float) Math.sin(Math.toRadians(pyr_pos.y - 90));
		xyz_pos.z += distance * (float) Math.cos(Math.toRadians(pyr_pos.y - 90));
	}

	
	
	/** Strafe the camera right relative to its current rotation (yaw) **/
	public void right(float distance) {
		xyz_pos.x -= distance * (float) Math.sin(Math.toRadians(pyr_pos.y + 90));
		xyz_pos.z += distance * (float) Math.cos(Math.toRadians(pyr_pos.y + 90));
	}

	
	
	/** Fly the camera up relative to its current rotation (yaw) **/
	public void up(float distance) {
		xyz_pos.y -= distance;
	}

	
	
	/** Fly the camera down relative to its current rotation (yaw) **/
	public void down(float distance) {
		xyz_pos.y += distance;
	}
}
