import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class AutoPick {

    // Cấu hình
    private static final String FILE_PATH = "Online Retail.csv";
    private static final int SAMPLE_SIZE = 500; // Tăng sample size để kiểm tra nhóm chính xác hơn
    private static final double DESCRIPTION_MIN_AVG_LENGTH = 15.0;
    private static final double CONTINUITY_THRESHOLD = 0.7; // Ngưỡng để xác định tính liên tục

    // Tên cột
    private static final String INVOICE_NO = "Mã giao dịch (InvoiceNo)";
    private static final String STOCK_CODE = "Mã sản phẩm (StockCode)";
    private static final String DESCRIPTION = "Tên sản phẩm (Description)";
    private static final String CUSTOMER_ID = "Mã khách hàng (CustomerID)";

    public static void main(String[] args) {
        try {
            List<String[]> dataSample = readCsvSample(FILE_PATH, SAMPLE_SIZE);
            if (dataSample.isEmpty()) {
                System.out.println("Không thể đọc dữ liệu hoặc file trống");
                return;
            }

            List<List<String>> columns = transposeData(dataSample);

            // Pass cả dataSample (dạng hàng) để kiểm tra nhóm
            Map<String, Integer> identifiedColumns = identifyColumns(columns, dataSample);

            System.out.println("✅ Kết quả phân tích file '" + FILE_PATH + "':");
            printResults(identifiedColumns, columns.size());

        } catch (FileNotFoundException e) {
            System.err.println("Không tìm thấy file: " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + e.getMessage());
        }
    }

    private static Map<String, Integer> identifyColumns(List<List<String>> columns, List<String[]> dataSample) {
        Map<String, Integer> result = new HashMap<>();
        boolean[] isAssigned = new boolean[columns.size()];
        Arrays.fill(isAssigned, false);

        // 1. Tìm Transaction ID (kiểm tra tính liên tục với TreeSet)
        int transactionIndex = findTransactionIdColumn(columns, isAssigned);
        if (transactionIndex != -1) {
            result.put(INVOICE_NO, transactionIndex);
            isAssigned[transactionIndex] = true;
        } else {
             // Nếu không tìm thấy mã giao dịch, không thể tìm mã khách hàng theo logic mới
            System.err.println("⚠️ Cảnh báo: Không thể xác định cột Mã Giao Dịch. Việc tìm Mã Khách Hàng có thể không chính xác.");
        }

        // 2. Tìm Product Name/Description (chuỗi dài nhất)
        int descIndex = findDescriptionColumn(columns, isAssigned);
        if (descIndex != -1) {
            result.put(DESCRIPTION, descIndex);
            isAssigned[descIndex] = true;
        }

        // 3. Tìm Product ID (kề với Product Name)
        int productIdIndex = findProductIdColumn(columns, isAssigned, descIndex);
        if (productIdIndex != -1) {
            result.put(STOCK_CODE, productIdIndex);
            isAssigned[productIdIndex] = true;
        }

        // 4. Tìm Customer ID (dựa vào tính nhất quán trong mỗi giao dịch)
        // Logic này cần transactionIndex đã được tìm thấy
        if (transactionIndex != -1) {
             int custIdIndex = findCustomerIdColumn(columns, isAssigned, transactionIndex, dataSample);
             if (custIdIndex != -1) {
                 result.put(CUSTOMER_ID, custIdIndex);
                 isAssigned[custIdIndex] = true;
             }
        }

        return result;
    }

    // =================================================================================
    // === LOGIC TÌM MÃ KHÁCH HÀNG ĐÃ ĐƯỢC MODIFY ===
    // =================================================================================

    /**
     * Tìm cột Customer ID bằng cách kiểm tra tính nhất quán của giá trị trong cùng một giao dịch.
     * Cột Customer ID phải có cùng một giá trị cho tất cả các dòng có cùng mã giao dịch.
     */
    private static int findCustomerIdColumn(List<List<String>> columns, boolean[] isAssigned, int transactionIndex, List<String[]> dataRows) {
        int bestCandidateIndex = -1;
        double lowestUniquenessScore = 2.0; // Bắt đầu với giá trị cao để tìm giá trị thấp hơn (lặp lại nhiều)

        // Duyệt qua các cột chưa được gán để tìm ứng viên
        for (int i = 0; i < columns.size(); i++) {
            if (isAssigned[i]) {
                continue;
            }

            // Kiểm tra xem cột ứng viên có nhất quán trong các nhóm giao dịch không
            if (isConsistentWithinTransaction(i, transactionIndex, dataRows)) {
                // Nếu nhất quán, nó là một ứng viên. Giờ ta chọn ứng viên tốt nhất.
                // Ứng viên tốt nhất là ứng viên có độ lặp lại cao nhất (tức là uniqueness thấp nhất)
                double uniqueness = getUniquenessRatio(columns.get(i));
                
                if (uniqueness < lowestUniquenessScore) {
                    lowestUniquenessScore = uniqueness;
                    bestCandidateIndex = i;
                }
            }
        }
        
        return bestCandidateIndex;
    }

    /**
     * Helper method: Kiểm tra xem một cột có giá trị nhất quán trong các nhóm giao dịch hay không.
     * @param candidateColIndex Chỉ số của cột ứng viên cần kiểm tra.
     * @param transactionColIndex Chỉ số của cột Mã Giao Dịch đã xác định.
     * @param dataRows Dữ liệu gốc theo từng hàng.
     * @return true nếu cột ứng viên nhất quán, false nếu không.
     */
    private static boolean isConsistentWithinTransaction(int candidateColIndex, int transactionColIndex, List<String[]> dataRows) {
        // Nhóm các giá trị của cột ứng viên theo từng mã giao dịch
        // Ví dụ: Map<"Mã GD 1", {"KH_A", "KH_A"}>, Map<"Mã GD 2", {"KH_B", "KH_B", "KH_B"}>
        Map<String, Set<String>> transactionGroups = new HashMap<>();

        for (String[] row : dataRows) {
            if (transactionColIndex >= row.length || candidateColIndex >= row.length) {
                continue; // Bỏ qua hàng không đủ cột
            }
            String transactionId = row[transactionColIndex].trim();
            String candidateValue = row[candidateColIndex].trim();
            
            // Chỉ xử lý các giao dịch không rỗng
            if (transactionId.isEmpty()) {
                continue;
            }

            // Bỏ qua các giá trị rỗng của cột KH (khách vãng lai) vì chúng luôn nhất quán
            if (candidateValue.isEmpty()){
                continue;
            }
            
            transactionGroups.computeIfAbsent(transactionId, k -> new HashSet<>()).add(candidateValue);
        }

        // Kiểm tra từng nhóm: nếu bất kỳ nhóm nào có nhiều hơn 1 giá trị khác nhau -> không nhất quán
        for (Set<String> groupValues : transactionGroups.values()) {
            if (groupValues.size() > 1) {
                return false; // Phát hiện sự không nhất quán -> đây không phải cột Customer ID
            }
        }
        
        // Nếu tất cả các nhóm đều nhất quán
        return true;
    }
    
    /**
     * Helper method: Tính tỉ lệ duy nhất (uniqueness) của một cột.
     * Tỉ lệ càng thấp, độ lặp lại càng cao.
     */
    private static double getUniquenessRatio(List<String> column) {
        if (column == null || column.isEmpty()) return 1.0;

        List<String> nonEmptyValues = column.stream()
            .filter(s -> s != null && !s.trim().isEmpty())
            .collect(Collectors.toList());

        if (nonEmptyValues.isEmpty()) return 1.0; // Coi như không có sự lặp lại
        
        long uniqueCount = new HashSet<>(nonEmptyValues).size();
        return (double) uniqueCount / nonEmptyValues.size();
    }


    // =================================================================================
    // === CÁC LOGIC KHÁC GIỮ NGUYÊN ===
    // =================================================================================

    private static int findTransactionIdColumn(List<List<String>> columns, boolean[] isAssigned) {
        double bestContinuityScore = 0;
        int bestIndex = -1;

        for (int i = 0; i < columns.size(); i++) {
            if (isAssigned[i]) continue;

            TreeSet<Integer> numericValues = new TreeSet<>();
            int totalNonEmpty = 0;
            int numericCount = 0;

            for (String cell : columns.get(i)) {
                if (cell != null && !cell.trim().isEmpty()) {
                    totalNonEmpty++;
                    String trimmed = cell.trim();
                    try {
                        if (trimmed.matches("\\d+")) {
                            numericValues.add(Integer.parseInt(trimmed));
                            numericCount++;
                        } else if (trimmed.matches("C\\d+")) {
                            numericValues.add(Integer.parseInt(trimmed.substring(1)));
                            numericCount++;
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua
                    }
                }
            }

            if (totalNonEmpty == 0 || (double)numericCount / totalNonEmpty < 0.8) {
                continue;
            }

            double continuityScore = calculateContinuityScore(numericValues);
            
            if (continuityScore > bestContinuityScore) {
                bestContinuityScore = continuityScore;
                bestIndex = i;
            }
        }

        return bestContinuityScore > CONTINUITY_THRESHOLD ? bestIndex : -1;
    }

    private static double calculateContinuityScore(TreeSet<Integer> values) {
        if (values.size() < 2) return 0;

        List<Integer> sortedValues = new ArrayList<>(values);
        int continuousRanges = 0;
        int totalGaps = 0;

        for (int i = 1; i < sortedValues.size(); i++) {
            int gap = sortedValues.get(i) - sortedValues.get(i - 1);
            totalGaps++;
            
            if (gap <= 2 && gap > 0) { // Thêm điều kiện gap > 0 để tránh mã trùng nhau
                continuousRanges++;
            }
        }

        return totalGaps > 0 ? (double)continuousRanges / totalGaps : 0;
    }

    private static int findDescriptionColumn(List<List<String>> columns, boolean[] isAssigned) {
        int bestIndex = -1;
        double maxAvgLength = 0;
        
        for (int i = 0; i < columns.size(); i++) {
            if (isAssigned[i]) continue;
            
            double avgLength = getAverageStringLength(columns.get(i));
            if (avgLength > maxAvgLength) {
                maxAvgLength = avgLength;
                bestIndex = i;
            }
        }
        
        return (maxAvgLength > DESCRIPTION_MIN_AVG_LENGTH) ? bestIndex : -1;
    }

    private static int findProductIdColumn(List<List<String>> columns, boolean[] isAssigned, int descriptionIndex) {
        if (descriptionIndex != -1) {
            int[] candidates = {descriptionIndex - 1, descriptionIndex + 1};
            for (int candidate : candidates) {
                if (candidate >= 0 && candidate < columns.size() && !isAssigned[candidate]) {
                    if (isValidProductIdColumn(columns.get(candidate))) {
                        return candidate;
                    }
                }
            }
        }
        return findProductIdByPattern(columns, isAssigned);
    }

    private static int findProductIdByPattern(List<List<String>> columns, boolean[] isAssigned) {
        for (int i = 0; i < columns.size(); i++) {
            if (isAssigned[i]) continue;
            if (isValidProductIdColumn(columns.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isValidProductIdColumn(List<String> column) {
        int nonEmptyCount = 0;
        int validCodeCount = 0;
        int spaceCount = 0;

        for (String cell : column) {
            if (cell != null && !cell.trim().isEmpty()) {
                nonEmptyCount++;
                String trimmed = cell.trim();
                if (trimmed.matches("[a-zA-Z0-9]+")) {
                    validCodeCount++;
                }
                if (trimmed.contains(" ")) {
                    spaceCount++;
                }
            }
        }
        if (nonEmptyCount == 0) return false;
        
        double validPercent = (double)validCodeCount / nonEmptyCount;
        double spacePercent = (double)spaceCount / nonEmptyCount;
        return validPercent > 0.8 && spacePercent < 0.1;
    }
    
    private static List<String[]> readCsvSample(String filePath, int sampleSize) throws IOException {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String delimiter = ",";
            
            // Đọc header để xác định delimiter và bỏ qua nó
            if ((line = br.readLine()) != null) {
                if (line.contains(";") && line.split(";", -1).length > line.split(",", -1).length) {
                    delimiter = ";";
                }
            }
            
            // Đọc các dòng dữ liệu
            while ((line = br.readLine()) != null && records.size() < sampleSize) {
                records.add(line.split(delimiter, -1));
            }
        }
        return records;
    }

    private static List<List<String>> transposeData(List<String[]> data) {
        if (data.isEmpty()) return new ArrayList<>();
        
        int numColumns = 0;
        for (String[] row : data) {
            if (row.length > numColumns) {
                numColumns = row.length;
            }
        }

        List<List<String>> columns = new ArrayList<>(numColumns);
        for (int i = 0; i < numColumns; i++) {
            columns.add(new ArrayList<>());
        }

        for (String[] row : data) {
            for (int i = 0; i < numColumns; i++) {
                if (i < row.length) {
                    columns.get(i).add(row[i].trim());
                } else {
                    columns.get(i).add("");
                }
            }
        }
        return columns;
    }

    private static double getAverageStringLength(List<String> column) {
        if (column.isEmpty()) return 0;
        double totalLength = 0;
        int nonEmptyCount = 0;
        for (String s : column) {
            if (s != null && !s.trim().isEmpty()) {
                totalLength += s.length();
                nonEmptyCount++;
            }
        }
        return nonEmptyCount > 0 ? totalLength / nonEmptyCount : 0;
    }

    private static void printResults(Map<String, Integer> identifiedColumns, int totalColumns) {
        String[] required = {INVOICE_NO, STOCK_CODE, DESCRIPTION, CUSTOMER_ID};
        
        System.out.println("Các cột đã xác định:");
        for (String key : required) {
            if (identifiedColumns.containsKey(key)) {
                int index = identifiedColumns.get(key);
                System.out.printf("   - %-30s -> Cột %d\n", key + ":", index);
            } else {
                System.out.printf("   - %-30s -> ⚠️ Không xác định\n", key + ":");
            }
        }
        
        System.out.println("\nMapping cho code của bạn:");
        System.out.println("int transactionIdIndex = " + identifiedColumns.getOrDefault(INVOICE_NO, -1) + ";");
        System.out.println("int productIdIndex = " + identifiedColumns.getOrDefault(STOCK_CODE, -1) + ";");
        System.out.println("int productNameIndex = " + identifiedColumns.getOrDefault(DESCRIPTION, -1) + ";");
        System.out.println("int customerIdIndex = " + identifiedColumns.getOrDefault(CUSTOMER_ID, -1) + ";");
    }
}
