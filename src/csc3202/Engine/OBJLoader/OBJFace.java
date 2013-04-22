/**
 * 
 */
package csc3202.Engine.OBJLoader;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Represent a face of the object model loaded
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Dec 2012
 */
public class OBJFace {

	private Vector3f[] face_verts;
	private Vector3f[] face_normals;
	private Vector2f[] tex_coords;
	
	private OBJMaterial material;
	
	/**
	 * Construct a face with properties:
	 */
	public OBJFace( Vector3f[] face_verts, 
					Vector3f[] face_normals, 
					Vector2f[] tex_coords, 
					OBJMaterial material ) {
		
		this.face_verts = face_verts;
		this.face_normals = face_normals;
		this.tex_coords = tex_coords;
		this.material = material;
	}

	
	
	/**
	 * @return the face_verts
	 */
	public Vector3f[] getVertices() {
		return face_verts;
	}
	
	

	/**
	 * @return the face_normals
	 */
	public Vector3f[] getNormals() {
		return face_normals;
	}

	
	
	/**
	 * @return the tex_coords
	 */
	public Vector2f[] getTextureCoords() {
		return tex_coords;
	}

	
	
	/**
	 * @return the material
	 */
	public OBJMaterial getMaterial() {
		return material;
	}
}
