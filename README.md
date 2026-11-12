# AlgoTrade
### Java-Based Stock Exchange Trading Simulator

AlgoTrade is a command-line stock exchange simulator built using Java 21. It models essential stock-market operations, including order placement, price-time priority matching, trade execution, portfolio management, risk validation, settlement, and persistent storage.

The project demonstrates object-oriented programming, data structures, algorithmic thinking, and file-based persistence.

## Features

- **Stock Exchange Management** – Register and manage multiple stocks.
- **Order Management** – Create BUY and SELL limit orders.
- **Order Book** – Maintain separate BUY and SELL books using price-time priority.
- **Matching Engine** – Match compatible orders and generate trade executions.
- **Trade Settlement** – Update buyer and seller cash balances and stock holdings.
- **Risk Management** – Validate trading orders before execution.
- **Portfolio Management** – Track cash balances, holdings, and average purchase prices.
- **CSV Persistence** – Save stocks, traders, holdings, orders, and trades.
- **State Restoration** – Restore saved exchange data and active orders after restarting.
- **Trade History** – Preserve and display historical trade records.

## Technology Stack

| Technology | Usage |
|---|---|
| Java 21 | Core application |
| OOP | Domain modelling and modular design |
| TreeMap | Price-level ordering |
| ArrayDeque | FIFO order queues |
| HashMap | Fast entity and order lookup |
| BigDecimal | Financial calculations |
| CSV | Data persistence |
| Eclipse IDE | Development environment |
| Git and GitHub | Version control and hosting |

## Architecture

```text
com.algotrade
│
├── AlgoTradeApp.java
│
├── enums
│   ├── OrderSide
│   ├── OrderType
│   └── OrderStatus
│
├── model
│   ├── Stock
│   ├── Trader
│   ├── Portfolio
│   ├── Holding
│   ├── Order
│   └── Trade
│
├── exchange
│   ├── StockExchange
│   ├── OrderBook
│   ├── MatchingEngine
│   ├── SettlementEngine
│   ├── ExchangeManager
│   └── RiskManager
│
├── persistence
│   ├── CsvUtil
│   ├── StockRepository
│   ├── TraderRepository
│   ├── OrderRepository
│   └── TradeRepository
│
└── util
    └── Money
```

## Order Matching

The order book maintains:

- BUY orders in descending price order.
- SELL orders in ascending price order.
- FIFO queues for orders at the same price level.

This provides price-time priority:

1. Better prices receive priority.
2. Earlier orders receive priority when prices are equal.
3. Compatible BUY and SELL orders are matched.
4. Executed trades are passed to the settlement engine.

## Data Persistence

AlgoTrade uses CSV repositories to store exchange state.

| File | Stored information |
|---|---|
| stocks.csv | Stock information |
| traders.csv | Trader accounts |
| holdings.csv | Portfolio holdings |
| orders.csv | Active orders |
| trades.csv | Historical trades |

The application saves data when exiting and restores saved data during startup.

The `data/` directory is generated locally and is not included in the public repository.

## Getting Started

### Requirements

- Java Development Kit (JDK) 21
- Eclipse IDE or another Java IDE

### Run from Eclipse

1. Clone or download this repository.
2. Import the project into Eclipse as an existing Java project.
3. Configure the project to use JDK 21.
4. Open `com.algotrade.AlgoTradeApp`.
5. Run it as a Java Application.

### Run the executable JAR

```bash
java -jar AlgoTrade.jar
```

On the first run, the application initializes a demo exchange. On subsequent runs, it attempts to restore locally saved data.

## Demo Accounts

The initial demo exchange includes:

- Demo Buyer – ₹500,000 starting cash.
- Demo Seller – 100 TCS shares with an average purchase price of ₹3,200.

Users can select accounts, view market information, place limit orders, inspect portfolios, and review trade history.

## Testing

The project has been manually tested through component-level test programs and terminal workflows, including:

- Stock and portfolio operations.
- Order creation and state transitions.
- Order-book price and FIFO priority.
- Matching and trade execution.
- Settlement of cash and stock holdings.
- Risk validation.
- CSV serialization and deserialization.
- Order restoration.
- Trade-history restoration.
- Application restart and portfolio persistence.

## Project Scope

AlgoTrade is an educational stock exchange simulator. It does not connect to a real stock exchange, brokerage, or live market data provider. It is not intended for real-money trading.

The current application provides command-line interaction and CSV-based persistence.

## Future Improvements

- Graphical trading dashboard.
- Database integration using PostgreSQL.
- Real-time market-data integration.
- Advanced order types.
- Order cancellation through the application interface.
- Transactional settlement and resource reservation.
- Automated unit and integration testing.
- REST API integration.

## Author

**Karthik**  
B.Tech – Computer Science and Engineering  
KL University

## License

This project is available under the MIT License. See `LICENSE` for details.