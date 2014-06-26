package questionanalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import questionanalysis.KeywordExtractor.KeyWord;
import ruc.irm.similarity.word.cilin.CilinDb;
import ruc.irm.similarity.word.hownet2.concept.XiaConceptParser;

public class SynonymExpander {
	
	private static final double HIGH_SIMILARITY = 0.9;
	private static final double LOW_SIMILARITY = 0.7;
	
	public static boolean initialize() {
		CilinDb.getInstance();
		XiaConceptParser.getInstance();
		return true;
	}
	
	/*
	 * �Դ���Ĺؼ��ʽ���ͬ����չ
	 * 
	 * @param keywords ����Ĺؼ���
	 * @return expandedKeywords ��ʾͬ����չ�Ĺؼ���
	 */
	public static List<KeyWord> expandKeyword(List<KeyWord> keywords) {
		List<KeyWord> expandedKeywords = new ArrayList<KeyWord>();
		CilinDb db =  CilinDb.getInstance();
		XiaConceptParser wordSim = XiaConceptParser.getInstance();
		for (KeyWord kw : keywords) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH) {
				continue;	// ����չ����ʵ��
			}
			Set<String> codeSet = db.getCilinCoding(kw.keyword);
			if (codeSet != null) {
				String code = codeSet.iterator().next();
				Set<String> synonyms =  db.getCilinWords(code);
				for (String synonym : synonyms) {
					double similarity = wordSim.getSimilarity(kw.keyword, synonym);
					if (similarity < LOW_SIMILARITY) {
						continue;	// ���������ƶȹ��͵�ͬ���
					}
					KeyWord ekw = new KeyWord(synonym, (similarity >= HIGH_SIMILARITY) ?
							KeywordExtractor.KEYWORD_WEIGHT_MEDIUM :
								KeywordExtractor.KEYWORD_WEIGHT_LOW);
					expandedKeywords.add(ekw);
					
					System.out.println(String.format("%s => %s : %f", 
							kw.keyword, synonym, similarity));
				}
			}
		}
		return expandedKeywords;
	}
}
