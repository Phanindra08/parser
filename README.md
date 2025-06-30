# 🚀 Parser Project
Welcome to the Parser Project. This guide will help you to set up, build and run the Parser application.

## 🔧 Prerequisites

Before you start, make sure you have the appropriate tools installed on your system:

| 🛠 Tools            | 🔢 Version | 🔗 Link                                                                      |
|---------------------|------------|------------------------------------------------------------------------------|
| ☕ **Java**          | `21`       | [Java Downloads](https://www.oracle.com/java/technologies/downloads/#java21) |
| 🛠 **Apache Maven** | `3.8.4+`   | [Apache Maven](https://maven.apache.org/download.cgi)                        |
| **IntelliJ IDEA**   | `Latest`   | [IntelliJ IDEA Download](https://www.jetbrains.com/idea/download/)           |

🔍 **You can verify your Java and Maven Versions using the below commands:**
```sh
java -version
mvn -version
```
## Maven Dependencies

Please find below the list of important maven dependencies used in this project.
| 🏷 Dependency    | 🔢 Version |
|------------------|------------|
| **ANTLR**        | `4.13.1`   |
| **Spring Boot**  | `3.4.4`    |
| **Spring Batch** | `5.2.2`    |
| **Lombok**       | `1.18.36`  |

## IDE Plugins

For a better development experience while using IntelliJ IDEA, please install these plugins: 

| Plugin Name  | Description                                   |
|--------------|-----------------------------------------------|
| **ANTLR v4** | Provides the support for ANTLR grammar files. |
| **Lombok**   | Provides the support for Lombok annotations.  |

## 📦️Build
If you make any code changes, follow the below commands to rebuild the project and generate a new package.
```sh
mvn clean package
```

## Running the Application

After a successful build, the generated JAR file will be located in the `target` directory. Move this JAR file into your designated `Package` directory for execution.
Before running the JAR file, edit the <b>dl-output</b> key in the `application.yml` file to specify your desired output location for the generated files.
After placing the JAR package in the Package folder. You can run the application using this command:
```sh
java -jar parser-*.jar --spring.config.location=application.yml --input.file=<Input File path> --job.name=<Any Job Name from the below list>
```

Example command for running the application:
```sh
java -jar parser-0.0.1-SNAPSHOT.jar --spring.config.location=application.yml --input.file=C:\Users\skothur1\Downloads\Parser_Inputs\Inputs\RelDL_Inputs\RelDlExample1 --job.name=REL_DL_AST_GENERATION
```

## Available Job Names:
Choose from the following job names based on your desired operation:

| Job Name                   | Description                                      |
|----------------------------|--------------------------------------------------|
| DL_AST_GENERATION          | Generating AST for DL Input Files                |
| DL_TO_KEYMAERAX_OUTPUT     | Converting DL Input File to KeYmaeraX output     |
| REL_DL_AST_GENERATION      | Generating AST for Rel DL Input Files            |
| REL_DL_TO_KEYMAERAX_OUTPUT | Converting Rel DL Input File to KeYmaeraX output |

## 📝 Notes
- <b>Sample Input and Output Files: </b> When referring to the sample input and output files, check out the [Inputs](./DocumentationAndSampleExamples/Inputs) and [Outputs](./DocumentationAndSampleExamples/Outputs) folders. Keep in mind that if you use the sample input files to generate KeYmaeraX output, the resulting output file will be nearly identical, except for a <b>unique ID</b> on the first three lines. This ID changes each time you run the application.
- <b>Parser Grammar: </b> For details of the grammar syntax used in this project, please refer the [Parser Grammar](./DocumentationAndSampleExamples/Documentation/ParserGrammar.pdf) file.