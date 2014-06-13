package querygeneration;

import java.util.ArrayList;
import java.util.List;

import questionanalysis.KeywordExtractor;
import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.Query;

public class QueryGenerator {
	
	/*
	 * 从一个经过分析的问题中生成搜索引擎的查询串
	 * 
	 * @param analyzedQuestion 经过分析处理的问题
	 * @return queries 表示返回的查询
	 */
	public static List<Query> generateQueries(AnalyzedQuestion analyzedQuestion) {
		List<Query> queries = new ArrayList<Query>();
		String answerType = "";
		if ("Q_PERSON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "";
		} else if ("Q_LOCATION".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "位置";
		} else if ("Q_REASON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "原因";
		} else if ("Q_TIME".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "时间";
		} else if ("Q_NUMBER".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "";
		} else {
			answerType = "";
		}
		
		// #1 query, 只包括答案类型和命名实体
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
		
		// #2 query, 只包括答案类型、命名实体。动词&名词
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
		
		// #3 query, 只包括答案类型、命名实体、问题焦点
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
		
		// #4 query, 只包括命名实体或问题焦点
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
