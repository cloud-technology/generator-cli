
## Jooq

如果有需要調整命名方式可以調整 `src/main/java/io/github/cloudtechnology/generator/jooq/CustomNamingStrategy.java` 這個檔案。  

產生邏輯在這邊 `src/main/java/io/github/cloudtechnology/generator/jooq/JooqJavaGenerator.java`。





``` bash
sdk use java 21.0.2-graalce

./gradlew --no-daemon clean nativeCompile
cp ./build/native/nativeCompile/generator-cli .
./generator-cli
```

get binary under build/native/nativeCompile directory.

``` bash
sdk use java 17.0.10-librca
./gradlew --no-daemon clean build
cp ./build/libs/generator-cli-0.0.1.jar .
java -jar ./generator-cli-0.0.1.jar
java -jar ./generator-cli-0.0.1.jar generator
```




