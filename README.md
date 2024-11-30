# Student Management System

## Overview
The Student Management System is a JavaFX-based desktop application. This application provides a comprehensive solution for managing student records, offering features such as data entry, modification, and export capabilities.

## Features
The application includes several key functionalities:

- Student record management (add, edit, delete)
- Data validation with advanced regex patterns
- CSV import/export capabilities
- Theme customization (light/dark modes)
- Secure user session management
- Profile image handling
- Status notifications for user feedback

## Technical Requirements

### Prerequisites
- Java Development Kit (JDK) 20 or higher
- Apache Maven 3.8.5 or higher
- MySQL/MariaDB Server
- IDE supporting JavaFX (IntelliJ IDEA recommended)

### Dependencies
- JavaFX 20.0.1
- MySQL Connector/J 8.1.0
- MariaDB Java Client 3.1.4
- SLF4J API 1.7.5

## Installation and Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/CSC311_DB_UI_semesterlongproject.git
```

2. Configure the database connection in `DbConnectivityClass.java`:
```java
final static String DB_NAME = "CSC311_BD_TEMP";
final static String SQL_SERVER_URL = "jdbc:mysql://your-server-url";
final static String USERNAME = "your-username";
final static String PASSWORD = "your-password";
```

3. Build the project using Maven:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn javafx:run
```

## Usage Guide

### Student Management
- Add new students using the form on the right panel
- Select a record from the table to edit or delete
- Use the image upload feature to add student profile pictures
- All fields are validated in real-time before submission

### Data Import/Export
- Import student records from CSV files using File → Import CSV
- Export current records to CSV using File → Export CSV
- CSV format: FirstName, LastName, Department, Major, Email, ImageURL

### Theme Customization
- Switch between light and dark themes via the Theme menu
- Theme preferences are preserved between sessions

## Security Features

- Form validation using regex patterns
- Thread-safe user session management
- Secure password handling
- Input sanitization for database operations

## Known Issues and Limitations
- Maximum supported record limit: 10,000 entries
- Image uploads limited to local file system
- Requires active database connection

## Contributing
This project is part of an academic course assignment. While it's not open for direct contributions, feedback and suggestions are welcome through issue reporting.

## License
This project is provided for educational purposes only. All rights reserved.

## Contact
For questions or support regarding this project, please contact:
- Developer: Efe Eryildiz
