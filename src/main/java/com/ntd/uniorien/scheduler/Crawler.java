package com.ntd.uniorien.scheduler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Crawler {
    public static void main(String[] args) {
        String url = "https://diemthi.tuyensinh247.com/diem-chuan/dai-hoc-kinh-te-quoc-dan-KHA.html";

        //Cấu hình Chrome chạy ở chế độ headless (ẩn)
        WebDriver driver = getWebDriver();

        try {
            long start = System.currentTimeMillis();
            driver.get(url);

            // Đợi bảng hiển thị đầy đủ
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ant-table-content table")));
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".ant-table-content table tbody tr"), 0));

            // Lấy toàn bộ HTML sau khi trang render xong
            String html = driver.getPageSource();

            // Phân tích HTML bằng Jsoup
            Document doc = Jsoup.parse(html);

            // 1️⃣ Tìm các khối bảng điểm chuẩn
            Elements cutoffBlocks = doc.select("div.cutoff-table");
            if (cutoffBlocks.isEmpty()) {
                System.out.println("⚠️ Không tìm thấy khối cutoff-table nào!");
            }

            for (Element block : cutoffBlocks) {
                // 2️⃣ Tên phương thức xét tuyển
                Element titleElement = block.selectFirst("h3.table__title strong");
                String methodName = (titleElement != null) ? titleElement.text().trim() : "Phương thức không rõ";

                System.out.println("\n========================================================");
                System.out.println("PHƯƠNG THỨC XÉT TUYỂN: " + methodName);
                System.out.println("========================================================");

                // 3️⃣ Bảng dữ liệu
                Element table = block.selectFirst("table");
                if (table != null) {
                    Elements rows = table.select("tr");

                    if (!rows.isEmpty()) {
                        // Header
                        Elements headerCols = rows.first().select("th");
                        System.out.println("Header: " + headerCols.eachText());

                        // Body
                        for (int i = 1; i < rows.size(); i++) {
                            Elements cols = rows.get(i).select("td");
                            System.out.println(cols.eachText());
                        }
                    }
                } else {
                    System.out.println("Không tìm thấy bảng dữ liệu trong khối này.");
                }
            }
            long end = System.currentTimeMillis();   //  Kết thúc
            long duration = end - start;             // Thời gian chạy (ms)

            System.out.printf("⏰ Thời gian crawl: %.2f giây%n", duration / 1000.0);
        } finally {
            // Đảm bảo tắt ChromeDriver dù có lỗi
            driver.quit();

        }

    }

    private static WebDriver getWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // chạy ẩn, không mở cửa sổ
        options.addArguments("--disable-gpu"); // tránh lỗi render trên Windows
        options.addArguments("--no-sandbox"); // cần thiết nếu chạy trên Linux
        options.addArguments("--disable-dev-shm-usage"); // tránh lỗi bộ nhớ
        options.addArguments("--window-size=1920,1080"); // tránh lỗi responsive layout

        // Tạo WebDriver với cấu hình headless
        return new ChromeDriver(options);
    }
}
