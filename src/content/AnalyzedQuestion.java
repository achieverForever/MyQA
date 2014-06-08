package content;

/*
 * 表示经过语法和语义分析处理后的问题
 */
public class AnalyzedQuestion {
	
	private String mFocus;				/* 问题的焦点 */
	
	private String mAnswerType;			/* 预期答案的类型 */
	
	private String[] mKeyWords;			/* 提取出来的关键词 */
		
	private String[][] mNamedEntities;	/* 提取出来的命名实体 */
	
	private String mQuestion;			/* 原始问题串 */
	
}
