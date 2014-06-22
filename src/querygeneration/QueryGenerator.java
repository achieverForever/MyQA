package querygeneration;

import java.util.ArrayList;
import java.util.List;

import questionanalysis.KeywordExtractor;
import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.Query;

public class QueryGenerator {
	
	public static final int MAX_QUERIES = 10;		/* 限制查询词的最大数量为10 */
	private static final String SITE = "";	/* 只搜索某个网站 */
	
	/*
	 * 从一个经过分析的问题中生成搜索引擎的查询串
	 * 
	 * @param analyzedQuestion 经过分析处理的问题
	 * @return queries 表示生成的查询
	 */
	// TODO: 应该赋予不同的查询穿不同的权重，如带有有命名实体的应该比没有命名实体的权重更高些
	public static List<Query> generateQueries(AnalyzedQuestion analyzedQuestion) {
		List<Query> queries = new ArrayList<Query>();
		Query q;
		String answerType = "";
		int count = 0;
		if ("Q_PERSON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "";
		} else if ("Q_LOCATION".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "位置";
		} else if ("Q_REASON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "原因";
		} else if ("Q_TIME".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "时间";
		} else if ("Q_NUMBER".equals(analyzedQuestion.getQuestionType().getType())) {
			int i;
			if ((i = analyzedQuestion.getTags().indexOf("r")) != -1) {
				answerType = analyzedQuestion.getWords().get(i);
			} else {
				answerType = "";
			}
		} else {
			answerType = "";
		}
		
		// #1 query, 只包括答案类型和命名实体
		StringBuilder sb = new StringBuilder();
		float score = 1.0f;
		if (!answerType.equals("")) { 
			sb.append(answerType + " ");
			count++;
		}
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH) {
				if (count < MAX_QUERIES) {
					sb.append(kw.keyword + " ");
					count++;
				}
			}
		}
		if (!sb.toString().trim().equals("")) {
			q = new Query(analyzedQuestion, sb.toString() + SITE, score);
			if (!queries.contains(q)) {
				queries.add(q);
			}
		}
		
		// #2 query, 只包括答案类型、命名实体、动词&名词
		count = 0;
		sb.delete(0, sb.length());
		score = 1.0f;
		if (!answerType.equals("")) {
			sb.append(answerType + " ");
			count++;
		}
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH
					|| kw.weight == KeywordExtractor.KEYWORD_WEIGHT_MEDIUM) {
				if (count < MAX_QUERIES) {
					sb.append(kw.keyword + " ");
					count++;
				}
			}
		}
		if (!sb.toString().trim().equals("")) {
			q = new Query(analyzedQuestion, sb.toString() + SITE, score);
			if (!queries.contains(q)) {
				queries.add(q);
			}
		}
		
		// #3 query, 只包括答案类型、命名实体、问题焦点
		count = 0;
		sb.delete(0, sb.length());
		score = 1.0f;
		if (!answerType.equals("")) {
			sb.append(answerType + " ");
			count++;
		}
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH) {
				if (count < MAX_QUERIES - 1) {
					sb.append(kw.keyword + " ");
					count++;
				}
			}
		}
		if (!analyzedQuestion.getFocus().trim().equals("")) {
			sb.append(analyzedQuestion.getFocus() + " ");
		}
		if (!sb.toString().trim().equals("")) {
			q = new Query(analyzedQuestion, sb.toString() + SITE, score);
			if (!queries.contains(q)) {
				queries.add(q);
			}
		}
		
		// #4 query, 只包括命名实体或问题焦点
		count = 0;
		sb.delete(0, sb.length());
		score = 1.0f;
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (kw.weight == KeywordExtractor.KEYWORD_WEIGHT_HIGH) {
				sb.append(kw.keyword + " ");
			}
		}
		if (!analyzedQuestion.getFocus().trim().equals("")) {
			sb.append(analyzedQuestion.getFocus() + " ");
		}
		if (!sb.toString().trim().equals("")) {
			q = new Query(analyzedQuestion, sb.toString() + SITE, score);
			if (!queries.contains(q)) {
				queries.add(q);
			}
		}
		
		for (int i = 0; i < queries.size(); i++) {
			if (queries.get(i).getQueryString().trim().equals("")) {
				queries.remove(i);
			}
		}
		
		// #5 问题原句 @_@
		score = 1.0f;
		q = new Query(analyzedQuestion, analyzedQuestion.getQuestion() + SITE, score);
		if (!queries.contains(q)) {
			queries.add(q);
		}

		return queries;
	} 

}
