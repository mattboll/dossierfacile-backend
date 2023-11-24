package fr.dossierfacile.process.file.service;

import fr.dossierfacile.common.entity.ocr.TaxIncomeMainFile;
import fr.dossierfacile.process.file.service.ocr.TaxIncomeLeafParser;
import fr.dossierfacile.process.file.service.ocr.TaxIncomeParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

@Disabled
public class TaxIncomeParserTest {
    private final TaxIncomeParser taxIncomeParser = new TaxIncomeParser(new TaxIncomeLeafParser());

    @Test
    void parse() {
        // Set lib path: System.setProperty("jna.library.path", "/usr/local/lib");
        System.setProperty("jna.library.path", "/usr/local/lib");
        // Set to test environment: "TESSDATA_PREFIX","/usr/..../share/tessdata"
        File file = new File(this.getClass().getResource("/documents/taxincome.pdf").getFile());

        TaxIncomeMainFile doc = taxIncomeParser.parse(file);
        System.out.print(doc);

    }
}