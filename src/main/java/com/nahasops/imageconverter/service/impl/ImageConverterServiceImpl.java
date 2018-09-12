package com.nahasops.imageconverter.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.icafe4j.image.ImageColorType;
import com.icafe4j.image.ImageIO;
import com.icafe4j.image.ImageParam;
import com.icafe4j.image.ImageType;
import com.icafe4j.image.options.PNGOptions;
import com.icafe4j.image.png.Filter;
import com.icafe4j.image.quant.DitherMethod;
import com.icafe4j.image.quant.QuantMethod;
import com.icafe4j.image.reader.ImageReader;
import com.icafe4j.io.PeekHeadInputStream;
import com.nahasops.imageconverter.ImageConverterApplication;
import com.nahasops.imageconverter.exception.ImageConvertionException;
import com.nahasops.imageconverter.service.ImageConverterService;

@Service("ImageConverterService")
public class ImageConverterServiceImpl implements ImageConverterService {

	private static final Logger logger = LoggerFactory.getLogger(ImageConverterApplication.class);

	@Async("imageConverterExecutor")
	@Override
	public CompletableFuture<Void> generateAsync(final Path k, final String inputPath, final String outputPath) {

		InputStream inputStream = null;

		try {

			final String imageType = k.getFileName() != null ? FilenameUtils.getExtension(k.getFileName().toString())
					: "";

			if (ImageType.TIFF.getExtension().equals(imageType)) {
				inputStream = Files.newInputStream(k);
				String targetFileName = FilenameUtils.removeExtension(k.getFileName().toString()).concat(".")
						.concat(ImageType.PNG.getExtension());
				FileUtils.copyInputStreamToFile(
						new ByteArrayInputStream(this.convertToPNG(inputStream, targetFileName).toByteArray()),
						new File(outputPath.concat("/").concat(targetFileName)));
			} else {
				logger.debug("Ignoring Not a TIF file = {} ", k.getFileName().toString());
			}

		} catch (FileNotFoundException e) {
			logger.error("Asset {} has error: {} ", k, e.getMessage());
		} catch (IOException e) {
			logger.error("Asset {} has error: {} ", k, e.getMessage());
		} catch (ImageConvertionException e) {
			logger.error("Asset {} has error: {} ", k, e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("Asset {} has error: {} ", k, e.getMessage());
				}
			}
		}

		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Convert TIFF Image to PNG
	 */
	private ByteArrayOutputStream convertToPNG(InputStream inputStream, final String targetFileName)
			throws ImageConvertionException {

		// Start the clock
		long start = System.currentTimeMillis();
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {

			PeekHeadInputStream peekHeadInputStream = new PeekHeadInputStream(inputStream,
					ImageIO.IMAGE_MAGIC_NUMBER_LEN);
			ImageReader reader = ImageIO.getReader(peekHeadInputStream);
			BufferedImage img = reader.read(peekHeadInputStream);
			peekHeadInputStream.close();

			if (img == null) {
				logger.error("Failed reading image name {} ", targetFileName);
				return null;
			}

			ImageParam.ImageParamBuilder builder = ImageParam.getBuilder();

			PNGOptions pngOptions = new PNGOptions();
			pngOptions.setApplyAdaptiveFilter(false);
			pngOptions.setCompressionLevel(6);
			pngOptions.setFilterType(Filter.NONE);
			builder.imageOptions(pngOptions);

			// Image generation
			ImageIO.write(img, os, ImageType.PNG,
					builder.quantMethod(QuantMethod.WU_QUANT).colorType(ImageColorType.FULL_COLOR).applyDither(true)
							.ditherMethod(DitherMethod.FLOYD_STEINBERG).hasAlpha(true).build());

		} catch (Exception e) {
			logger.error("File {} with problems, exception message is: {} ", targetFileName, e.getMessage());
			throw new ImageConvertionException(e.getMessage());
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException ex) {
					logger.info(ex.getMessage());
				}
			}
		}

		// Print elapsed time
		long timeMillis = System.currentTimeMillis();
		logger.info("Elapsed time to convert image {} was {} seconds or {} milliseconds ", targetFileName,
				TimeUnit.MILLISECONDS.toSeconds(timeMillis - start), (timeMillis - start));

		return os;
	}

}
