package content;

import java.util.List;
import java.util.Map;

import questionanalysis.KeywordExtractor.KeyWord;

/*
 * 表示经过语法和语义分析处理后的问题
 */
public class AnalyzedQuestion {
	
	private String focus;								/* 问题的焦点 */
	
	private QuestionType questionType;					/* 问题类型 */

	private List<KeyWord> keyWords;						/* 提取出来的关键词 */

	private Map<KeyWord, KeyWord> expandedKeyWords;		/* 可扩展关键词 */
		
	private String question;							/* 原始问题串 */

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
