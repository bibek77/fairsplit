# FairSplit - Bill Splitting Application

A full-stack web application for splitting expenses fairly among groups of people. The system automatically calculates who owes whom and optimizes settlements to minimize the number of transactions required.

![FairSplit Demo](kiro-app-demo.gif)

## Features

- **Group Management**: Create and manage expense groups with multiple participants
- **Expense Tracking**: Add expenses with equal or custom splits
- **Smart Settlements**: Optimized settlement calculations to minimize transactions
- **Member Balances**: View detailed balance information for each participant
- **Responsive Design**: Works seamlessly on desktop and mobile devices

## Technology Stack

### Backend
- Java 17+
- Spring Boot 3.2.0
- Spring Data JPA
- H2 In-Memory Database
- Maven

### Frontend
- React 18.x
- React Router
- Axios
- CSS3

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6 or higher
- **Node.js**: 16.x or higher
- **npm**: 8.x or higher

## Getting Started

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The backend server will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

The frontend application will start on `http://localhost:3000`

## Sample Data

The application comes pre-loaded with sample data for demonstration:

### Sample Groups:
1. **Weekend Trip** - 3 participants (Alice, Bob, Charlie)
   - 4 expenses totaling $471.00

2. **Office Lunch** - 4 participants (David, Emma, Frank, Grace)
   - 3 expenses totaling $264.00

3. **Apartment Expenses** - 2 participants (Henry, Iris)
   - 3 expenses totaling $2,270.00

## API Documentation

### Groups

- `POST /api/groups` - Create a new group
- `GET /api/groups` - Get all groups
- `GET /api/groups/{groupId}` - Get group details
- `DELETE /api/groups/{groupId}` - Delete a group

### Expenses

- `POST /api/groups/{groupId}/expenses` - Add an expense
- `GET /api/groups/{groupId}/expenses` - Get all expenses for a group

### Settlements

- `GET /api/groups/{groupId}/settlements` - Get optimized settlements for a group

## Application Features

### Group Creation
- Create groups with 1-10 participants
- Unique group names (case-insensitive)
- Maximum of 10 groups

### Expense Management
- Add expenses with description, amount, payer, and date
- Equal split (default) or custom contributions
- Automatic date assignment if not specified
- Validation for positive amounts and past dates

### Settlement Calculation
- Optimized algorithm to minimize transaction count
- Net balance calculation for each participant
- Clear "who owes whom" display
- Color-coded balances (green for positive, red for negative)

## Development

### Backend Development

The backend uses an H2 in-memory database that resets on each restart. To access the H2 console:

1. Navigate to `http://localhost:8080/h2-console`
2. Use JDBC URL: `jdbc:h2:mem:fairsplit`
3. Username: `sa`
4. Password: (leave empty)

### Frontend Development

The frontend is built with Create React App. Key directories:

- `src/pages/` - Page components
- `src/components/` - Reusable components
- `src/services/` - API client services

## Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Building for Production

### Backend
```bash
cd backend
mvn clean package
java -jar target/fairsplit-backend-1.0.0.jar
```

### Frontend
```bash
cd frontend
npm run build
```

The build artifacts will be in the `frontend/build` directory.

## Architecture

The application follows a clean architecture pattern:

**Backend:**
- Controllers → Services → Repositories → Database
- DTOs for API communication
- Global exception handling
- CORS configuration for frontend

**Frontend:**
- React Router for navigation
- Axios for API calls
- Component-based architecture
- Responsive CSS design

## Settlement Algorithm

The application uses a greedy algorithm to optimize settlements:

1. Calculate net balance for each participant (paid - owed)
2. Separate creditors (positive balance) and debtors (negative balance)
3. Sort by absolute value
4. Match largest creditor with largest debtor
5. Repeat until all balances are zero

This ensures the minimum number of transactions required to settle all debts.

## License

This is a demo application for educational purposes.

## Support

For issues or questions, please refer to the application documentation or contact the development team.
