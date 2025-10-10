package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.response.UniversityResponse;
import com.ntd.uniorien.entity.AdmissionInformation;
import com.ntd.uniorien.entity.Benchmark;
import com.ntd.uniorien.entity.Major;
import com.ntd.uniorien.entity.University;
import com.ntd.uniorien.repository.UniversityRepository;
import com.ntd.uniorien.utils.raw.AdmissionInfoRawData;
import com.ntd.uniorien.utils.raw.BenchmarkRawData;
import com.ntd.uniorien.utils.raw.SchoolInfo;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import com.ntd.uniorien.utils.raw.UniversityRawData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CrawlService {

    UniversityRepository universityRepository;

    @NonFinal
    @Value("${app.crawl.url}")
    protected String CRAWL_URL;

    @PreAuthorize("hasRole('ADMIN')")
    public List<UniversityRawData> crawlBenchmarks() {
        int cnt = 0;
        List<UniversityRawData> universityRawDataList = new ArrayList<>();
        // Example: Fetching a small batch of universities to crawl
//        List<UniversityResponse> universityList = universityRepository.findAllCodeAndName();
        List<UniversityResponse> universityList = universityRepository.findAllCodeAndName(PageRequest.of(0, 5));

        WebDriver driver = getWebDriver();

        try {
            for (var university : universityList) {
                System.out.println("STT: " + ++cnt);
                System.out.println("Crawling data for: " + university.getUniversityName() + " (" + university.getUniversityCode() + ")");
                try {
                    UniversityRawData universityRawData = new UniversityRawData();
                    universityRawData.setUniversityCode(university.getUniversityCode());
                    universityRawData.setUniversityName(university.getUniversityName());
                    universityRawData.setWebsiteUrl(university.getWebsite());

                    long start = System.currentTimeMillis();
                    driver.get(university.getWebsite());

                    // --- START: APPLIED ROBUST WAITING LOGIC ---
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

                    // 1. Wait for the table container to be visible
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ant-table-content table")));

                    // 2. Wait for at least one data row to appear in the table body
                    wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".ant-table-content table tbody tr"), 0));

                    // 3. CRITICAL: Wait for the table headers to be fully loaded (at least 6 columns)
                    // This prevents errors when trying to access data from columns that haven't rendered yet.
                    wait.until(driverInstance -> {
                        try {
                            WebElement tableElement = driverInstance.findElement(By.cssSelector(".ant-table-content table"));
                            Elements headers = Jsoup.parse(tableElement.getAttribute("outerHTML")).select("thead tr th");
                            // The table is considered "fully loaded" when all columns are present.
                            if (headers.size() >= 6) {
                                System.out.println("✓ Table fully loaded with " + headers.size() + " columns.");
                                return true;
                            }
                            return false;
                        } catch (Exception e) {
                            return false; // Continue waiting if element not found or other parsing error
                        }
                    });

                    // 4. Optional small delay for stability after dynamic content has loaded
                    Thread.sleep(500);
                    // --- END: APPLIED ROBUST WAITING LOGIC ---

                    String html = driver.getPageSource();
                    Document doc = Jsoup.parse(html);

                    Elements cutoffBlocks = doc.select("div.cutoff-table");
                    if (cutoffBlocks.isEmpty()) {
                        System.out.println("⚠️ No 'cutoff-table' blocks found for this university.");
                    }

                    for (Element block : cutoffBlocks) {
                        Element titleElement = block.selectFirst("h3.table__title strong");
                        String methodName = (titleElement != null) ? titleElement.text().trim() : "Unknown Method";

                        String yearOfAdmission = null;
                        Matcher matcher = Pattern.compile("(20\\d{2})").matcher(methodName);
                        if (matcher.find()) {
                            yearOfAdmission = matcher.group(1);
                        }

                        AdmissionInfoRawData admissionInfoRawData = new AdmissionInfoRawData();
                        admissionInfoRawData.setAdmissionMethod(methodName);
                        admissionInfoRawData.setYear(yearOfAdmission);
                        Element table = block.selectFirst("table");
                        if (table != null) {
                            Elements rows = table.select("tbody tr"); // Select only body rows

                            for (Element row : rows) {
                                Elements cols = row.select("td");
                                // Safety check to avoid IndexOutOfBoundsException on malformed rows
                                if (cols.size() >= 5) {
                                    BenchmarkRawData benchmarkRawData = BenchmarkRawData.builder()
                                            .majorCode(cols.get(1).text().trim())
                                            .majorName(cols.get(2).text().trim())
                                            .subjectCombinations(cols.get(3).text().trim())
                                            .score(cols.get(4).text().trim())
                                            .notes(cols.get(5).text().trim())
                                            .build();
                                    admissionInfoRawData.getBenchmarks().add(benchmarkRawData);
                                }
                            }
                        }
                        universityRawData.getAdmissions().add(admissionInfoRawData);
                    }

                    long end = System.currentTimeMillis();
                    System.out.printf("⏰ Crawl time for this university: %.2f seconds%n", (end - start) / 1000.0);
                    universityRawDataList.add(universityRawData);

                } catch (TimeoutException e) {
                    System.err.println("❌ ERROR: Timed out waiting for page content to load for " + university.getUniversityName() + ". Skipping.");
                } catch (Exception e) {
                    System.err.println("❌ ERROR: An unexpected error occurred while crawling " + university.getUniversityName() + ": " + e.getMessage());
                    // Optionally print stack trace for debugging: e.printStackTrace();
                }
                int delay = ThreadLocalRandom.current().nextInt(500, 1000);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }

        return universityRawDataList;
    }

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

    private static WebDriver getWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // chạy ẩn, không mở cửa sổ
        options.addArguments("--disable-gpu"); // tránh lỗi render trên Windows
        options.addArguments("--no-sandbox"); // cần thiết nếu chạy trên Linux
        options.addArguments("--disable-dev-shm-usage"); // tránh lỗi bộ nhớ
        options.addArguments("--window-size=1920,1080"); // tránh lỗi responsive layout
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        // Tạo WebDriver với cấu hình headless
        return new ChromeDriver(options);
    }
}
