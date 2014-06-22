import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import querygeneration.QueryGenerator;
import questionanalysis.KeywordExtractor;
import questionanalysis.QuestionAnalyzer;
import questionanalysis.QuestionClassifier;
import questionanalysis.SynonymExpander;
import ruc.irm.similarity.word.cilin.CilinCoding;
import ruc.irm.similarity.word.cilin.CilinDb;
import ruc.irm.similarity.word.hownet2.concept.XiaConceptParser;
import answerextraction.AnswerExtractor;
import content.AnalyzedQuestion;
import content.Query;
import content.Result;
import edu.hit.ir.ltp4j.NER;
import edu.hit.ir.ltp4j.Postagger;
import edu.hit.ir.ltp4j.Segmentor;

public class QAMain {
	
	public static void main(String[] args) {
		if (!initializeAll()) {
			System.out.println("Failed to initialize MyQA");
			return;
		}
		
		Scanner in = new Scanner(System.in);
		while (true) {
			String line = in.next();
			if (line.equals("exit")) {
				break;
			}
			
			// Test Question Analysis and Query Generation
/*			AnalyzedQuestion analyzedQuestion = QuestionAnalyzer.analyze(line);
			System.out.println(analyzedQuestion);
			
			List<Query> queries = QueryGenerator.generateQueries(analyzedQuestion);
			System.out.println("Generated Queries: ");
			System.out.println(queries);*/
			
			// Test Answer Extraction (numer and time)
/*			List<String> words = new ArrayList<String>();
			Segmentor.segment(line, words);
			List<String> tags = new ArrayList<String>();
			Postagger.postag(words, tags); 
			System.out.println(AnswerExtractor.extractTime(words, tags));
			System.out.println(AnswerExtractor.extractNumber(words, tags));*/
			
			// Test All
			System.out.println("Start");
			AnalyzedQuestion aq = QuestionAnalyzer.analyze("北京市有多少人口？");
			String snippet = "7月23日，北京市人大常委会人口专题调研组公布：截至2009年底北京市人口总量已经达到1972万。其中户籍人口1246万，登记流动人口763.8万。对照2005年1月国务院通过的《北京城市总体规划（2004年—2020年）》，当时确定的2020年北京市人口总量控制目标为1800万人左右。　　“这个数字（1972万）还是最保守的估计，因为很多流动人口统计不到。”中国人民大学社会与人口学院教授翟振武在接受《望东方周刊》采访时说，“北京历史上调控人口就是‘屡败屡战’的，每次定不要超过多少万，人口总是一步步逼近，就跟小孩儿穿鞋一样，没几年就又突破。”";
			System.out.println("Generated Queries: ");
			List<Query> queries = QueryGenerator.generateQueries(aq);
			System.out.println(queries);
			Result r = new Result(snippet, "url", 1, queries.get(0));
			List<Result> searchResults = new ArrayList<Result>();
			searchResults.add(r);
			List<Result> results = AnswerExtractor.extractTopN(searchResults, 5, -1000.0);
			for (Result rr : results) {
				System.out.println(rr);
			}
			
		}
		cleanupAll();
	}
	
	public static boolean initializeAll() {
		if (Segmentor.create("data/NEW/cws.model") < 0) {
			System.out.println("Failed to initialize Segmentor");
			return false;
		}
		if (Postagger.create("data/NEW/pos.model") < 0) {
			System.out.println("Failed to initialize PosTagger");
			return false;
		}
		if (NER.create("data/NEW/ner.model") < 0) {
			System.out.println("Failed to initialize NERecognizer");
			return false;
		}
		if (!KeywordExtractor.initialize()) {
			System.out.println("Failed to initialize KeywordExtractor");
			return false;
		}
		if (!SynonymExpander.initialize()) {
			System.out.println("Failed to initialize SynonymExpander");
			return false;
		}
		if (!QuestionClassifier.initialize()) {
			System.out.println("Failed to initialize QuestionClassifier");
			return false;
		}
		if (!AnswerExtractor.initialize()) {
			System.out.println("Failed to initialize QuestionClassifier");
			return false;
		}
		return true;
	}
	
	public static void cleanupAll() {
		Segmentor.release();
		Postagger.release();
		NER.release();
	}
}
