/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary;

import org.topicquests.os.asr.StatisticsHttpClient;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.asr.dictionary.server.DictionaryServerModel;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.asr.dictionary.server.api.IPersistentDictionary;
import org.topicquests.support.RootEnvironment;

/**
 * @author jackpark
 *
 */
public class DictionaryServerEnvironment extends RootEnvironment {
	private boolean isShutDown = false;
	private IPersistentDictionary dictionary;
	private IDictionaryServerModel model;
	private StopperListener stopper;
	private IStatisticsClient stats;
	/**
	 * 
	 */
	public DictionaryServerEnvironment() {
		super("config-props.xml", "logger.properties");
		stats = new StatisticsHttpClient(this);
		stopper = new StopperListener(this);
		isShutDown = false;
		dictionary = new PersistentDictionary(this);
        model = new DictionaryServerModel(this, dictionary);
         System.out.println("ENV-1 "+model);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			@Override
			public void run() {
				shutDown();
			}
		});

	}
	
	public IPersistentDictionary getDictionary() {
		return dictionary;
	}
	
	public IStatisticsClient getStats() {
		return stats;
	}
	
	public IDictionaryServerModel getModel() {
		logDebug("GetModel");
		return model;
	}
		
	public void shutDown() {
		logDebug("DictionaryServerEnvironment.shutDown "+isShutDown);
		if (!isShutDown) {
			isShutDown = true;
			System.out.println("DictionaryServerEnvironment.shutDown");
			model.shutDown();
		}
	}


}
