import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoPick {

    // Cấu hình
    private static final String FILE_PATH = "Test.csv";
    private static final int SAMPLE_SIZE = 100;
    private static final double DESCRIPTION_MIN_AVG_LENGTH = 15.0;
    private static final double CUSTOMER_ID_MIN_NUMERIC_PERCENT = 0.8;
    private static final double INVOICE_NO_MIN_NUMERIC_PERCENT = 0.8;
    private static final double STOCK_CODE_MAX_SPACE_PERCENT = 0.1;

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

            // Tất cả các dòng đều là dữ liệu (không có header)
            List<List<String>> columns = transposeData(dataSample);

            Map<String, Integer> identifiedColumns = identifyColumns(columns);

            System.out.println("✅ Kết quả phân tích file '" + FILE_PATH + "':");
            printResults(identifiedColumns, columns.size());

        } catch (FileNotFoundException e) {
            System.err.println("Không tìm thấy file: " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + e.getMessage());
        }
    }

    private static Map<String, Integer> identifyColumns(List<List<String>> columns) {
        Map<String, Integer> result = new HashMap<>();
        boolean[] isAssigned = new boolean[columns.size()];
        Arrays.fill(isAssigned, false);

        // 1. Tìm InvoiceNo (Transaction ID) FIRST - số, thường ở đầu
        int invoiceIndex = findInvoiceNoColumn(columns, isAssigned);
        if (invoiceIndex != -1) {
            result.put(INVOICE_NO, invoiceIndex);
            isAssigned[invoiceIndex] = true;
        }

        // 2. Tìm Description (chuỗi dài nhất)
        int descIndex = findDescriptionColumn(columns, isAssigned);
        if (descIndex != -1) {
            result.put(DESCRIPTION, descIndex);
            isAssigned[descIndex] = true;
        }

        // 3. Tìm CustomerID (số, thường ở cuối)
        int custIdIndex = findCustomerIdColumn(columns, isAssigned);
        if (custIdIndex != -1) {
            result.put(CUSTOMER_ID, custIdIndex);
            isAssigned[custIdIndex] = true;
        }

        // 4. Tìm StockCode (cột còn lại, alphanumeric)
        int stockCodeIndex = findStockCodeColumn(columns, isAssigned);
        if (stockCodeIndex != -1) {
            result.put(STOCK_CODE, stockCodeIndex);
        }

        return result;
    }

    private static int findInvoiceNoColumn(List<List<String>> columns, boolean[] isAssigned) {
        // Tìm cột chủ yếu là số, thường ở vị trí đầu - PRIORITY SEARCH
        for (int i = 0; i < Math.min(3, columns.size()); i++) {
            if (isAssigned[i]) continue;
            
            int nonEmptyCount = 0;
            int numericCount = 0;
            
            for (String cell : columns.get(i)) {
                if (cell != null && !cell.trim().isEmpty()) {
                    nonEmptyCount++;
                    if (cell.trim().matches("\\d+") || cell.trim().matches("C\\d+")) {
                        numericCount++;
                    }
                }
            }
            
            if (nonEmptyCount > 0 && (double)numericCount/nonEmptyCount > INVOICE_NO_MIN_NUMERIC_PERCENT) {
                return i;
            }
        }
        
        // Nếu không tìm thấy trong 3 cột đầu, tìm trong tất cả các cột
        for (int i = 0; i < columns.size(); i++) {
            if (isAssigned[i]) continue;
            
            int nonEmptyCount = 0;
            int numericCount = 0;
            
            for (String cell : columns.get(i)) {
                if (cell != null && !cell.trim().isEmpty()) {
                    nonEmptyCount++;
                    if (cell.trim().matches("\\d+") || cell.trim().matches("C\\d+")) {
                        numericCount++;
                    }
                }
            }
            
            if (nonEmptyCount > 0 && (double)numericCount/nonEmptyCount > INVOICE_NO_MIN_NUMERIC_PERCENT) {
                return i;
            }
        }
        
        return -1;
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

    private static int findCustomerIdColumn(List<List<String>> columns, boolean[] isAssigned) {
        // Tìm cột có pattern CustomerID (alphanumeric ngắn), thường ở vị trí cuối
        for (int i = columns.size() - 1; i >= 0; i--) {
            if (isAssigned[i]) continue;
            
            int nonEmptyCount = 0;
            int validCustomerIdCount = 0;
            
            for (String cell : columns.get(i)) {
                if (cell != null && !cell.trim().isEmpty()) {
                    nonEmptyCount++;
                    String trimmed = cell.trim();
                    
                    // CustomerID có thể là: số thuần, hoặc chữ + số (như KH1, KH2), hoặc mã ngắn
                    if (trimmed.matches("\\d+") || // Chỉ số
                        trimmed.matches("[A-Za-z]+\\d+") || // Chữ + số (KH1, KH2)
                        trimmed.matches("[A-Za-z0-9]{2,8}")) { // Alphanumeric ngắn
                        validCustomerIdCount++;
                    }
                }
            }
            
            if (nonEmptyCount > 0 && (double)validCustomerIdCount/nonEmptyCount > 0.7) {
                // Kiểm tra độ dài hợp lý cho Customer ID (2-8 ký tự)
                double avgLength = getAverageStringLength(columns.get(i));
                if (avgLength >= 2 && avgLength <= 8) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int findStockCodeColumn(List<List<String>> columns, boolean[] isAssigned) {
        for (int i = 0; i < columns.size(); i++) {
            if (isAssigned[i]) continue;
            
            int nonEmptyCount = 0;
            int alphanumericCount = 0;
            int spaceCount = 0;
            
            for (String cell : columns.get(i)) {
                if (cell != null && !cell.trim().isEmpty()) {
                    nonEmptyCount++;
                    String trimmed = cell.trim();
                    
                    // Kiểm tra alphanumeric (chữ và số, không có dấu cách)
                    if (trimmed.matches("[a-zA-Z0-9]+")) {
                        alphanumericCount++;
                    }
                    
                    if (trimmed.contains(" ")) {
                        spaceCount++;
                    }
                }
            }
            
            if (nonEmptyCount > 0) {
                double spacePercent = (double)spaceCount / nonEmptyCount;
                
                // StockCode nên có ít dấu cách
                if (spacePercent < STOCK_CODE_MAX_SPACE_PERCENT) {
                    return i;
                }
            }
        }
        
        // Nếu không tìm thấy, chọn cột chưa được gán đầu tiên
        for (int i = 0; i < columns.size(); i++) {
            if (!isAssigned[i]) {
                return i;
            }
        }
        return -1;
    }

    // Các phương thức tiện ích
    private static List<String[]> readCsvSample(String filePath, int sampleSize) throws IOException {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String delimiter = ",";
            
            if ((line = br.readLine()) != null) {
                if (line.contains(";") && line.split(";", -1).length > line.split(",", -1).length) {
                    delimiter = ";";
                }
                records.add(line.split(delimiter, -1));
            }
            
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
                System.out.printf("   - %-30s -> Không xác định\n", key + ":");
            }
        }
        
        System.out.println("\nMapping cho code của bạn:");
        System.out.println("int transactionIdIndex = " + identifiedColumns.getOrDefault(INVOICE_NO, -1) + ";");
        System.out.println("int productIdIndex = " + identifiedColumns.getOrDefault(STOCK_CODE, -1) + ";");
        System.out.println("int productNameIndex = " + identifiedColumns.getOrDefault(DESCRIPTION, -1) + ";");
        System.out.println("int customerIdIndex = " + identifiedColumns.getOrDefault(CUSTOMER_ID, -1) + ";");
    }
}