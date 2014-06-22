package content;

/*
 * ��ʾ����������Ĳ�ѯ
 */
public class Query {
	
	private AnalyzedQuestion analyzedQuestion;	/* �������������� */
	
	private String queryString;					/* �ύ����������Ĳ�ѯ�� */
	
	private float score;						/* ����ѯ�ķ�ֵ��ΪAnswer Selectionģ���ṩ�ο� */
												/* ��ѯ���Ķ�����Խǿ����ֵԽ�� */
	
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
