package com.ntd.uniorien.service;

import com.ntd.uniorien.utils.raw.SchoolInfo;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CrawlService {

    @NonFinal
    @Value("${app.crawl.url}")
    protected String CRAWL_URL;

    @PreAuthorize("hasRole('ADMIN')")
    public List<SchoolInfo> crawlUniversities() {
        List<SchoolInfo> schools = new ArrayList<>();

        try {
            // 1. Gửi request và kết nối
            Document doc = Jsoup.connect(CRAWL_URL)
                    .timeout(10000) // Timeout 10 giây
                    .get();

            // 2. Chọn khối chứa danh sách trường: div có class="list-schol-box"
            Element listScholBox = doc.selectFirst("div.list-schol-box");

            if (listScholBox == null) {
                System.err.println("Không tìm thấy khối chứa danh sách trường (div.list-schol-box).");
                return schools;
            }

            // 3. Chọn tất cả các thẻ <a> bên trong <li> của khối đó
            Elements schoolLinks = listScholBox.select("li > a");

            if (schoolLinks.isEmpty()) {
                System.err.println("Không tìm thấy bất kỳ đường dẫn trường nào.");
                return schools;
            }

            // 4. Lặp qua từng đường dẫn và trích xuất dữ liệu
            for (Element link : schoolLinks) {
                // Lấy URL tuyệt đối (Absolute URL)
                String schoolUrl = link.absUrl("href");

                // Lấy Mã trường (nằm trong thẻ <strong>)
                Element codeElement = link.selectFirst("strong");
                String schoolCode = (codeElement != null) ? codeElement.text().trim() : "N/A";

                // Lấy Tên trường: Lấy toàn bộ text của thẻ <a>, sau đó cắt bỏ Mã trường và ký tự thừa
                String fullText = link.text();
                String rawName = fullText.replace(schoolCode, "").trim();

                // Làm sạch Tên trường:
                // Xóa ký tự unicode (như ) và khoảng trắng thừa
                String schoolName = rawName.replaceAll("", "").trim();

                schools.add(SchoolInfo.builder()
                                .code(schoolCode)
                                .name(schoolName)
                                .url(schoolUrl)
                        .build());
            }

        } catch (IOException e) {
            System.err.println("Đã xảy ra lỗi khi truy cập URL: " + e.getMessage());
            throw new AppException(ErrorCode.CRAWL_URL_CONNECTION_ERROR);
        }
        return schools;
    }
}
