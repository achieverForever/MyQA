import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import answerextraction.AnswerExtractor;
import querygeneration.QueryGenerator;
import questionanalysis.KeywordExtractor;
import questionanalysis.QuestionAnalyzer;
import questionanalysis.QuestionClassifier;
import questionanalysis.SynonymExpander;
import content.AnalyzedQuestion;
import content.Query;
import edu.hit.ir.ltp4j.NER;
import edu.hit.ir.ltp4j.Postagger;
import edu.hit.ir.ltp4j.Segmentor;
import edu.hit.ir.ltp4j.SplitSentence;

public class QAMain {
	
	public static void main(String[] args) {
		if (!initializeAll()) {
			System.out.println("Failed to initialize QA");
			return;
		}
		
		Scanner in = new Scanner(System.in);
		while (true) {
			String line = in.next();
			if (line.equals("exit")) {
				break;
			}
			
			AnalyzedQuestion analyzedQuestion = QuestionAnalyzer.analyze(line);
			System.out.println(analyzedQuestion);
			
			List<Query> queries = QueryGenerator.generateQueries(analyzedQuestion);
			System.out.println(queries);
			
			System.out.println("�־䣺");
			List<String> sentences = new ArrayList<String>();
			SplitSentence.splitSentence(line, sentences);;
			System.out.println(sentences);
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
