package content;

/*
 * 表示问题的预期答案类型
 */
public class AnswerType {
	
	private double mConfidence;		/* 可信度 */
	
	private String mType;			/* 预期答案的主类别 */
	
	private AnswerType[] mSubTypes;	/* 预期答案的子类别 */
}
