package fr.dossierfacile.process.file.barcode.twoddoc.reader;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import fr.dossierfacile.process.file.barcode.twoddoc.TwoDDocRawContent;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.Optional;

@Slf4j
class TwoDDocDecoder {

    private static final DataMatrixReader READER = new DataMatrixReader();

    /**
     * Input image must be cropped to be centered on 2D-Doc, otherwise Zxing
     * won't find anything.
     */
    static Optional<TwoDDocRawContent> readTwoDDocOn(CroppedFile croppedFile) {
        return readTwoDDocOn(croppedFile.bufferedImage());
    }

    static Optional<TwoDDocRawContent> readTwoDDocOn(BufferedImage image) {
        BinaryBitmap binaryBitmap = createBinaryBitmap(image);

        try {
            return Optional.of(READER.decode(binaryBitmap))
                    .map(Result::getText)
                    .map(TwoDDocRawContent::new);
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (FormatException | ChecksumException e) {
            log.info("Format or checksum exception on found 2D-Doc", e);
            return Optional.empty();
        }
    }

    private static BinaryBitmap createBinaryBitmap(BufferedImage image) {
        return new BinaryBitmap(
                new HybridBinarizer(
                        new BufferedImageLuminanceSource(
                                image
                        )));
    }

}
