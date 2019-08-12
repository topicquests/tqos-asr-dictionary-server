/**
 * 
 */
package org.topicquests.asr.dictionary.server.api;
import java.util.*;

/**
 * @author jackpark
 *
 */
public interface IPostgresDictionary {

	long addNewWord(String word);
	
	/**
	 * Can return <code>null</code>
	 * @param id
	 * @return
	 */
	String getWordById(long id);
	
	/**
	 * Returns <code>-1</code>if not found
	 * @param word
	 * @return
	 */
	long getWordId(String word);

	
	boolean isEmpty();
	List<String> listWords(int offset, int count);

	void shutDown();

}
