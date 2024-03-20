package io.github.cloudtechnology.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@SpringBootApplication
public class GeneratorCliApplication {

  public static void main(String[] args) {
    SpringApplication.run(GeneratorCliApplication.class, args);
  }
}
