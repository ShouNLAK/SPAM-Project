import main.java.ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoSPAM;
import java.io.*;
import java.util.*;

public class IntegratedSalesAssistant {

    // Đặt các biến cấu hình ở cấp lớp để truy cập toàn cục
    private static double minSupDefault  = 0.4;
    private static int maxPatternLength  = 5;
    private static int minPatternLength  = 1;
    private static int maxGap            = Integer.MAX_VALUE;

    public static void main(String[] args) {
        // --- Bước 1: Khai phá SPAM trên lịch sử giao dịch ---
        String historicalFile = "sales_transactions.txt";
        String patternFile    = "sales_patterns.txt";
        // Xoá các khai báo biến cấu hình ở đây vì đã đưa lên trên
        // double minSupDefault  = 0.4;
        // int maxPatternLength  = 5;
        // int minPatternLength  = 1;
        // int maxGap            = Integer.MAX_VALUE;

        System.out.println("=== Khai phá SPAM trên lịch sử giao dịch ===");
        runSPAM(historicalFile, patternFile, minSupDefault, maxPatternLength, minPatternLength, maxGap);

        // Đọc mẫu khuyến mãi (dùng trong phiên giao dịch)
        List<PromotionPattern> promotionPatterns = readPromotionPatterns(patternFile);

        
        // Tạo productMapping tự động từ file sales_transactions.txt
        Map<Integer, String> productMapping = generateProductMappingFromFile("sales_transactions.txt");

        Scanner scanner = new Scanner(System.in);
        List<String> sessionHistory = new ArrayList<>();

        menu:
        while (true) {
            System.out.println();
            System.out.println("==================================================");
            System.out.println("|           SALES ASSISTANT MAIN MENU            |");
            System.out.println("==================================================");
            System.out.println("|  1. Bảng quy đổi trái cây (mã -> tên)          |");
            System.out.println("|  2. Nhập phiên giao dịch mới                   |");
            System.out.println("|  3. Xem lịch sử giao dịch trong phiên          |");
            System.out.println("|  4. Xem mẫu thường xuyên                       |");
            System.out.println("|  5. Gợi ý nâng cao                             |");
            System.out.println("|  6. Khai thác luật kết hợp                     |");
            System.out.println("|  7. Xem Top-K mẫu tuần tự                      |");
            System.out.println("|  8. Tóm tắt & trực quan hóa mẫu tuần tự        |");
            System.out.println("|  9. Truy vấn mẫu tuần tự                       |");
            System.out.println("| 10. Gợi ý sản phẩm tiếp theo                   |");
            System.out.println("| 11. Luật kết hợp trong cùng itemset            |");
            System.out.println("| -1. Tuỳ chọn nâng cao (minsup, độ dài, ...)    |");
            System.out.println("|  0. Thoát chương trình                         |");
            System.out.println("==================================================");
            System.out.print("Nhập lựa chọn của bạn (0-10, -1): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    hienThiBangQuyDoi(productMapping);
                    break;
                case "2":
                    String newTrans = nhapPhienGiaoDich(
                        scanner, productMapping, promotionPatterns, historicalFile
                    );
                    if (!newTrans.isEmpty()) {
                        sessionHistory.add(newTrans);
                    }
                    break;
                case "3":
                    hienThiLichSuPhien(sessionHistory, productMapping);
                    break;
                case "4":
                    hienThiFrequentPatterns(patternFile, productMapping);
                    break;
                case "5":
                    System.out.println("\n--- Gợi ý nâng cao ---");
                    System.out.print("Nhập giao dịch hiện tại (các mã cách nhau bằng dấu cách): ");
                    List<Integer> curr = parseCodes(scanner.nextLine());
                    gợiYenhanced(curr, promotionPatterns, productMapping);
                    break;
                case "6":
                    System.out.println("\n--- KHAI THÁC LUẬT KẾT HỢP ---");
                    System.out.println("1. Luật kết hợp tuần tự (dạng {a} -> {b})");
                    System.out.println("2. Luật kết hợp trong cùng itemset (dạng {a,b})");
                    System.out.print("Chọn kiểu luật (1-tuần tự, 2-cùng itemset): ");
                    String type = scanner.nextLine().trim();
                    if (type.equals("1")) {
                        System.out.print("Nhập min confidence (0.0–1.0): ");
                        double minConf = Double.parseDouble(scanner.nextLine().trim());
                        sinhAssociationRules(
                            patternFile, historicalFile, productMapping, minConf
                        );
                    } else if (type.equals("2")) {
                        System.out.print("Nhập min confidence phần trăm (ví dụ 60): ");
                        double minConfPercent = 0.0;
                        try {
                            minConfPercent = Double.parseDouble(scanner.nextLine().trim());
                        } catch (Exception e) {
                            System.out.println("Giá trị MinConf không hợp lệ.");
                            break;
                        }
                        generateItemsetAssociationRules("sales_transactions.txt", productMapping, minConfPercent);
                    } else {
                        System.out.println("Lựa chọn không hợp lệ.");
                    }
                    break;
                case "7":
                    System.out.println("\n--- Top-K Mẫu Tuần Tự ---");
                    System.out.print("Nhập K: ");
                    int k = Integer.parseInt(scanner.nextLine().trim());
                    hienThiTopK(patternFile, productMapping, k);
                    break;
                case "8":
                    summarizePatterns(patternFile, productMapping);
                    break;
                case "9":
                    System.out.println("\n----------- TRUY VẤN MẪU TUẦN TỰ -----------");
                    System.out.print("Nhập chuỗi mã sản phẩm (cách nhau bằng dấu cách): ");
                    List<Integer> querySeq = parseCodes(scanner.nextLine());
                    queryPatterns(patternFile, querySeq, productMapping);
                    break;
                case "10":
                    System.out.println("\n-------- GỢI Ý SẢN PHẨM TIẾP THEO ----------");
                    System.out.print("Nhập chuỗi mã sản phẩm hiện tại (cách nhau bằng dấu cách): ");
                    List<Integer> currSeq = parseCodes(scanner.nextLine());
                    recommendNext(patternFile, currSeq, productMapping);
                    break;
                case "-1":
                    optionMenu(scanner);
                    // Sau khi chỉnh sửa, chạy lại SPAM với tham số mới
                    runSPAM(historicalFile, patternFile, minSupDefault, maxPatternLength, minPatternLength, maxGap);
                    // Đọc lại mẫu khuyến mãi
                    promotionPatterns = readPromotionPatterns(patternFile);
                    break;
                case "0":
                    break menu;
                default:
                    System.out.println("Lựa chọn không hợp lệ. Vui lòng nhập 0–8.");
            }
        }

        System.out.println("\n--- Kết thúc chương trình ---");
        // Trước khi thoát, hiển thị toàn bộ lịch sử phiên
        hienThiLichSuPhien(sessionHistory, productMapping);
        scanner.close();
    }

    // Chạy SPAM với tham số cho trước (bổ sung các tham số tuỳ chỉnh)
    private static void runSPAM(String inputFile, String outputFile, double minSup, int maxLen, int minLen, int maxGap) {
        try {
            AlgoSPAM spam = new AlgoSPAM();
            spam.setMaximumPatternLength(maxLen);
            spam.setMinimumPatternLength(minLen);
            spam.setMaxGap(maxGap);
            spam.showSequenceIdentifiersInOutput(true);
            spam.runAlgorithm(inputFile, outputFile, minSup);
            spam.printStatistics();
        } catch (IOException e) {
            System.out.println("Lỗi khi chạy SPAM: " + e.getMessage());
        }
    }

    private static void printTransactionSequence(
            List<List<Integer>> seq,
            Map<Integer, String> productMapping
    ) {
        int i = 1;
        for (List<Integer> itemset : seq) {
            List<String> names = new ArrayList<>();
            for (Integer code : itemset) {
                names.add(productMapping.getOrDefault(code, "Unknown(" + code + ")"));
            }
            System.out.println("Purchase " + (i++) + ": " + String.join(", ", names));
        }
    }

    // 1) Hiển thị bảng quy đổi
    private static void hienThiBangQuyDoi(Map<Integer, String> map) {
        List<Integer> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        System.out.println("\nMã -> Tên trái cây:");
        for (Integer k : keys) {
            System.out.println(k + " -> " + map.get(k));
        }
    }

    // 2) Nhập phiên giao dịch mới
    private static String nhapPhienGiaoDich(
        Scanner scanner,
        Map<Integer, String> map,
        List<PromotionPattern> promoPatterns,
        String historyFile
    ) {
        List<List<Integer>> seq = new ArrayList<>();
        System.out.println("\nNhập phiên giao dịch mới:");
        System.out.print("Nhập mã sản phẩm lần mua đầu tiên: ");
        List<Integer> first = parseCodes(scanner.nextLine());
        if (first.isEmpty()) {
            System.out.println("Không có sản phẩm. Hủy phiên.");
            return "";
        }
        seq.add(first);
        System.out.println("Giao dịch hiện tại:");
        printTransactionSequence(seq, map);


        Map<Set<Integer>, Set<Integer>> promos = computePromos(seq, promoPatterns);
        inPromos(promos, map);

        // vòng mua thêm: Y hoặc 0
        while (!promos.isEmpty()) {
            String c;
            do {
                System.out.print("Nhấn Y để mua thêm, hoặc 0 để kết thúc: ");
                c = scanner.nextLine().trim();
                if (!c.equalsIgnoreCase("Y") && !c.equals("0")) {
                    System.out.println("Cú pháp không hợp lệ.");
                }
            } while (!c.equalsIgnoreCase("Y") && !c.equals("0"));
            if (c.equals("0")) break;

            System.out.print("Nhập mã sản phẩm bổ sung: ");
            List<Integer> extra = parseCodes(scanner.nextLine());
            if (extra.isEmpty()) {
                System.out.println("Không có sản phẩm.");
                break;
            }
            seq.add(extra);
            System.out.println("Giao dịch cập nhật:");
            printTransactionSequence(seq, map);


            promos = computePromos(seq, promoPatterns);
            inPromos(promos, map);
        }

        String formatted = formatSeq(seq);
        String summary   = decodeSeq(seq, map);
        System.out.println("Tổng kết: " + summary);
        appendToFile(historyFile, formatted);
        System.out.println("Đã lưu vào lịch sử.");
        return formatted;
    }

    // 3) Xem lịch sử phiên
    private static void hienThiLichSuPhien(
        List<String> history,
        Map<Integer, String> map
    ) {
        System.out.println("\nLịch sử giao dịch trong phiên:");
        if (history.isEmpty()) {
            System.out.println("(chưa có giao dịch)");
            return;
        }
        int idx = 1;
        for (String t : history) {
            List<List<String>> decoded = decodeTransaction(t, map);
            List<String> parts = new ArrayList<>();
            for (List<String> itemset : decoded) {
                parts.add("{" + String.join(", ", itemset) + "}");
            }
            System.out.println("GD" + (idx++) + ": " + String.join(" ; ", parts));
        }
    }

    // 4) Frequent Patterns
    private static void hienThiFrequentPatterns(
        String filename,
        Map<Integer, String> map
    ) {
        System.out.println("\nMẫu thường xuyên:");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                found = true;
                String[] p = line.split("#SUP:");
                String patternPart = p[0].trim();
                String sup = p.length>1 ? p[1].trim().split("\\s+")[0] : "";
                List<String> sets = new ArrayList<>();
                for (String blk : patternPart.split("-1")) {
                    blk = blk.trim();
                    if (blk.isEmpty()) continue;
                    List<String> names = new ArrayList<>();
                    for (String tok : blk.split("\\s+")) {
                        names.add(map.getOrDefault(Integer.parseInt(tok), "Unknown"));
                    }
                    sets.add("{" + String.join(", ", names) + "}");
                }
                System.out.println(String.join(" ; ", sets) + "    support=" + sup);
            }
            if (!found) System.out.println("(không tìm thấy)");
        } catch (IOException e) {
            System.out.println("Lỗi đọc file mẫu: " + e.getMessage());
        }
    }

    // 5) Gợi ý nâng cao
    private static void gợiYenhanced(
        List<Integer> curr,
        List<PromotionPattern> promos,
        Map<Integer, String> map
    ) {
        Set<Integer> tx = new HashSet<>(curr);
        Set<Integer> recs = new HashSet<>();
        for (PromotionPattern p : promos) {
            if (tx.containsAll(p.leftItems) && !tx.contains(p.promoItem)) {
                recs.add(p.promoItem);
            }
        }
        if (recs.isEmpty()) {
            System.out.println("Không có gợi ý nào.");
        } else {
            List<String> names = new ArrayList<>();
            for (Integer c : recs) {
                names.add(map.getOrDefault(c, "Unknown"));
            }
            System.out.println("Bạn có thể xem xét: " + String.join(" / ", names));
        }
    }

    // 6) Luật kết hợp tuần tự
    private static void sinhAssociationRules(
        String patternsFile,
        String transactionsFile,
        Map<Integer, String> map,
        double minConf
    ) {
        // đọc lịch sử
        List<String> txs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(transactionsFile))) {
            String l;
            while ((l=br.readLine())!=null) {
                l = l.trim();
                if (!l.isEmpty() && !l.startsWith("#")) txs.add(l);
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc giao dịch: " + e.getMessage());
            return;
        }

        System.out.println("\nLuật kết hợp (minConf=" + minConf + "):");
        try (BufferedReader br = new BufferedReader(new FileReader(patternsFile))) {
            String line;
            while ((line=br.readLine())!=null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("#SUP:");
                if (parts.length<2) continue;
                String patt = parts[0].trim();
                int supP = Integer.parseInt(parts[1].trim().split("\\s+")[0]);
                String[] toks = patt.split("-1")[0].trim().split("\\s+");
                if (toks.length<2) continue;
                List<Integer> X = new ArrayList<>();
                for (int i=0; i<toks.length-1; i++) X.add(Integer.parseInt(toks[i]));
                int Y = Integer.parseInt(toks[toks.length-1]);

                int supX = 0;
                for (String t : txs) {
                    if (itemsetContains(t, X)) supX++;
                }
                if (supX==0) continue;
                double conf = (double)supP/supX;
                if (conf>=minConf) {
                    List<String> namesX = new ArrayList<>();
                    for (int c : X) namesX.add(map.get(c));
                    String nameY = map.get(Y);
                    System.out.printf("%s -> {%s}  (sup=%d, conf=%.2f)%n",
                        namesX, nameY, supP, conf);
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc mẫu: " + e.getMessage());
        }
    }

    // 7) Top-K mẫu tuần tự
    private static void hienThiTopK(
        String patternsFile,
        Map<Integer, String> map,
        int k
    ) {
        class PS { String pattern; int sup; }
        List<PS> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(patternsFile))) {
            String line;
            while ((line=br.readLine())!=null) {
                line=line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("#SUP:");
                String patt = p[0].trim();
                int sup = Integer.parseInt(p[1].trim().split("\\s+")[0]);
                List<String> sets = new ArrayList<>();
                for (String blk : patt.split("-1")) {
                    blk=blk.trim();
                    if (blk.isEmpty()) continue;
                    List<String> names = new ArrayList<>();
                    for (String t : blk.split("\\s+")) {
                        names.add(map.get(Integer.parseInt(t)));
                    }
                    sets.add("{" + String.join(", ", names) + "}");
                }
                PS obj = new PS();
                obj.pattern = String.join(" ; ", sets);
                obj.sup     = sup;
                list.add(obj);
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc mẫu: " + e.getMessage());
            return;
        }
        list.sort((a,b)->Integer.compare(b.sup,a.sup));
        System.out.println("\nTop " + k + " mẫu tuần tự:");
        for (int i=0; i<Math.min(k, list.size()); i++) {
            PS o = list.get(i);
            System.out.printf("%2d) %s    support=%d%n", i+1, o.pattern, o.sup);
        }
    }

    // 8) Visualize/Summarize Sequential Patterns
    private static void summarizePatterns(String filename, Map<Integer, String> map) {
        System.out.println("\n================ TÓM TẮT MẪU TUẦN TỰ ================");
        int total = 0, maxSup = 0, minSup = Integer.MAX_VALUE;
        Map<Integer, Integer> itemFreq = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                total++;
                String[] p = line.split("#SUP:");
                int sup = Integer.parseInt(p[1].trim().split("\\s+")[0]);
                maxSup = Math.max(maxSup, sup);
                minSup = Math.min(minSup, sup);
                String[] items = p[0].replace("-1", "").trim().split("\\s+");
                for (String it : items) {
                    int code = Integer.parseInt(it);
                    itemFreq.put(code, itemFreq.getOrDefault(code, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file mẫu: " + e.getMessage());
            return;
        }
        System.out.println("Tổng số mẫu tuần tự: " + total);
        System.out.println("Support lớn nhất: " + maxSup + " | nhỏ nhất: " + (minSup==Integer.MAX_VALUE?0:minSup));
        System.out.println("Tần suất xuất hiện của từng sản phẩm trong mẫu:");
        System.out.println("-----------------------------------------------");
        itemFreq.entrySet().stream()
            .sorted((a,b)->b.getValue()-a.getValue())
            .forEach(e -> System.out.printf("%-12s : %d lần\n", map.getOrDefault(e.getKey(),"Unknown"), e.getValue()));
        System.out.println("\nBIỂU ĐỒ TẦN SUẤT (dạng text):");
        System.out.println("-----------------------------------------------");
        for (var e : itemFreq.entrySet()) {
            String name = map.getOrDefault(e.getKey(), "Unknown");
            int barLen = Math.min(e.getValue(), 40);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLen; i++) bar.append('#');
            System.out.printf("%-12s | %s (%d)\n", name, bar, e.getValue());
        }
        System.out.println("======================================================");
    }

    // 9) Pattern Querying
    private static void queryPatterns(String filename, List<Integer> query, Map<Integer, String> map) {
        if (query.isEmpty()) {
            System.out.println("Không có mã sản phẩm để truy vấn.");
            return;
        }
        System.out.println("Kết quả truy vấn mẫu chứa: " + decodeSeq(List.of(query), map));
        int found = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("#SUP:");
                String patt = p[0].replace("-1", "").trim();
                List<Integer> pattItems = new ArrayList<>();
                for (String t : patt.split("\\s+")) {
                    pattItems.add(Integer.parseInt(t));
                }
                if (containsSubsequence(pattItems, query)) {
                    found++;
                    List<String> names = new ArrayList<>();
                    for (int c : pattItems) names.add(map.getOrDefault(c, "Unknown"));
                    System.out.println("  " + names + " | support=" + p[1].trim().split("\\s+")[0]);
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file mẫu: " + e.getMessage());
        }
        if (found == 0) System.out.println("(Không tìm thấy mẫu phù hợp)");
    }

    // 10) Pattern Recommendation
    private static void recommendNext(String filename, List<Integer> curr, Map<Integer, String> map) {
        if (curr.isEmpty()) {
            System.out.println("Không có mã sản phẩm để gợi ý.");
            return;
        }
        Map<Integer, Integer> nextCounts = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("#SUP:");
                String patt = p[0].replace("-1", "").trim();
                List<Integer> pattItems = new ArrayList<>();
                for (String t : patt.split("\\s+")) {
                    pattItems.add(Integer.parseInt(t));
                }
                int idx = indexOfSubsequence(pattItems, curr);
                if (idx != -1 && idx + curr.size() < pattItems.size()) {
                    int nextItem = pattItems.get(idx + curr.size());
                    nextCounts.put(nextItem, nextCounts.getOrDefault(nextItem, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file mẫu: " + e.getMessage());
        }
        if (nextCounts.isEmpty()) {
            System.out.println("Không có gợi ý tiếp theo.");
            return;
        }
        System.out.println("Sản phẩm nên mua tiếp theo:");
        System.out.println("-----------------------------------------------");
        nextCounts.entrySet().stream()
            .sorted((a,b)->b.getValue()-a.getValue())
            .limit(5)
            .forEach(e -> System.out.printf("%-12s : gợi ý %d lần\n", map.getOrDefault(e.getKey(),"Unknown"), e.getValue()));
        System.out.println("-----------------------------------------------");
    }

    // Helper: check if pattern contains query as subsequence
    private static boolean containsSubsequence(List<Integer> pattern, List<Integer> query) {
        if (query.isEmpty()) return true;
        int i = 0, j = 0;
        while (i < pattern.size() && j < query.size()) {
            if (pattern.get(i).equals(query.get(j))) j++;
            i++;
        }
        return j == query.size();
    }

    // Helper: find index where query matches as subsequence in pattern
    private static int indexOfSubsequence(List<Integer> pattern, List<Integer> query) {
        if (query.isEmpty() || query.size() > pattern.size()) return -1;
        for (int i = 0; i <= pattern.size() - query.size(); i++) {
            boolean match = true;
            for (int j = 0; j < query.size(); j++) {
                if (!pattern.get(i + j).equals(query.get(j))) {
                    match = false; break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    // Menu tuỳ chọn nâng cao
    private static void optionMenu(Scanner scanner) {
        boolean done = false;
        while (!done) {
            System.out.println();
            System.out.println("===============================================");
            System.out.println("|         TUỲ CHỌN NÂNG CAO SPAM              |");
            System.out.println("===============================================");
            System.out.println("| 1. Thay đổi minsup (support tối thiểu)      |");
            System.out.println("| 2. Thay đổi độ dài mẫu tối đa                |");
            System.out.println("| 3. Thay đổi độ dài mẫu tối thiểu             |");
            System.out.println("| 4. Thay đổi khoảng cách tối đa (max gap)     |");
            System.out.println("| 0. Quay lại menu chính                      |");
            System.out.println("===============================================");
            System.out.print("Chọn tuỳ chọn (0-4): ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1":
                    System.out.print("Nhập minsup mới (0.0 - 1.0): ");
                    try {
                        double val = Double.parseDouble(scanner.nextLine().trim());
                        if (val > 0 && val <= 1) {
                            minSupDefault = val;
                            System.out.println("Đã cập nhật minsup = " + minSupDefault);
                        } else {
                            System.out.println("Giá trị không hợp lệ.");
                        }
                    } catch (Exception e) {
                        System.out.println("Giá trị không hợp lệ.");
                    }
                    break;
                case "2":
                    System.out.print("Nhập độ dài mẫu tối đa mới (>0): ");
                    try {
                        int val = Integer.parseInt(scanner.nextLine().trim());
                        if (val > 0) {
                            maxPatternLength = val;
                            System.out.println("Đã cập nhật độ dài mẫu tối đa = " + maxPatternLength);
                        } else {
                            System.out.println("Giá trị không hợp lệ.");
                        }
                    } catch (Exception e) {
                        System.out.println("Giá trị không hợp lệ.");
                    }
                    break;
                case "3":
                    System.out.print("Nhập độ dài mẫu tối thiểu mới (>=1): ");
                    try {
                        int val = Integer.parseInt(scanner.nextLine().trim());
                        if (val >= 1) {
                            minPatternLength = val;
                            System.out.println("Đã cập nhật độ dài mẫu tối thiểu = " + minPatternLength);
                        } else {
                            System.out.println("Giá trị không hợp lệ.");
                        }
                    } catch (Exception e) {
                        System.out.println("Giá trị không hợp lệ.");
                    }
                    break;
                case "4":
                    System.out.print("Nhập khoảng cách tối đa (max gap, số nguyên >=1, hoặc 0 để không giới hạn): ");
                    try {
                        int val = Integer.parseInt(scanner.nextLine().trim());
                        if (val == 0) {
                            maxGap = Integer.MAX_VALUE;
                            System.out.println("Đã bỏ giới hạn khoảng cách.");
                        } else if (val >= 1) {
                            maxGap = val;
                            System.out.println("Đã cập nhật max gap = " + maxGap);
                        } else {
                            System.out.println("Giá trị không hợp lệ.");
                        }
                    } catch (Exception e) {
                        System.out.println("Giá trị không hợp lệ.");
                    }
                    break;
                case "0":
                    done = true;
                    break;
                default:
                    System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }

    // parse mã
    private static List<Integer> parseCodes(String line) {
        List<Integer> list = new ArrayList<>();
        for (String tok : line.split("\\s+")) {
            try { list.add(Integer.parseInt(tok)); }
            catch (NumberFormatException ex) { }
        }
        return list;
    }

    // tính khuyến mãi
    private static Map<Set<Integer>,Set<Integer>> computePromos(
        List<List<Integer>> seq,
        List<PromotionPattern> patterns
    ) {
        Map<Set<Integer>,Set<Integer>> out = new HashMap<>();
        for (PromotionPattern p : patterns) {
            if (p.leftItems.contains(p.promoItem)) continue;
            boolean match = false;
            if (p.isSequential) {
                // Phải xuất hiện từng item ở các lần mua khác nhau, đúng thứ tự
                int idx = 0;
                for (List<Integer> blk : seq) {
                    if (blk.contains(p.leftItems.get(idx))) {
                        idx++;
                        if (idx == p.leftItems.size()) break;
                    }
                }
                if (idx == p.leftItems.size()) {
                    boolean hasPromo = false;
                    for (List<Integer> blk : seq) {
                        if (blk.contains(p.promoItem)) { hasPromo = true; break; }
                    }
                    if (!hasPromo) match = true;
                }
            } else {
                // Tất cả leftItems phải nằm trong cùng một lần mua
                for (List<Integer> blk : seq) {
                    if (blk.containsAll(p.leftItems) && !blk.contains(p.promoItem)) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                out.computeIfAbsent(new HashSet<>(p.leftItems), x->new HashSet<>())
                   .add(p.promoItem);
            }
        }
        return out;
    }

    // in khuyến mãi
    private static void inPromos(
        Map<Set<Integer>,Set<Integer>> promos,
        Map<Integer,String> map
    ) {
        System.out.println("Khuyến mãi:");
        if (promos.isEmpty()) {
            System.out.println("(không có)");
            return;
        }
        // Lấy hợp tất cả sản phẩm đã mua trong phiên
        Set<Integer> allPurchased = new HashSet<>();
        for (Set<Integer> s : promos.keySet()) allPurchased.addAll(s);
        // Tìm giao các khuyến mãi ứng với từng tập con đã mua
        Set<Integer> intersection = null;
        for (var e : promos.entrySet()) {
            if (allPurchased.containsAll(e.getKey())) {
                if (intersection == null) intersection = new HashSet<>(e.getValue());
                else intersection.retainAll(e.getValue());
            }
        }
        // In ra tập hợp các purchase
        List<String> purchaseNames = new ArrayList<>();
        for (int c : allPurchased) purchaseNames.add(map.get(c));
        if (intersection != null && !intersection.isEmpty()) {
            List<String> promoNames = new ArrayList<>();
            for (int c : intersection) promoNames.add(map.get(c));
            System.out.println("{" + String.join(", ", purchaseNames) + "} -> " + promoNames);
        } else {
            System.out.println("(không có)");
        }
    }

    // format SPMF
    private static String formatSeq(List<List<Integer>> seq) {
        StringBuilder sb = new StringBuilder();
        for (var blk : seq) {
            for (int c: blk) sb.append(c).append(" ");
            sb.append("-1 ");
        }
        sb.append("-2");
        return sb.toString();
    }

    // ghi file
    private static void appendToFile(String f, String line) {
        try (FileWriter fw = new FileWriter(f, true)) {
            fw.write(line + "\n");
        } catch (IOException e) { }
    }

    // decode để hiển thị
    private static String decodeSeq(
        List<List<Integer>> seq,
        Map<Integer,String> map
    ) {
        List<String> parts = new ArrayList<>();
        for (var blk : seq) {
            List<String> names = new ArrayList<>();
            for (int c : blk) names.add(map.get(c));
            parts.add("{" + String.join(", ", names) + "}");
        }
        return String.join(" ; ", parts);
    }

    // đọc lại để hiển thị lịch sử
    private static List<List<String>> decodeTransaction(
        String tx,
        Map<Integer,String> map
    ) {
        List<List<String>> res = new ArrayList<>();
        for (String blk : tx.split("-1")) {
            blk = blk.trim();
            if (blk.isEmpty() || blk.contains("-2")) continue;
            List<String> names = new ArrayList<>();
            for (String t : blk.split("\\s+")) {
                names.add(map.get(Integer.parseInt(t)));
            }
            res.add(names);
        }
        return res;
    }

    // đọc mẫu khuyến mãi
    private static List<PromotionPattern> readPromotionPatterns(String f) {
        List<PromotionPattern> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ln;
            while ((ln=br.readLine())!=null) {
                ln = ln.trim();
                if (ln.isEmpty()) continue;
                String[] p = ln.split("#SUP:");
                String patternPart = p[0].trim();
                // Parse itemsets by -1
                List<List<Integer>> itemsets = new ArrayList<>();
                for (String blk : patternPart.split("-1")) {
                    blk = blk.trim();
                    if (blk.isEmpty()) continue;
                    List<Integer> items = new ArrayList<>();
                    for (String t : blk.split("\\s+")) {
                        if (!t.equals("-2")) {
                            items.add(Integer.parseInt(t));
                        }
                    }
                    if (!items.isEmpty()) itemsets.add(items);
                }
                if (itemsets.size() >= 2) {
                    // Kiểm tra nếu tất cả itemset (trừ cuối) đều chỉ có 1 phần tử thì là tuần tự
                    boolean allSingle = true;
                    for (int i = 0; i < itemsets.size()-1; i++) {
                        if (itemsets.get(i).size() != 1) { allSingle = false; break; }
                    }
                    List<Integer> left = new ArrayList<>();
                    if (allSingle) {
                        // Tuần tự: leftItems là từng item riêng biệt
                        for (int i = 0; i < itemsets.size()-1; i++) {
                            left.add(itemsets.get(i).get(0));
                        }
                        List<Integer> lastSet = itemsets.get(itemsets.size()-1);
                        if (!lastSet.isEmpty()) {
                            int promo = lastSet.get(lastSet.size()-1);
                            list.add(new PromotionPattern(left, promo, true));
                        }
                    } else {
                        // Cùng itemset: gộp tất cả itemset (trừ cuối) lại
                        for (int i = 0; i < itemsets.size()-1; i++) {
                            left.addAll(itemsets.get(i));
                        }
                        List<Integer> lastSet = itemsets.get(itemsets.size()-1);
                        if (!lastSet.isEmpty()) {
                            int promo = lastSet.get(lastSet.size()-1);
                            list.add(new PromotionPattern(left, promo, false));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc khuyến mãi: " + e.getMessage());
        }
        return list;
    }

    private static boolean itemsetContains(String txLine, List<Integer> items) {
        for (String blk : txLine.split("-1")) {
            blk=blk.trim();
            if (blk.isEmpty() || blk.contains("-2")) continue;
            Set<Integer> set = new HashSet<>();
            for (String t: blk.split("\\s+")) {
                set.add(Integer.parseInt(t));
            }
            if (set.containsAll(items)) return true;
        }
        return false;
    }

    // lớp lưu khuyến mãi
    private static class PromotionPattern {
        List<Integer> leftItems;
        int promoItem;
        boolean isSequential; // true if pattern is sequential (e.g. {a},{b}), false if in same itemset (e.g. {a,b})
        PromotionPattern(List<Integer> left, int promo, boolean isSequential) {
            this.leftItems = left;
            this.promoItem  = promo;
        }
    }

    public static Map<Integer, String> generateProductMappingFromFile(String filePath) {
        TreeSet<Integer> uniqueCodes = new TreeSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                for (String blk : line.split("-1")) {
                    blk = blk.trim();
                    if (blk.isEmpty() || blk.contains("-2")) continue;
                    for (String tok : blk.split("\\s+")) {
                        try {
                            uniqueCodes.add(Integer.parseInt(tok));
                        } catch (NumberFormatException ex) { }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
        }
        // Tạo tên a, b, ..., z, aa, ab, ...
        List<String> names = new ArrayList<>();
        for (int i = 1; names.size() < uniqueCodes.size(); i++) {
            int n = i;
            StringBuilder sb = new StringBuilder();
            while (n > 0) {
                n--;
                sb.insert(0, (char)('a' + (n % 26)));
                n /= 26;
            }
            names.add(sb.toString());
        }
        Map<Integer, String> map = new LinkedHashMap<>();
        int idx = 0;
        for (Integer code : uniqueCodes) {
            map.put(code, names.get(idx++));
        }
        return map;
    }

    // Thêm hàm sinh luật kết hợp trong cùng itemset
    private static void generateItemsetAssociationRules(String file, Map<Integer, String> map, double minConfPercent) {
        Map<Integer, Integer> countA = new HashMap<>();
        Map<String, Integer> countAB = new HashMap<>();
        Set<Integer> allItems = map.keySet();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                for (String blk : line.split("-1")) {
                    blk = blk.trim();
                    if (blk.isEmpty() || blk.contains("-2")) continue;
                    List<Integer> set = new ArrayList<>();
                    for (String t : blk.split("\\s+")) {
                        try { set.add(Integer.parseInt(t)); } catch (NumberFormatException ex) { }
                    }
                    for (int i = 0; i < set.size(); i++) {
                        int a = set.get(i);
                        countA.put(a, countA.getOrDefault(a, 0) + 1);
                        for (int j = i + 1; j < set.size(); j++) {
                            int b = set.get(j);
                            String key = a + "," + b;
                            countAB.put(key, countAB.getOrDefault(key, 0) + 1);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file giao dịch: " + e.getMessage());
            return;
        }
        System.out.println("Các luật (Nếu A thì B) trong cùng itemset có confidence >= MinConf:");
        boolean found = false;
        // Chỉ in các cặp (A,B) đã được đếm (B đứng sau A trong itemset)
        for (Map.Entry<String, Integer> entry : countAB.entrySet()) {
            String[] ab = entry.getKey().split(",");
            int a = Integer.parseInt(ab[0]);
            int b = Integer.parseInt(ab[1]);
            int cntA = countA.getOrDefault(a, 0);
            int cntAB = entry.getValue();
            if (cntA == 0) continue;
            double conf = (double) cntAB / cntA * 100.0;
            if (conf >= minConfPercent) {
                found = true;
                System.out.printf("  Nếu %-12s thì %-12s : %.2f%% (%d/%d)\n", map.get(a), map.get(b), conf, cntAB, cntA);
            }
        }
        if (!found) System.out.println("(Không có luật nào thỏa mãn MinConf)");
    }
}