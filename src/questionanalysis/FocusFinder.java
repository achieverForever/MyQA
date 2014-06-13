package questionanalysis;

import java.util.List;

public class FocusFinder {
	
	/*
	 * 提取出问题的焦点
	 * 
	 * @param words 表示已经分好词的问题串
	 * @param tags 表示对问题串词性标注的结果
	 * @return String 表示问题的焦点
	 */
	public static String findFocus(List<String> words, List<String> tags) {
		
		int quesPos = tags.indexOf("r");
		if (quesPos < 0) {
			return "";
		}
		if (quesPos == 0) {
			// 疑问词是第一个词，寻找离疑问词最近的名词作为中心词
			for (int i = 1; i < words.size(); i++) {
				if (tags.get(i).equals("n")) {
					return words.get(i);
				}
			}
			return "";
		} else if (quesPos == (words.size() - 1)) {
			// 疑问词是最后一个词，寻找离疑问词最近的名词作为中心词
			for (int i = words.size() - 2; i >= 0; i--) {
				if (tags.get(i).equals("n")) {
					return words.get(i);
				}
			}
			return "";
		} else {
			// 疑问词出现在其它位置，从疑问词位置往后搜索，把搜索到的名词作为中心词
			for (int i = quesPos + 1; i < words.size(); i++) {
				if (tags.get(i).equals("n")) {
					return words.get(i);
				}
			}
			return "";
		}
	}
}
