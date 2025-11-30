# Implementation Plan

- [x] 1. Set up backend project structure
  - Create Spring Boot project with Maven
  - Configure H2 database and JPA
  - Set up CORS configuration for frontend communication
  - Configure application.properties with database and server settings
  - _Requirements: 6.1, 6.2_

- [-] 2. Implement core domain models and entities
  - Create Group entity with JPA annotations
  - Create Expense entity with JPA annotations
  - Define SplitType enum (EQUAL, CUSTOM)
  - Set up entity relationships and collection mappings
  - _Requirements: 1.1, 2.1_


- [x] 3. Create DTOs for API requests and responses
  - Implement GroupRequest and GroupResponse DTOs
  - Implement ExpenseRequest and ExpenseResponse DTOs
  - Implement SettlementResponse DTO with nested objects
  - Add Bean Validation annotations (@NotNull, @NotBlank, @Positive, @Size)
  - _Requirements: 6.1, 6.3, 6.4_

- [x] 4. Implement repository layer
  - Create GroupRepository extending JpaRepository
  - Add custom query method findByGroupNameIgnoreCase
  - Create ExpenseRepository extending JpaRepository
  - Add custom query method findByGroupIdOrderByDateDesc
  - Add deleteByGroupId method for cascade deletion
  - _Requirements: 1.1, 1.2, 2.1, 8.1_

- [-] 5. Implement GroupService with business logic
  - Implement createGroup method with validation
  - Add group name uniqueness check (case-insensitive)
  - Add participant uniqueness validation (case-sensitive)
  - Enforce 10 group limit constraint
  - Enforce 10 participant limit per group
  - Implement getAllGroups with summary calculations
  - Implement getGroupById method
  - Implement deleteGroup with cascade deletion
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 8.1, 9.1, 9.3_

- [-] 6. Implement ExpenseService with split calculations
  - Implement addExpense method with validation
  - Add expense amount validation (positive numbers only)
  - Add date validation (no future dates)
  - Implement default date assignment (current date if not specified)
  - Implement calculateEqualSplit method using BigDecimal
  - Handle custom contributions when specified
  - Implement getExpensesByGroup method
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 4.1_


- [-] 7. Implement SettlementService with optimization algorithm
  - Implement calculateNetBalances method using BigDecimal
  - Calculate total paid per participant
  - Calculate total owed per participant (based on expense shares)
  - Compute net balance (paid - owed) for each participant
  - Implement optimizeTransactions method with greedy algorithm
  - Separate creditors (positive balance) and debtors (negative balance)
  - Sort by absolute value and match largest creditor with largest debtor
  - Generate settlement transactions minimizing count
  - Implement getMemberBalances method
  - _Requirements: 3.1, 3.2, 3.3, 3.5, 5.3, 10.1, 10.2, 10.3_


- [x] 8. Implement REST controllers
  - Create GroupController with endpoints
  - POST /api/groups - create group
  - GET /api/groups - list all groups
  - GET /api/groups/{groupId} - get group details
  - DELETE /api/groups/{groupId} - delete group
  - Create ExpenseController with endpoints
  - POST /api/groups/{groupId}/expenses - add expense
  - GET /api/groups/{groupId}/expenses - list expenses
  - GET /api/groups/{groupId}/settlements - get settlements
  - Add proper HTTP status codes (200, 201, 400, 404, 500)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_


- [x] 9. Implement global exception handling
  - Create @ControllerAdvice for global exception handling
  - Define custom exceptions (GroupNotFoundException, ValidationException, BusinessRuleException)
  - Implement error response DTO with consistent structure
  - Map exceptions to appropriate HTTP status codes
  - Add logging for all errors
  - _Requirements: 6.5_

- [x] 10. Add sample data initializer
  - Create @Component with @PostConstruct method
  - Initialize 2-3 sample groups with different participant counts
  - Add 5-10 sample expenses with various amounts
  - Demonstrate settlement calculations with realistic data
  - _Requirements: 1.1, 2.1_

- [x] 11. Set up frontend project structure
  - Create React application with npm/yarn
  - Set up React Router for navigation
  - Configure Axios for API communication
  - Create folder structure (components, pages, services, utils)
  - Set up CSS modules or styled-components
  - _Requirements: 7.1_

- [x] 12. Implement API client service
  - Create axios instance with base URL configuration
  - Implement groupService with API methods (create, getAll, getById, delete)
  - Implement expenseService with API methods (add, getByGroup)
  - Implement settlementService with API method (getSettlements)
  - Add axios interceptors for error handling
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 13. Create Dashboard page
  - Implement Dashboard component
  - Fetch and display all groups on mount
  - Create GroupCard component for group display
  - Show group name, participant count, total expense
  - Add "Create New Group" button with navigation
  - Implement loading state while fetching data
  - Add error handling with user feedback
  - _Requirements: 1.5, 7.1_

- [x] 14. Create Group Creation page
  - Implement CreateGroupPage component
  - Create form with group name input
  - Implement dynamic participant list (add/remove functionality)
  - Add real-time validation for group name and participants
  - Validate participant uniqueness (case-sensitive)
  - Enforce 10 participant limit
  - Handle form submission with API call
  - Display success notification and navigate to dashboard
  - Display error messages for validation failures
  - _Requirements: 1.1, 1.2, 1.4, 7.2, 9.1, 9.3_

- [x] 15. Create Group Detail page
  - Implement GroupDetailPage component
  - Fetch group details and expenses on mount
  - Display group header with name and participant info
  - Create ExpenseList component showing expenses chronologically
  - Display date, description, amount, payer for each expense
  - Create SettlementSummary component
  - Display "who owes whom" with amounts
  - Create MemberBalanceCard component
  - Show total paid, total owed, net balance per participant
  - Use color coding (green for positive, red for negative balances)
  - Add "Add Expense" button with navigation
  - _Requirements: 3.3, 5.1, 5.2, 5.3, 5.4, 7.1, 7.6_

- [x] 16. Create Add Expense modal/page
  - Implement AddExpensePage or modal component
  - Create form with expense description input
  - Add amount input with validation (positive numbers)
  - Implement date picker with default to current date
  - Create payer selection dropdown from group participants
  - Add optional contribution inputs for each participant
  - Display split calculation preview
  - Validate date (no future dates)
  - Handle form submission with API call
  - Display success notification and navigate back to group detail
  - Display error messages for validation failures
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.2_

- [x] 17. Implement global error handling and notifications
  - Create Toast/Notification component for user feedback
  - Implement error boundary for React component errors
  - Add global axios error interceptor
  - Display user-friendly error messages
  - Handle network failures gracefully
  - _Requirements: 7.4_

- [x] 18. Add responsive styling
  - Implement responsive CSS for mobile and desktop
  - Ensure card layouts adapt to screen size
  - Make forms mobile-friendly
  - Test on different screen sizes
  - Add loading indicators for API calls
  - _Requirements: 7.3, 7.5_

- [x] 19. Create README with setup instructions
  - Document backend setup (Java version, Maven commands)
  - Document frontend setup (Node version, npm commands)
  - Provide step-by-step instructions to run the application
  - Include API documentation or link to Swagger
  - Add screenshots or demo instructions
  - List sample data available on startup

- [ ] 20. Final checkpoint - Ensure all tests pass
  - Run all backend unit tests and property-based tests
  - Run all frontend tests
  - Manually test all user flows end-to-end
  - Verify all acceptance criteria are met
  - Ensure all tests pass, ask the user if questions arise
