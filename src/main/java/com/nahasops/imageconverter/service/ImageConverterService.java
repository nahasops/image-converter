package com.nahasops.imageconverter.service;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface ImageConverterService {
	
	CompletableFuture<Void> generateAsync(final Path path, final String inputPath, final String outputPath);
	
}
