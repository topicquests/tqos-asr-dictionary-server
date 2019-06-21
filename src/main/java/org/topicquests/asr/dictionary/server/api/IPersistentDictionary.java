/**
 * 
 */
package org.topicquests.asr.dictionary.server.api;
import java.util.Iterator;
/**
 * @author jackpark
 *
 */
public interface IPersistentDictionary {

	boolean isEmpty();
	
	void addNewWord(String word, long id);
	
	////////////////////
	// id:word
	////////////////////
	
	void addId(long id, String word);
	
	/**
	 * Can return <code>null</code>
	 * @param id
	 * @return
	 */
	String getWordById(long id);

	////////////////////
	// word:id
	////////////////////

	Iterator<String> listWords();
	
	/**
	 * Returns <code>-1</code>if not found
	 * @param word
	 * @return
	 */
	long getWordId(String word);
	
	void addWord(String word, long id);
	
	void shutDown();
}
