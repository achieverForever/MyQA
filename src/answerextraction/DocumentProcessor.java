package answerextraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DocumentProcessor {
	
	public static List<String> splitSentence(String doc) {
		List<String> sentences = new ArrayList<String>();
		String[] sents = doc.split("[。？！]+");
		for (String sent : sents) {
			sentences.add(sent.trim());
		}
		return sentences;
	}
	
	public static void main(String[] args) {
		String doc = "自动问答系统给某个提问提供简单而精确回答，与信息检索任务和与信息提取任务极为不同。目前的信息检索系统能让我们对与提问切题的相关文献进行定位，把从文本的等级列表中抽取答案的任务留给用户。在信息检索中，相关文本的识别是使用将提问与文献集匹配的方法来实现的，信息检索系统并不负责回答用户的问题。信息抽取与信息检索不同，信息抽取系统抽取的东西是用户感兴趣的信息，抽取的条件是信息已经存在于预先规定的被称为模板的目标表现形式中。从总体上，信息抽取系统在一个与提取任务相关的文献集合上操作。信息抽取系统在完成抽取的任务时，可以成功地组拼模板。";
	}
}
