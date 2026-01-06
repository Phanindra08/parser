# 🚀 RDDL Transformation Engine

> A transformation engine converting **Relational Differential Dynamic Logic (RDDL)** into **Differential Dynamic Logic (dL)** and **dReal** formats for semi-automatic formal verification.

![Java 21](https://img.shields.io/badge/Java_21-orange?logo=openjdk&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring_Batch_5.2.x-green?logo=spring&logoColor=white)
![ANTLR](https://img.shields.io/badge/ANTLR_4.13.x-blue?logo=antlr&logoColor=white)

## 📋 Table of Contents
* [Prerequisites](#prerequisites)
* [Tech Stack](#tech-stack)
* [Build Instructions](#build-instructions)
* [Running the Application](#running-the-application)
* [Available Jobs](#available-job-names)
* [Documentation & Samples](#documentation--samples)

<a id="prerequisites"></a>
## 🔧 Prerequisites

Ensure your system has the following installed before proceeding:

| 🛠 Tool | 🔢 Version | 🔗 Download |
| :--- | :--- | :--- |
| **Java** | `21` | [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) |
| **Apache Maven** | `3.8.4+` | [Apache Maven Download](https://maven.apache.org/download.cgi) |
| **IntelliJ IDEA** | `Latest` | [IntelliJ IDEA Download](https://www.jetbrains.com/idea/download/) |

> **🔍 Verification:** Run `java -version` and `mvn -version` in your terminal to verify installations.

<a id="tech-stack"></a>
## 🛠 Tech Stack

Key libraries and frameworks driving the transformation engine:

| 🏷 Dependency | 🔢 Version | Description |
| :--- | :--- | :--- |
| **ANTLR** | `4.13.1` | Lexer and Parser generation for logic syntax. |
| **Spring Boot** | `3.4.4` | Application framework and dependency injection. |
| **Spring Batch** | `5.2.2` | High-performance batch processing for file transformation. |
| **Lombok** | `1.18.36` | Boilerplate code reduction. |

> **💡 IDEA Tip:** For the best development experience, install the **ANTLR v4** and **Lombok** plugins in IntelliJ.

<a id="build-instructions"></a>
## 📦 Build Instructions

To build the project and generate the executable JAR, run the following command in the project root:

```sh
mvn clean package
```
> Upon success, the artifact will be generated in the **/target** directory (e.g., **parser-0.0.1-SNAPSHOT.jar**).

## 🚀 Running the Application<a id="running-the-application"></a>

### 1. Prepare the Package Directory
Locate the existing `Package` folder in the project directory.

1.  Move the generated JAR file (from `/target`) into this `Package` folder.
2.  Ensure the `application.yml` file is present in this folder.

### 2. Configuration
Open the `application.yml` file inside the `Package` folder and update the **`dl-output`** property to specify where you want the generated files to be saved.

### 3. Execution Command
Open your terminal, navigate into the `Package` directory, and run the command below.

⚠️ **Important:** You must modify the command to match your specific file and desired operation:
* **JAR Name:** Replace `parser-0.0.1-SNAPSHOT.jar` with the actual name of your generated JAR file.
* **Input File:** Replace `--input.file` with the full path to your source file.
* **Job Name:** Replace `--job.name` with one of the options from the list below.

```sh
cd Package
java -jar parser-0.0.1-SNAPSHOT.jar \
  --spring.config.location=application.yml \
  --input.file="C:/path/to/your/InputFile" \
  --job.name=REL_DL_AST_GENERATION
```

> **Note:** The `--spring.config.location` argument ensures the application reads the configuration file located in your `Package` directory, not the one embedded inside the JAR.

## ⚙️ Available Job Names<a id="available-job-names"></a>

Select the appropriate `job.name` argument based on the transformation you need:

| Job Name | Description |
| :--- | :--- |
| **`DL_AST_GENERATION`** | Generate Abstract Syntax Tree (AST) visualization for **dL** files. |
| **`DL_TO_KEYMAERAX_OUTPUT`** | Convert **dL** files into KeYmaeraX-compatible format. |
| **`REL_DL_AST_GENERATION`** | Generate AST visualization for **RDDL** files. |
| **`REL_DL_TO_KEYMAERAX_OUTPUT`** | Convert **RDDL** files into KeYmaeraX-compatible format. |
| **`DL_TO_D_REAL_OUTPUT`** | Convert **dL** files into dReal solver format. |

## 📚 Documentation & Samples<a id="documentation--samples"></a>

* **Sample Data:**
    * 📂 **[Input Examples](./DocumentationAndSampleExamples/Inputs)**
    * 📂 **[Output Examples](./DocumentationAndSampleExamples/Outputs)**
    * *Note:* Generated KeYmaeraX files contain a unique ID in the header which changes on every run.
* **Grammar Reference:**
    * 📄 **[Parser Grammar PDF](./DocumentationAndSampleExamples/Documentation/ParserGrammar.pdf)** – Detailed grammar syntax guide for the supported formats in this project.
