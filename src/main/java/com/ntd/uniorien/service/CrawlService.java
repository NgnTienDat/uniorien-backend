package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.response.UniversityResponse;
import com.ntd.uniorien.repository.UniversityRepository;
import com.ntd.uniorien.utils.raw.*;
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

    CsvExportService csvExportService;

    @NonFinal
    @Value("${app.crawl.url-nganh-dao-tao}")
    protected String CRAWL_URL_MAJOR;

    @NonFinal
    @Value("${app.crawl.url-diem-chuan}")
    protected String CRAWL_URL;

    @PreAuthorize("hasRole('ADMIN')")
    public List<UniversityRawData> crawlBenchmarks() {
        int cnt = 0;
        List<UniversityRawData> universityRawDataList = new ArrayList<>();
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
                        System.out.println("No 'cutoff-table' blocks found for this university.");
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
                    System.err.println("ERROR: Timed out waiting for page content to load for " + university.getUniversityName() + ". Skipping.");
                } catch (Exception e) {
                    System.err.println("ERROR: An unexpected error occurred while crawling " + university.getUniversityName() + ": " + e.getMessage());
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

        csvExportService.exportToCsv(universityRawDataList, "E:/benchmarks.csv");

        return universityRawDataList;
    }


    //    @PreAuthorize("hasRole('ADMIN')")
//    public List<MajorGroupRawData> crawlMajorGroups() {
//        List<MajorGroupRawData> groupList = new ArrayList<>();
//        WebDriver driver = getWebDriver();
//
//        try {
//            System.out.println("🌐 Navigating to: " + CRAWL_URL_MAJOR);
//            driver.get(CRAWL_URL_MAJOR);
//
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ant-collapse-item")));
//
//            // --- 1️⃣ MỞ TẤT CẢ NHÓM NGÀNH ---
//            List<WebElement> headers = driver.findElements(By.cssSelector(".ant-collapse-item .ant-collapse-header"));
//            System.out.println("🔍 Found " + headers.size() + " major groups.");
//
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            for (WebElement header : headers) {
//                try {
//                    js.executeScript("arguments[0].scrollIntoView(true);", header);
//                    Thread.sleep(200);
//                    header.click();
//                    Thread.sleep(300); // đợi nội dung render
//                } catch (Exception ignored) {}
//            }
//
//            // --- 2️⃣ LẤY LẠI HTML SAU KHI MỞ HẾT ---
//            String fullHtml = driver.getPageSource();
//            Document doc = Jsoup.parse(fullHtml);
//            Elements groups = doc.select(".ant-collapse-item");
//
//            System.out.println("📦 Parsing " + groups.size() + " groups...");
//
//            // --- 3️⃣ PHÂN TÍCH NỘI DUNG ---
//            for (Element group : groups) {
//                try {
//                    String groupName = group.selectFirst(".ant-collapse-header").text().trim();
//                    Elements majorElements = group.select(".ant-collapse-content li, .ant-collapse-content a");
//
//                    List<String> majors = new ArrayList<>();
//                    for (Element m : majorElements) {
//                        String majorName = m.text().trim();
//                        if (!majorName.isEmpty()) majors.add(majorName);
//                    }
//
//                    int numberOfMajors = majors.size();
//                    groupList.add(new MajorGroupRawData(groupName, numberOfMajors, majors));
//
//                    System.out.println("✅ " + groupName + " (" + numberOfMajors + " ngành)");
//
//                } catch (Exception e) {
//                    System.err.println("⚠️ Error parsing group: " + e.getMessage());
//                }
//            }
//
//        } catch (Exception e) {
//            System.err.println("❌ Failed to crawl majors: " + e.getMessage());
//        } finally {
//            driver.quit();
//        }
//
//        csvExportService.exportMajorGroupsToCsv(groupList, "E:/major-groups.csv");
//        System.out.println("📄 CSV exported to E:/major-groups.csv");
//
//        return groupList;
//    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<MajorGroupRawData> crawlMajorGroups() {
        List<MajorGroupRawData> majorGroupList = new ArrayList<>();
        WebDriver driver = getWebDriver();

        try {
            System.out.println("Starting to crawl major groups from: " + CRAWL_URL_MAJOR);
            long start = System.currentTimeMillis();

            driver.get(CRAWL_URL_MAJOR);

            // --- START: ROBUST WAITING LOGIC ---
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // 1. Wait for the collapse container to be visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ant-collapse.programs")));

            // 2. Wait for at least one collapse item to be present
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".ant-collapse-item"), 0));

            // 3. Find all collapse items that need to be expanded
            List<WebElement> collapseHeaders = driver.findElements(By.cssSelector(".ant-collapse-item .ant-collapse-header"));
            System.out.println("Found " + collapseHeaders.size() + " major groups to expand.");

            // 4. Click each header to expand all sections
            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (int i = 0; i < collapseHeaders.size(); i++) {
                try {
                    // Re-fetch elements to avoid stale element reference
                    List<WebElement> headers = driver.findElements(By.cssSelector(".ant-collapse-item .ant-collapse-header"));
                    WebElement header = headers.get(i);

                    // Check if the item is already expanded
                    WebElement parentItem = header.findElement(By.xpath("./.."));
                    String className = parentItem.getAttribute("class");

                    if (!className.contains("ant-collapse-item-active")) {
                        // Scroll into view
                        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", header);
                        Thread.sleep(300);

                        // Use JavaScript click to bypass overlay elements
                        js.executeScript("arguments[0].click();", header);
                        Thread.sleep(500); // Wait for content to load after expansion
                        System.out.println("✓ Expanded section " + (i + 1));
                    } else {
                        System.out.println("- Section " + (i + 1) + " already expanded");
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not expand section " + (i + 1) + ": " + e.getMessage());
                }
            }

            // 5. Wait for all content to be fully loaded
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                    By.cssSelector(".ant-collapse-item .ant-collapse-content-active .program-group ul li"), 0));

            Thread.sleep(1000); // Final stability wait
            // --- END: ROBUST WAITING LOGIC ---

            // Parse the page with Jsoup
            String html = driver.getPageSource();
            Document doc = Jsoup.parse(html);

            // Select all collapse items
            Elements collapseItems = doc.select(".ant-collapse-item");

            if (collapseItems.isEmpty()) {
                System.out.println("No major groups found on the page.");
                return majorGroupList;
            }

            System.out.println("Parsing " + collapseItems.size() + " major groups...");

            for (Element item : collapseItems) {
                try {
                    MajorGroupRawData majorGroupData = new MajorGroupRawData();

                    // Extract group name from header
                    Element headerName = item.selectFirst(".program__header-name");
                    if (headerName != null) {
                        String groupName = headerName.text().trim();
                        majorGroupData.setGroupName(groupName);
                    }

                    // Extract number of majors
                    Element nganhElement = item.selectFirst(".program__header-group .nganh");
                    if (nganhElement != null) {
                        String nganhText = nganhElement.text().trim();
                        // Extract number from text like "7 ngành"
                        Matcher matcher = Pattern.compile("(\\d+)").matcher(nganhText);
                        if (matcher.find()) {
                            majorGroupData.setNumberOfMajors(Integer.parseInt(matcher.group(1)));
                        }
                    }

                    // Extract list of majors from the expanded content
                    Elements majorLinks = item.select(".program-group ul li a");
                    for (Element majorLink : majorLinks) {
                        String majorName = majorLink.text().trim();
                        // Remove the arrow icon from the text
                        majorName = majorName.replaceAll("\\s*►\\s*$", "").trim();
                        if (!majorName.isEmpty()) {
                            majorGroupData.getMajors().add(majorName);
                        }
                    }

                    // Validation check
                    if (majorGroupData.getGroupName() != null && !majorGroupData.getMajors().isEmpty()) {
                        majorGroupList.add(majorGroupData);
                        System.out.println("✓ Parsed: " + majorGroupData.getGroupName() +
                                " (" + majorGroupData.getMajors().size() + " majors)");
                    } else {
                        System.out.println("⚠ Skipped incomplete group data");
                    }

                } catch (Exception e) {
                    System.err.println("ERROR: Failed to parse a major group: " + e.getMessage());
                }
            }

            long end = System.currentTimeMillis();
            System.out.printf("⏰ Total crawl time: %.2f seconds%n", (end - start) / 1000.0);
            System.out.println("✓ Successfully crawled " + majorGroupList.size() + " major groups.");

        } catch (TimeoutException e) {
            System.err.println("ERROR: Timed out waiting for page content to load. The page may be slow or unavailable.");
        } catch (Exception e) {
            System.err.println("ERROR: An unexpected error occurred while crawling major groups: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        // Optional: Export to CSV if you have a similar service
        // csvExportService.exportMajorGroupsToCsv(majorGroupList, "E:/major_groups.csv");
        csvExportService.exportMajorGroupsToCsv(majorGroupList, "E:/major_groups.csv");



        return majorGroupList;
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
                String schoolName = rawName.replaceAll("^[\\s-]+", "").trim();

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
