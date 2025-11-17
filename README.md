# Shticell Project

Shticell is a **Java-based client–server spreadsheet management application** featuring a JavaFX graphical user interface.  
The project is structured into three main modules:

- **ShticellWebApp** – Server module  
- **ShticellClient** – Client-side application  
- **ShticellEngine** – Core spreadsheet engine and logic

---

## 📌 Overview

Shticell provides an infrastructure for viewing, editing, and managing spreadsheet data in a multi-user environment.  
The server handles user requests, permissions, and data synchronization, while the client offers a visual interface and communicates with the backend.  
The engine module performs all spreadsheet calculations, history tracking, dependency management, and function parsing.

---

## 🚀 Project Modules

### 1. ShticellWebApp – Server Module

**Purpose**  
Acts as the backend service responsible for handling client requests, managing spreadsheet data, processing updates, and controlling user permissions.  
This module receives requests from the client, processes them, interacts with the engine, and returns the relevant data back to the client.

**Main Classes**

- **`SheetServlet`**  
  Handles requests from clients who want to view a specific sheet, and returns all relevant data for that sheet.

- **`SheetListServlet`**  
  Sends details about all the sheets available in the system, to be displayed on the main screen for all clients.

- **`UpdateCellStyleServlet`**  
  Processes update requests for a single cell value in a given sheet, sent by a client.

---

### 2. ShticellClient – Client Module

**Purpose**  
Responsible for the graphical user interface (JavaFX) and for communication with the server.  
This module displays spreadsheet data to the user, sends new or updated information to the server, and handles permissions and responses from the backend.

**Main Classes**

- **`ShticellClient`**  
  The main entry point of the client application.

- **`SheetManagementController`**  
  Manages the main application window, including the display of all spreadsheets in the system.

- **`ViewSheet`**  
  Manages the window for displaying and interacting with a single spreadsheet.

---

### 3. ShticellEngine – Spreadsheet Engine

**Purpose**  
Handles the data and business logic of the spreadsheet system.  
This is a passive module: it receives commands from the server, executes them, and returns the results.  
It is responsible for managing cells, sheets, versions, dependencies, and function evaluations.

**Main Classes**

- **`ClientImp`**  
  Represents a single cell in the spreadsheet.  
  Stores the original and effective values of the cell, the last version in which the cell was modified, a list of cells that depend on it, and more.

- **`SheetImpl`**  
  Represents the entire spreadsheet.  
  Responsible for managing all cells, saving different versions of the sheet, updating cell values, etc.

- **`LoadingXML`**  
  Responsible for loading a spreadsheet from an XML file and creating sheet and cell objects according to the data in the file.

- **`FunctionParser`**  
  Parses effective cell values.  
  Based on the original string value entered by the user, it determines whether the string is a function or a simple value and processes it accordingly.

---

## 🛠️ Technologies Used

- Java
- Java Servlets
- JavaFX
- XML Parsing
- Client–Server Architecture
