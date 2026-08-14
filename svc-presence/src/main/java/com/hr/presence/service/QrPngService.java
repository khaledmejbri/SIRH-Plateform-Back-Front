package com.hr.presence.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class QrPngService {

	private static final int DEFAULT_SIZE = 512;

	public byte[] toPng(String qrToken) {
		try {
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix matrix = writer.encode(
					qrToken,
					BarcodeFormat.QR_CODE,
					DEFAULT_SIZE,
					DEFAULT_SIZE,
					Map.of(
							EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
							EncodeHintType.MARGIN, 1,
							EncodeHintType.CHARACTER_SET, "UTF-8"
					));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", out);
			return out.toByteArray();
		} catch (WriterException | IOException e) {
			throw new IllegalStateException("Échec génération PNG QR", e);
		}
	}
}
