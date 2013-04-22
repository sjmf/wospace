package csc3202.Engine.Interfaces;

public interface DisplayList {

	/**
	 * Sets this model to DisplayList mode (GPU)
	 * Once set cannot be reverted.
	 */
	public abstract void useDisplayList();

	/**
	 * Render the model
	 */
	public abstract void render();

	/**
	 * Draw the Skybox cube
	 */
	public abstract void draw();

}