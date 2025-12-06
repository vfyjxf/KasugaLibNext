package lib.kasuga.content.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QrCodeCache {
    public static BitMatrix encodeBitMatrix(String content) throws WriterException {
        return new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 64, 64);
    }
}
