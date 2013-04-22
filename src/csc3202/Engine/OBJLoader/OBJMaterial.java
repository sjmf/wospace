
package csc3202.Engine.OBJLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;

import csc3202.Engine.TextureManager;

/**
 * A material based on the .mtl material specification 
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Dec 2012
 */
public class OBJMaterial {

	private static final Vector3f KDEF = new Vector3f(1f,1f,1f);
	
	private String name;
	
	private Vector3f ambient = KDEF; 			// Ambient light colour rgb Ka
	private Vector3f diffuse = KDEF; 			// Diffuse light colour rgb Kd
	private Vector3f specular = KDEF; 			// Specular light colour rgb Ks
	private float spec_coeff = 1f; 				// Specular coefficient range
	private float alpha_trans = 1f;				// Transparency alpha d
	
//	private int illumination = 2; 				// Illumination model (nope!)

	private Texture texture; 					// Texture
	private String tex_path;					// Path of texture for re-loading
	

	/**
	 * An OBJMaterial is initialised with default values (other than name),
	 *  which are overwritten if the .mtl definition includes data about
	 *  them specifically
	 */
	public OBJMaterial(String name) {
		this.name = name;
	}

	
	
	/**
	 * @return the ambient
	 */
	public Vector3f getAmbient() {
		return ambient;
	}



	/**
	 * @param ambient the ambient to set
	 */
	public void setAmbient(Vector3f ambient) {
		this.ambient = ambient;
	}



	/**
	 * @return the diffuse
	 */
	public Vector3f getDiffuse() {
		return diffuse;
	}



	/**
	 * @param diffuse the diffuse to set
	 */
	public void setDiffuse(Vector3f diffuse) {
		this.diffuse = diffuse;
	}



	/**
	 * @return the specular
	 */
	public Vector3f getSpecular() {
		return specular;
	}



	/**
	 * @param specular the specular to set
	 */
	public void setSpecular(Vector3f specular) {
		this.specular = specular;
	}



	/**
	 * @return the spec_coeff
	 */
	public float getSpecCoeff() {
		return spec_coeff;
	}



	/**
	 * @param spec_coeff the spec_coeff to set
	 */
	public void setSpecCoeff(float spec_coeff) {
		this.spec_coeff = spec_coeff;
	}



	/**
	 * @return the alpha_trans
	 */
	public float getAlphaTrans() {
		return alpha_trans;
	}



	/**
	 * @param alpha_trans the alpha_trans to set
	 */
	public void setAlphaTrans(float alpha_trans) {
		this.alpha_trans = alpha_trans;
	}



	/**
	 * @return the texture
	 */
	public Texture getTexture() {
		return texture;
	}



	/**
	 * @param texture the texture to set
	 */
	public void setTexture(String tex_path,Texture texture) {
		
		this.tex_path = tex_path;
		this.texture = texture;
	}



	/**
	 * @return the tex_path
	 */
	public String getTexturePath() {
		return tex_path;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	
	
	/**
	 * Parse and load a .mtl material file into OBJMaterial objects
	 * @param path
	 * @return
	 */
	public static HashMap<String, OBJMaterial> load(String path) {
		
		System.out.println("Material " + path);

		HashMap<String, OBJMaterial> materials = new HashMap<String, OBJMaterial>();			// Materials stored against names
		BufferedReader in = null;
		int line_no = 0;																		// Line number for error handling
		int mtl_count = 0;

		try {																					// Load the model using a BufferedReader
			in = new BufferedReader(new FileReader(path));
			
			String line = null;
    		OBJMaterial mtl = null;
	        while ((line = in.readLine()) != null) {
	        	
	        	line_no++;
	        	
	        	if (line.startsWith("#"))														// Skip comments
	        		continue;
	        	if (line.trim().isEmpty())														// and whitespace
					continue;
	        	
	        	String[] values = line.split(" ");
	        	
	        	if (line.startsWith("newmtl")) {												// Begin parsing material (initialised with defaults)
	        		
	        		if(values.length > 1) {														// Set-up next material
	        			mtl = new OBJMaterial(values[1]);										// values[1] will contain name
	        		} else {
	        			mtl = new OBJMaterial("");												// No name. Assume only one material. This won't barf.
	        		}
	        		
	        		materials.put(mtl.getName(), mtl);
	        		
	        		mtl_count++;
	        	}												        						// Read commands beginning with ambient. 
	        	
	        	else if(line.startsWith("Ka ")) {												// Ambient colour rgb Ka
	        		
	        		mtl.setAmbient(new Vector3f(
        					Float.valueOf(values[1]),											// Ka 0.000000 0.000000 0.000000
        					Float.valueOf(values[2]),
        					Float.valueOf(values[3])
        				));
	        	}
	        	else if(line.startsWith("Kd ")) {												// Diffuse colour rgb Kd 
	        		
	        		mtl.setDiffuse(new Vector3f(
        					Float.valueOf(values[1]),											// Kd 0.000000 0.000000 0.000000
        					Float.valueOf(values[2]),
        					Float.valueOf(values[3])
        				));
	        	}
	        	else if(line.startsWith("Ks ")) {												// Specular colour rgb Ks
	        		
	        		mtl.setSpecular(new Vector3f(
        					Float.valueOf(values[1]),											// Ks 0.000000 0.000000 0.000000
        					Float.valueOf(values[2]),
        					Float.valueOf(values[3])
        				));
	        	}
	        	else if(line.startsWith("Ns ")) {												// Specular coefficient (range)
	        		
	        		mtl.setSpecCoeff(Float.valueOf(values[1]));
	        	}
	        	else if(line.startsWith("d ") || line.startsWith("Tr ")) {						// Transparency (alpha)
	        		
	        		mtl.setAlphaTrans(Float.valueOf(values[1]));
	        	}
	        	else if(line.startsWith("map_Ka ") 												// Ambient and diffuse textures are treated as the same thing in this implementation
	        			|| line.startsWith("map_Kd ")) {

	        		if( values.length < 2 )
	        			continue;
	        		
	    			if(values[1].lastIndexOf('/') == -1)	// If we have a relative path, make it relative to the java executable root
	    				values[1] = path.substring(0, path.lastIndexOf('/') +1) + values[1];
	    				
	        		mtl.setTexture(
	        				values[1],
	        				TextureManager.getManager().getTexture(values[1])					// Load texture in- introduces lag with large textures :(
	        			);
	        	}
//	        	else  {																			// Some .mtl commands are unsupported due to time constraints.
//	        		System.out.println("Unsupported .mtl command: " + line);					// Unsupported commands: Ni, illum, map_bump, map_d, bump, displ, decal, refl and probably quite a few others
//	        	}
	        }
	        
	        System.out.println(mtl_count + " material(s) loaded from " + path);
	        
	        in.close();
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error in file " + path + " on line " + line_no);
			System.err.println(".mtl file not in correct format or corrupt?");
			e.printStackTrace();
		}
		
		return materials;
	}
}
