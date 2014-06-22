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
	 * 对搜索引擎返回的文档，先进行分句，再结合TFIDF和关键词权重计算每一句的
	 * 分值，最后提取出分值最高的不超过topN个结果
	 * 
	 * @param results 搜索引擎返回的搜索结果
	 * @param topN 最大返回结果数
	 * @param minScore 返回结果的最低分值
	 * @return new_ 表示分值为topN的答案
	 */
	public static List<Result> extractTopN(List<Result> results, int topN, double minScore) {
		List<Result> newResults = new ArrayList<Result>();
		List<ScoredSentence> scoredSentences = new ArrayList<ScoredSentence>();
		
		System.out.print("Extracting answers...");

		for(int i = 0; i < results.size(); i++) {
			// 分句
			List<String> sentences = DocumentProcessor.splitSentence(results.get(i)
					.getSnippet());
			// 计算每个句子的得分 
			for (String sent : sentences) {
				ScoredSentence scoredSent = computeScore(results.get(i).getQuery()
						.getAnalyzedQuestion(), results.get(i).getRanking(), sent,
						sentences, i);
				scoredSentences.add(scoredSent);
			}
		}
		
		// 选出得分最高的topN个句子
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
	 * 根据一定方法算出句子的分值
	 * 
	 * @param analyzedQuestion 经过分析处理的问题
	 * @param ranking 该句子所在文档在搜索结果中的排名
	 * @param sent 将要对其打分的句子
	 * @param sentences 该句子所在的文档
	 * @param docId 索引搜索结果，以便构造新的Result
	 * @return ScoredSentence 表示已打分的句子
	 */
	// TODO - 计算方式不科学!
	public static ScoredSentence computeScore(AnalyzedQuestion analyzedQuestion, int ranking,
			String sent, List<String> sentences, int docId) {
		// 先对句子进行分词和词性标注
		List<String> words = new ArrayList<String>();
		Segmentor.segment(sent, words);
		List<String> tags = new ArrayList<String>();
		Postagger.postag(words, tags);
				
		// 计算该句的得分
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
		//根据问题类型抽取答案
		String answer = "";
		List<String> nes = new ArrayList<String>();
		if ("Q_PERSON".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的人名作为问题的答案
			NER.recognize(words, tags, nes);
			for (int i = 0; i < nes.size(); i++) {
				if (nes.get(i).equals("S-Nh")) { 
					answer = words.get(i);
					score += 20.0;
					break;
				}
			}
		} else if ("Q_LOCATION".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的地名作为问题的答案
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
			// 提取出句子中的原因作为答案，整句返回
		} else if ("Q_TIME".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的时间作为答案
			answer = extractTime(words, tags);
		} else if ("Q_NUMBER".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的数字作为答案
			answer = extractNumber(words, tags);
		}
		
		// 问题类别不明或句中没有想要的命名实体，返回整句作为答案
		if (answer.equals("")) {
			answer = sent;
		}
		return new ScoredSentence(sent, score, docId, answer);
	}
	
	/*
	 * 计算给定词的在文档doc中的词频TF
	 * 
	 * @param term 给定的词
	 * @param doc 表示该词所在的文档
	 * @return 给定词的词频
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
	 * 计算给定词的逆文档频IDF值
	 */
	private static double computeIDF(String term) {
		if (!sWordFrequency.containsKey(term)) {
			// 语料库中没有该词，赋予一个很大的IDF
			return Math.log(1 / 0.0000001);
		} else {
			return Math.log(1 / sWordFrequency.get(term));
		}
	}
	
	/*
	 * 提取出句子中的时间
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
		
		// 尝试提取出最长的词性为m, q, n串
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
	 * 提取出句子中的数量信息
	 */
	public static String extractNumber(List<String> words, List<String> tags) {
		// 对分词进行调整，以便更精确的提取出时间，如"1995年/10月/6日"进一步细分
		// 为"1995/年/10/月/6/日"
		List<String> modifiedWords = new ArrayList<String>();
		List<String> modifiedTags = new ArrayList<String>();
		StringBuilder sb2 = new StringBuilder();
		Pattern p = Pattern.compile("[0-9|一|二|三|四|五|六|七|八|九|十]+");
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
		set2.add("年");
		set2.add("月");
		set2.add("日");
		set2.add("号");
		set2.add("时");
		set2.add("分");
		
		// 尝试提取出最长的词性为nt, m, q, n串，但剔除含"年月日时分"的串
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
	 * 工具函数，从list的startPos下标处开始，找到第一个包含在集合set中的元素所在的下标；
	 * 如果没有，返回-1
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
	 * 工具函数，从list的startPos下标处开始，找到第一个不包含在集合set中的元素所在的下标；
	 * 如果没有，list最有一个元素的下标+1
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
	 * 表示已打分句子的实体类
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

		String line = "7月23日，北京市五月一号六时，五百八十七员人口：截至2009年底北京市人口总量已经达到1972万";
		List<String> words = new ArrayList<String>();
		Segmentor.segment(line, words);
		List<String> tags = new ArrayList<String>();
		Postagger.postag(words, tags); 
		System.out.println(extractTime(words, tags));
	}
}
