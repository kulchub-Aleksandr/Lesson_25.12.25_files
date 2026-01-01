package kulchub;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import kulchub.model.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.pdftest.PDF.containsText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FilesParsingTest {

    private ClassLoader cl = FilesParsingTest.class.getClassLoader();
    private static final JsonFactory jsonFactory = new JsonFactory();


    @BeforeEach
    void setUp() {
        //open("https://demoqa.com/text-box");
        //Configuration.browserSize = "1920x1080";
        //Configuration.baseUrl = "https://demoqa.com";
        //Configuration.pageLoadStrategy = "eager";
        //Configuration.holdBrowserOpen = true;
        //Configuration.timeout = 10000; // default 4000
        //executeJavaScript("$('#fixedban').remove()");
        //executeJavaScript("$('footer').remove()");
    }

    @Test
    void pdfFileParsingTest() throws Exception {

        open("https://pdf-editor.su/doc-templates.php");
        File download = $("[href='doc-templates/dogovor-kupli-prodazhi-avtomobilya.pdf']").download();
        PDF pdf = new PDF(download);
        assertEquals("Копия Договор купли-продажи автомобиля", pdf.title);

    }

    @Test
    void xlsFileParsingTest() throws Exception {
        open("https://excelvba.ru/programmes/Teachers?ysclid=lfcu77j9j9951587711");
        File downloaded = $("[href='https://ExcelVBA.ru/sites/default/files/teachers.xls']").download();
        XLS xls = new XLS(downloaded);
        String actualValue = xls.excel.getSheetAt(0).getRow(3).getCell(2).getStringCellValue();
        Assertions.assertTrue(actualValue.contains("Суммарное количество часов планируемое на штатную по всем разделам"));

    }

    @Test
    void csvFileParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("enteringFullData.csv");
             CSVReader csvReader = new CSVReader(new InputStreamReader(is))) {

            List<String[]> data = csvReader.readAll();
            assertEquals(2, data.size());
            Assertions.assertArrayEquals(
                    new String[]{"Alex Dmitr", " alex@dmitr.com ", " Some street 1", " Some street 1"},
                    data.get(0)
            );
            Assertions.assertArrayEquals(
                    new String[]{"Oleg Egorov", " oleg@egorov.com ", " Another street 2", " Another street 2"},
                    data.get(1)
            );
        }
    }



    @Test
    void pdfFileInZipParsingTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                cl.getResourceAsStream("Pump.zip")
        )) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                System.out.println("Нашел файл в архиве: " + name);
                if (entry.getName().endsWith(".pdf")) {
                    PDF pdf = new PDF(zis);
                    System.out.println("Начало проверкм PDF");
                    assertEquals("qaguru", pdf.keywords);
                    assertThat(pdf, containsText("Ремонтные работы"));
                    System.out.printf("Проверен PDF: %s%n", name);
                    System.out.println("Проверка закончена  ");
                    break;

                }
            }
        }
    }

    @Test
    void xlsxFileInZipParsingTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                cl.getResourceAsStream("Pump.zip")
        )) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                System.out.println("Нашел файл в архиве: " + name);
                if (entry.getName().endsWith(".xlsx")) {
                    XLS xls = new XLS(zis);
                    System.out.println("Начало проверкм XLS");
                    String actualValue = xls.excel.getSheetAt(0).getRow(7).getCell(1).getStringCellValue();
                    Assertions.assertTrue(actualValue.contains("Ремонтные работы"));
                    System.out.printf("Проверен xlsx: %s%n", name);
                    System.out.println("Проверка закончена  ");
                    break;
                }
            }
        }

    }

    @Test
    void csvFileInZipParsingTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                cl.getResourceAsStream("Pump.zip")
        )) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                System.out.println("Нашел файл в архиве: " + name);
                if (entry.getName().endsWith(".csv")) {
                    System.out.println("Начало проверкм CSV");
                    // ВАЖНО: Не закрываем ридер внутри, чтобы не убить ZipInputStream, не как в примере выше
                    InputStreamReader isr = new InputStreamReader(zis, StandardCharsets.UTF_8);
                    CSVReader reader = new CSVReader(isr);
                    List<String[]> data = reader.readAll();

                    assertEquals(112, data.size());
                    Assertions.assertArrayEquals(
                            new String[]{"№п/п;Ремонтные работы;Сумма;;"},
                            data.get(7)
                    );
                    Assertions.assertArrayEquals(
                            new String[]{"1;Мойка;4 500", "00р.;;"},
                            data.get(8)
                    );
                    System.out.printf("Проверен CSV: %s%n", name);
                    System.out.println("Проверка закончена  ");
                    break;
                }
            }
        }
    }

    @Test
    void jsonFileParsingImprovedTest() throws Exception {
        try (JsonParser parser = jsonFactory.createParser(
                cl.getResourceAsStream("order.json")
        )) {
            ObjectMapper objectMapper = new ObjectMapper();

            Order order = objectMapper.readValue(parser, Order.class);

            assertNotNull(order);
            assertEquals("RU-775", order.getOrderId());
            assertEquals("DELIVERED", order.getStatus());
            assertEquals("1550.50", order.getTotalPrice());

            Order.Customer customer = order.getCustomer();
            assertNotNull(customer);
            assertEquals("Ivan", customer.getName());
            assertEquals("79001112233", customer.getPhone());

            List<Order.Item> items = order.getItems();
            assertNotNull(items);
            assertEquals(2, items.size());

            Order.Item firstItem = items.get(0);
            assertEquals(1, firstItem.getId());
            assertEquals("Pizza Margarita", firstItem.getTitle());
            assertEquals(1, firstItem.getQuantity());

            Order.Item secondItem = items.get(1);
            assertEquals(2, secondItem.getId());
            assertEquals("Coca-Cola", secondItem.getTitle());
            assertEquals(2, secondItem.getQuantity());

        }

    }

}






