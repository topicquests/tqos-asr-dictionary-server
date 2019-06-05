/**
 * 
 */
package org.topicquests.asr.dictionary.server.api;


import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public interface IDictionaryServerModel {
	public static final String
		VERB			= "verb",
		CLIENT_ID		= "clientId",
		WORD			= "word",
		GET_WORD_ID		= "getWordId",
		ADD_WORD		= "addWord",
		GET_WORD		= "getWord",
		GET_DICTIONARY	= "getDictionary",
		IS_NEW_WORD		= "isNewWord",	// boolean <code>true</code> if is new word
		TEST			= "test",
		ERROR			= "error",
		CARGO			= "cargo"; //return object - wordId or word
	
		
	/**
	 * A request takes one form:<br>
	 * {verb:"add", word:<word>, clientId:<clientId>}<br/>
	 * It responds with {resp:"ok", value:<value>, isNewWord:<true/false>}<br/>
	 * @param request
	 * @return
	 */
	IResult handleRequest(JSONObject request);

	void saveDictionary() throws Exception;
}
