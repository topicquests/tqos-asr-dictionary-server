/**
 * 
 */
package devtests;


/**
 * @author jackpark
 *
 */
public class ServerTester {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//new BootTest(); // simple boot test
		//new Stringolizer(); // used to make some query strings
		//new DictionaryFillerTest(); // pumps 1 million words into it
		new BigDictionaryGetTest(); // asks for a huge dictionary over http
	}

}
