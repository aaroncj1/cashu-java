package cashu.java.wallet;

import cashu.java.wallet.persistence.MintEntity;
import cashu.java.wallet.persistence.MintRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WalletServiceImpl implements WalletService {
    private final MintRepository mintRepository;

    public WalletServiceImpl(MintRepository mintRepository) {
        this.mintRepository = mintRepository;
    }

    @Override
    public void receive(String proof) {
        //deserialize into Proofs
        //swap for new tokens
        // store in DB
    }

    @Override
    public void send(String amount) {
        // check DB for tokens available
        // swap for new tokens to make change if needed
        // serialize and return (? should wallet delete the tokens after it sends? even though it may not get spend?)

    }

    @Override
    public void deposit(String amount) {
        // ask mint for lightning invoice
        // watch for it to be paid, claim tokens once its paid

    }

    @Override
    public void withdraw(String amount) {
        // ask mint to melt tokens by sending LN invoice
        // send mint the tokens to pay the quote and wait for invoice to be paid
    }

    @Override
    public void checkBalance() {
        // check db and add up token amounts stored
    }

    public void addMint(String name, String mintUrl) throws IOException, InterruptedException {
        // adds mint to mint map
        mintRepository.save(new MintEntity(name, mintUrl));
    }

    public void listMints() throws JsonProcessingException {
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(mintRepository.findAll());
        System.out.println(json);
    }

}
