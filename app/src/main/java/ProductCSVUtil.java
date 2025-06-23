package main.java;

import java.io.*;
import java.util.*;

public class ProductCSVUtil {

    // Đọc ánh xạ mã sản phẩm -> tên sản phẩm từ Product_Details.csv
    public static Map<String, String> readProductNameMap(String csvFile) {
        Map<String, String> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;
                String id = parts[0].trim();
                String name = parts[1].trim();
                map.put(id, name);
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file " + csvFile + ": " + e.getMessage());
        }
        return map;
    }

    // Đọc Online Retail.csv và xuất Product_Details.csv (STT, Product ID, Product Name)
    public static void exportProductDetails(String onlineRetailCsv, String productDetailsCsv) {
        Map<String, String> productMap = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(onlineRetailCsv))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",", 4);
                if (parts.length < 3) continue;
                String id = parts[1].trim();
                String name = parts[2].trim();
                // Chỉ lấy ID bắt đầu bằng số
                if (!id.matches("^\\d.*")) continue;
                if (!id.isEmpty() && !name.isEmpty()) {
                    productMap.put(id, name);
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi đọc file " + onlineRetailCsv + ": " + e.getMessage());
            return;
        }

        // Sắp xếp ID tăng dần (theo số)
        List<String> sortedIds = new ArrayList<>(productMap.keySet());
        sortedIds.sort(Comparator.comparingLong(id -> {
            try {
                return Long.parseLong(id.replaceAll("\\D.*", "")); // lấy phần số đầu tiên
            } catch (NumberFormatException e) {
                return Long.MAX_VALUE;
            }
        }));

        // Ghi ra Product_Details.csv với STT
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(productDetailsCsv))) {
            bw.write("STT,Product ID, Product Name\n");
            int stt = 1;
            for (String id : sortedIds) {
                bw.write(stt + "," + id + "," + productMap.get(id) + "\n");
                stt++;
            }
        } catch (IOException e) {
            System.out.println("Lỗi ghi file " + productDetailsCsv + ": " + e.getMessage());
        }
    }

    
}
