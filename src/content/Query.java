package content;

/*
 * 表示到搜索引擎的查询
 */
public class Query {
	
	private AnalyzedQuestion mAnalyzedQuestion;	/* 分析处理后的问题 */
	
	private String mQueryString;				/* 提交到搜索引擎的查询串 */
	
	private float mScore;						/* 本查询的分值，为Answer Selection模块提供参考 */
												/* 查询串的独特性越强，分值越高 */
	
}
