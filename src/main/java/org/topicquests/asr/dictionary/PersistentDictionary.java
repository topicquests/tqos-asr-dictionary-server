/**
 * 
 */
package org.topicquests.asr.dictionary;
import java.io.*;
import java.util.Iterator;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.topicquests.asr.dictionary.server.api.IPersistentDictionary;

/**
 * @author jackpark
 *
 */
public class PersistentDictionary implements IPersistentDictionary {
	private DictionaryServerEnvironment environment;
	private DB database;
	private HTreeMap<String, Long> wordMap;
	private HTreeMap<Long, String> idMap;
	private boolean isClosed = false;

	/**
	 * 
	 */
	public PersistentDictionary(DictionaryServerEnvironment env) {
		environment = env;
		try {
			openDB();
			isClosed = false;
		} catch (Exception e) {
			
//DBMaker.checksumHeaderBypass()` to recover your data. 
//Use clean shutdown or enable transactions to protect the store in the future.
			throw new RuntimeException(e);
		}
	}
	
	void openDB() throws Exception {
		String dbWordPath = environment.getStringProperty("WordMapDictionaryPath");
		String dbWordName = environment.getStringProperty("WordMapDictionaryName");
		String dbIdPath = environment.getStringProperty("IdMapDictionaryPath");
		String dbIdName = environment.getStringProperty("IdMapDictionaryName");
			File f = new File(dbWordPath);
			database = DBMaker.fileDB(f)
					//.closeOnJvmShutdown()
					.checksumHeaderBypass()
					.make();
			wordMap = database.hashMap(dbWordName)
	        	.keySerializer(Serializer.STRING)
	        	.valueSerializer(Serializer.LONG)
	        	.createOrOpen();
			f = new File(dbIdPath);
			database = DBMaker.fileDB(f)
					//.closeOnJvmShutdown()
					.make();
			idMap = database.hashMap(dbIdName)
	        	.keySerializer(Serializer.LONG)
	        	.valueSerializer(Serializer.STRING)
	        	.createOrOpen();
	}

	@Override
	public void addId(long id, String word) {
		idMap.put(id, word);
	}

	@Override
	public String getWordById(long id) {
		return idMap.get(id);
	}

	@Override
	public long getWordId(String word) {
		Object x = wordMap.get(word);
		if (x == null)
			return -1;
		return ((Long)x).longValue(); 
	}

	@Override
	public void addWord(String word, long id) {
		wordMap.put(word, id);
	}

	@Override
	public void shutDown() {
		if (!isClosed) {
			System.out.println("PersistentDictionary shutting down "+database.isClosed());
			database.commit();
			if (!database.isClosed())
				database.close();
			System.out.println("PersistentMap closed");
			isClosed = true;
		}	
	}

	@Override
	public boolean isEmpty() {
		return wordMap.isEmpty();
	}

	@Override
	public void addNewWord(String word, long id) {
		System.out.println("DB.addNewWord "+database.isClosed());
		this.addWord(word, id);
		this.addId(id, word.toLowerCase());
		
	}

	@Override
	public Iterator<String> listWords() {
		return wordMap.keySet().iterator();
	}

}
