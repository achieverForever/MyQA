package answerextraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.Segment;

import questionanalysis.KeywordExtractor.KeyWord;
import content.AnalyzedQuestion;
import content.Result;
import edu.hit.ir.ltp4j.NER;
import edu.hit.ir.ltp4j.Postagger;
import edu.hit.ir.ltp4j.Segmentor;

public class AnswerExtractor {
	
	private static Filter[] sFilters;
	
	private static List<Float> sScores; 
	
	private static Map<String, Float> sWordFrequency;
	
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
	
	/*
	 * 对搜索引擎返回的Results应用一系列的Filters进行过滤，返回
	 * 最多maxResults个结果，门限最小分值为minScore
	 * 
	 * @param results 搜索引擎返回的搜索结果
	 * @param maxResults 最大返回结果数
	 * @param minScore 返回结果的最低分值
	 */
	public static List<Result> extractTopN(List<Result> results, int maxResults,
			double minScore) {
		List<Result> newResults = new ArrayList<Result>();
		List<ScoredSentence> scoredSentences = new ArrayList<ScoredSentence>();

		// For each search result
		for(int i = 0; i < results.size(); i++) {
			// 分句
			List<String> sentences = DocumentProcessor.splitSentence(
					results.get(i).getSnippet());
			// 计算每个句子的得分 
			for (String sent : sentences) {
				ScoredSentence scoredSent = computeScore(results.get(i).getQuery()
						.getAnalyzedQuestion(), sent, sentences, i);
				scoredSentences.add(scoredSent);
			}
		}
		
		// 选出得分最高的maxResults个句子
		Collections.sort(scoredSentences, new Comparator<ScoredSentence>() {
			@Override
			public int compare(ScoredSentence sent1, ScoredSentence sent2) {
				return Double.compare(sent2.score, sent1.score);
			}
		});
		for (int j = 0; j < maxResults; j++) {
			Result r = results.get(scoredSentences.get(j).docId);
			r.setScore(scoredSentences.get(j).score);
			r.setAnswer(scoredSentences.get(j).answer);
			newResults.add(r);
		}
		
		return newResults;
	}
	
	public static ScoredSentence computeScore(AnalyzedQuestion analyzedQuestion,
			String sent, List<String> sentences, int docId) {
		// 先对句子进行分词和词性标注
		List<String> words = new ArrayList<String>();
		Segmentor.segment(sent, words);
		List<String> tags = new ArrayList<String>();
		Postagger.postag(words, tags);
		
		// 规范化关键词的权重
		double sum = 0.0f;
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			sum += kw.weight;
		}
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			kw.weight = kw.weight / sum;
		} 
		
		// 计算该句的得分
		double score = 0.0f;
		for (KeyWord kw : analyzedQuestion.getKeyWords()) {
			if (words.contains(kw.keyword)) {
				score += kw.weight * computeTF(kw.keyword, sentences);
			}
		}
		
		String answer = "";
		List<String> nes = new ArrayList<String>();
		if ("Q_PERSON".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的人名作为问题的答案
			NER.recognize(words, tags, nes);
			for (int i = 0; i < nes.size(); i++) {
				if (nes.get(i).equals("S-Nh")) { 
					answer = words.get(i);
					break;
				}
			}
		} else if ("Q_LOCATION".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的地名作为问题的答案
			NER.recognize(words, tags, nes);
			for (int i = 0; i < nes.size(); i++) {
				if (nes.get(i).equals("S-Ns")) { 
					answer = words.get(i);
					break;
				}
			}
		} else if ("Q_REASON".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的原因作为答案
			// TODO
		} else if ("Q_TIME".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的原因作为答案
			// TODO
		} else if ("Q_NUMBER".equals(analyzedQuestion.getQuestionType().getType())) {
			// 提取出句子中的数字作为答案
			// TODO
		}
		
		// 问题类别不明或句中没有想要的命名实体，返回整句作为答案
		if (answer.equals("")) {
			answer = sent;
		}
		return new ScoredSentence(sent, score, docId, answer);
	}
	
	private static double computeTF(String term, List<String> doc) {
		List<String> allWords = new ArrayList<String>();
		for (String sent : doc) {
			List<String> words = new ArrayList<String>();
			Segmentor.segment(sent, words);
			allWords.addAll(words);
		}
		return (double) Collections.frequency(allWords, term) / allWords.size();
	}
	
	private static double computeIDF(String term) {
		if (!sWordFrequency.containsKey(term)) {
			// 语料库中没有该词，赋予一个很大的IDF
			return Math.log(1 / 0.0000001);
		} else {
			return Math.log(1 / sWordFrequency.get(term));
		}
	}
	
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
	}
	
	public static void main(String[] args) {
		initialize();
	}
}
