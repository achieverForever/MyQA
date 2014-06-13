package questionanalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.QuestionType;
import edu.hit.ir.ltp4j.NER;
import edu.hit.ir.ltp4j.Postagger;
import edu.hit.ir.ltp4j.Segmentor;

public class QuestionAnalyzer {
	
	/*
	 * 对传入的问题串进行分析
	 * 
	 * @param question 问题串
	 * @return result 表示经过分析处理的问题
	 */
	public static AnalyzedQuestion analyze(String question) {
		AnalyzedQuestion result = null;
		
		// 去除问题结尾的标点
		question = question.replaceAll("。？]", "");

		// 分词
		List<String> words = new ArrayList<String>();
		Segmentor.segment(question, words);
		
		// 词性标注
		List<String> tags = new ArrayList<String>();
		Postagger.postag(words, tags); 
		for (int i = 0; i < tags.size(); i++) {
			System.out.print(words.get(i) + "/" + tags.get(i) + " ");
		}
		System.out.println("");
		
		// 命名实体识别
		List<String> nes = new ArrayList<String>();
		NER.recognize(words, tags, nes);
		for (int i = 0; i < nes.size(); i++) {
			System.out.print(words.get(i) + "/" + nes.get(i) + " ");
		}
		System.out.println("");
		
		// 提取关键词
		System.out.println("提取的关键词：");
		List<KeyWord> keywords = KeywordExtractor.getKeywords(words, tags);
		System.out.println(keywords);
		
		// 关键词同义扩展
		System.out.println("可同义扩展的关键词：");
		Map<KeyWord, KeyWord> expandedKeywords = SynonymExpander.expandKeyword(keywords);
		System.out.println(expandedKeywords);
		
		// 提取问题焦点
		System.out.println("提取的问题焦点：");
		String focus =  FocusFinder.findFocus(words, tags);
		System.out.println(focus);
		
		// 确定问题类型
		QuestionType type = QuestionClassifier.classify(question);
		
		result = new AnalyzedQuestion(focus, type, keywords, expandedKeywords, question);
		
		return result;
	}
}
