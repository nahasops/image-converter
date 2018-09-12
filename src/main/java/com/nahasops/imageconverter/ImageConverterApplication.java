package com.nahasops.imageconverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.nahasops.imageconverter.exception.ExitException;
import com.nahasops.imageconverter.service.ImageConverterService;

@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class ImageConverterApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(ImageConverterApplication.class);

	@Autowired
	private ImageConverterService imageConverterService;

	@Value("${input.path}")
	private String inputPath;

	@Value("${output.path}")
	private String outputPath;

	public static void main(String[] args) {
		SpringApplication.run(ImageConverterApplication.class, args);
	}

	@Override
	public void run(String... args) {

		logger.info("Starting conversion process with InputPath = {}  and  OutputPath = {}", inputPath, outputPath);

		long start = System.currentTimeMillis();

		List<CompletableFuture<?>> jobs = new ArrayList<>();

		try {

			Files.walk(Paths.get(inputPath)).filter(Files::isRegularFile).forEach(k -> {
				jobs.add(imageConverterService.generateAsync(k, inputPath, outputPath));
			});

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		// Wait until they are all done
		CompletableFuture.allOf(jobs.toArray(new CompletableFuture[jobs.size()])).join();

		logger.info("Elapsed time for whole process : {} ", (System.currentTimeMillis() - start));

		if (args.length > 0 && args[0].equals("exitcode")) {
			throw new ExitException();
		}

		System.exit(0);
	}

	public String getInputPath() {
		return this.inputPath;
	}

	public String getoutputPath() {
		return this.outputPath;
	}

}