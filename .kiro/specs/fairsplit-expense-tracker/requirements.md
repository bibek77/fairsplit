# Requirements Document

## Introduction

FairSplit is a bill-splitting web application that enables groups of friends, colleagues, or any collection of people to fairly divide expenses. The system automatically calculates who owes whom and optimizes settlements to minimize the number of transactions required. This is a full-stack demo application with a Java backend and Node.js frontend, using an in-memory database for simplicity.

## Glossary

- **FairSplit System**: The complete web application including backend and frontend components
- **Expense Group**: A collection of participants who share expenses together
- **Participant**: An individual member within an expense group
- **Expense Entry**: A record of money spent by one participant on behalf of the group
- **Payer**: The participant who paid for an expense
- **Contribution**: An amount paid by a participant toward an expense
- **Settlement**: A calculated payment from one participant to another to balance debts
- **Net Balance**: The total amount a participant owes (negative) or is owed (positive)
- **Backend Service**: The Java Spring Boot REST API server running on port 8080
- **Frontend Application**: The Node.js React/Vue.js client running on port 3000

## Requirements

### Requirement 1: Group Creation and Management

**User Story:** As a user, I want to create and manage expense groups, so that I can organize expenses for different sets of people.

#### Acceptance Criteria

1. WHEN a user submits a group creation form with a unique name and participant list, THE FairSplit System SHALL create a new expense group with a unique identifier
2. WHEN a user attempts to create a group with a duplicate name (case-insensitive), THE FairSplit System SHALL reject the request and display an error message
3. WHEN a user attempts to create more than 10 groups, THE FairSplit System SHALL prevent creation and display a limit reached message
4. WHEN a user attempts to add more than 10 participants to a group, THE FairSplit System SHALL reject the request and display an error message
5. WHEN a user requests the list of all groups, THE FairSplit System SHALL display all groups with their names, participant counts, and total expenses

### Requirement 2: Expense Entry and Recording

**User Story:** As a user, I want to add expense entries to a group, so that I can track who paid for what and how expenses should be split.

#### Acceptance Criteria

1. WHEN a user submits an expense with description, amount, payer, and date to a group, THE FairSplit System SHALL create an expense entry and calculate equal splits among all participants
2. WHEN a user attempts to add an expense with a negative or zero amount, THE FairSplit System SHALL reject the request and display an error message
3. WHEN a user attempts to add an expense with a future date, THE FairSplit System SHALL reject the request and display an error message
4. WHEN a user adds an expense without specifying a date, THE FairSplit System SHALL automatically use the current date
5. WHEN a user specifies individual contributions for an expense, THE FairSplit System SHALL record each contribution and calculate remaining balances

### Requirement 3: Settlement Calculation and Optimization

**User Story:** As a user, I want to see optimized settlement calculations, so that I can understand who owes whom with the minimum number of transactions.

#### Acceptance Criteria

1. WHEN a user requests settlements for a group, THE FairSplit System SHALL calculate net balances for each participant based on all expenses and contributions
2. WHEN calculating settlements, THE FairSplit System SHALL minimize the number of transactions required to settle all debts
3. WHEN displaying settlements, THE FairSplit System SHALL show each required payment as "from participant" to "to participant" with the amount
4. WHEN a participant has paid exactly their share, THE FairSplit System SHALL display their net balance as zero
5. WHEN all expenses and contributions are processed, THE FairSplit System SHALL ensure the sum of all net balances equals zero

### Requirement 4: Data Validation and Constraints

**User Story:** As a system administrator, I want the system to enforce business rules and validate data, so that the application maintains data integrity.

#### Acceptance Criteria

1. WHEN processing any monetary amount, THE FairSplit System SHALL round to two decimal places for precision
2. WHEN a user attempts to add a participant with a duplicate name within the same group, THE FairSplit System SHALL reject the request and display an error message
3. WHEN a user submits any form with empty required fields, THE FairSplit System SHALL display validation errors for each missing field
4. WHEN a user enters an invalid date format, THE FairSplit System SHALL reject the input and display a format error message
5. WHEN the Backend Service receives a request with invalid data types, THE FairSplit System SHALL return an HTTP 400 error with descriptive error details

### Requirement 5: Expense History and Reporting

**User Story:** As a user, I want to view expense history and summaries for each group, so that I can track spending patterns and verify calculations.

#### Acceptance Criteria

1. WHEN a user views a group detail page, THE FairSplit System SHALL display all expenses in chronological order with date, description, amount, and payer
2. WHEN displaying a group summary, THE FairSplit System SHALL calculate and show the total expenses for that group
3. WHEN a user views member balances, THE FairSplit System SHALL display each participant's total paid, total owed, and net balance
4. WHEN displaying expense history, THE FairSplit System SHALL include split details showing how each expense was divided
5. WHEN a group has no expenses, THE FairSplit System SHALL display an empty state message with an option to add the first expense

### Requirement 6: REST API Communication

**User Story:** As a frontend developer, I want well-defined REST APIs, so that the Frontend Application can communicate reliably with the Backend Service.

#### Acceptance Criteria

1. WHEN the Frontend Application sends a POST request to create a group, THE Backend Service SHALL return HTTP 201 with the created group details including groupId
2. WHEN the Frontend Application sends a GET request for all groups, THE Backend Service SHALL return HTTP 200 with an array of group summaries
3. WHEN the Frontend Application sends a POST request to add an expense, THE Backend Service SHALL return HTTP 201 with the expense details and calculated splits
4. WHEN the Frontend Application sends a GET request for settlements, THE Backend Service SHALL return HTTP 200 with optimized settlement transactions
5. WHEN the Backend Service encounters an error, THE Backend Service SHALL return appropriate HTTP status codes (400 for validation errors, 404 for not found, 500 for server errors) with error messages in JSON format

### Requirement 7: User Interface and Experience

**User Story:** As a user, I want an intuitive and responsive interface, so that I can easily manage expenses on any device.

#### Acceptance Criteria

1. WHEN a user accesses the dashboard, THE Frontend Application SHALL display all groups in a card or tile layout with navigation options
2. WHEN a user creates or edits data, THE Frontend Application SHALL provide real-time validation feedback with clear error messages
3. WHEN the Frontend Application makes an API call, THE Frontend Application SHALL display loading indicators until the response is received
4. WHEN an API operation succeeds or fails, THE Frontend Application SHALL display a notification message to inform the user
5. WHEN a user views the application on mobile or desktop, THE Frontend Application SHALL adapt the layout responsively to the screen size
6. WHEN displaying positive balances, THE Frontend Application SHALL use green color coding, and WHEN displaying negative balances, THE Frontend Application SHALL use red color coding

### Requirement 8: Group Deletion

**User Story:** As a user, I want to delete expense groups I no longer need, so that I can keep my dashboard organized.

#### Acceptance Criteria

1. WHEN a user requests to delete a group, THE FairSplit System SHALL remove the group and all associated expenses from the system
2. WHEN a group is successfully deleted, THE FairSplit System SHALL redirect the user to the dashboard and display a confirmation message
3. WHEN a user attempts to access a deleted group, THE Backend Service SHALL return HTTP 404 with an appropriate error message

### Requirement 9: Participant Name Uniqueness

**User Story:** As a user, I want participant names to be unique within a group, so that I can clearly identify who owes what without confusion.

#### Acceptance Criteria

1. WHEN creating a group, THE FairSplit System SHALL validate that all participant names are unique within that group
2. WHEN a user attempts to add duplicate participant names, THE FairSplit System SHALL reject the request and display an error indicating which names are duplicated
3. WHEN comparing participant names for uniqueness, THE FairSplit System SHALL treat names as case-sensitive to allow variations like "John" and "john" if desired by users

### Requirement 10: Settlement Algorithm Correctness

**User Story:** As a user, I want accurate settlement calculations, so that I can trust the system to fairly distribute expenses.

#### Acceptance Criteria

1. WHEN calculating settlements for any group with expenses, THE FairSplit System SHALL ensure that following all suggested settlements results in all participants having zero net balance
2. WHEN multiple participants owe money and multiple participants are owed money, THE FairSplit System SHALL generate settlements that minimize the total number of transactions
3. WHEN a participant both paid for expenses and owes for other expenses, THE FairSplit System SHALL calculate the net amount (offset credits against debts) before generating settlements
4. WHEN all participants have equal contributions and equal shares, THE FairSplit System SHALL generate zero settlements
5. WHEN calculating splits for an expense, THE FairSplit System SHALL ensure the sum of all individual shares equals the total expense amount (within rounding tolerance)
