package querygeneration;

import java.util.ArrayList;
import java.util.List;

import questionanalysis.KeywordExtractor;
import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.Query;

public class QueryGenerator {
	
	public static final int MAX_QUERIES = 10;		/* ���Ʋ�ѯ�ʵ��������Ϊ10 */
	private static final String SITE = "";	/* ֻ����ĳ����վ */
	
	/*
	 * ��һ������������������������������Ĳ�ѯ��
	 * 
	 * @param analyzedQuestion �����������������
	 * @return queries ��ʾ���ɵĲ�ѯ
	 */
	// TODO: Ӧ�ø��費ͬ�Ĳ�ѯ����ͬ��Ȩ�أ������������ʵ���Ӧ�ñ�û������ʵ���Ȩ�ظ���Щ
	public static List<Query> generateQueries(AnalyzedQuestion analyzedQuestion) {
		List<Query> queries = new ArrayList<Query>();
		Query q;
		String answerType = "";
		int count = 0;
		if ("Q_PERSON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "";
		} else if ("Q_LOCATION".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "λ��";
		} else if ("Q_REASON".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "ԭ��";
		} else if ("Q_TIME".equals(analyzedQuestion.getQuestionType().getType())) {
			answerType = "ʱ��";
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
		
		// #1 query, ֻ���������ͺ�����ʵ��
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
		
		// #2 query, ֻ���������͡�����ʵ�塢����&����
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
		
		// #3 query, ֻ���������͡�����ʵ�塢���⽹��
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
		
		// #4 query, ֻ��������ʵ������⽹��
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
		
		// #5 ����ԭ�� @_@
		score = 1.0f;
		q = new Query(analyzedQuestion, analyzedQuestion.getQuestion() + SITE, score);
		if (!queries.contains(q)) {
			queries.add(q);
		}

		return queries;
	} 

}
