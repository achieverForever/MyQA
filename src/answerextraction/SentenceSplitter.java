package answerextraction;

import java.util.ArrayList;
import java.util.List;

public class SentenceSplitter {
	
	/*
	 * ���ĵ����з־�
	 */
	public static List<String> splitSentence(String doc) {
		List<String> sentences = new ArrayList<String>();
		String[] sents = doc.split("[������]+");
		for (String sent : sents) {
			sentences.add(sent.trim());
		}
		return sentences;
	}
	
}
