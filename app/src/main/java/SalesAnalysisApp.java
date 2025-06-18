package main.java;
import main.java.ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoSPAM;
import java.io.IOException;

/**
 * SalesAnalysisApp uses the SPAM algorithm to mine sequential patterns from sales transactions.
 * It reads a transaction file (formatted per SPMF standard) and writes the discovered patterns
 * along with statistics into an output file.
 */
public class SalesAnalysisApp {

    public static void main(String[] args) {
        // Path to the input transactions file and output patterns file.
        String inputFile = "sales_transactions.txt";
        String outputFile = "sales_patterns.txt";
        
        // Relative minimum support (e.g., 0.4 means patterns must appear in at least 40% of sequences).
        double minSupRelative = 0.4;
        
        try {
            AlgoSPAM spam = new AlgoSPAM();
            // Optionally, set maximum pattern length (in number of itemsets).
            spam.setMaximumPatternLength(5);
            // Optionally, enable output of sequence identifiers.
            spam.showSequenceIdentifiersInOutput(true);
            
            System.out.println("Starting sales transaction pattern mining using SPAM...");
            spam.runAlgorithm(inputFile, outputFile, minSupRelative);
            spam.printStatistics();
            System.out.println("Pattern mining completed. Check the output file: " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}