package questionanalysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import questionanalysis.KeywordExtractor.KeyWord;

public class SynonymExpander {
	
	private static HashMap<String, String> sSynonymMap;
	
	public static boolean initialize() {
		sSynonymMap = new HashMap<String, String>();
		if (!loadSynonyms("data/models/synonyms.txt")) 
			return false;
		else 
			return true;
	}
	
	/*
	 * 对传入的关键词进行同义扩展
	 * 
	 * @param keywords 传入的关键词
	 * @return expandedKeywords 表示扩展后的关键词，对于存在扩展的
	 * 关键词，返回的HashMap中将有值；否则没有值。
	 */
	public static HashMap<KeyWord, KeyWord> expandKeyword(List<KeyWord> keywords) {
		HashMap<KeyWord, KeyWord> expandedKeywords = new HashMap<KeyWord, KeyWord>();
		for (KeyWord kw : keywords) { 
			if (sSynonymMap.get(kw.keyword) != null) {
				expandedKeywords.put(kw, new KeyWord(sSynonymMap.get(kw.keyword),
						KeywordExtractor.KEYWORD_WEIGHT_LOW));
			}
		}
		return expandedKeywords;
	}
	
	private static boolean loadSynonyms(String synonymfile) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(synonymfile));
			String line;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split(" ");
				if (tokens[0].endsWith("=") && tokens.length > 2) {
					String synonym = tokens[1];	// 选取具有同义关系的词的第一个词作为同义词
					for (int i = 2; i < tokens.length; i++) {
						sSynonymMap.put(tokens[i], synonym);
					}
					sSynonymMap.put(synonym, tokens[2]);
				}
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
}
