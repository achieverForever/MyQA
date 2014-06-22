package content;

/*
 * 表示到搜索引擎的查询
 */
public class Query {
	
	private AnalyzedQuestion analyzedQuestion;	/* 分析处理后的问题 */
	
	private String queryString;					/* 提交到搜索引擎的查询串 */
	
	private float score;						/* 本查询的分值，为Answer Selection模块提供参考 */
												/* 查询串的独特性越强，分值越高 */
	
	public Query(AnalyzedQuestion analyzedQuestion, String queryString,
			float score) {
		this.analyzedQuestion = analyzedQuestion;
		this.queryString = queryString;
		this.score = score;
	}

	public AnalyzedQuestion getAnalyzedQuestion() {
		return analyzedQuestion;
	}

	public String getQueryString() {
		return queryString;
	}

	public float getScore() {
		return score;
	}

	@Override
	public String toString() {
		return "'" + queryString + "'";
	}

	@Override
	public int hashCode() {
		return queryString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} 
		if (!(obj instanceof Query)) {
			return false;
		}
		return ((Query)obj).queryString.equals(this.queryString);
	}
	
	
}
