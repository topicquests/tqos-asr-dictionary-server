/**
 * 
 */
package devtests;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;

/**
 * @author jackpark
 *
 */
public class BootTest {
	private DictionaryServerEnvironment environment;

	/**
	 * 
	 */
	public BootTest() {
		environment = new DictionaryServerEnvironment();
		System.out.println("AAA "+environment.getProperties());
		environment.shutDown();
		System.exit(0);
	}

}
