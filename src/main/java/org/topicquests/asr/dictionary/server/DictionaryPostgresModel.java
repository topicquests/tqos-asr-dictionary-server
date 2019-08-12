/**
 * 
 */
package org.topicquests.asr.dictionary.server;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.api.IResult;
import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import org.topicquests.os.asr.api.IStatisticsClient;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class DictionaryPostgresModel implements IDictionaryServerModel {
	private DictionaryServerEnvironment environment;
	private IPostgresDictionary dictionary;
	private IStatisticsClient stats;
	/**
	 * 
	 */
	public DictionaryPostgresModel(DictionaryServerEnvironment env, IPostgresDictionary d) {
		environment = env;
		dictionary = d;
		stats = environment.getStats();
	}

	@Override
	public IResult handleRequest(JSONObject request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub

	}

}
