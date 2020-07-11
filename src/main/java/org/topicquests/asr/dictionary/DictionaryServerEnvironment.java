/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary;

import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.asr.dictionary.server.DictionaryPostgresModel;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import org.topicquests.support.RootEnvironment;

/**
 * @author jackpark
 *
 */
public class DictionaryServerEnvironment extends RootEnvironment {
	private boolean isShutDown = false;
	private PostgresConnectionFactory database = null;
	private IPostgresDictionary pgDictionary;
	private IDictionaryServerModel pgModel;

	/**
	 * 
	 */
	public DictionaryServerEnvironment() {
		super("config-props.xml", "logger.properties");
		String schemaName = getStringProperty("DictionaryDatabaseSchema");
		String dbName = getStringProperty("DictionaryDatabaseName");
		database = new PostgresConnectionFactory(dbName, schemaName);
		logDebug("DictionaryServerEnvironment- "+dbName+" "+schemaName);
		isShutDown = false;
		pgDictionary = new PostgresDictionary(this);
		pgModel = new DictionaryPostgresModel(this, pgDictionary);
       // System.out.println("ENV-1 "+model);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			@Override
			public void run() {
				shutDown();
			}
		});

	}
	
	public IDictionaryServerModel getPostgresModel() {
		return pgModel;
	}
	public IPostgresDictionary getPostgresDictionary() {
		return pgDictionary;
	}
	
	public PostgresConnectionFactory getPostgresFactory() {
		return database;
	}
	
	/*public IPersistentDictionary getDictionary() {
		return dictionary;
	}*/
	
	
	/*public IDictionaryServerModel getModel() {
		logDebug("GetModel");
		return model;
	}*/
		
	public void shutDown() {
		logDebug("DictionaryServerEnvironment.shutDown "+isShutDown);
		if (!isShutDown) {
			isShutDown = true;
			System.out.println("DictionaryServerEnvironment.shutDown");
			pgModel.shutDown();
			//if (model != null)
			//	model.shutDown();
			//stopper.shutDown();
		}
	}


}
