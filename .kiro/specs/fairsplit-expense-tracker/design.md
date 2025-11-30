# FairSplit Design Document

## Overview

FairSplit is a full-stack web application that enables groups to track shared expenses and calculate fair settlements. The system consists of a Java Spring Boot backend with an H2 in-memory database and a React frontend. The architecture follows a clean separation of concerns with RESTful APIs as the communication layer.

The core challenge is implementing an efficient settlement algorithm that minimizes the number of transactions required to balance all debts within a group. The system must handle decimal precision carefully to avoid rounding errors in financial calculations.

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (Port 3000)                     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Dashboard   │  │ Group Detail │  │ Add Expense  │     │
│  │  Component   │  │  Component   │  │   Component  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│           │                │                  │             │
│           └────────────────┴──────────────────┘             │
│                          │                                  │
│                   ┌──────▼──────┐                          │
│                   │  API Client │                          │
│                   └──────┬──────┘                          │
└──────────────────────────┼─────────────────────────────────┘
                           │ HTTP/JSON
                           │
┌──────────────────────────▼─────────────────────────────────┐
│                  Backend (Port 8080)                        │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │              REST Controllers                       │   │
│  │  GroupController  │  ExpenseController             │   │
│  └────────────┬───────────────────┬───────────────────┘   │
│               │                   │                        │
│  ┌────────────▼───────────────────▼───────────────────┐   │
│  │              Service Layer                          │   │
│  │  GroupService  │  ExpenseService  │ SettlementSvc  │   │
│  └────────────┬───────────────────┬───────────────────┘   │
│               │                   │                        │
│  ┌────────────▼───────────────────▼───────────────────┐   │
│  │           Repository Layer                          │   │
│  │  GroupRepository  │  ExpenseRepository             │   │
│  └────────────┬───────────────────┬───────────────────┘   │
│               │                   │                        │
│  ┌────────────▼───────────────────▼───────────────────┐   │
│  │              H2 In-Memory Database                  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

**Backend:**
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- H2 Database (in-memory)
- Maven for dependency management

**Frontend:**
- Node.js 18+
- React 18.x
- Axios for HTTP requests
- React Router for navigation
- CSS Modules or Styled Components for styling

## Components and Interfaces

### Backend Components

#### 1. REST Controllers

**GroupController**
- `POST /api/groups` - Create new group
- `GET /api/groups` - List all groups
- `GET /api/groups/{groupId}` - Get group details
- `DELETE /api/groups/{groupId}` - Delete group

**ExpenseController**
- `POST /api/groups/{groupId}/expenses` - Add expense
- `GET /api/groups/{groupId}/expenses` - List expenses
- `GET /api/groups/{groupId}/settlements` - Get settlement calculations

#### 2. Service Layer

**GroupService**
- `createGroup(GroupRequest)` - Validates and creates group
- `getAllGroups()` - Retrieves all groups with summaries
- `getGroupById(String groupId)` - Retrieves group details
- `deleteGroup(String groupId)` - Removes group and expenses
- `validateGroupConstraints()` - Enforces business rules

**ExpenseService**
- `addExpense(String groupId, ExpenseRequest)` - Creates expense entry
- `getExpensesByGroup(String groupId)` - Retrieves expenses
- `calculateEqualSplit(double amount, int participants)` - Splits amount equally
- `validateExpense()` - Validates expense data

**SettlementService**
- `calculateSettlements(String groupId)` - Computes optimized settlements
- `calculateNetBalances(List<Expense>)` - Computes net balance per participant
- `optimizeTransactions(Map<String, Double>)` - Minimizes transaction count
- `getMemberBalances(String groupId)` - Returns detailed balance info

#### 3. Repository Layer

**GroupRepository** (extends JpaRepository)
- Standard CRUD operations
- `findByGroupNameIgnoreCase(String name)` - Check name uniqueness
- `count()` - Check group limit

**ExpenseRepository** (extends JpaRepository)
- Standard CRUD operations
- `findByGroupIdOrderByDateDesc(String groupId)` - Get expenses chronologically
- `deleteByGroupId(String groupId)` - Cascade delete

### Frontend Components

#### 1. Pages

**Dashboard**
- Displays all groups in card layout
- Shows group summary (name, participants, total)
- "Create New Group" button
- Navigation to group details

**GroupDetailPage**
- Group header with info
- Expense list with date, description, amount, payer
- Settlement summary section
- Member balance cards
- "Add Expense" button

**CreateGroupPage**
- Form with group name input
- Dynamic participant list (add/remove)
- Validation messages
- Submit/cancel actions

#### 2. Components

**GroupCard**
- Displays group summary
- Click to navigate to details

**ExpenseList**
- Renders expenses chronologically
- Shows split details

**SettlementSummary**
- Displays "who owes whom"
- Color-coded balances

**AddExpenseModal**
- Form for expense entry
- Date picker, amount input
- Payer selection
- Contribution inputs (optional)

**MemberBalanceCard**
- Shows individual balance
- Total paid, total owed, net balance

## Data Models

### Group Entity

```java
@Entity
@Table(name = "groups")
public class Group {
    @Id
    private String groupId;          // UUID
    
    @Column(unique = true, nullable = false)
    private String groupName;
    
    @ElementCollection
    @CollectionTable(name = "group_participants")
    private List<String> participants;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Transient
    private Double totalExpense;     // Calculated field
}
```

### Expense Entity

```java
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    private String expenseId;        // UUID
    
    @Column(nullable = false)
    private String groupId;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private String paidBy;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @ElementCollection
    @CollectionTable(name = "expense_contributions")
    @MapKeyColumn(name = "participant")
    @Column(name = "amount")
    private Map<String, Double> contributions;
    
    @Enumerated(EnumType.STRING)
    private SplitType splitType;     // EQUAL, CUSTOM
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

### DTOs

**GroupRequest**
```java
{
    "groupName": "string",
    "participants": ["string"]
}
```

**GroupResponse**
```java
{
    "groupId": "string",
    "groupName": "string",
    "participants": ["string"],
    "participantCount": int,
    "totalExpense": double,
    "createdAt": "datetime"
}
```

**ExpenseRequest**
```java
{
    "description": "string",
    "amount": double,
    "paidBy": "string",
    "date": "date",
    "contributions": {"participantName": double}  // optional
}
```

**ExpenseResponse**
```java
{
    "expenseId": "string",
    "groupId": "string",
    "description": "string",
    "amount": double,
    "paidBy": "string",
    "date": "date",
    "contributions": {"participantName": double},
    "splitDetails": {"participantName": double},
    "createdAt": "datetime"
}
```

**SettlementResponse**
```java
{
    "settlements": [
        {
            "from": "string",
            "to": "string",
            "amount": double
        }
    ],
    "memberBalances": {
        "participantName": {
            "totalPaid": double,
            "totalOwed": double,
            "netBalance": double
        }
    }
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Group creation with unique names succeeds

*For any* valid group name and participant list (1-10 participants with unique names), creating a group should succeed and return a unique group identifier.

**Validates: Requirements 1.1**

### Property 2: Duplicate group names are rejected

*For any* existing group, attempting to create another group with the same name (regardless of case) should be rejected with an error.

**Validates: Requirements 1.2**

### Property 3: Group list completeness

*For any* set of created groups, retrieving the group list should return all groups with their correct names, participant counts, and total expenses.

**Validates: Requirements 1.5**

### Property 4: Expense creation with equal split

*For any* valid expense (positive amount, valid date, valid payer) added to a group, the system should create the expense and calculate equal splits where each participant's share equals the total amount divided by the number of participants (within rounding tolerance).

**Validates: Requirements 2.1**

### Property 5: Default date assignment

*For any* expense created without a specified date, the system should automatically assign the current date.

**Validates: Requirements 2.4**

### Property 6: Custom contributions are recorded

*For any* expense with specified individual contributions, the system should record each contribution exactly as specified and calculate remaining balances correctly.

**Validates: Requirements 2.5**

### Property 7: Net balance sum is zero

*For any* group with expenses and contributions, the sum of all participants' net balances should equal zero (within rounding tolerance of 0.01).

**Validates: Requirements 3.5**

### Property 8: Settlement completeness

*For any* group with expenses, if all participants follow the suggested settlements, every participant should end up with a net balance of zero (within rounding tolerance).

**Validates: Requirements 10.1**

### Property 9: Settlement transaction minimization

*For any* group with expenses where settlements are needed, the number of settlement transactions should be minimal (at most min(creditors, debtors) transactions).

**Validates: Requirements 3.2, 10.2**

### Property 10: Settlement structure correctness

*For any* calculated settlements, each settlement should have a "from" participant (with negative balance), a "to" participant (with positive balance), and a positive amount.

**Validates: Requirements 3.3**

### Property 11: Monetary precision

*For any* monetary calculation (splits, balances, settlements), all amounts should be rounded to exactly two decimal places.

**Validates: Requirements 4.1**

### Property 12: Participant uniqueness validation

*For any* group creation or modification, if duplicate participant names exist within the same group, the operation should be rejected with an error.

**Validates: Requirements 4.2, 9.1**

### Property 13: Expense chronological ordering

*For any* group with multiple expenses, retrieving the expense list should return expenses ordered by date in descending order (most recent first).

**Validates: Requirements 5.1**

### Property 14: Total expense calculation

*For any* group, the total expense should equal the sum of all individual expense amounts in that group (within rounding tolerance).

**Validates: Requirements 5.2**

### Property 15: Split sum equals total

*For any* expense, the sum of all individual participant shares should equal the total expense amount (within rounding tolerance of 0.01).

**Validates: Requirements 10.5**

### Property 16: Balance calculation correctness

*For any* participant in a group, their net balance should equal the total they paid minus the total they owe based on their share of all expenses.

**Validates: Requirements 3.1, 5.3**

### Property 17: Cascade deletion

*For any* group with expenses, deleting the group should remove both the group and all associated expenses from the system.

**Validates: Requirements 8.1**

### Property 18: API response structure for group creation

*For any* successful group creation request, the API should return HTTP 201 status with a response containing groupId, groupName, participants, and createdAt fields.

**Validates: Requirements 6.1**

### Property 19: API response structure for settlements

*For any* settlement request for a valid group, the API should return HTTP 200 status with a response containing a settlements array and memberBalances object.

**Validates: Requirements 6.4**

### Property 20: Case-sensitive participant names

*For any* group, participant names "John" and "john" should be treated as different participants (case-sensitive comparison).

**Validates: Requirements 9.3**

### Property 21: Net amount calculation with offsetting

*For any* participant who both paid for some expenses and owes for others, their net balance should reflect the offset (credits minus debts) before settlement generation.

**Validates: Requirements 10.3**

## Error Handling

### Validation Errors

The system implements comprehensive validation at multiple layers:

**Backend Validation:**
- Request DTOs use Bean Validation annotations (@NotNull, @NotBlank, @Positive, @Size)
- Service layer performs business rule validation
- Custom validators for complex rules (unique names, date constraints)

**Frontend Validation:**
- Real-time field validation as user types
- Form-level validation before submission
- Display inline error messages with specific guidance

### Error Response Format

All API errors follow a consistent JSON structure:

```json
{
    "timestamp": "2024-01-15T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "errors": [
        {
            "field": "groupName",
            "message": "Group name already exists"
        }
    ],
    "path": "/api/groups"
}
```

### HTTP Status Codes

- **200 OK** - Successful GET request
- **201 Created** - Successful POST request
- **400 Bad Request** - Validation error or invalid input
- **404 Not Found** - Resource doesn't exist
- **500 Internal Server Error** - Unexpected server error

### Exception Handling Strategy

**Backend:**
- `@ControllerAdvice` for global exception handling
- Custom exceptions: `GroupNotFoundException`, `ValidationException`, `BusinessRuleException`
- Logging all errors with stack traces for debugging

**Frontend:**
- Axios interceptors for global error handling
- Toast notifications for user feedback
- Error boundaries for React component errors
- Graceful degradation for network failures

## Testing Strategy

### Unit Testing

Unit tests verify specific examples, edge cases, and error conditions:

**Backend Unit Tests:**
- Service layer business logic (settlement algorithm, split calculations)
- Validation logic (constraints, business rules)
- Repository operations (CRUD, queries)
- Edge cases: empty groups, single participant, zero balances
- Error conditions: invalid inputs, constraint violations

**Frontend Unit Tests:**
- Component rendering with different props
- User interaction handlers (button clicks, form submissions)
- State management logic
- API client error handling

**Testing Framework:** JUnit 5 for backend, Jest + React Testing Library for frontend

### Property-Based Testing

Property-based tests verify universal properties that should hold across all inputs. Together with unit tests, they provide comprehensive coverage: unit tests catch concrete bugs, property tests verify general correctness.

**Property-Based Testing Library:** jqwik for Java backend

**Configuration:** Each property-based test should run a minimum of 100 iterations to ensure thorough random input coverage.

**Test Tagging:** Each property-based test must be tagged with a comment explicitly referencing the correctness property in this design document using the format: `**Feature: fairsplit-expense-tracker, Property {number}: {property_text}**`

**Implementation Requirements:**
- Each correctness property listed above must be implemented by a SINGLE property-based test
- Tests should use jqwik's generators to create random valid inputs
- Tests should verify the property holds for all generated inputs
- Failed tests should provide clear counterexamples

**Key Property Tests:**
- Settlement algorithm correctness (Properties 7, 8, 9, 10, 21)
- Split calculations (Properties 4, 15)
- Balance calculations (Property 16)
- Monetary precision (Property 11)
- Data integrity (Properties 3, 13, 14, 17)
- Validation rules (Properties 2, 12, 20)

### Integration Testing

Integration tests verify component interactions:
- API endpoint tests with MockMvc
- Database integration with test containers
- End-to-end user flows (create group → add expenses → view settlements)

## Settlement Algorithm Design

### Algorithm Overview

The settlement optimization problem is equivalent to the "minimum cash flow" problem. Given a set of net balances (positive = owed money, negative = owes money), find the minimum number of transactions to settle all debts.

### Algorithm Steps

1. **Calculate Net Balances:**
   - For each participant, sum all amounts they paid
   - Subtract their share of all expenses
   - Result is net balance (positive = creditor, negative = debtor)

2. **Separate Creditors and Debtors:**
   - Create list of participants with positive balances (creditors)
   - Create list of participants with negative balances (debtors)
   - Sort both lists by absolute value (largest first)

3. **Greedy Matching:**
   - Take largest creditor and largest debtor
   - Transfer min(creditor_balance, |debtor_balance|)
   - Remove settled participants (balance = 0)
   - Repeat until all balances are zero

### Complexity Analysis

- Time Complexity: O(n log n) for sorting + O(n) for matching = O(n log n)
- Space Complexity: O(n) for storing balances
- Transaction Count: At most min(creditors, debtors) transactions

### Example

```
Initial Balances:
Alice: +30 (owed)
Bob: -20 (owes)
Charlie: -10 (owes)

Settlements:
Bob pays Alice: 20
Charlie pays Alice: 10

Final Balances: All zero
Transactions: 2 (optimal)
```

### Rounding Considerations

- All calculations use `BigDecimal` for precision
- Round to 2 decimal places using `HALF_UP` rounding mode
- Final balance check allows tolerance of ±0.01 to handle rounding errors

## Implementation Notes

### Backend Configuration

**application.properties:**
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:fairsplit
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Server
server.port=8080
```

### Frontend Configuration

**API Base URL:**
- Development: `http://localhost:8080/api`
- Configurable via environment variables

**Routing:**
- `/` - Dashboard
- `/groups/new` - Create Group
- `/groups/:id` - Group Detail
- `/groups/:id/expenses/new` - Add Expense

### Data Initialization

For demo purposes, include a data initializer that creates sample groups and expenses on startup:
- 2-3 sample groups with different participant counts
- 5-10 sample expenses with various amounts
- Demonstrates settlement calculations

### CORS Configuration

Backend must enable CORS for frontend origin:
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "DELETE");
            }
        };
    }
}
```

## Performance Considerations

### Backend Optimizations

- Use `@Transactional(readOnly = true)` for read operations
- Eager fetch participants and contributions to avoid N+1 queries
- Cache group summaries if needed (not critical for in-memory DB)

### Frontend Optimizations

- Lazy load group details (don't fetch all expenses on dashboard)
- Debounce form validation
- Memoize expensive calculations (settlement display)
- Use React.memo for list items

### Scalability Notes

For production deployment beyond demo scope:
- Replace H2 with PostgreSQL or MySQL
- Add pagination for group and expense lists
- Implement caching layer (Redis)
- Add database indexes on groupId, date fields
- Consider event sourcing for expense history
