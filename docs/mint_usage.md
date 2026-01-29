# Cashu Java Mint

A simple Cashu Mint implementation.

## Prerequisites

- Java 17 or higher
- Gradle (optional, wrapper is included)

## Getting Started

### 1. Build the Project

From the project root directory, run:

```bash
./gradlew build -x test
```

### 2. Run the Mint Server

To start the mint server:

```bash
./gradlew :cashu-java-mint:bootRun
```

The mint will start on port 8080 (default).

## Configuration

The mint is configured via `src/main/resources/application.properties`.

Key configurations:
- `cashu.activePath`: Derivation path for keys.
- `cashu.masterSeed`: Seed for generating mint keys. **CHANGE THIS FOR PRODUCTION!**

## API Endpoints

The mint exposes standard Cashu NUT API endpoints:

-   `GET /v1/info`: Mint information (NUT-06)
-   `GET /v1/keys`: Active public keys (NUT-01)
-   `GET /v1/keysets`: List of all keysets (NUT-02)
-   `GET /v1/keys/{keysetId}`: Get keys for specific keyset
-   `POST /v1/mint/quote/{method}`: Request a mint quote (NUT-04)
-   `POST /v1/mint/{method}`: Execute minting
-   `POST /v1/swap`: Swap tokens (NUT-03)
