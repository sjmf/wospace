package csc3202.Engine.AI;
/**
 * Write a runnable enemy spawner with this interface
 */

import java.util.Observable;

import csc3202.Entities.Enemy;


public abstract class Spawner extends Observable implements Runnable {

	/* 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public abstract void run();

	/**
	 * Stop spawning enemies
	 */
	public abstract void stop();
	
	/**
	 * Spawn an enemy or two.
	 * Override this method in a class and define custom behaviour
	 */
	public abstract Enemy[] makeEnemies();

}