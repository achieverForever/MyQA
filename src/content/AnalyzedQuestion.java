package content;

import java.util.List;

import questionanalysis.KeywordExtractor.KeyWord;

/*
 * 表示经过语法和语义分析处理后的问题
 */
public class AnalyzedQuestion {
	
	private String focus;								/* 问题的焦点 */
	
	private QuestionType questionType;					/* 问题类型 */

	private List<KeyWord> keyWords;						/* 提取出来的关键词 */

	private List<KeyWord> expandedKeyWords;		/* 可扩展关键词 */
		
	private String question;							/* 原始问题串 */
	
	private List<String> words;							/* 分词的结果 */
	
	private List<String> tags;							/* 词性 */

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
