package cashu.java.common.api.response;

public class PRInvoice {
    public String hash;
    public String pr;
    public boolean paid;

    public PRInvoice(InvoiceResponse invoiceResponse, boolean paid) {
        this.hash = invoiceResponse.hash;
        this.pr = invoiceResponse.pr;
        this.paid = paid;
    }
    public PRInvoice(InvoiceStatusResponse invoiceStatusResponse, boolean paid) {
        this.hash = invoiceStatusResponse.quote;
        this.pr = invoiceStatusResponse.request;
        this.paid = paid;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "paid='" + paid + '\'' +
                ", pr='" + pr + '\'' +
                ", hash=" + hash +
                '}';
    }
}
