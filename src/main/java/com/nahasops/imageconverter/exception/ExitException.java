package com.nahasops.imageconverter.exception;

import org.springframework.boot.ExitCodeGenerator;

public class ExitException extends RuntimeException implements ExitCodeGenerator {

	private static final long serialVersionUID = 1L;

	@Override
	public int getExitCode() {
		return 10;
	}

}
