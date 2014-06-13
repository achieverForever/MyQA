package content;

import java.util.Arrays;

/*
 * 表示问题的预期答案类型
 */
public class QuestionType {

	private float confidence;			/* 可信度 */
	
	private String type;				/* 预期答案的主类别 */
	
	private QuestionType[] subTypes;	/* 预期答案的子类别 */

	public QuestionType(float confidence, String type, QuestionType[] subTypes) {
		super();
		this.confidence = confidence;
		this.type = type;
		this.subTypes = subTypes;
	}

	public float getConfidence() {
		return confidence;
	}

	public String getType() {
		return type;
	}

	public QuestionType[] getSubTypes() {
		return subTypes;
	}

	@Override
	public String toString() {
		return type;
	}
}
