package content;

import java.util.List;
import java.util.Map;

import questionanalysis.KeywordExtractor.KeyWord;

/*
 * ��ʾ�����﷨�������������������
 */
public class AnalyzedQuestion {
	
	private String focus;								/* ����Ľ��� */
	
	private QuestionType questionType;					/* �������� */

	private List<KeyWord> keyWords;						/* ��ȡ�����Ĺؼ��� */

	private Map<KeyWord, KeyWord> expandedKeyWords;		/* ����չ�ؼ��� */
		
	private String question;							/* ԭʼ���⴮ */

	public AnalyzedQuestion(String focus, QuestionType questionType,
			List<KeyWord> keyWords, Map<KeyWord, KeyWord> expandedKeyWords,
			String question) {
		this.focus = focus;
		this.questionType = questionType;
		this.keyWords = keyWords;
		this.expandedKeyWords = expandedKeyWords;
		this.question = question;
	}

	public String getFocus() {
		return focus;
	}

	public QuestionType getQuestionType() {
		return questionType;
	}

	public List<KeyWord> getKeyWords() {
		return keyWords;
	}

	public Map<KeyWord, KeyWord> getExpandedKeyWords() {
		return expandedKeyWords;
	}

	public String getQuestion() {
		return question;
	}

	@Override
	public String toString() {
		return "AnalyzedQuestion [focus=" + focus + ", questionType="
				+ questionType + ", keyWords=" + keyWords
				+ ", expandedKeyWords=" + expandedKeyWords + ", question="
				+ question + "]";
	}
}
