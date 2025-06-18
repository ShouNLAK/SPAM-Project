# SPAM Project - Sales Pattern Mining

Chương trình sử dụng **thuật toán SPAM** của thư viện **SPMF** để khai phá các mẫu tuần tự phổ biến từ dữ liệu giao dịch bán hàng.

## Cách chương trình hoạt động

1. **Dữ liệu đầu vào**  
   - Chương trình sử dụng file `sales_transactions.txt` chứa các giao dịch bán hàng theo định dạng SPMF.
   - Mỗi dòng là một chuỗi giao dịch của khách hàng, các sản phẩm cách nhau bằng dấu cách, `-1` đánh dấu kết thúc một lần mua, `-2` đánh dấu kết thúc chuỗi.

2. **Khai phá mẫu tuần tự**  
   - Chương trình áp dụng thuật toán **SPAM** (Sequential Pattern Mining) của **SPMF** để tìm các mẫu tuần tự phổ biến từ dữ liệu giao dịch.
   - Tham số ngưỡng hỗ trợ tối thiểu (`minsup`) và các tham số khác (độ dài mẫu tối đa/tối thiểu, khoảng cách tối đa) có thể chỉnh sửa trong menu tuỳ chọn nâng cao.

3. **Kết quả mẫu**  
   - Các mẫu tuần tự được lưu vào file `sales_patterns.txt` theo định dạng SPMF, kèm theo số lần xuất hiện (support) và danh sách các chuỗi chứa mẫu đó.

4. **Menu tương tác**  
   - Chương trình chính (`IntegratedSalesAssistant.java`) cung cấp menu dạng văn bản cho người dùng với các chức năng sau:

### Ý nghĩa 10 chức năng chính trong menu

| Số | Chức năng | Ý nghĩa |
|----|-----------|---------|
| 1  | Bảng quy đổi trái cây (mã -> tên) | Hiển thị bảng ánh xạ mã sản phẩm sang tên trái cây tương ứng. |
| 2  | Nhập phiên giao dịch mới | Nhập một chuỗi giao dịch mới, nhận gợi ý khuyến mãi theo mẫu tuần tự. |
| 3  | Xem lịch sử giao dịch trong phiên | Hiển thị các giao dịch đã nhập trong phiên làm việc hiện tại. |
| 4  | Xem mẫu thường xuyên | Hiển thị các mẫu tuần tự phổ biến (frequent patterns) đã khai phá được. |
| 5  | Gợi ý nâng cao | Đưa ra gợi ý sản phẩm nên mua thêm dựa trên các mẫu khuyến mãi. |
| 6  | Khai thác luật kết hợp tuần tự | Sinh và hiển thị các luật kết hợp tuần tự (sequential association rules) với ngưỡng độ tin cậy do người dùng nhập. |
| 7  | Xem Top-K mẫu tuần tự | Hiển thị K mẫu tuần tự phổ biến nhất theo số lần xuất hiện. |
| 8  | Tóm tắt & trực quan hóa mẫu tuần tự | Thống kê tổng quan, tần suất xuất hiện của các sản phẩm trong mẫu, biểu đồ text. |
| 9  | Truy vấn mẫu tuần tự | Cho phép nhập một chuỗi sản phẩm, tìm các mẫu tuần tự chứa chuỗi đó. |
| 10 | Gợi ý sản phẩm tiếp theo | Dựa vào chuỗi sản phẩm hiện tại, gợi ý sản phẩm có khả năng xuất hiện tiếp theo dựa trên mẫu tuần tự đã khai phá. |

- Ngoài ra, chọn **-1** để vào menu tuỳ chọn nâng cao (chỉnh minsup, độ dài mẫu, max gap...).

5. **Tuỳ chỉnh**  
   - Sau khi thay đổi các tham số khai phá, chương trình sẽ tự động chạy lại thuật toán SPAM và cập nhật mẫu mới.

6. **Phụ thuộc**  
   - Chương trình sử dụng **thư viện SPMF** (Java) để khai phá mẫu tuần tự.

---

**Quy trình sử dụng điển hình:**
1. Khởi động chương trình.
2. Chọn các chức năng từ menu để phân tích, truy vấn hoặc trực quan hóa mẫu bán hàng.
3. Có thể điều chỉnh các tham số khai phá để tìm mẫu phù hợp hơn.
4. Sử dụng kết quả cho phân tích bán hàng hoặc gợi ý sản phẩm.

```
- `sales_transactions.txt` : Dữ liệu giao dịch đầu vào (định dạng SPMF)
- `sales_patterns.txt`     : Kết quả mẫu tuần tự phổ biến
- `IntegratedSalesAssistant.java` : Chương trình trợ lý bán hàng chính
```
