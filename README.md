# SPAM Project - Trợ Lý Bán Hàng Phân Tích Mẫu Tuần Tự

**Tác giả: ShouNLAK**

---

## 🚀 Giới thiệu

**SPAM Project** là một hệ thống trợ lý bán hàng thông minh sử dụng thuật toán khai phá mẫu tuần tự (SPAM) từ thư viện SPMF. Chương trình giúp phân tích lịch sử giao dịch, phát hiện các mẫu mua hàng phổ biến, gợi ý sản phẩm, khuyến mãi, thống kê doanh thu và hỗ trợ nghiệp vụ bán hàng thực tế.

---

## 🗂️ Thành phần chính

- **IntegratedSalesAssistant.java**: Chương trình chính, giao diện menu dòng lệnh, xử lý toàn bộ chức năng.
- **sales_transactions.txt**: File dữ liệu giao dịch đầu vào (định dạng SPMF).
- **sales_patterns.txt**: File kết quả chứa các mẫu tuần tự phổ biến đã khai phá.
- **Product_Details.csv**: File ánh xạ mã sản phẩm sang tên và giá sản phẩm.

---

## 🛠️ Chức năng nổi bật

### 1. Quản lý sản phẩm & giao dịch

- **Bảng quy đổi mã sản phẩm**: Hiển thị danh sách mã và tên sản phẩm.
- **Tìm kiếm sản phẩm theo tên**: Nhập từ khóa để tra cứu nhanh sản phẩm.
- **Nhập phiên giao dịch mới**: Giao diện nhập mã sản phẩm, tự động lưu lịch sử, gợi ý khuyến mãi theo combo.

### 2. Lịch sử & thống kê

- **Xem lịch sử giao dịch trong phiên**: Hiển thị các giao dịch vừa nhập.
- **Xem lịch sử giao dịch chi tiết (theo SID)**: Truy xuất từng giao dịch trong file lịch sử, chọn xem tất cả hoặc chỉ 1 SID.
- **Thống kê doanh thu & số lượng bán**: Thống kê số lần bán và tổng doanh thu từng sản phẩm (tự động lấy giá từ Product_Details.csv).

### 3. Khai phá mẫu tuần tự & gợi ý

- **Khai phá mẫu tuần tự (SPAM)**: Tự động chạy khi khởi động hoặc thay đổi tham số.
- **Xem mẫu thường xuyên**: Hiển thị các mẫu tuần tự phổ biến nhất.
- **Đề xuất sản phẩm nên mua thêm**: Gợi ý sản phẩm dựa trên mẫu tuần tự và lịch sử mua hàng.
- **Danh sách khuyến mãi mở rộng**: Hiển thị các combo sản phẩm, khuyến mãi mua kèm.
- **Sinh luật kết hợp tuần tự**: Xem các luật kết hợp với ngưỡng support tùy chỉnh.
- **Top-K mẫu tuần tự**: Xem K mẫu phổ biến nhất.
- **Tóm tắt & trực quan hóa mẫu tuần tự**: Thống kê, biểu đồ tần suất xuất hiện sản phẩm trong mẫu.
- **Truy vấn mẫu tuần tự**: Tìm mẫu chứa chuỗi sản phẩm bất kỳ.

### 4. Tuỳ chỉnh nâng cao

- **Thay đổi tham số khai phá**: Minsup, độ dài mẫu tối đa/tối thiểu, max gap.
- **Tự động cập nhật mẫu khi thay đổi tham số**.

---

## 📋 Hướng dẫn sử dụng

### 1. Chuẩn bị dữ liệu

- Đảm bảo có các file:
  - `sales_transactions.txt` (lịch sử giao dịch, định dạng SPMF)
  - `Product_Details.csv` (danh sách sản phẩm, giá)

### 2. Chạy chương trình

- Biên dịch và chạy `IntegratedSalesAssistant.java` bằng Java 8+.
- Chương trình sẽ tự động khai phá mẫu tuần tự và hiển thị menu.

### 3. Giao diện menu (ví dụ)

```
==================================================
|           SALES ASSISTANT MAIN MENU            |
==================================================
|  1. Bảng quy đổi trái cây (mã -> tên)          |
|  1.1. Tìm kiếm sản phẩm theo tên               |
|  2. Nhập phiên giao dịch mới                   |
|  3. Xem lịch sử giao dịch trong phiên          |
|  3.1. Xem lịch sử giao dịch chi tiết (theo SID) |
|  4. Xem mẫu thường xuyên                       |
|  5. Đề xuất sau mua                            |
|  6. Danh sách khuyến mãi                       |
|  7. Khai thác luật kết hợp                     |
|  8. Xem Top-K mẫu tuần tự                      |
|  9. Tóm tắt & trực quan hóa mẫu tuần tự        |
|  9.1. Thống kê doanh thu & số lượng bán        |
| 10. Truy vấn mẫu tuần tự                       |
| -1. Tuỳ chọn nâng cao (minsup, độ dài, ...)    |
|  0. Thoát chương trình                         |
==================================================
Nhập lựa chọn của bạn (0-10, -1):
```

### 4. Một số ví dụ thao tác

- **Xem bảng quy đổi sản phẩm**: Chọn `1`
- **Tìm kiếm sản phẩm**: Chọn `1.1`, nhập từ khóa
- **Nhập giao dịch mới**: Chọn `2`, nhập mã sản phẩm, chương trình tự động gợi ý combo/khuyến mãi
- **Xem lịch sử chi tiết**: Chọn `3.1`, nhập `all` để xem tất cả hoặc nhập số SID cụ thể
- **Thống kê doanh thu**: Chọn `9.1`, chương trình sẽ hiển thị số lần bán và tổng doanh thu từng sản phẩm

---

## 📄 Định dạng dữ liệu

### sales_transactions.txt

```
1 2 3 -1 4 5 -1 -2
...
```
- Mỗi dòng là một chuỗi giao dịch, các sản phẩm cách nhau bằng dấu cách, `-1` kết thúc một lần mua, `-2` kết thúc chuỗi.

### Product_Details.csv

```
Số thứ tự,Tên sản phẩm,Giá
1,Chuối,10000
2,Ổi,12000
...
```

---

## 💡 Lưu ý & mở rộng

- Chương trình tự động cập nhật mẫu khi thay đổi tham số khai phá.
- Có thể mở rộng thêm các chức năng phân tích, xuất báo cáo, giao diện đồ họa...
- Hỗ trợ tốt cho nghiệp vụ bán hàng, quản lý khuyến mãi, phân tích hành vi khách hàng.

---

## 📚 Phụ thuộc

- **SPMF**: Thư viện Java khai phá mẫu tuần tự (https://www.philippe-fournier-viger.com/spmf/).
- Java 8+

---

## 📝 Bản quyền

- Tác giả: ShouNLAK
- Dựa trên thuật toán SPAM và thư viện SPMF
- Tối ưu giao diện và chức năng cho nghiệp vụ bán hàng thực tế

---
