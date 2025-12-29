package kulchub;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class SelenideFilesTest {


    @Test
    void downloadFileTest() throws Exception {
        //open("https://github.com/junit-team/junit5/blob/main/README.md");
        open("https://demoqa.com/upload-download");

        File download =
                //$(".react-blob-header-edit-and-raw-actions [href*='main/README.md']")
                $("[href*='data:image/jpeg;base64,']")
                        .download();

        System.out.println();

//        try (InputStream is = new FileInputStream(download)) {
//            byte[] data = is.readAllBytes();
//            String dataAsString = new String(data, StandardCharsets.UTF_8);
//            Assertions.assertTrue(dataAsString.contains("Contributions to JUnit 5 are both welcomed and appreciated"));
//        }
        try (InputStream stream = new FileInputStream(download)) {
            BufferedImage image = ImageIO.read(download);
            Assertions.assertTrue(download.exists(), "Файл не был загружен");
            Assertions.assertTrue(download.length() > 0, "Файл пуст");
        }




    }

    @Test
    void uploadFileTest() throws Exception {
        open("https://demoqa.com/upload-download");
        $("input[type='file']").uploadFromClasspath("icons8-pubg-50.png");
        $("#uploadedFilePath").shouldHave(text("icons8-pubg-50.png"));
    }



}
