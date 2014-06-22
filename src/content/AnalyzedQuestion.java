package content;

import java.util.List;

import questionanalysis.KeywordExtractor.KeyWord;

/*
 * ��ʾ�����﷨�������������������
 */
public class AnalyzedQuestion {
	
	private String focus;								/* ����Ľ��� */
	
	private QuestionType questionType;					/* �������� */

	private List<KeyWord> keyWords;						/* ��ȡ�����Ĺؼ��� */

	private List<KeyWord> expandedKeyWords;		/* ����չ�ؼ��� */
		
	private String question;							/* ԭʼ���⴮ */
	
	private List<String> words;							/* �ִʵĽ�� */
	
	private List<String> tags;							/* ���� */

	public AnalyzedQuestion(String focus, QuestionType questionType,
			List<KeyWord> keyWords, List<KeyWord> expandedKeyWords,
			String question, List<String> words, List<String> tags) {
		this.focus = focus;
		this.questionType = questionType;
		this.keyWords = keyWords;
		this.expandedKeyWords = expandedKeyWords;
		this.question = question;
		this.words = words;
		this.tags = tags;
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

	public List<KeyWord> getExpandedKeyWords() {
		return expandedKeyWords;
	}

	public String getQuestion() {
		return question;
	}

	public List<String> getWords() {
		return words;
	}
	
	public List<String> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		return "AnalyzedQuestion [focus=" + focus + ", questionType="
				+ questionType + ", keyWords=" + keyWords
				+ ", expandedKeyWords=" + expandedKeyWords + ", question="
				+ question + "]";
	}

}
