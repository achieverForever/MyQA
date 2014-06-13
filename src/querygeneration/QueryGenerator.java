package querygeneration;

import java.util.ArrayList;
import java.util.List;

import questionanalysis.KeywordExtractor;
import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.Query;

public class QueryGenerator {
	
	/*
	 * ��һ������������������������������Ĳ�ѯ��
	 * 
	 * @param analyzedQuestion �����������������
	 * @return queries ��ʾ���صĲ�ѯ
	 */
	public static List<Query> generateQueries(AnalyzedQuestion analyzedQuestion) {
		List<Query> queries = new ArrayList<Query>();
		String answerType = "";
		if ("Q_PERSON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "";
		} else if ("Q_LOCATION".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "λ��";
		} else if ("Q_REASON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "ԭ��";
		} else if ("Q_TIME".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "ʱ��";
		} else if ("Q_NUMBER".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "";
		} else {
			answerType = "";
		}
		
		// #1 query, ֻ���������ͺ�����ʵ��
		StringBuilder sb = new StringBuilder();
		float score = 1.0f;
		if (!answerType.equals(""))
			sb.append(answerType + " ");
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH) {
				sb.append(kw.keyword + " ");
			}
		}
		queries.add(new Query(analyzedQuestion, sb.toString(), score));
		
		// #2 query, ֻ���������͡�����ʵ�塣����&����
		sb.delete(0, sb.length());
		score = 1.0f;
		if (!answerType.equals(""))
			sb.append(answerType + " ");
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH
					|| kw.weight == KeywordExtractor.KEYWORD_WEIGHT_MEDIUM) {
				sb.append(kw.keyword + " ");
			}
		}
		queries.add(new Query(analyzedQuestion, sb.toString(), score));
		
		// #3 query, ֻ���������͡�����ʵ�塢���⽹��
		sb.delete(0, sb.length());
		score = 1.0f;
		if (!answerType.equals(""))
			sb.append(answerType + " ");
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH) {
				sb.append(kw.keyword + " ");
			}
		}
		sb.append(analyzedQuestion.getFocus());
		queries.add(new Query(analyzedQuestion, sb.toString(), score));
		
		// #4 query, ֻ��������ʵ������⽹��
		sb.delete(0, sb.length());
		score = 1.0f;
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH) {
				sb.append(kw.keyword + " OR ");
			}
		}
		sb.append(analyzedQuestion.getFocus());
		queries.add(new Query(analyzedQuestion, sb.toString(), score));
		
		return queries;
	} 

}
