import main.java.ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoSPAM;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


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
                    List<List<Integer>> currTransaction = List.of(curr); // wrap as one itemset
                    gợiYenhanced(currTransaction, promotionPatterns, productMapping);
                    break;
                case "6":
                    System.out.println("\n--- KHAI THÁC LUẬT KẾT HỢP ---");
                    sinhAssociationRules(
                        patternFile, historicalFile, productMapping
                    );
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

    private static String nhapPhienGiaoDich(
        Scanner scanner,
        Map<Integer, String> map,
        List<PromotionPattern> promoPatterns,
        String historyFile
    ) {
        List<List<Integer>> seq = new ArrayList<>();
        System.out.println("\n===== CHÀO MỪNG ĐẾN VỚI TRỢ LÝ BÁN HÀNG THÔNG MINH =====");

        while (true) {
            List<Integer> currentPurchase = new ArrayList<>();
            boolean comboChecked = false;
            while (true) {
                System.out.print("\nNhập mã SP cho lần mua này (cách nhau bởi dấu cách), hoặc nhập '0' để kết thúc lần mua, hoặc nhập '00' để kết thúc phiên: ");
                String line = scanner.nextLine().trim();
                if (line.equals("00")) {
                    // Kết thúc phiên giao dịch
                    if (!currentPurchase.isEmpty()) {
                        System.out.println("   -> Đã ghi nhận: " + decodeItemset(currentPurchase, map));
                        seq.add(new ArrayList<>(currentPurchase));
                    }
                    System.out.println("\n===== KẾT THÚC PHIÊN GIAO DỊCH =====");
                    if (seq.isEmpty()) {
                        System.out.println("Không có sản phẩm nào trong giỏ hàng. Cảm ơn!");
                    } else {
                        System.out.println("Tổng kết giỏ hàng: " + decodeSeq(seq, map));
                        String formatted = formatSeq(seq);
                        appendToFile(historyFile, formatted);
                        System.out.println("Đã lưu phiên giao dịch vào " + historyFile);
                    }
                    return formatSeq(seq);
                }
                if (line.equals("0")) {
                    // Kết thúc lần mua hiện tại
                    if (!currentPurchase.isEmpty()) {
                        // Sort before display and save
                        List<Integer> sortedCurrent = new ArrayList<>(currentPurchase);
                        Collections.sort(sortedCurrent);
                        System.out.println("   -> Xác nhận giỏ hàng: " + decodeItemset(sortedCurrent, map));
                        // Hiển thị lại khuyến mãi khi xác nhận giỏ hàng
                        boolean comboActivated = false;
                        Set<Integer> enteredSet = new HashSet<>(currentPurchase);
                        try (BufferedReader br = new BufferedReader(new FileReader("sales_patterns.txt"))) {
                            String pline;
                            while ((pline = br.readLine()) != null) {
                                pline = pline.trim();
                                if (pline.isEmpty() || !pline.contains("#SUP:")) continue;
                                String[] parts = pline.split("#SUP:");
                                String patternPart = parts[0].trim();
                                String[] itemsetBlocks = patternPart.split("-1");
                                List<Integer> itemset = new ArrayList<>();
                                int nonEmptyBlockCount = 0;
                                for (String blk : itemsetBlocks) {
                                    blk = blk.trim();
                                    if (blk.isEmpty()) continue;
                                    nonEmptyBlockCount++;
                                    for (String t : blk.split("\\s+")) {
                                        if (!t.isEmpty()) itemset.add(Integer.parseInt(t));
                                    }
                                }
                                if (nonEmptyBlockCount == 1 && itemset.size() > 1) {
                                    Set<Integer> comboSet = new HashSet<>(itemset);
                                    if (comboSet.equals(enteredSet)) {
                                        System.out.println("✅ Khuyến mãi: Ưu đãi cho combo " + decodeItemset(itemset, map));
                                        comboActivated = true;
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Lỗi đọc file pattern: " + e.getMessage());
                        }
                        seq.add(new ArrayList<>(currentPurchase));
                    }
                    break;
                }
                List<Integer> entered = parseCodes(line);
                if (!entered.isEmpty()) {
                    currentPurchase.addAll(entered);
                    // Sort itemset in ascending order before displaying
                    List<Integer> sortedCurrent = new ArrayList<>(currentPurchase);
                    Collections.sort(sortedCurrent);
                    System.out.println("   -> Đã thêm vào giỏ: " + decodeItemset(sortedCurrent, map));
                    // Hiển thị khuyến mãi ngay sau khi thêm sản phẩm vào giỏ
                    boolean comboActivated = false;
                    Set<Integer> enteredSet = new HashSet<>(currentPurchase);
                    try (BufferedReader br = new BufferedReader(new FileReader("sales_patterns.txt"))) {
                        String pline;
                        while ((pline = br.readLine()) != null) {
                            pline = pline.trim();
                            if (pline.isEmpty() || !pline.contains("#SUP:")) continue;
                            String[] parts = pline.split("#SUP:");
                            String patternPart = parts[0].trim();
                            String[] itemsetBlocks = patternPart.split("-1");
                            List<Integer> itemset = new ArrayList<>();
                            int nonEmptyBlockCount = 0;
                            for (String blk : itemsetBlocks) {
                                blk = blk.trim();
                                if (blk.isEmpty()) continue;
                                nonEmptyBlockCount++;
                                for (String t : blk.split("\\s+")) {
                                    if (!t.isEmpty()) itemset.add(Integer.parseInt(t));
                                }
                            }
                            if (nonEmptyBlockCount == 1 && itemset.size() > 1) {
                                Set<Integer> comboSet = new HashSet<>(itemset);
                                if (comboSet.equals(enteredSet)) {
                                    System.out.println("✅ ĐÃ KÍCH HOẠT khuyến mãi: Ưu đãi cho combo " + decodeItemset(itemset, map) + "!");
                                    comboActivated = true;
                                    break;
                                } else if (enteredSet.size() < itemset.size() && itemset.containsAll(enteredSet)) {
                                    List<Integer> missing = new ArrayList<>(itemset);
                                    missing.removeAll(currentPurchase);
                                    System.out.println("🔥 Khuyến mãi: Nếu bạn mua thêm " + decodeItemset(missing, map) +
                                        " thì sẽ nhận ưu đãi cho combo " + decodeItemset(itemset, map) + "!");
                                    comboActivated = true;
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Lỗi đọc file pattern: " + e.getMessage());
                    }
                }
            }

            if (seq.isEmpty() || seq.get(seq.size()-1).isEmpty()) continue;

            // Đề xuất tuần tự cho lần mua tiếp theo (dựa vào tất cả các itemset đã mua)
            Set<Integer> sequentialSuggestions = new LinkedHashSet<>();
            Set<Integer> allPurchased = new HashSet<>();
            for (List<Integer> itemset : seq) allPurchased.addAll(itemset);
            try (BufferedReader br = new BufferedReader(new FileReader("sales_patterns.txt"))) {
                String pline;
                while ((pline = br.readLine()) != null) {
                    pline = pline.trim();
                    if (pline.isEmpty() || !pline.contains("#SUP:")) continue;
                    String[] parts = pline.split("#SUP:");
                    String patternPart = parts[0].trim();
                    List<List<Integer>> itemsets = new ArrayList<>();
                    for (String blk : patternPart.split("-1")) {
                        blk = blk.trim();
                        if (blk.isEmpty()) continue;
                        List<Integer> items = new ArrayList<>();
                        for (String t : blk.split("\\s+")) {
                            if (!t.isEmpty()) items.add(Integer.parseInt(t));
                        }
                        if (!items.isEmpty()) itemsets.add(items);
                    }
                    // Đề xuất sản phẩm từ các mẫu tuần tự có itemset đầu hoặc các itemset con là tập con của các lần mua
                    for (List<Integer> iset : itemsets) {
                        if (!allPurchased.containsAll(iset)) {
                            // Nếu iset chưa mua hết, đề xuất các sản phẩm còn thiếu
                            for (Integer sug : iset) {
                                if (!allPurchased.contains(sug)) sequentialSuggestions.add(sug);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Lỗi đọc file pattern: " + e.getMessage());
            }
            System.out.println("\n--- Đề xuất cho lần mua tiếp theo ---");
            if (sequentialSuggestions.isEmpty()) {
                System.out.println("(Không có gợi ý nào)");
            } else {
                System.out.println("Bạn có thể cân nhắc mua tiếp: " +
                    decodeItemset(new ArrayList<>(sequentialSuggestions), map));
            }
            System.out.println("=====================================================================");
        }
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
                        // FIX: Use productMapping instead of map if inside hienThiFrequentPatterns
                        names.add(map != null ? map.getOrDefault(Integer.parseInt(tok), "Unknown")
                                             : "Unknown");
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

    private static void gợiYenhanced(
    // THAY ĐỔI: Nhận vào toàn bộ chuỗi giao dịch để có đầy đủ thông tin
    List<List<Integer>> currentTransaction,
    List<PromotionPattern> promos,
    Map<Integer, String> map
) {
    // BƯỚC 1: Tái sử dụng logic tính toán chính xác mà chúng ta đã xây dựng.
    // Không cần viết lại logic so khớp ở đây.
    Set<Integer> recommendations = computeRecommendations(currentTransaction, promos);

    // BƯỚC 2: Giữ lại logic in kết quả của bạn vì nó rõ ràng và hiệu quả.
    System.out.println("\n--- Gợi ý nâng cao cho bạn ---");
    if (recommendations.isEmpty()) {
        System.out.println("Không có gợi ý nào phù hợp dựa trên giỏ hàng của bạn.");
    } else {
        List<String> names = new ArrayList<>();
        for (Integer itemCode : recommendations) {
            names.add(map.getOrDefault(itemCode, "Sản phẩm #" + itemCode));
        }
        System.out.println("Bạn có thể xem xét các sản phẩm sau: " + String.join(" / ", names));
    }
}

    // 6) Sinh luật kết hợp
    // 6) Sinh luật kết hợp
    private static void sinhAssociationRules(
        String patternFile,
        String transactionFile,
        Map<Integer, String> productMapping
    ) {
        // Đếm tổng số SID trong transaction
        int totalSID = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                totalSID++;
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file transaction: " + e.getMessage());
            return;
        }
        if (totalSID == 0) {
            System.out.println("Không có transaction nào.");
            return;
        }

        // Đọc các pattern và tính tỷ lệ support %
        List<String> patterns = new ArrayList<>();
        List<Double> sups = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(patternFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("#SUP:");
                String patternPart = p[0].trim();
                int sup = Integer.parseInt(p[1].trim().split("\\s+")[0]);
                double percent = (sup * 100.0) / totalSID;
                patterns.add(line);
                sups.add(percent);
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file pattern: " + e.getMessage());
            return;
        }

        if (patterns.isEmpty()) {
            System.out.println("Không có mẫu tuần tự.");
            return;
        }

        // Hỏi người dùng nhập tỷ lệ phần trăm (từ minsup*100 đến 100)
        double minPercent = minSupDefault * 100;
        Scanner sc = new Scanner(System.in);
        double userPercent = -1;
        while (userPercent < minPercent || userPercent > 100) {
            System.out.printf("Nhập tỷ lệ support tối thiểu (%.2f–100, đơn vị %%): ", minPercent);
            try {
                userPercent = Double.parseDouble(sc.nextLine().trim());
                if (userPercent < minPercent || userPercent > 100) {
                    System.out.println("Giá trị không hợp lệ.");
                }
            } catch (Exception e) {
                System.out.println("Giá trị không hợp lệ.");
            }
        }

        // Hiển thị các pattern có support >= userPercent
        System.out.println("\nCác mẫu có support >= " + userPercent + "%:");
        boolean found = false;
        for (int i = 0; i < patterns.size(); i++) {
            if (sups.get(i) >= userPercent) {
                found = true;
                String line = patterns.get(i);
                String[] p = line.split("#SUP:");
                String patternPart = p[0].trim();
                String supStr = p[1].trim().split("\\s+")[0];
                List<String> sets = new ArrayList<>();
                for (String blk : patternPart.split("-1")) {
                    blk = blk.trim();
                    if (blk.isEmpty()) continue;
                    List<String> names = new ArrayList<>();
                    for (String tok : blk.split("\\s+")) {
                        names.add(productMapping.getOrDefault(Integer.parseInt(tok), "Unknown"));
                    }
                    sets.add("{" + String.join(", ", names) + "}");
                }
                System.out.printf("%s    support=%s (%.2f%%)\n", String.join(" ; ", sets), supStr, sups.get(i));
            }
        }
        if (!found) {
            System.out.println("(Không có mẫu nào thỏa mãn)");
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
                        names.add(map.getOrDefault(Integer.parseInt(t), "Unknown"));
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
                String patt = p[0].trim();
                // Parse pattern into itemsets
                List<List<Integer>> itemsets = new ArrayList<>();
                for (String blk : patt.split("-1")) {
                    blk = blk.trim();
                    if (blk.isEmpty()) continue;
                    List<Integer> items = new ArrayList<>();
                    for (String t : blk.split("\\s+")) {
                        try { items.add(Integer.parseInt(t)); } catch (NumberFormatException ex) {}
                    }
                    if (!items.isEmpty()) itemsets.add(items);
                }
                // Flatten for subsequence check
                List<Integer> pattItems = new ArrayList<>();
                for (List<Integer> iset : itemsets) pattItems.addAll(iset);
                if (containsSubsequence(pattItems, query)) {
                    found++;
                    String decoded = decodeSeq(itemsets, map);
                    System.out.println("  " + decoded + " | support=" + p[1].trim().split("\\s+")[0]);
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
        if (line == null || line.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(line.trim().split("\\s+"))
                     .map(s -> {
                         try { return Integer.parseInt(s); } 
                         catch (NumberFormatException e) { return null; }
                     })
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    private static void printRecommendations(
    Set<Integer> recommendations,
    Map<Integer, String> map
    ) {
        System.out.println("--- Gợi ý từ hệ thống ---");
        if (recommendations.isEmpty()) {
            System.out.println("(Không có gợi ý nào thêm)");
            return;
        }
        System.out.println("Dựa trên giỏ hàng của bạn, có thể bạn sẽ quan tâm đến:");
        for (int itemCode : recommendations) {
            System.out.println(" -> " + map.getOrDefault(itemCode, "Sản phẩm mã " + itemCode));
        }
        System.out.println("-------------------------");
    }

    private static boolean isPatternMatch(List<List<Integer>> transaction, List<List<Integer>> patternAntecedent) {
        if (patternAntecedent.isEmpty()) return false;
        if (patternAntecedent.size() == 1) { // Co-occurrence
            List<Integer> requiredItems = patternAntecedent.get(0);
            for (List<Integer> transactionItemset : transaction) {
                if (new HashSet<>(transactionItemset).containsAll(requiredItems)) return true;
            }
            return false;
        } else { // Sequential
            int transactionIndex = 0;
            for (List<Integer> patternItemset : patternAntecedent) {
                boolean partFound = false;
                while (transactionIndex < transaction.size()) {
                    if (new HashSet<>(transaction.get(transactionIndex)).containsAll(patternItemset)) {
                        partFound = true;
                        transactionIndex++;
                        break;
                    }
                    transactionIndex++;
                }
                if (!partFound) return false;
            }
            return true;
        }
    }

    // tính khuyến mãi
    private static Set<Integer> computeRecommendations(
    List<List<Integer>> currentTransaction,
    List<PromotionPattern> patterns
) {
    Set<Integer> recommendations = new HashSet<>();
    for (PromotionPattern p : patterns) {
        // 1. Kiểm tra xem sản phẩm đề xuất đã được mua chưa
        boolean alreadyPurchased = false;
        for (List<Integer> itemset : currentTransaction) {
            if (itemset.contains(p.consequent)) {
                alreadyPurchased = true;
                break;
            }
        }
        if (alreadyPurchased) {
            continue; // Bỏ qua nếu đã mua rồi
        }

        // 2. Kiểm tra xem giỏ hàng hiện tại có khớp với mẫu vế trái không
        if (isPatternMatch(currentTransaction, p.antecedent)) {
            recommendations.add(p.consequent);
        }
    }
    return recommendations;
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

    // HÀM 1: Chỉ in ra các khuyến mãi dạng COMBO (MUA KÈM)
private static void printComboPromotions(List<Integer> justEnteredItems, List<PromotionPattern> allPatterns, Map<Integer, String> map) {
        System.out.println("\n--- Khuyến mãi Combo ---");
        boolean found = false;
        Set<Integer> justEnteredSet = new HashSet<>(justEnteredItems);
        for (PromotionPattern p : allPatterns) {
            if (p.isSequential) continue;

            List<Integer> fullComboItems = new ArrayList<>(p.antecedent.get(0));
            fullComboItems.add(p.consequent);

            if (new HashSet<>(fullComboItems).containsAll(justEnteredSet)) {
                List<Integer> remainingItems = new ArrayList<>(fullComboItems);
                remainingItems.removeAll(justEnteredSet);
                
                if (remainingItems.isEmpty()) {
                     System.out.println("✅ ĐÃ KÍCH HOẠT khuyến mãi cho combo " + decodeItemset(fullComboItems, map) + "!");
                } else {
                    System.out.println("🔥 Mua thêm " + decodeItemset(remainingItems, map) + " để hoàn thành combo " + decodeItemset(fullComboItems, map) + "!");
                }
                found = true;
            }
        }
        if (!found) {
            System.out.println("(Không có combo nào phù hợp)");
        }
    }


// HÀM 2: Chỉ in ra các đề xuất cho LẦN MUA TIẾP THEO (TUẦN TỰ)
private static void printSequentialRecommendations(List<List<Integer>> currentTransaction, List<PromotionPattern> allPatterns, Map<Integer, String> map) {
        System.out.println("\n--- Đề xuất cho lần mua tiếp theo ---");
        List<PromotionPattern> triggeredPatterns = new ArrayList<>();
        Set<Integer> allPurchasedItems = currentTransaction.stream().flatMap(List::stream).collect(Collectors.toSet());

        for (PromotionPattern p : allPatterns) {
            if (!p.isSequential || allPurchasedItems.contains(p.consequent)) continue;
            if (isPatternMatch(currentTransaction, p.antecedent)) {
                triggeredPatterns.add(p);
            }
        }

        if (triggeredPatterns.isEmpty()) {
            System.out.println("(Không có gợi ý nào)");
            return;
        }

        triggeredPatterns.sort((p1, p2) -> Double.compare(p2.confidence, p1.confidence));
        
        for (PromotionPattern p : triggeredPatterns) {
            String promoItemName = map.getOrDefault(p.consequent, "SP #" + p.consequent);
            if (p.confidence >= 0.95) {
                System.out.println("🔥 (Rất nên mua - " + String.format("%.0f%%", p.confidence * 100) + ") -> " + promoItemName);
            } else {
                System.out.println("💡 (Gợi ý - " + String.format("%.0f%%", p.confidence * 100) + ") -> " + promoItemName);
            }
        }
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

    // Hàm readPromotionPatterns cần được cập nhật để set cờ isSequential
    private static List<PromotionPattern> readPromotionPatterns(String f) {
        Map<String, Integer> supportData = new HashMap<>();
        List<String> patternKeys = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                if (ln.trim().isEmpty() || !ln.contains("#SUP:")) continue;
                String[] parts = ln.split("#SUP:");
                String key = parts[0].trim();
                int support = Integer.parseInt(parts[1].split("#SID:")[0].trim());
                supportData.put(key, support);
                patternKeys.add(key);
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file mẫu: " + e.getMessage());
            return new ArrayList<>();
        }

        List<PromotionPattern> list = new ArrayList<>();
        for (String patternKey : patternKeys) {
            List<List<Integer>> fullPattern = parsePatternString(patternKey);
            if (countTotalItems(fullPattern) < 2) continue;

            List<List<Integer>> antecedent = new ArrayList<>();
            for (List<Integer> itemset : fullPattern) {
                antecedent.add(new ArrayList<>(itemset));
            }

            List<Integer> lastItemset = antecedent.get(antecedent.size() - 1);
            int consequent = lastItemset.remove(lastItemset.size() - 1);

            if (lastItemset.isEmpty()) {
                antecedent.remove(antecedent.size() - 1);
            }
            if (antecedent.isEmpty()) continue;

            String antecedentKey = formatPatternToString(antecedent);
            Integer antecedentSupport = supportData.get(antecedentKey);
            Integer patternSupport = supportData.get(patternKey);

            if (antecedentSupport != null && antecedentSupport > 0) {
                double confidence = (double) patternSupport / antecedentSupport;
                boolean isSeq = fullPattern.size() > 1;
                list.add(new PromotionPattern(antecedent, consequent, confidence, isSeq));
            }
        }
        return list;
    }

    private static List<List<Integer>> parsePatternString(String patternKey) {
        List<List<Integer>> pattern = new ArrayList<>();
        for (String blk : patternKey.split("-1")) {
            blk = blk.trim();
            if (blk.isEmpty()) continue;
            List<Integer> items = new ArrayList<>();
            for (String t : blk.split("\\s+")) {
                if (!t.isEmpty()) items.add(Integer.parseInt(t));
            }
            if (!items.isEmpty()) pattern.add(items);
        }
        return pattern;
    }

    private static String decodeItemset(List<Integer> itemset, Map<Integer, String> map) {
        if (itemset == null || itemset.isEmpty()) return "{}";
        return "{" + itemset.stream()
                             .map(id -> map.getOrDefault(id, "SP #" + id))
                             .collect(Collectors.joining(", ")) + "}";
    }
    
    private static int countTotalItems(List<List<Integer>> pattern) {
        return pattern.stream().mapToInt(List::size).sum();
    }

    private static String formatPatternToString(List<List<Integer>> pattern) {
        return formatSeq(pattern);
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
    

    private static class PromotionPattern {
        List<List<Integer>> antecedent; // Vế trái của quy tắc
        int consequent;                 // Vế phải (sản phẩm đề xuất)
        double confidence;              // Độ tin cậy của quy tắc
        boolean isSequential;           // Cờ xác định loại quy tắc

        PromotionPattern(List<List<Integer>> antecedent, int consequent, double confidence, boolean isSequential) {
            this.antecedent = antecedent;
            this.consequent = consequent;
            this.confidence = confidence;
            this.isSequential = isSequential;
        }

        @Override
        public String toString() {
            return formatPatternToString(antecedent) + " -> {" + consequent + "} (Conf: " + String.format("%.2f", confidence) + ", Seq: " + isSequential + ")";
        }
    }

    // Helper function to get missing items for combo
private static List<Integer> getMissingItems(List<Integer> combo, List<Integer> entered) {
    List<Integer> missing = new ArrayList<>(combo);
    missing.removeAll(entered);
    return missing;
}

    public static Map<Integer, String> generateProductMappingFromFile(String filePath) {
        // Đọc từ file CSV download.csv, lấy Số thứ tự và Tên sản phẩm
        Map<Integer, String> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("Product_Details.csv"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // Bỏ qua dòng tiêu đề
                    isFirstLine = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 3);
                if (parts.length < 2) continue;
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    map.put(id, name);
                } catch (NumberFormatException ex) {
                    // Bỏ qua dòng không hợp lệ
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file download.csv: " + e.getMessage());
        }
        return map;
    }
}