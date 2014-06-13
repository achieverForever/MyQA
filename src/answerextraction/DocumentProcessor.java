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
		String[] sents = doc.split("[������]+");
		for (String sent : sents) {
			sentences.add(sent.trim());
		}
		return sentences;
	}
	
	public static void main(String[] args) {
		String doc = "�Զ��ʴ�ϵͳ��ĳ�������ṩ�򵥶���ȷ�ش�����Ϣ�������������Ϣ��ȡ����Ϊ��ͬ��Ŀǰ����Ϣ����ϵͳ�������Ƕ������������������׽��ж�λ���Ѵ��ı��ĵȼ��б��г�ȡ�𰸵����������û�������Ϣ�����У�����ı���ʶ����ʹ�ý����������׼�ƥ��ķ�����ʵ�ֵģ���Ϣ����ϵͳ��������ش��û������⡣��Ϣ��ȡ����Ϣ������ͬ����Ϣ��ȡϵͳ��ȡ�Ķ������û�����Ȥ����Ϣ����ȡ����������Ϣ�Ѿ�������Ԥ�ȹ涨�ı���Ϊģ���Ŀ�������ʽ�С��������ϣ���Ϣ��ȡϵͳ��һ������ȡ������ص����׼����ϲ�������Ϣ��ȡϵͳ����ɳ�ȡ������ʱ�����Գɹ�����ƴģ�塣";
	}
}
