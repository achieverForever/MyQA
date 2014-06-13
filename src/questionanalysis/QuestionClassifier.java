package questionanalysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import content.QuestionType;

public class QuestionClassifier {
	
	public static final String[] QUESTION_TYPES = {
		"Q_PERSON",
		"Q_LOCATION",
		"Q_REASON",
		"Q_TIME",
		"Q_NUMBER",
		"Q_OTHERS"
	};
	
	private static Map<String, List<Pattern>> sQuestionTemplates;
	
	/*
	 * 对输入的问题串进行分类
	 * 
	 * @param question 问题串
	 * @return AnswerType 表示预期答案的类别
	 */
	public static QuestionType classify(String question) {
		System.out.println("正在对问题进行分类: " + question);
		for (String type : sQuestionTemplates.keySet()) {
			for (Pattern p : sQuestionTemplates.get(type)) {
				if (p.matcher(question).find()) {
					System.out.println("Matched " + type);
					return new QuestionType(1.0f, type, null);
				}
			}
		}
		System.out.println("Matched " + "Q_OTHERS");
		return new QuestionType(1.0f, "Q_OTHERS", null);
	}
	
	public static boolean initialize() {
		sQuestionTemplates = new HashMap<String, List<Pattern>>();
		if (!loadQuestionTemplates("data/question_types.txt"))
			return false;
		else 
			return true;
	}
	
	public static boolean loadQuestionTemplates(String filename) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split(":");
				String questionType = tokens[0].trim();
				String[] templates = tokens[1].trim().split("\\s+");
				List<Pattern> patterns = new ArrayList<Pattern>();
				for (String t : templates) 
					patterns.add(Pattern.compile(t));
				sQuestionTemplates.put(questionType, patterns); 
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	public static void main(String[] args) {
		initialize();
		System.out.println(sQuestionTemplates);
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String in = scanner.nextLine();
			if (in.equals("exit")) {
				break;
			}
			classify(in);
		}
	}
}
