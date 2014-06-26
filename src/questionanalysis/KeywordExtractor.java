package questionanalysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.hit.ir.ltp4j.NER;

public class KeywordExtractor {
	
	public static final int KEYWORD_WEIGHT_LOW = 10;
	public static final int KEYWORD_WEIGHT_MEDIUM = 40;
	public static final int KEYWORD_WEIGHT_HIGH = 80;
	
	private static HashSet<String> NE_SET;
	private static HashSet<String> STOPWORDS_SET;
	
	/*
	 * 提取出句子的关键词，抽取关键词的原则为：
	 * 1.命名实体；2.名词和动词； 3.去掉停用词和重复词
	 * 
	 * @param words 表示分好词的问题串
	 * @param tags 表示对问题串词性标注的结果
	 * @return result 表示提取出的关键词
	 */
	public static List<KeyWord> getKeywords(List<String> words, List<String> tags) {
		List<KeyWord> result = new ArrayList<KeyWord>();
		addNounAndVerb(words, tags, result);
		addNamedEntity(words, tags, result);
		dropStopwords(result);
		return result;
	}
	
	/*
	 * 提取出命名实体
	 */
	public static void addNamedEntity(List<String> words, List<String> tags, 
			List<KeyWord> result) {
		if (words.size() == 0) 
			return;
		
		for (int i = 0; i < words.size(); i++) {
			if (tags.get(i).equals("nz")) {
				result.add(new KeyWord(words.get(i), KEYWORD_WEIGHT_HIGH));
			}
		}
		
		List<String> nes = new ArrayList<String>();
		NER.recognize(words, tags, nes);
		for (int i = 0; i < nes.size(); i++) {
			String ne = nes.get(i);
			String word = words.get(i);
			// 只对NE_SET中的命名实体感兴趣，并赋予命名实体最高权重
			if (!ne.equals("O") && NE_SET.contains(ne)) {
				// 对于机构名需要一些特殊处理
				if (ne.equals("B-Ni")) {
					StringBuilder sb = new StringBuilder();
					do {
						ne = nes.get(i);
						word = words.get(i);
						sb.append(word);
						// 如果命名实体包含了之前抽取普通的名词或动词，丢弃原来的动词或名词
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
	
	/*
	 * 去掉与命名实体重合的的动词或名词，因为命名实体的权重更高
	 */
	private static void checkIfAlreadyContained(List<KeyWord> result, String word) {
		for (KeyWord kw : result) {
			if (word.equals(kw.keyword)) {
				result.remove(kw);
				return;
			}
		}
	}
	
	/*
	 * 提取出名词和动词
	 */
	public static void addNounAndVerb(List<String> words, List<String> tags,
			List<KeyWord> result) {
		for (int i = 0; i < tags.size(); i++) {
			// 名词、动词抽取为关键词
			if (tags.get(i).equals("n") || tags.get(i).equals("v")) { 
				result.add(new KeyWord(words.get(i), KEYWORD_WEIGHT_MEDIUM));	
			} 
		}
	}
	
	/*
	 * 去掉停用词
	 */
	public static void dropStopwords(List<KeyWord> result) {
		for (Iterator<KeyWord> it = result.iterator(); it.hasNext(); ) {
			if (STOPWORDS_SET.contains(it.next().keyword)) {
				it.remove();
			}
		}
	}
	
	/*
	 * 从文件中读入停用词
	 */
	private static boolean loadStopwords(String stopwordsfile) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(stopwordsfile), "UTF-8"));
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
	
	/*
	 * 初始化
	 */
	public static boolean initialize() {
		NE_SET = new HashSet<String>();
		STOPWORDS_SET = new HashSet<String>();
		// 分别表示人名。地名和机构名
		String[] nes = { "S-Nh", "S-Ns", "B-Ni", "S-Ni" };
		for (String ne : nes) {
			NE_SET.add(ne);
		}
		if (!loadStopwords("data/models/stopwords/StopWords.txt"))
			return false;
		else
			return true;
	}
	
	/*
	 * 表示一个关键词的实体类
	 */
	public static class KeyWord {
		public String keyword;
		public double weight;
		
		public KeyWord(String keyword, int weight) {
			this.keyword = keyword;
			this.weight = (double) weight;
		}
		
		@Override
		public String toString() {
			return keyword + " " + weight;
		}
	}
	
}
