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

        open("https://demoqa.com/upload-download");

        File download =

                $("[href*='data:image/jpeg;base64,']")
                        .download();

        System.out.println();

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
