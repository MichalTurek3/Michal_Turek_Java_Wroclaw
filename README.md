# 💳 Payment Method Discount Optimizer

## 📘 Overview
This Java application is designed to determine the most cost-effective way to fully pay for orders using a combination of loyalty points (PUNKTY) and various traditional payment methods (such as bank cards). It calculates the best possible discount that can be applied for each order and outputs the total spent per method.

This project was built using Maven and includes source code, unit tests, example input files, and compiled class files.

## 🛠 Requirements
- Java 17+ (Java 21 recommended)
- Maven 3.x

## 🛠️ Technologies & Dependencies

- Java 21
- Maven
- Lombok – for automatic generation of getters, setters, constructors, etc.
- Jackson Databind – for parsing JSON input files
- SLF4J + Logback – for logging (optional but included)
- JUnit 5 – for unit testing

## 🔧 How to Build
From the root project directory, run:
```bash
mvn clean package
```

## ▶️ How to Run
Use the following command, specifying paths to the JSON input files:
```bash
cd ~/"path"/Michal_Turek_Java_Wroclaw
mvn clean package
java -jar target/app.jar target/classes/orders.json target/classes/paymentmethods.json
```

This will print to standard output the total amounts paid using each method.

## 🧪 How to Test
Run unit tests with:
```bash
mvn test
```

## 📤 Output Example
```
PUNKTY 100.00
mZysk 120.00
BosBankrut 190.00
```

## 💡 Notes
- The application selects the highest discount per order.
- Loyalty points and traditional payment discounts are mutually exclusive.
- Partial payment with points is allowed if at least 10% of the order value is covered.

## 👨‍💻 Author
Michał Turek
