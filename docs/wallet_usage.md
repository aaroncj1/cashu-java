# Cashu Java Wallet

A JavaFX-based desktop wallet for the [Cashu](https://cashu.space) protocol.

## Prerequisites

- Java 17 or higher
- Gradle (optional, wrapper is included)

## Getting Started

### 1. Build the Project

From the project root directory, run:

```bash
./gradlew build -x test
```

### 2. Run the Wallet

To start the wallet client:

```bash
./gradlew :cashu-java-wallet:bootRun
```

This will launch the JavaFX application window.

## Usage

### Connecting to a Mint

1.  Find a Cashu mint URL (e.g., `https://testnut.cashu.space`).
2.  In the wallet UI, enter the URL in the **Mint URL** field.
3.  Click **Add Mint**.
4.  Check the log area to confirm connection.

### Checking Balance

1.  To check your total balance across all mints, leave the **Balance** field empty and click **Check Balance**.
2.  To check balance for a specific mint, enter the mint URL and click **Check Balance**.

### Sending Ecash

1.  Enter the amount (in sats) you want to send in the **Amount** field.
2.  Click **Send Ecash**.
3.  The wallet will generate a token string.
4.  Copy this token string to send to someone else.

### Receiving Ecash

1.  Copy a Cashu token string (starts with `cashuA` or `cashuB`).
2.  Paste it into the **Token** text area.
3.  Click **Receive Ecash**.
4.  The wallet will swap the token with the mint (re-minting) and update your balance.

## Troubleshooting

-   **Database**: The wallet stores data in a local H2 database located at `./data/walletdb`. If you encounter corruption or want to reset, delete the `data` folder.
-   **Logs**: Check the application log output in the terminal where you started the wallet for detailed error messages.
