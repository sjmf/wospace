package csc3202.Engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * Simple Texture manager implemented as a Singleton
 * 
 * Loading textures more than once is crazy inefficient.
 * Just load 'em once in this manager and do all the mipmap stuff at the same time
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
public class TextureManager {

	private static TextureManager instance;
	
	private HashMap<String, Texture> textures;
	
	
	/**
	 * Singleton Constructor
	 */
	private TextureManager() {

		textures = new HashMap<String, Texture>();
	}
	
	
	
	/**
	 * Get (lazy-loading if need be) the texture at the given path
	 * @param path
	 * @return OBJModel
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Texture getTexture(String path) throws FileNotFoundException, IOException {
		
		Texture tex = textures.get(path);
		
		if(tex == null) {		// Then load the model!
			String ext = path.substring(path.lastIndexOf(".") +1, path.length()).toUpperCase();		// Check file type by splitting off extension
			
			System.out.println(ext + " image " + path);
			
			tex = TextureLoader.getTexture( ext, new FileInputStream(path) );
			
			// Create MipMaps for texture
			glBindTexture(GL_TEXTURE_2D, tex.getTextureID());
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
			glGenerateMipmap(GL_TEXTURE_2D);
			
			textures.put(path, tex);
		}
		
		return tex;
	}
	
	
	
	/**
	 * Clear all textures from the manager
	 */
	public void clear() {
		textures.clear();
	}

	
	
	/**
	 * Get the singleton model manager instance
	 * @return
	 */
	public static TextureManager getManager() {
		
		if(instance == null) {
			instance = new TextureManager();
		}
		
		return instance;
	}
}
