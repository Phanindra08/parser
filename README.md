# 🚀 Welcome to Our Parser Project

## 🔧 Prerequisites

Before you start, make sure you have the appropriate tools which are listed below.:

| 🛠 Requirement      | 🔢 Version | 🔗 Link                                                                       |
|---------------------|------------|-------------------------------------------------------------------------------|
| ☕ **Java**          | `21`       | [Java Downloads](https://www.oracle.com/java/technologies/downloads/#java21/) |
| 🛠 **Apache Maven** | `3.8.4+`   | [Apache Maven](https://maven.apache.org/download.cgi)                         |
| **IntelliJ IDEA**   | `Latest`   | [IntelliJ IDEA Download](https://www.jetbrains.com/idea/download/)            |

Please find below the list of important maven dependencies used in this project.
## Maven Dependencies
| 🏷 Dependency    | 🔢 Version |
|------------------|------------|
| **ANTLR**        | `4.13.1`   |
| **Spring Boot**  | `3.4.4`    |
| **Spring Batch** | `5.0.0`    |
| **Lombok**       | `1.18.36`  |

🔍 **Check Your Java and Maven Versions using the below commands:**
```sh
java -version
mvn -version
```

## 📦️Build
Follow the commands below to build the project and generate a new package if any code changes are made.
```sh
mvn clean package
```

## Running the Application
The jar file generated after the build will be located in the `target` directory. Please place the jar file in the Package directory for the execution of the application. Please edit the value of the key dl-output in the `application.yml` file to place the output files in the desired location.
After placing the JAR package in the Package folder. You can run the application using the following command:
```sh
java -jar parser-*.jar --spring.config.location=application.yml --input.file=<Input File path> --job.name=<Any Job Name from the below list>
```

Example command for running the application:
```sh
java -jar parser-*.jar --spring.config.location=application.yml --input.file=C:\Users\skothur1\Downloads\Parser_Inputs\Inputs\RelDL_Inputs\RelDlExample1 --job.name=REL_DL_AST_GENERATION
```

## Available Job Names:
| Job Name                   | Description                                      |
|----------------------------|--------------------------------------------------|
| DL_AST_GENERATION          | Generating AST for DL Input Files                |
| DL_TO_KEYMAERAX_OUTPUT     | Converting DL Input File to KeYmaeraX output     |
| REL_DL_AST_GENERATION      | Generating AST for DL Input Files                |
| REL_DL_TO_KEYMAERAX_OUTPUT | Converting Rel DL Input File to KeYmaeraX output |