package questionanalysis;

import java.util.ArrayList;
import java.util.List;

import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.QuestionType;
import edu.hit.ir.ltp4j.NER;
import edu.hit.ir.ltp4j.Postagger;
import edu.hit.ir.ltp4j.Segmentor;

public class QuestionAnalyzer {
	
	/*
	 * ��������з���
	 * 
	 * @param question ���⴮
	 * @return result ��ʾ�����������������
	 */
	public static AnalyzedQuestion analyze(String question) {
		AnalyzedQuestion result = null;
		
		// ȥ�������β�ı��
		question = question.replaceAll("[����]+", "");

		// �ִ�
		List<String> words = new ArrayList<String>();
		Segmentor.segment(question, words);
		
		// ���Ա�ע
		List<String> tags = new ArrayList<String>();
		Postagger.postag(words, tags); 
		for (int i = 0; i < tags.size(); i++) {
			System.out.print(words.get(i) + "/" + tags.get(i) + " ");
		}
		System.out.println("");
		
		// ����ʵ��ʶ��
		if (words.size() > 0) {
			List<String> nes = new ArrayList<String>();
			NER.recognize(words, tags, nes);
			for (int i = 0; i < nes.size(); i++) {
				System.out.print(words.get(i) + "/" + nes.get(i) + " ");
			}
			System.out.println("");
		}
		
		// ��ȡ�ؼ���
		System.out.println("Extracted Keywords��");
		List<KeyWord> keywords = KeywordExtractor.getKeywords(words, tags);
		System.out.println(keywords);
		
		// �ؼ���ͬ����չ
		System.out.println("Extended Keywords��");
		List<KeyWord> expandedKeywords = SynonymExpander.expandKeyword(keywords);
		System.out.println(expandedKeywords);
		
		// ��ȡ���⽹��
		System.out.println("Extracted Focus: ");
		String focus =  FocusFinder.findFocus(words, tags);
		System.out.println(focus.equals("") ? "null" : focus);
		
		// ȷ����������
		QuestionType type = QuestionClassifier.classify(question);
		
		result = new AnalyzedQuestion(focus, type, keywords, expandedKeywords, question,
				words, tags);
		
		return result;
	}
}
