/**
 * 
 */
package org.topicquests.asr.dictionary;

import org.topicquests.asr.dictionary.server.DictionaryServerModel;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.RootEnvironment;

/**
 * @author jackpark
 *
 */
public class DictionaryServerEnvironment extends RootEnvironment {
	private boolean isShutDown = false;
	private IDictionaryServerModel model;
	private StopperListener stopper;
	/**
	 * 
	 */
	public DictionaryServerEnvironment() {
		super("config-props.xml", "logger.properties");
		stopper = new StopperListener(this);
		isShutDown = false;
        try {
        	model = new DictionaryServerModel(this);
        	logDebug("Environment "+model);
        } catch (Exception e) {
        	logError(e.getMessage(), e);
        	throw new RuntimeException(e);
        }
        System.out.println("ENV-1 "+model);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			@Override
			public void run() {
				shutDown();
			}
		});

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
			try {
				model.saveDictionary();
				logDebug("DictionaryServerEnvironment.shutDown");
			} catch (Exception e) {
				logError(e.getMessage(), e);
				e.printStackTrace();
			}
		}
	}


}
