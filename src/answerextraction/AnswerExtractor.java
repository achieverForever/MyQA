package answerextraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.Result;
import edu.hit.ir.ltp4j.NER;
import edu.hit.ir.ltp4j.Postagger;
import edu.hit.ir.ltp4j.Segmentor;

public class AnswerExtractor {
	
	private static Map<String, Float> sWordFrequency;
	
	private static final int MAX_SEARCH_RESULTS = 10;
	
	/*
	 * ���������淵�ص��ĵ����Ƚ��з־䣬�ٽ��TFIDF�͹ؼ���Ȩ�ؼ���ÿһ���
	 * ��ֵ�������ȡ����ֵ��ߵĲ�����topN�����
	 * 
	 * @param results �������淵�ص��������
	 * @param topN ��󷵻ؽ����
	 * @param minScore ���ؽ������ͷ�ֵ
	 * @return new_ ��ʾ��ֵΪtopN�Ĵ�
	 */
	public static List<Result> extractTopN(List<Result> results, int topN, double minScore) {
		List<Result> newResults = new ArrayList<Result>();
		List<ScoredSentence> scoredSentences = new ArrayList<ScoredSentence>();
		
		System.out.print("Extracting answers...");

		for(int i = 0; i < results.size(); i++) {
			// �־�
			List<String> sentences = DocumentProcessor.splitSentence(results.get(i)
					.getSnippet());
			// ����ÿ�����ӵĵ÷� 
			for (String sent : sentences) {
				ScoredSentence scoredSent = computeScore(results.get(i).getQuery()
						.getAnalyzedQuestion(), results.get(i).getRanking(), sent,
						sentences, i);
				scoredSentences.add(scoredSent);
			}
		}
		
		// ѡ���÷���ߵ�topN������
		Collections.sort(scoredSentences, new Comparator<ScoredSentence>() {
			@Override
			public int compare(ScoredSentence sent1, ScoredSentence sent2) {
				return Double.compare(sent2.score, sent1.score);
			}
		});
		int n = scoredSentences.size() < topN ? scoredSentences.size() : topN;
		for (int j = 0; j < n; j++) {
			Result old = results.get(scoredSentences.get(j).docId);
			Result new_ = new Result(old);
			new_.setSentence(scoredSentences.get(j).sentence);;
			new_.setScore(scoredSentences.get(j).score);
			new_.setAnswer(scoredSentences.get(j).answer);
			newResults.add(new_);
		}
		
		System.out.print(" Done.\n");
		return newResults;
	}
	
	/*
	 * ����һ������������ӵķ�ֵ
	 * 
	 * @param analyzedQuestion �����������������
	 * @param ranking �þ��������ĵ�����������е�����
	 * @param sent ��Ҫ�����ֵľ���
	 * @param sentences �þ������ڵ��ĵ�
	 * @param docId ��������������Ա㹹���µ�Result
	 * @return ScoredSentence ��ʾ�Ѵ�ֵľ���
	 */
	// TODO - ���㷽ʽ����ѧ!
	public static ScoredSentence computeScore(AnalyzedQuestion analyzedQuestion, int ranking,
			String sent, List<String> sentences, int docId) {
		// �ȶԾ��ӽ��зִʺʹ��Ա�ע
		List<String> words = new ArrayList<String>();
		Segmentor.segment(sent, words);
		List<String> tags = new ArrayList<String>();
		Postagger.postag(words, tags);
				
		// ����þ�ĵ÷�
		double score = 0.0f, attenuation = 1.0;
		if (ranking > 1) {
			attenuation = 1.0 - (ranking / MAX_SEARCH_RESULTS) * (ranking / MAX_SEARCH_RESULTS)
					* (ranking / MAX_SEARCH_RESULTS);
		}
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (words.contains(kw.keyword)) {
				score += kw.weight * computeTF(kw.keyword, sentences) 
						* computeIDF(kw.keyword) * attenuation;
			}
		}
		//�����������ͳ�ȡ��
		String answer = "";
		List<String> nes = new ArrayList<String>();
		if ("Q_PERSON".equals(analyzedQuestion.getQuestionType().getType())) {
			// ��ȡ�������е�������Ϊ����Ĵ�
			NER.recognize(words, tags, nes);
			for (int i = 0; i < nes.size(); i++) {
				if (nes.get(i).equals("S-Nh")) { 
					answer = words.get(i);
					score += 20.0;
					break;
				}
			}
		} else if ("Q_LOCATION".equals(analyzedQuestion.getQuestionType().getType())) {
			// ��ȡ�������еĵ�����Ϊ����Ĵ�
			StringBuilder sb = new StringBuilder();
			NER.recognize(words, tags, nes);
			boolean containsLocation = false;
			for (int i = 0; i < nes.size(); i++) {
				if (nes.get(i).matches("Ns")) {
					containsLocation = true;
					sb.append(words.get(i));
				}
			} 
			if (containsLocation) {
				score += 20.0;
			}
			answer = sb.toString();
		} else if ("Q_REASON".equals(analyzedQuestion.getQuestionType().getType())) {
			// ��ȡ�������е�ԭ����Ϊ�𰸣����䷵��
		} else if ("Q_TIME".equals(analyzedQuestion.getQuestionType().getType())) {
			// ��ȡ�������е�ʱ����Ϊ��
			answer = extractTime(words, tags);
		} else if ("Q_NUMBER".equals(analyzedQuestion.getQuestionType().getType())) {
			// ��ȡ�������е�������Ϊ��
			answer = extractNumber(words, tags);
		}
		
		// ��������������û����Ҫ������ʵ�壬����������Ϊ��
		if (answer.equals("")) {
			answer = sent;
		}
		return new ScoredSentence(sent, score, docId, answer);
	}
	
	/*
	 * ��������ʵ����ĵ�doc�еĴ�ƵTF
	 * 
	 * @param term �����Ĵ�
	 * @param doc ��ʾ�ô����ڵ��ĵ�
	 * @return �����ʵĴ�Ƶ
	 */
	private static double computeTF(String term, List<String> doc) {
		List<String> allWords = new ArrayList<String>();
		for (String sent : doc) {
			List<String> words = new ArrayList<String>();
			Segmentor.segment(sent, words);
			allWords.addAll(words);
		}
		return (double) Collections.frequency(allWords, term) / allWords.size();
	}
	
	/*
	 * ��������ʵ����ĵ�ƵIDFֵ
	 */
	private static double computeIDF(String term) {
		if (!sWordFrequency.containsKey(term)) {
			// ���Ͽ���û�иôʣ�����һ���ܴ��IDF
			return Math.log(1 / 0.0000001);
		} else {
			return Math.log(1 / sWordFrequency.get(term));
		}
	}
	
	/*
	 * ��ȡ�������е�ʱ��
	 */
	public static String extractTime(List<String> words, List<String> tags) {
		StringBuilder sb = new StringBuilder();
		int currLen = -1;
		int beg = 0, end = 0, start = 0;
		Set<String> set = new HashSet<String>();
		set.add("nt");
		set.add("m");
		set.add("q");
		set.add("n");
		
		// ������ȡ����Ĵ���Ϊm, q, n��
		while (true) {
			if (end == tags.size() || (beg = findFirstOf(tags, end, set)) == -1) {
				break;
			}
			end = findFirstNotOf(tags, beg, set);
			if (currLen < end - beg) {
				currLen = end - beg;
				start = beg;
			}
		}
		for (int i = start; i < start + currLen; i++) {
			sb.append(words.get(i));
		}
		return sb.toString();
	}

	/*
	 * ��ȡ�������е�������Ϣ
	 */
	public static String extractNumber(List<String> words, List<String> tags) {
		// �Էִʽ��е������Ա����ȷ����ȡ��ʱ�䣬��"1995��/10��/6��"��һ��ϸ��
		// Ϊ"1995/��/10/��/6/��"
		List<String> modifiedWords = new ArrayList<String>();
		List<String> modifiedTags = new ArrayList<String>();
		StringBuilder sb2 = new StringBuilder();
		Pattern p = Pattern.compile("[0-9|һ|��|��|��|��|��|��|��|��|ʮ]+");
		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			Matcher m = p.matcher(word);
			int lastEnd = 0;
			if (m.find()) {
				do {
					if (m.start(0) > lastEnd) {
						sb2.append(word.substring(lastEnd, m.start(0)) + "/");
					}
					sb2.append(m.group(0) + "/");
					lastEnd = m.end(0);
				} while (m.find());
				if (lastEnd < word.length()) {
					sb2.append(word.substring(lastEnd, word.length()) + "/");
				}
			} else {
				sb2.append(word + "/");
			}
		}
		String[] ws = sb2.toString().split("/");
		for (String w : ws) {
			if (!w.equals("")) {
				modifiedWords.add(w);
			}
		}
		Postagger.postag(modifiedWords, modifiedTags);
/*		for (int i = 0; i < modifiedTags.size(); i++) {
			System.out.print(modifiedWords.get(i) + "/" + modifiedTags.get(i) + "  ");
		}
		System.out.println();*/
		
		StringBuilder sb = new StringBuilder();
		int currLen = -1;
		int beg = 0, end = 0, start = 0;
		Set<String> set = new HashSet<String>();
		set.add("m");
		set.add("q");
		Set<String> set2 = new HashSet<String>();
		set2.add("��");
		set2.add("��");
		set2.add("��");
		set2.add("��");
		set2.add("ʱ");
		set2.add("��");
		
		// ������ȡ����Ĵ���Ϊnt, m, q, n�������޳���"������ʱ��"�Ĵ�
		while (true) {
			if (end == modifiedTags.size() || (beg = findFirstOf(modifiedTags, end, set)) == -1) {
				break;
			}
			end = findFirstNotOf(modifiedTags, beg, set);
			boolean next = false;
			for (int i = beg; i < end; i++) {
				if (set2.contains(modifiedWords.get(i))) {
					next = true;
				}
			}
			if (currLen < end - beg && !next) {
				currLen = end - beg;
				start = beg;
			}
		}
		for (int i = start; i < start + currLen; i++) {
			sb.append(modifiedWords.get(i));
		}
		return sb.toString();
	}
	
	/*
	 * ���ߺ�������list��startPos�±괦��ʼ���ҵ���һ�������ڼ���set�е�Ԫ�����ڵ��±ꣻ
	 * ���û�У�����-1
	 */
	private static int findFirstOf(List<String> list, int startPos, Set<String> set) 
		throws IndexOutOfBoundsException {
		
		if (startPos < 0 || startPos >= list.size()) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = startPos; i < list.size(); i++) {
			if (set.contains(list.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	/*
	 * ���ߺ�������list��startPos�±괦��ʼ���ҵ���һ���������ڼ���set�е�Ԫ�����ڵ��±ꣻ
	 * ���û�У�list����һ��Ԫ�ص��±�+1
	 */
	private static int findFirstNotOf(List<String> list, int startPos, Set<String> set)
			throws IndexOutOfBoundsException {
			if (startPos < 0 || startPos >= list.size()) {
				throw new IndexOutOfBoundsException();
			}
			for (int i = startPos; i < list.size(); i++) {
				if (!set.contains(list.get(i))) {
					return i;
				}
			}
			return list.size();
		}
	
	/*
	 * ��ʾ�Ѵ�־��ӵ�ʵ����
	 */
	private static class ScoredSentence {
		public String sentence;
		public double score;
		public int docId;
		public String answer;
		
		public ScoredSentence(String sentence, double score, int docId,
				String answer) {
			this.sentence = sentence;
			this.score = score;
			this.docId = docId;
			this.answer = answer;
		}

		@Override
		public String toString() {
			return "ScoredSentence [sentence=" + sentence + ", score=" + score
					+ ", docId=" + docId + ", answer=" + answer + "]";
		}
	}
	
	public static boolean initialize() {
		sWordFrequency = new HashMap<String, Float>();
		if (!loadWordFrequency("data/word_frequency.txt")) {
			return false;
		} else {
			return true;
		}
	}
	
	private static boolean loadWordFrequency(String filename) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "GBK"));
			String line;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split(",");
				sWordFrequency.put(tokens[0], Float.parseFloat(tokens[2]
						.replaceAll("%", "")) * 0.01f);
			}
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
		return true;
	}
	
	public static void main(String[] args) {
		initialize();
		if (Segmentor.create("data/models/cws.model") < 0) {
			System.out.println("Failed to initialize Segmentor");
		}
		if (Postagger.create("data/models/pos.model") < 0) {
			System.out.println("Failed to initialize PosTagger");
		}

		String line = "7��23�գ�����������һ����ʱ����ٰ�ʮ��Ա�˿ڣ�����2009��ױ������˿������Ѿ��ﵽ1972��";
		List<String> words = new ArrayList<String>();
		Segmentor.segment(line, words);
		List<String> tags = new ArrayList<String>();
		Postagger.postag(words, tags); 
		System.out.println(extractTime(words, tags));
	}
}
