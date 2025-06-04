package edu.charlotte.parser;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class ParserApplication {
	public static void main(String[] args) {
		SpringApplication.run(ParserApplication.class, args);
	}
}
