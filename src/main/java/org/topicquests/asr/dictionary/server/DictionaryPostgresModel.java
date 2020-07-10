/**
 * 
 */
package org.topicquests.asr.dictionary.server;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class DictionaryPostgresModel implements IDictionaryServerModel {
	private DictionaryServerEnvironment environment;
	private IPostgresDictionary dictionary;
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
	public DictionaryPostgresModel(DictionaryServerEnvironment env, IPostgresDictionary d) {
		environment = env;
		dictionary = d;
		clientId = environment.getStringProperty("ClientId");
		environment.logDebug("BootingDictionary");
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
		long wID = -1;
		String theWord = word.toLowerCase();
		if (theWord.equals("\""))
			wID = 0; // reserved
		else
			wID =  dictionary.getWordId(word);
		environment.logDebug("DictServerModel.addWord-1 "+wID);
		if (wID > -1)	 {
			result.put(IDictionaryServerModel.CARGO, wID);
			result.put(IDictionaryServerModel.IS_NEW_WORD, false);
		} else {
			wID = dictionary.addNewWord(word);
			result.put(IDictionaryServerModel.CARGO, wID);
			result.put(IDictionaryServerModel.IS_NEW_WORD, true);
			environment.logDebug("DictServerModel.addWord-2 "+result);
			isDirty = true;
		}
		return result;
	}


	@Override
	public void shutDown() {
		dictionary.shutDown();
	}

}
