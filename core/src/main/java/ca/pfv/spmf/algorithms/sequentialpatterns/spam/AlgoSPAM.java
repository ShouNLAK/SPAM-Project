package main.java.ca.pfv.spmf.algorithms.sequentialpatterns.spam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import main.java.ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import main.java.ca.pfv.spmf.tools.MemoryLogger;


/**
 * Implementation of the SPAM algorithm for sequential pattern mining.
 */
public class AlgoSPAM {
    
    // Statistics
    private long startTime;
    private long endTime;
    private int patternCount;
    
    // Absolute minimum support.
    private int minsup = 0;
    
    // Writer for output file.
    BufferedWriter writer = null;
    
    // Vertical database (item -> bitmap).
    Map<Integer, Bitmap> verticalDB = new HashMap<>();
    
    // Sequence positions for allocating the BitSet.
    List<Integer> sequencesSize = null;
    
    // Last bit index used.
    int lastBitIndex = 0;
    
    // Minimum and maximum pattern lengths.
    private int minimumPatternLength = 0;
    private int maximumPatternLength = 1000;
    
    // Maximum gap between itemsets in a pattern.
    private int maxGap = Integer.MAX_VALUE;
    
    // Option to output sequence identifiers.
    private boolean outputSequenceIdentifiers;
    
    public AlgoSPAM() { }
    
    /**
     * Runs the SPAM algorithm.
     * @param input path to input file.
     * @param outputFilePath path to output file.
     * @param minsupRel relative minimum support (fraction).
     * @throws IOException if IO error occurs.
     */
    public void runAlgorithm(String input, String outputFilePath, double minsupRel) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFilePath));
        patternCount = 0;
        MemoryLogger.getInstance().reset();
        startTime = System.currentTimeMillis();
        spam(input, minsupRel);
        endTime = System.currentTimeMillis();
        writer.close();
    }
    
    /**
     * Main SPAM procedure.
     */
    private void spam(String input, double minsupRel) throws IOException {
        verticalDB = new HashMap<>();
        sequencesSize = new ArrayList<>();
        lastBitIndex = 0;
        try {
            FileInputStream fin = new FileInputStream(new File(input));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String thisLine;
            int bitIndex = 0;
            while ((thisLine = reader.readLine()) != null) {
                if (thisLine.isEmpty() ||
                    thisLine.charAt(0) == '#' ||
                    thisLine.charAt(0) == '%' ||
                    thisLine.charAt(0) == '@') {
                    continue;
                }
                sequencesSize.add(bitIndex);
                for (String token : thisLine.split(" ")) {
                    if(token.equals("-1")){
                        bitIndex++;
                    }
                }
            }
            lastBitIndex = bitIndex - 1;
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Calculate absolute minimum support.
        minsup = (int)Math.ceil((minsupRel * sequencesSize.size()));
        if(minsup == 0){
            minsup = 1;
        }
        
        // Step 1: Build the vertical database.
        try {
            FileInputStream fin = new FileInputStream(new File(input));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String thisLine;
            int sid = 0;
            int tid = 0;
            while ((thisLine = reader.readLine()) != null) {
                if (thisLine.isEmpty() ||
                    thisLine.charAt(0) == '#' ||
                    thisLine.charAt(0) == '%' ||
                    thisLine.charAt(0) == '@') {
                    continue;
                }
                for (String token : thisLine.split(" ")) {
                    if(token.equals("-1")){
                        tid++;
                    } else if(token.equals("-2")){
                        sid++;
                        tid = 0;
                    } else {
                        Integer item = Integer.parseInt(token);
                        Bitmap bitmapItem = verticalDB.get(item);
                        if(bitmapItem == null){
                            bitmapItem = new Bitmap(lastBitIndex);
                            verticalDB.put(item, bitmapItem);
                        }
                        bitmapItem.registerBit(sid, tid, sequencesSize);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Step 2: Remove infrequent items.
        List<Integer> frequentItems = new ArrayList<>();
        Iterator<Entry<Integer, Bitmap>> iter = verticalDB.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, Bitmap> entry = iter.next();
            if(entry.getValue().getSupport() < minsup){
                iter.remove();
            } else {
                if(minimumPatternLength <= 1 && maximumPatternLength >= 1) {
                    savePattern(entry.getKey(), entry.getValue());
                }
                frequentItems.add(entry.getKey());
            }
        }
        
        // Step 3: DFS to extend patterns.
        if(maximumPatternLength == 1){
            return;
        }
        for (Entry<Integer, Bitmap> entry: verticalDB.entrySet()){
            Prefix prefix = new Prefix();
            prefix.addItemset(new Itemset(entry.getKey()));
            dfsPruning(prefix, entry.getValue(), frequentItems, frequentItems, entry.getKey(), 2);
        }
    }
    
    /**
     * Recursive DFS to extend patterns.
     */
    private void dfsPruning(Prefix prefix, Bitmap prefixBitmap, List<Integer> sn, List<Integer> in, int hasToBeGreaterThanForIStep, int m) throws IOException {
        // S-step.
        List<Integer> sTemp = new ArrayList<>();
        List<Bitmap> sTempBitmaps = new ArrayList<>();
        for (Integer i : sn) {
            Bitmap newBitmap = prefixBitmap.createNewBitmapSStep(verticalDB.get(i), sequencesSize, lastBitIndex, maxGap);
            if(newBitmap.getSupportWithoutGapTotal() >= minsup){
                sTemp.add(i); 
                sTempBitmaps.add(newBitmap);
            }
        }
        for (int k = 0; k < sTemp.size(); k++){
            int item = sTemp.get(k);
            Prefix prefixSStep = prefix.cloneSequence();
            prefixSStep.addItemset(new Itemset(item));
            Bitmap newBitmap = sTempBitmaps.get(k);
            if(newBitmap.getSupport() >= minsup) {
                if(m >= minimumPatternLength) {
                    savePattern(prefixSStep, newBitmap);
                }
                if(maximumPatternLength > m ){
                    dfsPruning(prefixSStep, newBitmap, sTemp, sTemp, item, m + 1);
                }
            }
        }
        
        // I-step.
        List<Integer> iTemp = new ArrayList<>();
        List<Bitmap> iTempBitmaps = new ArrayList<>();
        for (Integer i : in) {
            if(i > hasToBeGreaterThanForIStep){
                Bitmap newBitmap = prefixBitmap.createNewBitmapIStep(verticalDB.get(i), sequencesSize, lastBitIndex);
                if(newBitmap.getSupport() >= minsup){
                    iTemp.add(i);
                    iTempBitmaps.add(newBitmap);
                }
            }
        }
        for (int k = 0; k < iTemp.size(); k++){
            int item = iTemp.get(k);
            Prefix prefixIStep = prefix.cloneSequence();
            prefixIStep.getItemsets().get(prefixIStep.size() - 1).addItem(item);
            Bitmap newBitmap = iTempBitmaps.get(k);
            if(m >= minimumPatternLength) {
                savePattern(prefixIStep, newBitmap);
            }
            if(maximumPatternLength > m){
                dfsPruning(prefixIStep, newBitmap, sTemp, iTemp, item, m + 1);
            }
        }
        MemoryLogger.getInstance().checkMemory();
    }
    
    /**
     * Saves a 1-length pattern.
     */
    private void savePattern(Integer item, Bitmap bitmap) throws IOException {
        patternCount++;
        StringBuilder r = new StringBuilder();
        r.append(item).append(" -1 ");
        r.append("#SUP: ").append(bitmap.getSupport());
        if(outputSequenceIdentifiers){
            r.append(" #SID: ").append(bitmap.getSIDs(sequencesSize));
        }
        writer.write(r.toString());
        writer.newLine();
    }
    
    /**
     * Saves a pattern longer than 1.
     */
    private void savePattern(Prefix prefix, Bitmap bitmap) throws IOException {
        patternCount++;
        StringBuilder r = new StringBuilder();
        for (Itemset itemset : prefix.getItemsets()){
            for (Integer item : itemset.getItems()){
                r.append(item).append(" ");
            }
            r.append("-1 ");
        }
        r.append("#SUP: ").append(bitmap.getSupport());
        if(outputSequenceIdentifiers){
            r.append(" #SID: ").append(bitmap.getSIDs(sequencesSize));
        }
        writer.write(r.toString());
        writer.newLine();
    }
    
    /**
     * Print statistics about the algorithm execution.
     */
    public void printStatistics() {
        StringBuilder r = new StringBuilder();
        r.append("============= SPAM STATISTICS =============\n Total time: ")
         .append(endTime - startTime).append(" ms\n")
         .append("Frequent sequences count: ").append(patternCount).append("\n")
         .append("Max memory (MB): ").append(MemoryLogger.getInstance().getMaxMemory()).append("\n")
         .append("minsup: ").append(minsup).append("\n")
         .append("===========================================\n");
        System.out.println(r.toString());
    }
    
    public int getMaximumPatternLength() {
        return maximumPatternLength;
    }
    
    public void setMaximumPatternLength(int maximumPatternLength) {
        this.maximumPatternLength = maximumPatternLength;
    }
    
    public void setMinimumPatternLength(int minimumPatternLength) {
        this.minimumPatternLength = minimumPatternLength;
    }
    
    public void setMaxGap(int maxGap) {
        this.maxGap = maxGap;
    }
    
    public void showSequenceIdentifiersInOutput(boolean showSequenceIdentifiers) {
        this.outputSequenceIdentifiers = showSequenceIdentifiers;
    }
}