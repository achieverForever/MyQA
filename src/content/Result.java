package content;

/*
 * 表示QA系统最终返回给用户的结果
 */
public class Result {
	
	private String mAnswer;		/* 最终答案串 */
	
	private String mSentence;	/* 答案所在的原句 */
	
	private String mSnippet;	/* 答案所在的文档片段 */
				
	private String mUrl;		/* 答案所在网页的URL */
	
	private Query mQuery;		/* 获得此结果的Query */
	
	private float mScore;		/* 结果的分值 */
	
	private float mNormScore;	/* 规范化的分值 */
	
}
