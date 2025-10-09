//package com.ntd.uniorien.scheduler;
//
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//// Class để lưu trữ thông tin của mỗi trường
//class SchoolInfo {
//    String code;
//    String name;
//    String url;
//
//    public SchoolInfo(String code, String name, String url) {
//        this.code = code;
//        this.name = name;
//        this.url = url;
//    }
//
//    @Override
//    public String toString() {
//        return "Mã: " + code + ", Tên: " + name + ", URL: " + url;
//    }
//}
//
//public class CrawlSchoolsList {
//
//    public static void main(String[] args) {
//        String url = "https://diemthi.tuyensinh247.com/diem-chuan.html";
//        List<SchoolInfo> schoolsList = crawlSchoolData(url);
//
//        // In kết quả
//        System.out.println("========================================================");
//        System.out.println("DANH SÁCH CÁC TRƯỜNG ĐẠI HỌC VÀ ĐƯỜNG DẪN ĐIỂM CHUẨN");
//        System.out.println("Tổng số trường tìm thấy: " + schoolsList.size());
//        System.out.println("========================================================");
//
//        for (SchoolInfo school : schoolsList) {
//            System.out.println(school);
//        }
//    }
//
//    public static List<SchoolInfo> crawlSchoolData(String url) {
//        List<SchoolInfo> schools = new ArrayList<>();
//
//        try {
//            // 1. Gửi request và kết nối
//            Document doc = Jsoup.connect(url)
//                    .timeout(10000) // Timeout 10 giây
//                    .get();
//
//            // 2. Chọn khối chứa danh sách trường: div có class="list-schol-box"
//            Element listScholBox = doc.selectFirst("div.list-schol-box");
//
//            if (listScholBox == null) {
//                System.err.println("Không tìm thấy khối chứa danh sách trường (div.list-schol-box).");
//                return schools;
//            }
//
//            // 3. Chọn tất cả các thẻ <a> bên trong <li> của khối đó
//            Elements schoolLinks = listScholBox.select("li > a");
//
//            if (schoolLinks.isEmpty()) {
//                System.err.println("Không tìm thấy bất kỳ đường dẫn trường nào.");
//                return schools;
//            }
//
//            // 4. Lặp qua từng đường dẫn và trích xuất dữ liệu
//            for (Element link : schoolLinks) {
//                // Lấy URL tuyệt đối (Absolute URL)
//                String schoolUrl = link.absUrl("href");
//
//                // Lấy Mã trường (nằm trong thẻ <strong>)
//                Element codeElement = link.selectFirst("strong");
//                String schoolCode = (codeElement != null) ? codeElement.text().trim() : "N/A";
//
//                // Lấy Tên trường: Lấy toàn bộ text của thẻ <a>, sau đó cắt bỏ Mã trường và ký tự thừa
//                String fullText = link.text();
//                String rawName = fullText.replace(schoolCode, "").trim();
//
//                // Làm sạch Tên trường:
//                // Xóa ký tự unicode (như ) và khoảng trắng thừa
//                String schoolName = rawName.replaceAll("", "").trim();
//
//                schools.add(new SchoolInfo(schoolCode, schoolName, schoolUrl));
//            }
//
//        } catch (IOException e) {
//            System.err.println("Đã xảy ra lỗi khi truy cập URL: " + e.getMessage());
//        }
//
//        return schools;
//    }
//}