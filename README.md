# VARL TRADING APPLICATION
In-memory order book to place a limit order with order matching including the ability to view all open orders

## Overview

The VARL Trading Application is a robust application built with Spring Boot version 3.4.2 and Vert.x, utilizing Kotlin. It features an in-memory order book for placing limit orders, real-time order matching, and the ability to view all open orders. The application employs role-based authentication using JWT for secure access.

## Features

- **In-memory Order Book**: Efficiently place and manage limit orders.
- **Order Matching**: Real-time matching of buy and sell orders.
- **View Open Orders**: Users can view all currently open orders.
- **Role-Based Authentication**: Secure access using JWT.
- **API Rate Limiting**: Limit the number of requests to enhance performance.
- **API have scoped permissions: View access, Trade, Withdraw and Transfer

## Endpoints

### Authentication

- **Register**:
    - `POST http://localhost:8080/api/auth/register`
    - {
      "firstName": "Edwin",
      "lastName": "Magodi",
      "password": "Password@123",
      "dateOfBirth": "1991-12-19",
      "residentialCountry": "SA",
      "identityIssuingCountry": "ZW",
      "identityType": "Passport",
      "identityNumber": "A123456789",
      "identityExpiryDate": "2030-01-01",
      "cellNumber": "+1234567890",
      "email": "magodiedwin@gmail.com",
      "purpose": "TRADING",
      "employmentStatus": "EMPLOYED_PART_TIME",
      "sourceOfFunds": "ALLOWANCE",
      "role": "USER"
      }
- **Login**:
    - `POST http://localhost:8080/api/auth/login`
    - {
      "email": "magodiedwin@gmail.com",
      "password": "Password@123"
      }
  
- **Change Password**:
    - `POST http://localhost:8080/api/auth/changepassword?email=magodiedwin@gmail.com`
    - {
      "currentPassword": "Password@123",
      "newPassword": "Password@1234"
      }

### Orders

- **Create Limit Order**:
    - `POST http://localhost:8080/api/orders/limit`
    - {
      "side": "SELL",
      "quantity": 4.0,
      "price": 200.0,
      "pair": "BTCZAR",
      "customerOrderId": "0002",
      "timeInForce": "GTC",
      "postOnly": false,
      "allowMargin": false,
      "reduceOnly": false,
      "status": "PLACED"
      }
- **Get All Orders**:
    - `GET http://localhost:8080/api/orders`
- **Get Order by ID**:
    - `GET http://localhost:8080/api/orders/:id`
- **Get Order Book**:
    - `GET http://localhost:8080/:pair/orderbook`
- **Get Recent Trades**:
    - `GET http://localhost:8080/:pair/tradehistory`
- **Get Open Orders** (Restricted to ADMIN):
    - `GET http://localhost:8080/:pair/openorders`
- **Get Open Orders by Customer ID**:
    - `GET http://localhost:8080/customerOrderId/:customerOrderId/openorders`
- **Modify Order**:
    - `GET http://localhost:8080/api/orders/modify/:id`

## Getting Started

### Prerequisites

- Kotlin 1.9.25
- Java 17
- Maven
- Docker

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/emagodi/trading.git
   cd trading
   
2. Build the project:
    ```bash
    mvn clean package
    docker-compose up --build

### DataLoader
- JavaFaker
- Automatically create 100 fake users for testing
- Automatically create 100 orders and match to easily view trade history and order book

### Caching
- In order to improve the latency of requests, all GET requests for orders are cached by default.
- I used the HTTP Cache-Control Header, which comprises one or more comma separated directives. These directives determine whether a GET response is cachable, and if so, the duration.

### Rate Limiting
- There is an API call limit of 5 API calls per minute per API key just for testing, beyond which the API calls will fail with a 429 Too Many Requests response. 
- A response header of X-RateLimit-Limit	5, 
- A response header of X-RateLimit-Remaining	4
- A response header of X-RateLimit-Reset	1739626766791 (Unix exact time for reset)

### Email Notification
- Asynchronous event driven approach is used to send email when order is created
- Email is extracted from the authenticated user using jwt