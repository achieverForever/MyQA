package content;

/*
 * 表示QA系统最终返回给用户的结果
 */
public class Result {
	
	private String answer;		/* 最终答案串 */
	
	private String sentence;	/* 答案所在的原句 */
	
	private String snippet;		/* 答案所在的文档片段 */
				
	private String url;			/* 答案所在网页的URL */
	
	private int ranking;		/* 答案所在文档在搜素结果中的排名 */
	
	private Query query;		/* 获得此结果的Query */
	
	private double score;		/* 此答案的分值 */
	
	private double normScore;	/* 规范化的分值 */
	

	public Result(String snippet, String url, int ranking, Query query) {
		this.snippet = snippet;
		this.url = url;
		this.ranking = ranking;
		this.query = query;
	}
	
	public Result(Result copy) {
		this.snippet = copy.snippet;
		this.url = copy.url;
		this.ranking = copy.ranking;
		this.query = copy.query;
	}

	public Result(String answer, String sentence, String snippet, String url,
			int ranking, Query query, double score, double normScore) {
		this(snippet, url, ranking, query);
		this.answer = "";
		this.sentence = "";
		this.score = 0.0;
		this.normScore = 0.0;
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

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	@Override
	public String toString() {
		return "Result { \n\tanswer:" + answer + ", \n\tscore:" + score 
				+ ", \n\tsentence:" + sentence + ", \n\turl:" + url
				+ ", \n\tranking:" + ranking + ", \n\tquery:" + query+ "\n}";
	}
}
