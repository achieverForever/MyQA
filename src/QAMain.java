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
			AnalyzedQuestion aq = QuestionAnalyzer.analyze("�������ж����˿ڣ�");
			String snippet = "7��23�գ��������˴�ί���˿�ר������鹫��������2009��ױ������˿������Ѿ��ﵽ1972�����л����˿�1246�򣬵Ǽ������˿�763.8�򡣶���2005��1�¹���Ժͨ���ġ�������������滮��2004�ꡪ2020�꣩������ʱȷ����2020�걱�����˿���������Ŀ��Ϊ1800�������ҡ�������������֣�1972�򣩻�����صĹ��ƣ���Ϊ�ܶ������˿�ͳ�Ʋ��������й������ѧ������˿�ѧԺ���ڵ������ڽ��ܡ��������ܿ����ɷ�ʱ˵����������ʷ�ϵ����˿ھ��ǡ��Ű���ս���ģ�ÿ�ζ���Ҫ�����������˿�����һ�����ƽ����͸�С������Ьһ����û�������ͻ�ơ���";
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
