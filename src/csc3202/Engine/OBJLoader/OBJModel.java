package csc3202.Engine.OBJLoader;

import static org.lwjgl.opengl.GL11.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.TextureImpl;

import csc3202.Engine.Hitbox;
import csc3202.Engine.TextureManager;
import csc3202.Engine.Interfaces.DisplayList;
import csc3202.Engine.Interfaces.Texturable;


/**
 * OBJ Model file and loader. Inspired by:
 * 	[1] https://github.com/OskarVeerhoek/YouTube-tutorials/blob/master/src/utility/OBJLoader.java
 * 	[2] http://fabiensanglard.net/Mykaruga/index.php (WavefrontObject.java)
 * 
 * Limitations:
 * 		Normals MUST be included in the model file.
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Dec 2012
 */
public class OBJModel implements DisplayList, Texturable {
	
	private String path;
	private OBJFace[] faces;
	
	private boolean displayListOn = false;
	private int displayList = 0;
	
	// Size parameters. Calculated on model load.
	private Vector3f min;	// Minimum x/y/z corner
	private Vector3f max;	// Maximum x/y/z corner
	private Vector3f rot;	// Rotation
	
	private Hitbox hit;
	
	private boolean blend = false;
	
	
	/**
	 * Construct a 3D model representing an OBJ file
	 * 
	 * @param faces
	 * @param materials
	 */
	public OBJModel( OBJFace[] faces, String path ) {
		
		this.path = path;
		this.faces = faces;
		this.rot = new Vector3f();
	}
	
	
	/**
	 * 
	 * @param blend
	 */
	public void alphaBlend(boolean blend) {
		this.blend = blend; 
	}
	
	
	/**
	 * Sets this model to DisplayList mode (GPU)
	 * Once set cannot be reverted.
	 */
	@Override
	public void useDisplayList() {
		
		displayListOn = true;
		
		displayList = glGenLists(1);
        glNewList(displayList, GL_COMPILE);
        	draw();
        glEndList();
	}
	
	
	/**
	 * Render the model
	 */
	@Override
	public void render() {
		
		glPushMatrix();
			glRotatef(rot.x, 0, 1, 0);
			
			if(displayListOn) {
		        glCallList(displayList);
			} else {
				draw();
			}
		glPopMatrix();
	}
	
	
	
	/**
	 * Draw the model
	 * - Can be called once in display list mode, or every time to render on CPU
	 */
	@Override
	public void draw () {
		
		Vector3f[] vertices;
		Vector3f[] normals;
		Vector2f[] tx_coord;
		OBJMaterial material = new OBJMaterial("empty");
		OBJFace face = null;
		
		TextureImpl.bindNone();								// Release bindings for other textures (i.e. models) in Slick
		
		for (int i = 0; i < faces.length; i++) {
			
			face = faces[i];
			
			vertices = face.getVertices();
			normals = face.getNormals();
			tx_coord = face.getTextureCoords();
			
			if ( face.getMaterial() != null ) {
				if(!material.equals(face.getMaterial()) ) {
				
					material = face.getMaterial();
//					System.out.println("Tex " + material.getName());
					
					if (material.getTexture() != null) {
						material.getTexture().bind();
					}
					
					// WARNING: Models with this property will not alpha blend correctly
					if (!blend && material.getDiffuse() != null) {
						glColor3f(material.getDiffuse().x, material.getDiffuse().y, material.getDiffuse().z);
					}
				}
			}
			else {
				if(material != null && material.getTexture() != null)
					material.getTexture().release();
			}
			
			if(vertices.length == 3) {		// Begin drawing correct polygon
				glBegin(GL_TRIANGLES);
			} else if(vertices.length == 4) {
				glBegin(GL_QUADS);
			} else {
				glBegin(GL_POLYGON);
			}
			
			for (int j=0; j<vertices.length; j++) {
				
				if (j < normals.length &&  normals[j] != null)
					GL11.glNormal3f(normals[j].x, normals[j].y, normals[j].z);
				
				if (j < tx_coord.length && tx_coord[j] != null )
					GL11.glTexCoord2f(tx_coord[j].x, (1.0f - tx_coord[j].y));
				
				GL11.glVertex3f(vertices[j].x, vertices[j].y, vertices[j].z);
			}
			
			glEnd();
		}
		
	}


	/**
	 * @return the width
	 */
	public float getWidth() {
		return max.x - min.x;
	}


	/**
	 * @return the height
	 */
	public float getHeight() {
		return max.y - min.y;
	}


	/**
	 * @return the depth
	 */
	public float getDepth() {
		return max.z - min.z;
	}
	
	
	/**
	 * Get the hitbox for this model
	 * @return
	 */
	public Hitbox getDefaultHitbox() {
		return hit;
	}


	/**
	 * @param min the min to set
	 */
	public void setMin(Vector3f min) {
		this.min = min;
	}


	/**
	 * @param max the max to set
	 */
	public void setMax(Vector3f max) {
		this.max = max;
	}
	
	
	/**
	 * Set the hitbox for this model
	 * @param hit
	 */
	public void setDefaultHitbox(Hitbox hit) {
		this.hit = hit;
	}

	

	/**
	 * 
	 * @return
	 */
	public Vector3f getRot() {
		return rot;
	}

	
	
	/**
	 * 
	 * @param rot
	 */
	public void setRot(Vector3f rot) {
		this.rot = rot;
	}



	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	


	/**
	 * Load an OBJ file from the path set in this class.
	 * 
	 * @return OBJModel
	 */
	public static OBJModel load(String path) {

		final String separator = "/";
		
		int face_ctr = 0;													// Poly counter
		int line_no = 0;	// Line number for error handling
		
		BufferedReader in = null;
		String line = null;
		
		ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
		ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
		ArrayList<Vector2f> coordinates = new ArrayList<Vector2f>();
		ArrayList<String> face_definitions = new ArrayList<String>();
		ArrayList<OBJFace> obj_faces = new ArrayList<OBJFace>();
		HashMap<String, OBJMaterial> materials = null;
		
		OBJMaterial current_mtl = null;
		
		// Space coordinates used to create hitbox
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		
		try {
			in = new BufferedReader(new FileReader(path));										// Load the model using a BufferedReader
			
	        while ((line = in.readLine()) != null) {
	        	
	        	line_no++;
	        	
	        	if (line.startsWith("#"))														// Skip comments
	        		continue;
	        	
	        	String[] values = line.split(" ");
	        	
	        	if (line.startsWith("v ")) {													// Vertices
	        		 
	        		Vector3f vertex = new Vector3f(
    						Float.valueOf(values[1].trim()),
    						Float.valueOf(values[2].trim()),
    						Float.valueOf(values[3].trim())
    					);
	        		
        			vertices.add(vertex);
	        		
	        		if(vertex.x < min.x)	min.x = vertex.x;									// Perform space coordinate calculations (used for hitbox construction)
	        		if(vertex.y < min.y)	min.y = vertex.y;
	        		if(vertex.z < min.z)	min.z = vertex.z;

	        		if(vertex.x > max.x)	max.x = vertex.x;
	        		if(vertex.y > max.y)	max.y = vertex.y;
	        		if(vertex.z > max.z)	max.z = vertex.z;
	        	}
	        	else if (line.startsWith("vn ")) {												// Normals
	        		
	        		normals.add( 
	        			new Vector3f(
	        				Float.valueOf(values[1].trim()),
	        				Float.valueOf(values[2].trim()),
	        				Float.valueOf(values[3].trim())
	        			));
	        	}
	        	else if (line.startsWith("vt ")) {												// Texture coordinates 
	        		
	        		coordinates.add(
	        			new Vector2f(
		        				Float.valueOf(values[1].trim()),
		        				Float.valueOf(values[2].trim())
	        			));
	        	}
	        	else if (line.startsWith("f ")) {												// Face (vertices)- store and process after fully loaded
	        		face_definitions.add(line);
	            }
	        	else if(line.startsWith("usemtl ")) {											// Material to use. Will be parsed when loading faces 
	        		face_definitions.add(line);													// Assume mtllib has been loaded (will NullPointer otherwise).
	        	}
	        	else if(line.startsWith("mtllib ")) {											// .mtl file for current Material

	        		String mtlPath;
	        		
	        		int rel = path.lastIndexOf('/');
	        		if ( rel != -1)
	        			mtlPath = path.substring(0,rel+1) + values[1];							// Get path of mtllib relative to running directory
	        		else
	        			mtlPath = values[1];
	        		
	        		materials = OBJMaterial.load(mtlPath);
	        	}
	        }
	        
	        
	        Iterator<String> it = face_definitions.iterator();									// Process faces
	        
    		while(it.hasNext()){
    			
    			line = it.next();		// This was defined up-top
    			String[] values = line.split(" ");
	    		String[] raw;
	    		Vector3f[] face_verts	= new Vector3f[values.length -1];
				Vector3f[] face_normals = new Vector3f[values.length -1];
				Vector2f[] text_coords   = new Vector2f[values.length -1];
				
				if(line.startsWith("f ")) {
    				for (int i=0; i < values.length -1; i++) {
		    			raw = values[i+1].split(separator);
		    			
		    			if(Integer.parseInt(raw[0]) -1 > vertices.size())						// Array bounds checking
		    				continue;
		    			
		    			face_verts[i] = vertices.get(Integer.parseInt(raw[0]) -1);				// Resolve reference to vector (i.e. 8225 -> x/y/z)
		    			
		    			if(! raw[1].isEmpty())
		    				text_coords[i] = coordinates.get(Integer.parseInt(raw[1]) -1);
		    			
		    			face_normals[i] = normals.get(Integer.parseInt(raw[2]) -1);
    	    		}
    				
    	    		face_ctr++;
    	    		obj_faces.add( new OBJFace( face_verts, face_normals, text_coords, current_mtl ));
    	    		
    			} else if(line.startsWith("usemtl")) {
    				if(values.length > 1)													
	        			current_mtl = materials.get(values[1].trim());
    				else
    					current_mtl = null;
    			}
	    		
	    		it.remove();
    		}
	        
	        in.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error in file " + path + " on line " + line_no);
			System.err.println(".obj model not in correct format or corrupt?");
			System.err.println(line);
		}
		
		System.out.println("Model " + path + ": " + face_ctr + " faces");
		
		OBJModel model = new OBJModel( obj_faces.toArray(new OBJFace[obj_faces.size()]), path );		// Create the model
		
		model.setDefaultHitbox(new Hitbox(min,max));
		model.setMax(max);
		model.setMin(min);
		
		return model;
	}


	@Override
	public void loadTextures() {
		
		String path;
		OBJMaterial mat;
		
		for(OBJFace face : faces) {

			mat = face.getMaterial();
			
			if(mat == null)	continue;	// Skip faces with no material
			
			path = face.getMaterial().getTexturePath();
			
			if(path != null && !path.isEmpty()) {
				try {
					face.getMaterial().setTexture(
							path, 
							TextureManager.getManager().getTexture(path)						// Note- this will reload from file.
						);																		// TODO: Find a way to keep image in memory for faster reload
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return;
	}
}
