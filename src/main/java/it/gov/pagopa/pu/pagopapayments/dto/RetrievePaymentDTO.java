package it.gov.pagopa.pu.pagopapayments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievePaymentDTO {
    private String idPA;
    private String idBrokerPA;
    private String idStation;
    private String fiscalCode;
    private String noticeNumber;
    //only used for paGetPayment
    private Boolean postalTransfer;
}
