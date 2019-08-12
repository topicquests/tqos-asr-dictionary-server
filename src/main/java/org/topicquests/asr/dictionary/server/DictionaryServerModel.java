/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary.server;

import org.topicquests.os.asr.api.IStatisticsClient;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.PostgresDictionary;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.asr.dictionary.server.api.IPersistentDictionary;
import org.topicquests.os.asr.common.api.IASRFields;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class DictionaryServerModel implements IDictionaryServerModel {
	private DictionaryServerEnvironment environment;
	private IPersistentDictionary dictionary;
	private IStatisticsClient stats;
	//We reserve 0 for the quote character, taken care of at the client
	private long nextNumber = 1;
	private long wordCount = 1;
	private final String clientId;
	private boolean isDirty = false;
	private final String 
		WORDS 		= "words",
		//an index of words, returning their id values
		IDS			= "ids",
		NUMBER		= "_DictionaryNumber",
		SIZE		= "size",
		WORD_COUNT	= "_DictionaryWordCount"; //,

	/**
	 * 
	 */
	public DictionaryServerModel(DictionaryServerEnvironment env, IPersistentDictionary d)  {
		environment = env;
		dictionary = d;
		stats = environment.getStats();
		clientId = environment.getStringProperty("ClientId");
		environment.logDebug("BootingDictionary");
	}

	void updateCounts() {
		dictionary.addWord(WORD_COUNT, wordCount);
		dictionary.addWord(NUMBER, nextNumber);		
	}

	/////////////////////////
	// given Id, get the whole word
	// given word, find Id with lowercase word
	/////////////////////////
	/**
	 * Serves two different verbs:
	 * <ul><li>GetWordId: if it tries to get the idea on a particular word
	 *  which does not exist, it adds the word and returns the id</li>
	 *  <li>AddWord: simply adds the word and returns its id</li>
	 * <p>Returns { wordId, boolean }<br/>
	 * boolean is true if this is a new word</p>
	 * @param word
	 * @return
	 */
	private JSONObject addWord(String word) {
		environment.logDebug("DictServerModel.addWord- "+word);
		JSONObject result = new JSONObject();
		String theWord = word.toLowerCase();
		//always look for Id with
		String wordId = null;
		if (theWord.equals("\""))
			wordId = "0"; // reserved
		else
			wordId = getWordId(theWord);
		environment.logDebug("DictServerModel.addWord-1 "+wordId);
		if (wordId != null)	 {
			result.put(IDictionaryServerModel.CARGO, wordId);
			result.put(IDictionaryServerModel.IS_NEW_WORD, false);
		} else {
			this.wordCount++;
			stats.addToKey(IASRFields.WORDS_NEW);
			wordId = newNumericId();
			long ix = Long.parseLong(wordId);
			dictionary.addNewWord(word, ix);
			result.put(IDictionaryServerModel.CARGO, wordId);
			result.put(IDictionaryServerModel.IS_NEW_WORD, true);
			environment.logDebug("DictServerModel.addWord-2 "+result);
			isDirty = true;
		}
		return result;
	}
	
	
	String newNumericId() {
		String result =Long.toString(nextNumber);
		nextNumber += 1L;
		environment.logDebug("DictNewId "+result+" "+nextNumber);
		return result;
	}
	
	String getWordId(String word) {
		//This tests for the word and, if necessary, its lowercase version
		String result = null;
		long ix = dictionary.getWordId(word);
		if (ix != -1) {
				//does this word have caps?
				String lc = word.toLowerCase();
				if (!lc.equals(word)) {
					ix = dictionary.getWordId(lc);				
				}
		}
		if (ix != -1)
			result = Long.toString(ix);
		return result;
	}
	
	@Override
	public IResult handleRequest(JSONObject request) {
		IResult result = new ResultPojo();
		JSONObject jo = new JSONObject(); // empty default
		result.setResultObject(jo);
		environment.logDebug("DictionaryServerModel.handleNewRequest- "+request);
		String verb = request.getAsString(IDictionaryServerModel.VERB);
		String clientIx = request.getAsString(IDictionaryServerModel.CLIENT_ID);
		String x;
		if (clientIx.equals(clientId)) {
			if (verb.equals(IDictionaryServerModel.GET_WORD_ID) ||
				verb.equals(IDictionaryServerModel.ADD_WORD)) {
				jo = getWordId(request);
				environment.logDebug("DictionaryServerModel.handleNewRequest-1 "+jo);
			} else if (verb.equals(IDictionaryServerModel.GET_WORD)) {
				x = getWordById(request);
				jo.put(IDictionaryServerModel.CARGO, x);
			} else if (verb.equals(IDictionaryServerModel.GET_DICTIONARY)) {
				//WE DON'T DO THIS
			} else if (verb.equals(IDictionaryServerModel.TEST))  {
				jo.put(IDictionaryServerModel.CARGO, "Yup");
			} else {
				jo.put(IDictionaryServerModel.ERROR, "BAD VERB: "+verb);
			}
		} else {
			jo.put(IDictionaryServerModel.ERROR, "Invalid Client");
		}
		environment.logDebug("DictionaryServerModel.handleNewRequest+ "+jo);
		result.setResultObject(jo);
		environment.logDebug("DictionaryServerModel.handleNewRequest++ "+result.getResultObject());;

		return result;
	}


	String getWordById(JSONObject jo) {
		//theWord is wordId
			String id = jo.getAsString(IDictionaryServerModel.WORD);
			long ix = Long.parseLong(id);
			return dictionary.getWordById(ix);
	}
	
	JSONObject getWordId(JSONObject jo) {
		String theWord = jo.getAsString(IDictionaryServerModel.WORD);
		JSONObject result = null;
		if (theWord.equals("\""))
			result = addWord("\"");
		else
			result = addWord(theWord);
		System.out.println("DictionaryServerModel.getWordId "+theWord+" "+result);
		return result;
	}

	@Override
	public void shutDown() {
		System.out.println("DictionaryModel shutting down");
		dictionary.shutDown();
	}
	

}
