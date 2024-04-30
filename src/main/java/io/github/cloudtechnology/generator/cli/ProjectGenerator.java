package io.github.cloudtechnology.generator.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.SingleItemSelector.SingleItemSelectorContext;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.StringInput.StringInputContext;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.github.cloudtechnology.generator.bo.BuildToolEnum;
import io.github.cloudtechnology.generator.bo.RuntimeEnum;
import io.github.cloudtechnology.generator.command.CreateProjectCommand;
import io.github.cloudtechnology.generator.service.ProjectService;
import io.github.cloudtechnology.generator.transform.GeneratorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * https://github.com/spring-projects/spring-shell/blob/main/spring-shell-docs/src/test/java/org/springframework/shell/docs/UiComponentSnippets.java
 * https://wearenotch.com/developing-cli-application-with-spring-shell-part-1/
 * https://dev.to/noelopez/spring-shell-build-cli-apps-2l1o
 * https://www.baeldung.com/spring-shell-cli
 */

@Slf4j
@Command
@RequiredArgsConstructor
public class ProjectGenerator extends AbstractShellComponent {

  private final GeneratorMapper generatorMapper;
  private final ProjectService projectService;

  @Command(command = { "generator" })
  public void generator(
      @Option(longNames = "buildTool") BuildToolEnum buildToolEnum,
      @Option(longNames = "groupId") String groupId,
      @Option(longNames = "artifactId") String artifactId,
      @Option(longNames = "name") String name,
      @Option(longNames = "description") String description,
      @Option(longNames = "packageName") String packageName,
      @Option(longNames = "jvmVersion") String jvmVersion,
      @Option(longNames = "openapiFilePath") String openapiFilePath,
      @Option(longNames = "dbUrl") String dbUrl,
      @Option(longNames = "dbUsername") String dbUsername,
      @Option(longNames = "dbPassword") String dbPassword,
      @Option(longNames = "runtime") RuntimeEnum runtimeEnum) {
    if (ObjectUtils.isEmpty(buildToolEnum)) {
      String buildToolStr = this.promptForBuildTool();
      buildToolEnum = BuildToolEnum.valueOf(buildToolStr.toUpperCase());
    }

    groupId = StringUtils.hasText(groupId)
        ? groupId
        : promptForInput("Please enter group id", "com.example");
    artifactId = StringUtils.hasText(artifactId)
        ? artifactId
        : promptForInput("Please enter artifact id", "demo");
    name = StringUtils.hasText(name)
        ? name
        : promptForInput("Please enter project name", "demo");
    description = StringUtils.hasText(description)
        ? description
        : promptForInput(
            "Please enter project description",
            "Demo project for Spring Boot");
    packageName = StringUtils.hasText(packageName)
        ? packageName
        : promptForInput("Please enter package name", "com.example.demo");
    jvmVersion = StringUtils.hasText(jvmVersion)
        ? jvmVersion
        : promptForJvmVersion();
    openapiFilePath = StringUtils.hasText(openapiFilePath)
        ? openapiFilePath
        : promptForInput(
            "Please enter OpenAPI file path",
            "/path/to/openapi.yaml");
    dbUrl = StringUtils.hasText(dbUrl)
        ? dbUrl
        : promptForInput(
            "Please enter database URL",
            "jdbc:postgresql://localhost:5432/mydatabase");
    dbUsername = StringUtils.hasText(dbUsername)
        ? dbUsername
        : promptForInput("Please enter database username", "myuser");
    dbPassword = StringUtils.hasText(dbPassword)
        ? dbPassword
        : promptForSecretInput("Please enter database password", "secret");
    if (ObjectUtils.isEmpty(runtimeEnum)) {
      String runtimeStr = promptForRuntime();
      runtimeEnum = RuntimeEnum.valueOf(runtimeStr.toUpperCase());
    }

    CreateProjectCommand createProjectCommand = null;
    Path projectTempPath = null;
    try {
      // projectTempPath = Files.createTempDirectory("project_");
      Path userDirectoryPath = Paths.get("");
      projectTempPath = userDirectoryPath.resolve(
          Path.of(name + "-" + new Random().nextInt(1000)));

      createProjectCommand = generatorMapper.toCreateProjectCommand(
          projectTempPath,
          buildToolEnum,
          groupId,
          artifactId,
          name,
          description,
          packageName,
          jvmVersion,
          openapiFilePath,
          dbUrl,
          dbUsername,
          dbPassword,
          runtimeEnum);
      projectService.create(createProjectCommand);
      //

    } catch (Exception e) {
      log.error("", e);
    }
  }

  private String promptForBuildTool() {
    return selectFromOptions(
        "Please choose a build tool",
        List.of(BuildToolEnum.values())
            .stream()
            .map(BuildToolEnum::getValue)
            .collect(Collectors.toList()),
        BuildToolEnum.GRADLE.getValue());
  }

  private String promptForRuntime() {
    return selectFromOptions(
        "Please choose a runtime environment",
        List.of(RuntimeEnum.values())
            .stream()
            .map(RuntimeEnum::getValue)
            .collect(Collectors.toList()),
        RuntimeEnum.CLOUDRUN.getValue());
  }

  private String promptForJvmVersion() {
    return selectFromOptions(
        "Please choose a Java version",
        List.of("17"),
        "17");
  }

  private String selectFromOptions(
      String prompt,
      List<String> options,
      String defaultValue) {
    List<SelectorItem<String>> items = options
        .stream()
        .map(option -> SelectorItem.of(option, option.toUpperCase()))
        .collect(Collectors.toList());

    SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(getTerminal(), items, prompt,
        null);
    configureComponent(selector);
    SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelectorContext.empty());

    return context
        .getResultItem()
        .map(SelectorItem::getItem)
        .orElse(defaultValue);
  }

  private String promptForInput(String prompt, String defaultValue) {
    StringInput input = new StringInput(getTerminal(), prompt, defaultValue);
    configureComponent(input);
    StringInputContext context = input.run(StringInputContext.empty());
    return context.getResultValue();
  }

  private String promptForSecretInput(String prompt, String defaultValue) {
    StringInput input = new StringInput(getTerminal(), prompt, defaultValue);
    input.setMaskCharacter('*');
    configureComponent(input);
    StringInputContext context = input.run(StringInputContext.empty());
    return context.getResultValue();
  }

  private void configureComponent(
      SingleItemSelector<String, SelectorItem<String>> component) {
    component.setResourceLoader(getResourceLoader());
    component.setTemplateExecutor(getTemplateExecutor());
  }

  private void configureComponent(StringInput component) {
    component.setResourceLoader(getResourceLoader());
    component.setTemplateExecutor(getTemplateExecutor());
  }
}
