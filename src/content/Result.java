package content;

/*
 * 表示QA系统最终返回给用户的结果
 */
public class Result {
	
	private String answer;		/* 最终答案串 */
	
	private String sentence;	/* 答案所在的原句 */
	
	private String snippet;	/* 答案所在的文档片段 */
				
	private String url;		/* 答案所在网页的URL */
	
	private Query query;		/* 获得此结果的Query */
	
	private double score;		/* 结果的分值 */
	
	private double normScore;	/* 规范化的分值 */

	public Result(String answer, String sentence, String snippet, String url,
			Query query, double score, double normScore) {
		this.answer = answer;
		this.sentence = sentence;
		this.snippet = snippet;
		this.url = url;
		this.query = query;
		this.score = score;
		this.normScore = normScore;
	}

	public String getAnswer() {
		return answer;
	}

	public String getSentence() {
		return sentence;
	}

	public String getSnippet() {
		return snippet;
	}

	public String getUrl() {
		return url;
	}

	public Query getQuery() {
		return query;
	}

	public double getScore() {
		return score;
	}

	public double getNormScore() {
		return normScore;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setNormScore(double normScore) {
		this.normScore = normScore;
	}
}
