/**
 * 
 */
package org.topicquests.asr.dictionary.server;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.util.JSONUtil;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class DictionaryServerModel implements IDictionaryServerModel {
	private DictionaryServerEnvironment environment;
	private JSONObject dictionary;
	private JSONUtil util;
	private long nextNumber = 0;
	private long wordCount = 0;
	private final String clientId;
	private boolean isDirty = false;
	private final String 
		WORDS 		= "words",
		//an index of words, returning their id values
		IDS			= "ids",
		NUMBER		= "number",
		SIZE		= "size",
		WORD_COUNT	= "wordCount"; //,

	/**
	 * 
	 */
	public DictionaryServerModel(DictionaryServerEnvironment env) throws Exception {
		environment = env;
		clientId = environment.getStringProperty("ClientId");
		util = new JSONUtil();
		bootDictionary();
		environment.logDebug("BootingDictionary");
	}

	void bootDictionary() throws Exception {
		String path = environment.getStringProperty("WordDictionaryPath");
		dictionary = util.load(path);
		if (dictionary.isEmpty()) {
			dictionary = new JSONObject();
			dictionary.put(WORDS, new JSONObject());
			dictionary.put(IDS, new JSONObject());
			dictionary.put(WORD_COUNT, "0");
			dictionary.put(NUMBER, "0");
			wordCount = 0L;
			nextNumber = 0L;
		} else {
			String y = dictionary.getAsString(WORD_COUNT);
			long x = 0;
			if (y != null) {
				x = Long.parseLong(y);
				wordCount = x;
			} else
				wordCount = 0L;
			y = dictionary.getAsString(NUMBER);
			if (y != null) {
				x = Long.parseLong(y);
				nextNumber = x;
			} else
				nextNumber = 0L;
			
		}
		environment.logDebug("SavingDictionary "+wordCount+" "+nextNumber);
		isDirty = false;
	}
	
	public void saveDictionary() throws Exception {
		environment.logDebug("SavingDictionary "+isDirty+" "+wordCount+" "+nextNumber);
		if (isDirty) {
			synchronized(dictionary) {
				System.out.println("SavingDict "+wordCount+" "+nextNumber);
				updateCounts();
				String path = environment.getStringProperty("WordDictionaryPath");
				util.save(path, dictionary);
			}
			isDirty = false;
		}
	}

	void updateCounts() {
		dictionary.put(WORD_COUNT, Long.toString(wordCount));
		dictionary.put(NUMBER, Long.toString(nextNumber));		
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
		JSONObject result = new JSONObject();
		String theWord = word.toLowerCase();
		//always look for Id with
		String wordId = getWordId(theWord);
		if (wordId != null)	 {
			result.put(IDictionaryServerModel.CARGO, wordId);
			result.put(IDictionaryServerModel.IS_NEW_WORD, false);
		} else {
			this.wordCount++;
///////////////
// This project is going to get a simple HttpClient to communicate
// with tqos-asr-statistics-server
//////////////
//TODO			environment.getStats().addDictionaryWord();
			wordId = newNumericId();
			JSONObject words = getWords();
			//put whole word
			words.put(wordId, word);
			words = getIDs(); //reuse variable
			//put //put lowercase version
			words.put(theWord, wordId);		
			result.put(IDictionaryServerModel.CARGO, wordId);
			result.put(IDictionaryServerModel.IS_NEW_WORD, true);
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
		synchronized(dictionary) {
			JSONObject ids = getIDs();
			String result = (String)ids.get(word);
			if (result == null) {
				//does this word have caps?
				String lc = word.toLowerCase();
				if (!lc.equals(word)) {
					//see if it exists as lowercase
					result = (String)ids.get(lc);
				}
			}
			return result;
		}
	}
	
	@Override
	public IResult handleRequest(JSONObject request) {
		IResult result = new ResultPojo();
		JSONObject jo = new JSONObject(); // empty default
		environment.logDebug("DictionaryServerModel.handleNewRequest "+request);
		String verb = request.getAsString(IDictionaryServerModel.VERB);
		String clientIx = request.getAsString(IDictionaryServerModel.CLIENT_ID);
		String x;
		if (clientIx.equals(clientId)) {
			if (verb.equals(IDictionaryServerModel.GET_WORD_ID) ||
				verb.equals(IDictionaryServerModel.ADD_WORD))
				jo = getWordId(request);
			else if (verb.equals(IDictionaryServerModel.GET_WORD)) {
				x = getWordById(request);
				jo.put(IDictionaryServerModel.CARGO, x);
			} else if (verb.equals(IDictionaryServerModel.GET_DICTIONARY)) {
				synchronized(dictionary) {
					updateCounts();
				}
				jo = dictionary;
			} else if (verb.equals(IDictionaryServerModel.TEST))  {
				jo = new JSONObject();
				jo.put(IDictionaryServerModel.CARGO, "Yup");
			} else {
				jo = new JSONObject();
				jo.put(IDictionaryServerModel.ERROR, "BAD VERB: "+verb);
			}
		} else {
			jo.put(IDictionaryServerModel.ERROR, "Invalid Client");
		}
		result.setResultObject(jo);

		return result;
	}

	JSONObject getWords() {
		return (JSONObject)dictionary.get(WORDS);
	}

	JSONObject getIDs() {
		return (JSONObject)dictionary.get(IDS);
	}

	String getWordById(JSONObject jo) {
		//theWord is wordId
		String result = null;
		synchronized(dictionary) {
			String theWord = jo.getAsString(IDictionaryServerModel.WORD);
			result = getWords().getAsString(theWord);
		}
		return result;
	}
	
	JSONObject getWordId(JSONObject jo) {
		String theWord = jo.getAsString(IDictionaryServerModel.WORD);
		JSONObject result = addWord(theWord);
		System.out.println("DictionaryServerModel.getWordId "+theWord+" "+result);
		return result;
	}
	

}
