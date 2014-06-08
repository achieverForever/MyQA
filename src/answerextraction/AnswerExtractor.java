package answerextraction;

import content.Result;

public class AnswerExtractor {
	
	private static Filter[] sFilters;
	
	/*
	 * 对搜索引擎返回的Results应用一系列的Filters进行过滤，返回
	 * 最多maxResults个结果，门限最小分值为minScore
	 * 
	 * @param results 搜索引擎返回的搜索结果
	 * @param maxResults 最大返回结果数
	 * @param minScore 返回结果的最低分值
	 */
	public static Result[] filterResults(Result[] results, int maxResults, float minScore) {
		return null;
	}
	
	public static void addFilter(Filter filter) {
		
	}	
}
