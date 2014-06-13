package content;

/*
 * ��ʾQAϵͳ���շ��ظ��û��Ľ��
 */
public class Result {
	
	private String answer;		/* ���մ𰸴� */
	
	private String sentence;	/* �����ڵ�ԭ�� */
	
	private String snippet;	/* �����ڵ��ĵ�Ƭ�� */
				
	private String url;		/* ��������ҳ��URL */
	
	private Query query;		/* ��ô˽����Query */
	
	private double score;		/* ����ķ�ֵ */
	
	private double normScore;	/* �淶���ķ�ֵ */

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
