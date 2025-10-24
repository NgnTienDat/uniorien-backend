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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
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
    public List<UniversityRawData> crawlBenchmarks(String year) {
        int cnt = 0;
        List<UniversityRawData> universityRawDataList = new ArrayList<>();
        List<UniversityResponse> universityList = universityRepository
                .findAllCodeAndName(PageRequest.of(0, 2));

        WebDriver driver = getWebDriver();

        try {
            int targetYear = Integer.parseInt(year);

            for (var university : universityList) {
                System.out.println("\n-------------------------------------------");
                System.out.println("STT: " + ++cnt);
                System.out.println("Crawling data for: "
                        + university.getUniversityName()
                        + " (" + university.getUniversityCode() + ")");
                System.out.println("-------------------------------------------");

                try {
                    UniversityRawData universityRawData = new UniversityRawData();
                    universityRawData.setUniversityCode(university.getUniversityCode());
                    universityRawData.setUniversityName(university.getUniversityName());
                    universityRawData.setWebsiteUrl(university.getWebsite());

                    long start = System.currentTimeMillis();
                    driver.get(university.getWebsite());

                    // WebDriverWait chính chờ tải trang và tiêu đề (20 giây)
                    WebDriverWait mainWait = new WebDriverWait(driver, Duration.ofSeconds(20));

                    // WebDriverWait phụ chờ kiểm tra data (5 giây)
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));


                    // Chờ trang tải ít nhất một khối phương thức
                    mainWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.cutoff-table")));

                    // GIAI ĐOẠN 1: TƯƠNG TÁC (SELENIUM) - Lặp qua từng phương thức để click

                    // Lấy tất cả các block phương thức. dùng findElements vì sẽ lặp bằng index.
                    List<WebElement> methodBlocks = driver.findElements(By.cssSelector("div.cutoff-table"));
                    System.out.println("Found " + methodBlocks.size() + " admission method blocks on page.");

                    for (int i = 0; i < methodBlocks.size(); i++) {

                        WebElement currentBlock;
                        try {
                            currentBlock = driver.findElements(By.cssSelector("div.cutoff-table")).get(i);
                        } catch (IndexOutOfBoundsException e) {
                            System.err.println("WARNING: DOM structure changed, stopping block processing.");
                            break;
                        }

                        // 1. Lấy thông tin năm hiện tại của block
                        String blockTitle;
                        try {
                            WebElement titleElement = currentBlock.findElement(By.cssSelector("h3.table__title strong"));
                            blockTitle = titleElement.getText();
                        } catch (NoSuchElementException e) {
                            System.err.println("WARNING: Block " + (i + 1) + " has no title, skipping click logic.");
                            continue;
                        }

                        Matcher initialYearMatcher = Pattern.compile("(20\\d{2})").matcher(blockTitle);
                        int currentTopYear = 2025;
                        if (initialYearMatcher.find()) {
                            currentTopYear = Integer.parseInt(initialYearMatcher.group(1));
                        }

                        System.out.println("Processing block " + (i + 1) + " (" + blockTitle + ")");


                        // 2. Logic click lùi thời gian
                        if (targetYear < currentTopYear) {
                            int yearToClick = currentTopYear - 1;

                            while (yearToClick >= targetYear) {
                                System.out.println(" -> Finding 'Xem thêm' link for year: " + yearToClick);

                                String linkXPath = String.format(".//div[@class='more-link']/a[contains(text(), 'năm %s')]", yearToClick);

                                WebElement yearLink;
                                try {
                                    // chờ link click được
                                    yearLink = mainWait.until(ExpectedConditions.elementToBeClickable(
                                            By.xpath(String.format("(//div[@class='cutoff-table'])[%d]%s", i + 1, linkXPath.substring(1)))
                                    ));
                                } catch (TimeoutException e) {
                                    System.out.println(" -> WARNING: Link for year " + yearToClick + " not found in this block. Stopping year navigation for this block.");
                                    break;
                                }

                                System.out.println(" -> ✓ Found. Clicking for year: " + yearToClick);

                                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", yearLink);
                                Thread.sleep(500); // Chờ scroll
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", yearLink);

                                // Chờ cho dữ liệu MỚI (năm yearToClick) xuất hiện trong DOM
                                String newTitleXPath = String.format("(//div[@class='cutoff-table'])[%d]//h3[contains(., 'năm %s')]", i + 1, yearToClick);
                                try {
                                    // Wait 1: Chờ TIÊU ĐỀ
                                    mainWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(newTitleXPath)));

                                    // Wait 2: Chờ DỮ LIỆU (ROWS) BÊN TRONG KHỐI ĐÓ
                                    String newTableDataXPath = String.format(
                                            "(//div[@class='cutoff-table'])[%d][.//h3[contains(., 'năm %s')]]//tbody/tr",
                                            i + 1, yearToClick
                                    );
                                    // Giảm thời gian chờ data xuống 5s. Nếu timeout, Jsoup kiểm tra sau.
                                    shortWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath(newTableDataXPath), 0));
                                    System.out.println(" -> ✓ Content (rows) for year " + yearToClick + " successfully loaded.");

                                } catch (TimeoutException e) {
                                    // Bắt TimeoutException ở đây và bỏ qua mà không in ra lỗi lớn.
                                    System.out.println(" -> NOTE: Data for year " + yearToClick + " did not load within 5s. Assuming no data/slow load and continuing.");
                                }

                                Thread.sleep(500);
                                yearToClick--;
                            }
                        } else {
                            System.out.println(" -> Target year " + year + " is >= current top year " + currentTopYear + ". No clicks needed for this block.");
                        }
                    }

                    Thread.sleep(1000);

                    // GIAI ĐOẠN 2: TRÍCH XUẤT (JSOUP)

                    String html = driver.getPageSource();
                    Document doc = Jsoup.parse(html);

                    Elements cutoffBlocks = doc.select("div.cutoff-table");

                    for (Element block : cutoffBlocks) {

                        Elements titleElements = block.select("h3.table__title");

                        for (Element titleElement : titleElements) {
                            String methodName = (titleElement.text() != null) ? titleElement.text().trim() : "Unknown Method";

                            String yearOfAdmission = null;
                            Matcher matcher = Pattern.compile("(20\\d{2})").matcher(methodName);
                            if (matcher.find()) {
                                yearOfAdmission = matcher.group(1);
                            }

                            if (yearOfAdmission != null && !yearOfAdmission.equals(year)) {
                                continue;
                            }

                            System.out.println("   [PARSE] Processing data block: " + methodName);

                            AdmissionInfoRawData admissionInfoRawData = new AdmissionInfoRawData();
                            admissionInfoRawData.setAdmissionMethod(methodName);
                            admissionInfoRawData.setYear(yearOfAdmission);

                            Element tableWrapper = titleElement.nextElementSibling();
                            Element table = null;

                            if (tableWrapper != null && tableWrapper.is("div")) {
                                table = tableWrapper.selectFirst("table");
                            } else {
                                Element strongElement = titleElement.selectFirst("strong");
                                if (strongElement != null) {
                                    tableWrapper = titleElement.nextElementSibling();
                                    if (tableWrapper != null && tableWrapper.is("div")) {
                                        table = tableWrapper.selectFirst("table");
                                    }
                                }
                            }

                            if (table != null) {
                                Elements rows = table.select("tbody tr");
                                // Jsoup tự động thấy rows.isEmpty() nếu không có data.
                                if (rows.isEmpty()) {
                                    System.out.println("   [PARSE] NOTE: Found title for " + methodName + " but no data rows extracted.");
                                }

                                for (Element row : rows) {
                                    Elements cols = row.select("td");
                                    if (cols.size() >= 5) {
                                        String notes = (cols.size() >= 6) ? cols.get(5).text().trim() : "";
                                        // BenchmarkRawData benchmarkRawData = BenchmarkRawData.builder()
                                        //         .majorCode(cols.get(1).text().trim())
                                        //         .majorName(cols.get(2).text().trim())
                                        //         .subjectCombinations(cols.get(3).text().trim())
                                        //         .score(cols.get(4).text().trim())
                                        //         .notes(notes)
                                        //         .build();
                                        // admissionInfoRawData.getBenchmarks().add(benchmarkRawData);


                                        BenchmarkRawData benchmarkRawData = new BenchmarkRawData();
                                        benchmarkRawData.setMajorCode(cols.get(1).text().trim());
                                        benchmarkRawData.setMajorName(cols.get(2).text().trim());
                                        benchmarkRawData.setSubjectCombinations(cols.get(3).text().trim());
                                        benchmarkRawData.setScore(cols.get(4).text().trim());
                                        benchmarkRawData.setNotes(notes);
                                        admissionInfoRawData.getBenchmarks().add(benchmarkRawData);
                                    }
                                }
                            }

                            if (!admissionInfoRawData.getBenchmarks().isEmpty()) {
                                universityRawData.getAdmissions().add(admissionInfoRawData);
                            }
                        }
                    }
                    // HẾT GIAI ĐOẠN 2

                    long end = System.currentTimeMillis();
                    System.out.printf("⏰ Crawl time for this university: %.2f seconds%n", (end - start) / 1000.0);
                    universityRawDataList.add(universityRawData);

                } catch (TimeoutException e) {
                    System.err.println("ERROR: Timed out waiting for page content to load for " + university.getUniversityName() + ". Skipping.");
                } catch (Exception e) {
                    System.err.println("ERROR: An unexpected error occurred while crawling " + university.getUniversityName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
                int delay = ThreadLocalRandom.current().nextInt(500, 1000);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (NumberFormatException e) {
            System.err.println("FATAL ERROR: Invalid year format provided: " + year);
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }

        csvExportService.exportToCsv(universityRawDataList, "E:/benchmarks_" + year + ".csv");

        return universityRawDataList;
    }


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
