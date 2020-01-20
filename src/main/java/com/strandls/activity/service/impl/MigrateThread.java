/**
 * 
 */
package com.strandls.activity.service.impl;

/**
 * @author Abhishek Rudra
 *
 */
public class MigrateThread extends ActivityServiceImpl implements Runnable {

	@Override
	public void run() {
		migrateData();

	}

}
