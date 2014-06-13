package content;

import java.util.Arrays;

/*
 * ��ʾ�����Ԥ�ڴ�����
 */
public class QuestionType {

	private float confidence;			/* ���Ŷ� */
	
	private String type;				/* Ԥ�ڴ𰸵������ */
	
	private QuestionType[] subTypes;	/* Ԥ�ڴ𰸵������ */

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
