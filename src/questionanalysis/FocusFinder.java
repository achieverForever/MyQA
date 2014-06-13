package questionanalysis;

import java.util.List;

public class FocusFinder {
	
	/*
	 * ��ȡ������Ľ���
	 * 
	 * @param words ��ʾ�Ѿ��ֺôʵ����⴮
	 * @param tags ��ʾ�����⴮���Ա�ע�Ľ��
	 * @return String ��ʾ����Ľ���
	 */
	public static String findFocus(List<String> words, List<String> tags) {
		
		int quesPos = tags.indexOf("r");
		if (quesPos < 0) {
			return "";
		}
		if (quesPos == 0) {
			// ���ʴ��ǵ�һ���ʣ�Ѱ�������ʴ������������Ϊ���Ĵ�
			for (int i = 1; i < words.size(); i++) {
				if (tags.get(i).equals("n")) {
					return words.get(i);
				}
			}
			return "";
		} else if (quesPos == (words.size() - 1)) {
			// ���ʴ������һ���ʣ�Ѱ�������ʴ������������Ϊ���Ĵ�
			for (int i = words.size() - 2; i >= 0; i--) {
				if (tags.get(i).equals("n")) {
					return words.get(i);
				}
			}
			return "";
		} else {
			// ���ʴʳ���������λ�ã������ʴ�λ����������������������������Ϊ���Ĵ�
			for (int i = quesPos + 1; i < words.size(); i++) {
				if (tags.get(i).equals("n")) {
					return words.get(i);
				}
			}
			return "";
		}
	}
}
