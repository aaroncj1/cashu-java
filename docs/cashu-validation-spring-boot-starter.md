## Cashu eCash validation Spring Boot starter

This starter adds a lightweight Cashu validation layer to your Spring Boot
API endpoints. It parses Cashu tokens, checks that the total amount is high
enough, and calls the mint `/v1/check` endpoint to ensure the proofs are
unspent (and optionally not pending).

### Add the dependency

If you include this module in the same Gradle build:

```gradle
dependencies {
    implementation project(":cashu-validation-spring-boot-starter")
}
```

If you publish the module to your own repository, use the coordinates you
publish with in your application build.

### Basic usage

Annotate your controller or handler methods with `@CashuRequired` to require
validation. Set the minimum amount in the annotation or use the default from
configuration.

```java
import io.github.aaroncj1.cashu.validation.CashuRequired;
import io.github.aaroncj1.cashu.validation.CashuValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PremiumController {

    @CashuRequired(minAmount = 1000)
    @GetMapping("/premium")
    public String premium(HttpServletRequest request) {
        CashuValidationResult result =
                (CashuValidationResult) request.getAttribute(CashuValidationResult.REQUEST_ATTRIBUTE);
        return "Paid with " + result.totalAmount();
    }
}
```

### Configuration

All properties are under `cashu.validation`:

| Property | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Turn validation on or off. |
| `token-header` | `X-Cashu-Token` | Header that carries the Cashu token. |
| `default-min-amount` | `0` | Minimum amount when no annotation override is used. |
| `require-annotation` | `true` | If `false`, all endpoints are validated. |
| `check-endpoint-path` | `/v1/check` | Mint endpoint used to check spendable proofs. |
| `reject-pending` | `true` | Reject proofs marked as pending by the mint. |
| `allow-multiple-mints` | `true` | Allow tokens that include multiple mints. |

Example `application.yaml`:

```yaml
cashu:
  validation:
    token-header: X-Cashu-Token
    default-min-amount: 500
    require-annotation: true
    check-endpoint-path: /v1/check
    reject-pending: true
    allow-multiple-mints: true
```

### Response behavior

- Missing token header returns `401 Unauthorized`.
- Invalid, spent, or insufficient tokens return `402 Payment Required`.

### Notes

- This starter validates spendability by calling the mint `/v1/check` endpoint.
- Future enhancements can add redemption and wallet persistence.
