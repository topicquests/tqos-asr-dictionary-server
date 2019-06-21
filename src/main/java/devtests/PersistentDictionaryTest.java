/**
 * 
 */
package devtests;

import java.util.Iterator;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IPersistentDictionary;

/**
 * @author jackpark
 *
 */
public class PersistentDictionaryTest {
	private DictionaryServerEnvironment environment;
	private IPersistentDictionary dictionary;
	private long NUMBER = 2;
	private final String WORD = "MyOtherNewWord";
	/**
	 * 
	 */
	public PersistentDictionaryTest() {
		environment = new DictionaryServerEnvironment();
		dictionary = environment.getDictionary();
		long ix = dictionary.getWordId(WORD);
		System.out.println("A "+ix);
		if (ix == -1)
			dictionary.addNewWord(WORD, NUMBER++);
		ix = dictionary.getWordId(WORD);
		System.out.println("B "+ix);
		String w = dictionary.getWordById(ix);
		System.out.println("C "+w);
		Iterator<String> itr = dictionary.listWords();
		while (itr.hasNext()) {
			System.out.println("D "+itr.next());
		}
		environment.shutDown();
		System.exit(0);
	}

}
