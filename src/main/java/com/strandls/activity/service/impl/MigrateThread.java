/**
 * 
 */
package com.strandls.activity.service.impl;

import com.google.inject.Inject;
import com.strandls.activity.service.ActivityService;

/**
 * @author Abhishek Rudra
 *
 */
public class MigrateThread implements Runnable {

	@Inject
	ActivityService service;

	@Override
	public void run() {
		service.migrateData();

	}

}
