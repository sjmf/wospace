package csc3202.Engine.OBJLoader;

import java.util.HashMap;
import java.util.Iterator;

import csc3202.Engine.Engine;


/**
 * Simple Object manager implemented as a Singleton
 * 
 * Instantiating a million model objects simply doesn't make sense.
 * Therefore, manage them with this class.
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
public class OBJManager {

	private static OBJManager instance;
	
	// Map of model size to model object
	private HashMap<String, OBJModel> models;

	private static Engine engine;
	
	private static boolean pre_loaded = false;
	
	
	/**
	 * Singleton Constructor
	 * Instantiate a modelManager, and set up the model to use
	 */
	private OBJManager() {

		models = new HashMap<String, OBJModel>();
	}
	
	
	
	/**
	 * Get (instantiating if need be) the model at the given path
	 * @param path
	 * @return OBJModel
	 */
	public synchronized OBJModel getModel(String path) {
		
		OBJModel model = models.get(path);
		
		if(model == null) {		// Then load the model!

			model = OBJModel.load(path);
			models.put(path, model);

			engine.registerTexturable(model);
		}
		
		return model;
	}
	
	
	
	/**
	 * Tell all the loaded models to render from display lists
	 */
	public void useDisplayLists() {
		
		Iterator<OBJModel> it = models.values().iterator();
		
		while(it.hasNext()) {

			OBJModel model = it.next();
			model.useDisplayList();
			engine.registerList(model);
		}
	}
	
	
	
	/**
	 * Set the engine in which to register displayLists
	 * (and textures later hopefully)
	 * @param e
	 */
	public static void setEngine(Engine e) {
		engine = e;
	}
	
	
	/**
	 * Get the singleton model manager instance
	 * @return
	 */
	public static synchronized OBJManager getManager() {
		
		if(instance == null) {
			instance = new OBJManager();
		}
		
		return instance;
	}
	
	
	/**
	 * Return true if the loader thread has finished
	 * @return
	 */
	public synchronized boolean isLoaded() {
		return pre_loaded;
	}
	
	
	/**
	 * Set true when the loader thread has finished
	 * @return
	 */
	public synchronized void setLoaded() {
		pre_loaded = true;
	}
}
