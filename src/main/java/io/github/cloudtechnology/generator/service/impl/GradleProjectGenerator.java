package io.github.cloudtechnology.generator.service.impl;

import com.samskivert.mustache.Mustache;
import io.github.cloudtechnology.generator.service.ProjectGenerator;
import io.github.cloudtechnology.generator.vo.ProjectVo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradleProjectGenerator implements ProjectGenerator {

  private final Mustache.Compiler compiler;

  @Override
  public void generate(ProjectVo projectVo) throws Exception {
    Path javaPath = Path.of("src", "main", "java");
    // 取出設定的 package name, 並將 . 換成檔案路徑的分隔符號
    Path javaMainPath = javaPath.resolve(
      projectVo.packageName().replaceAll("\\.", File.separator)
    );
    Path configurationPath = javaPath.resolve(
      projectVo.packageName().replaceAll("\\.", File.separator) +
      File.separator +
      "configuration"
    );

    //
    this.writeTemplate(
        projectVo,
        "templates/project/.gitignore.mustache",
        Path.of(".")
      );

    // src/main/resources/templates/project/DemoApplication.java.mustache
    this.writeTemplate(
        projectVo,
        "templates/project/DemoApplication.java.mustache",
        Path.of("src", "main", "java", "com", "example", "demo")
      );
    //
    this.writeTemplate(
        projectVo,
        "templates/project/DemoApplicationTests.java.mustache",
        Path.of("src", "test", "java", "com", "example", "demo")
      );
    this.writeTemplate(
        projectVo,
        "templates/project/TestContainerConfiguration.java.mustache",
        Path.of("src", "test", "java", "com", "example", "demo")
      );
    //

    this.writeTemplate(
        projectVo,
        "templates/project/application.yml.mustache",
        Path.of("src", "main", "resources")
      );
    this.writeTemplate(
        projectVo,
        "templates/project/application-gcp.yml.mustache",
        Path.of("src", "main", "resources")
      );
    // config
    this.writeTemplate(
        projectVo,
        "templates/project/application-dev.yml.mustache",
        Path.of("config")
      );
    this.writeTemplate(
        projectVo,
        "templates/project/application-ut.yml.mustache",
        Path.of("config")
      );

    // this.writeTemplate(projectVo, "templates/project/RedisConfig.java.mustache",
    // configurationPath);
    // this.writeTemplate(projectVo,
    // "templates/project/RepositoryConfig.java.mustache", configurationPath);
    // this.writeTemplate(projectVo,
    // "templates/project/OpenAPIConfig.java.mustache", configurationPath);

    // this.copyClasspathFile(projectVo, "static/application-loadtests.yml",
    // Path.of("config"));
    // this.copyClasspathFile(projectVo, "static/application-unittest.yml",
    // Path.of("config"));
    // this.copyClasspathFile(projectVo, "static/application-chaos-monkey.yml",
    // Path.of("config"));

    this.writeTemplate(
        projectVo,
        "templates/project/settings.gradle.mustache",
        Path.of(".")
      );
    this.writeTemplate(
        projectVo,
        "templates/project/build.gradle.mustache",
        Path.of(".")
      );

    this.writeTemplate(
        projectVo,
        "templates/project/service.yaml.mustache",
        Path.of("dev-resources")
      );

    // //
    this.copyClasspathFile(projectVo, "static/compose.yaml", Path.of("."));
    this.copyClasspathFile(
        projectVo,
        "static/db.changelog-master.yaml",
        Path.of("src", "main", "resources", "db", "changelog")
      );
    // this.copyClasspathFile(projectVo, "static/logback-spring.xml", Path.of("src",
    // "main", "resources"));
    // //
    // this.unzipDirectory(Path.of(new
    // ClassPathResource("static/devcontainer.zip").getURI()),
    // projectVo.projectTempPath());
    //

    this.copyClasspathFile(
        projectVo,
        "static/devcontainer.json",
        Path.of(".devcontainer")
      );
    this.copyClasspathFile(
        projectVo,
        "static/postCreateCommand.sh",
        Path.of(".devcontainer")
      );

    this.copyClasspathFile(projectVo, "static/gradlew.bat", Path.of("."));
    this.copyClasspathFile(projectVo, "static/gradlew", Path.of("."));
    this.copyClasspathFile(
        projectVo,
        "static/gradle/wrapper/gradle-wrapper.jar",
        Path.of("gradle", "wrapper")
      );
    this.copyClasspathFile(
        projectVo,
        "static/gradle/wrapper/gradle-wrapper.properties",
        Path.of("gradle", "wrapper")
      );
  }

  private void writeTemplate(
    ProjectVo projectVo,
    String templatePath,
    Path outputPath
  ) throws IOException {
    Path projectTempPath = projectVo.projectTempPath();
    ClassPathResource resource = new ClassPathResource(templatePath);
    // Resource resource = new ClassPathResource(templatePath);
    String templateContent = this.readResourceToString(templatePath);
    String outputContent = compiler.compile(templateContent).execute(projectVo);
    Files.createDirectories(projectTempPath.resolve(outputPath));
    Files.writeString(
      projectTempPath
        .resolve(outputPath)
        .resolve(resource.getFilename().replaceAll("\\.mustache", "")),
      outputContent,
      StandardCharsets.UTF_8,
      StandardOpenOption.CREATE_NEW
    );
  }

  private void copyClasspathFile(
    ProjectVo projectVo,
    String classpathFilePath,
    Path outputPath
  ) throws IOException {
    Path projectTempPath = projectVo.projectTempPath();
    // 確保輸出目錄存在
    Files.createDirectories(projectTempPath.resolve(outputPath));
    // 使用 try-with-resources 以確保流正確關閉
    try (
      InputStream resourceStream = new ClassPathResource(
        classpathFilePath
      ).getInputStream()
    ) {
      // 定義目標文件路徑
      Path targetPath = projectTempPath
        .resolve(outputPath)
        .resolve(Paths.get(classpathFilePath).getFileName());
      log.info("resourcePath={}", classpathFilePath);
      log.info("outputPath={}", outputPath);
      // 將流覆制到目標位置
      Files.copy(
        resourceStream,
        targetPath,
        StandardCopyOption.REPLACE_EXISTING
      );
      // 檢查是否為gradlew文件，且非Windows系統
      if (
        targetPath.getFileName().toString().equals("gradlew") &&
        !System.getProperty("os.name").startsWith("Windows")
      ) {
        // 設置文件為可執行
        File file = targetPath.toFile();
        boolean result = file.setExecutable(true, false);
        if (!result) {
          log.warn("Failed to set execute permission for {}", targetPath);
        }
      }
    }
  }

  private String readResourceToString(String filePath) {
    try {
      String fileText = IOUtils.toString(
        getClass().getClassLoader().getResourceAsStream(filePath),
        "UTF-8"
      );
      return fileText;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // private String readResourceToString(Resource resource) throws IOException {
  // return Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);
  // }

  // private String readResourceToString(String path) throws IOException {
  // Resource resource = new ClassPathResource(path);
  // return Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);
  // }

  private void unzipDirectory(Path zipFile, Path targetDirectory)
    throws IOException {
    if (!Files.exists(zipFile)) {
      return;
    }
    if (!Files.exists(targetDirectory)) {
      Files.createDirectory(targetDirectory);
    }
    try (
      ZipInputStream zipInputStream = new ZipInputStream(
        Files.newInputStream(zipFile)
      )
    ) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        final Path toPath = targetDirectory.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectory(toPath);
        } else {
          if (!Files.exists(toPath.getParent())) {
            Files.createDirectories(toPath.getParent());
          }
          Files.copy(zipInputStream, toPath);
        }
      }
    }
  }
}
