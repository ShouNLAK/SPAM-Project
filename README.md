# SPAM Project - Trợ Lý Bán Hàng Phân Tích Mẫu Tuần Tự

Chương trình này sử dụng **thuật toán SPAM** từ thư viện **SPMF** để khai phá các mẫu tuần tự phổ biến từ dữ liệu giao dịch bán hàng, đồng thời cung cấp giao diện tương tác giúp phân tích, truy vấn và gợi ý sản phẩm cho người bán.

## 1. Thành phần chính

- **IntegratedSalesAssistant.java**: Chương trình chính, giao diện menu dòng lệnh, xử lý toàn bộ chức năng.
- **sales_transactions.txt**: File dữ liệu giao dịch đầu vào (định dạng SPMF).
- **sales_patterns.txt**: File kết quả chứa các mẫu tuần tự phổ biến đã khai phá.
- **Product_Details.csv**: File ánh xạ mã sản phẩm sang tên sản phẩm.

## 2. Quy trình hoạt động

1. **Đọc dữ liệu giao dịch**  
   - Dữ liệu đầu vào là file `sales_transactions.txt`, mỗi dòng là một chuỗi giao dịch, các sản phẩm cách nhau bằng dấu cách, `-1` kết thúc một lần mua, `-2` kết thúc chuỗi.

2. **Khai phá mẫu tuần tự**  
   - Thuật toán SPAM được chạy tự động khi khởi động chương trình hoặc khi thay đổi tham số (minsup, độ dài mẫu, max gap).
   - Kết quả lưu vào `sales_patterns.txt`.

3. **Tạo bảng quy đổi sản phẩm**  
   - Tự động ánh xạ mã sản phẩm sang tên từ file `Product_Details.csv`.

4. **Menu chức năng tương tác**  
   - Giao diện dòng lệnh với các lựa chọn:
     1. Hiển thị bảng quy đổi mã sản phẩm.
     2. Nhập phiên giao dịch mới, nhận gợi ý khuyến mãi.
     3. Xem lịch sử giao dịch trong phiên.
     4. Xem các mẫu tuần tự phổ biến.
     5. Đề xuất sản phẩm nên mua thêm dựa trên mẫu tuần tự.
     6. Hiển thị danh sách khuyến mãi mở rộng từ từng sản phẩm.
     7. Sinh và xem các luật kết hợp tuần tự (association rules).
     8. Xem Top-K mẫu tuần tự phổ biến nhất.
     9. Tóm tắt & trực quan hóa mẫu tuần tự (thống kê, biểu đồ text).
     10. Truy vấn mẫu tuần tự chứa chuỗi sản phẩm bất kỳ.
     -1. Tuỳ chọn nâng cao: thay đổi minsup, độ dài mẫu, max gap...
     0. Thoát chương trình.

5. **Gợi ý & khuyến mãi**  
   - Đề xuất sản phẩm nên mua tiếp theo dựa trên mẫu tuần tự và lịch sử giao dịch.
   - Hiển thị các combo khuyến mãi, mở rộng sản phẩm, luật kết hợp.

## 3. Cách sử dụng

1. **Chuẩn bị dữ liệu**  
   - Đảm bảo có các file: `sales_transactions.txt`, `Product_Details.csv`.
2. **Chạy chương trình**  
   - Biên dịch và chạy `IntegratedSalesAssistant.java`.
3. **Sử dụng menu**  
   - Chọn các chức năng từ menu để nhập giao dịch, xem mẫu, truy vấn, nhận gợi ý, điều chỉnh tham số khai phá.
4. **Tùy chỉnh tham số**  
   - Vào menu nâng cao để thay đổi minsup, độ dài mẫu, max gap, sau đó chương trình sẽ tự động khai phá lại mẫu mới.

## 4. Định dạng dữ liệu

- **sales_transactions.txt**:  
  ```
  1 2 3 -1 4 5 -1 -2
  ```
  (Mỗi dòng là một chuỗi giao dịch, các sản phẩm cách nhau bằng dấu cách, `-1` kết thúc một lần mua, `-2` kết thúc chuỗi.)

- **Product_Details.csv**:  
  ```
  Mã,Tên sản phẩm
  1,Chuối
  2,Ổi
  ...
  ```

## 5. Phụ thuộc

- **SPMF**: Thư viện Java khai phá mẫu tuần tự (https://www.philippe-fournier-viger.com/spmf/).

## 6. Ghi chú

- Chương trình tự động cập nhật mẫu khi thay đổi tham số khai phá.
- Có thể mở rộng thêm các chức năng phân tích, trực quan hóa hoặc xuất dữ liệu theo nhu cầu.

---

**Tác giả:**  
- Dựa trên thuật toán SPAM và thư viện SPMF  
- Tối ưu giao diện và chức năng cho nghiệp vụ bán hàng thực tế
