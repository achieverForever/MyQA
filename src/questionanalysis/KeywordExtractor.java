package questionanalysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.hit.ir.ltp4j.NER;

public class KeywordExtractor {
	
	public static final int KEYWORD_WEIGHT_LOW = 10;
	public static final int KEYWORD_WEIGHT_MEDIUM = 40;
	public static final int KEYWORD_WEIGHT_HIGH = 80;
	
	private static HashSet<String> NE_SET;
	private static HashSet<String> STOPWORDS_SET;
	
	/*
	 * ��ȡ�����ӵĹؼ��ʣ���ȡ�ؼ��ʵ�ԭ��Ϊ��
	 * 1.����ʵ�壻2.���ʺͶ��ʣ� 3.ȥ��ͣ�ôʺ��ظ���
	 * 
	 * @param words ��ʾ�ֺôʵ����⴮
	 * @param tags ��ʾ�����⴮���Ա�ע�Ľ��
	 * @return result ��ʾ��ȡ���Ĺؼ���
	 */
	public static List<KeyWord> getKeywords(List<String> words, List<String> tags) {
		List<KeyWord> result = new ArrayList<KeyWord>();
		addNounAndVerb(words, tags, result);
		addNamedEntity(words, tags, result);
		dropStopwords(result);
		return result;
	}
	
	public static void addNamedEntity(List<String> words, List<String> tags, 
			List<KeyWord> result) {
		List<String> nes = new ArrayList<String>();
		NER.recognize(words, tags, nes);
		for (int i = 0; i < nes.size(); i++) {
			String ne = nes.get(i);
			String word = words.get(i);
			// ֻ��NE_SET�е�����ʵ�����Ȥ������������ʵ�����Ȩ��
			if (!ne.equals("O") && NE_SET.contains(ne)) {
				// ���ڻ�������ҪһЩ���⴦��
				if (ne.equals("B-Ni")) {
					StringBuilder sb = new StringBuilder();
					do {
						ne = nes.get(i);
						word = words.get(i);
						sb.append(word);
						// �������ʵ�������֮ǰ��ȡ��ͨ�����ʻ򶯴ʣ�����ԭ���Ķ��ʻ�����
						checkIfAlreadyContained(result, word);
						i++;
					} while (!ne.equals("E-Ni") && i < nes.size());
					result.add(new KeyWord(sb.toString(), KEYWORD_WEIGHT_HIGH));
				} else {
					result.add(new KeyWord(word, KEYWORD_WEIGHT_HIGH));
				}
			}
		}
	}
	
	private static void checkIfAlreadyContained(List<KeyWord> result, String word) {
		for (KeyWord kw : result) {
			if (word.equals(kw.keyword)) {
				result.remove(kw);
				return;
			}
		}
	}
	
	public static void addNounAndVerb(List<String> words, List<String> tags,
			List<KeyWord> result) {
		for (int i = 0; i < tags.size(); i++) {
			if (tags.get(i).equals("n") || tags.get(i).equals("v")) { // ���ʡ����ʳ�ȡΪ�ؼ���
				result.add(new KeyWord(words.get(i), KEYWORD_WEIGHT_MEDIUM));	
			} 
		}
	}
	
	public static void dropStopwords(List<KeyWord> result) {
		for (KeyWord kw : result) {
			if (STOPWORDS_SET.contains(kw.keyword)) {
				result.remove(kw);
			}
		}
	}
	
	private static boolean loadStopwords(String stopwordsfile) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(stopwordsfile));
			String line;
			while ((line = in.readLine()) != null) {
				STOPWORDS_SET.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
		return true;
	}
	
	public static boolean initialize() {
		NE_SET = new HashSet<String>();
		STOPWORDS_SET = new HashSet<String>();
		// �ֱ��ʾ�����������ͻ�����
		String[] nes = { "S-Nh", "S-Ns", "B-Ni" };
		for (String ne : nes) 
			NE_SET.add(ne);
		if (!loadStopwords("data/models/stopwords/StopWords.txt")) 
			return false;
		else
			return true;
	}
	
	public static class KeyWord {
		public String keyword;
		public double weight;
		
		public KeyWord(String keyword, int weight) {
			this.keyword = keyword;
			this.weight = (double) weight;
		}
		
		@Override
		public String toString() {
			return keyword + ":" + weight;
		}
	}
	
}