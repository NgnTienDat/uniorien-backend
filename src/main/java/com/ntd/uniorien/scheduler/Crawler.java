//package com.ntd.uniorien.scheduler;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.openqa.selenium.*;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.time.Duration;
//
//public class Crawler {
//    public static void main(String[] args) {
//        String url = "https://diemthi.tuyensinh247.com/diem-chuan/dai-hoc-kinh-te-quoc-dan-KHA.html";
//
//        WebDriver driver = getWebDriver();
//
//        try {
//            long start = System.currentTimeMillis();
//            driver.get(url);
//
//            // Wait for the table to be present
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ant-table-content table")));
//
//            // Wait for table body rows to be present
//            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
//                    By.cssSelector(".ant-table-content table tbody tr"), 0));
//
//            // CRITICAL: Wait for the table headers to be fully loaded
//            // The presence of 6 columns (th elements) indicates full loading
//            wait.until(driver1 -> {
//                try {
//                    WebElement table = driver1.findElement(By.cssSelector(".ant-table-content table"));
//                    Elements headers = Jsoup.parse(table.getAttribute("outerHTML"))
//                            .select("thead tr th");
//
//                    // Check if we have at least 6 columns (including STT and Mã ngành)
//                    if (headers.size() >= 6) {
//                        System.out.println("✓ Table fully loaded with " + headers.size() + " columns");
//                        return true;
//                    }
//                    System.out.println("⏳ Waiting... Currently " + headers.size() + " columns");
//                    return false;
//                } catch (Exception e) {
//                    return false;
//                }
//            });
//
//            // Additional small delay to ensure everything is stable
//            Thread.sleep(500);
//
//            // Get the full HTML after rendering
//            String html = driver.getPageSource();
//
//            // Parse with Jsoup
//            Document doc = Jsoup.parse(html);
//
//            // Find cutoff tables
//            Elements cutoffBlocks = doc.select("div.cutoff-table");
//            if (cutoffBlocks.isEmpty()) {
//                System.out.println("⚠️ Không tìm thấy khối cutoff-table nào!");
//            }
//
//            for (Element block : cutoffBlocks) {
//                // Method name
//                Element titleElement = block.selectFirst("h3.table__title strong");
//                String methodName = (titleElement != null) ? titleElement.text().trim() : "Phương thức không rõ";
//
//                System.out.println("\n========================================================");
//                System.out.println("PHƯƠNG THỨC XÉT TUYỂN: " + methodName);
//                System.out.println("========================================================");
//
//                // Data table
//                Element table = block.selectFirst("table");
//                if (table != null) {
//                    Elements rows = table.select("tr");
//
//                    if (!rows.isEmpty()) {
//                        // Header
//                        Elements headerCols = rows.first().select("th");
//                        System.out.println("Header: " + headerCols.eachText());
//                        System.out.println("Số cột header: " + headerCols.size());
//
//                        // Body rows
//                        for (int i = 1; i < rows.size(); i++) {
//                            Elements cols = rows.get(i).select("td");
//                            System.out.println(cols.eachText());
//                        }
//                    }
//                } else {
//                    System.out.println("Không tìm thấy bảng dữ liệu trong khối này.");
//                }
//            }
//
//            long end = System.currentTimeMillis();
//            long duration = end - start;
//
//            System.out.printf("\n⏰ Thời gian crawl: %.2f giây%n", duration / 1000.0);
//
//        } catch (InterruptedException e) {
//            System.err.println("Thread interrupted: " + e.getMessage());
//            Thread.currentThread().interrupt();
//        } catch (Exception e) {
//            System.err.println("Error during crawling: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            driver.quit();
//        }
//    }
//
//    private static WebDriver getWebDriver() {
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--window-size=1920,1080");
//
//        // Additional options for stability
//        options.addArguments("--disable-blink-features=AutomationControlled");
//        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
//
//        return new ChromeDriver(options);
//    }
//
//
//}