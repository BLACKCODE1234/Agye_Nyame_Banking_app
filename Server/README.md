# JavaFX Banking System

A small JavaFX demo app with an in-memory banking domain:
- Login with a 10-digit mobile number + 4-digit PIN (with show/hide PIN toggle)
- Reset PIN screen (verify mobile + account ID, set new PIN)
- View balance
- Deposit / withdraw
- See a transaction history

## Run

1. Install Maven (since your workspace uses `pom.xml`) and ensure `mvn` is on your PATH.
2. From this folder run:

```powershell
mvn javafx:run
```

## Sample Accounts

The app starts with a few in-memory accounts:
- `9876543210` / `1234` (Alice)
- `9123456780` / `4321` (Bob)

