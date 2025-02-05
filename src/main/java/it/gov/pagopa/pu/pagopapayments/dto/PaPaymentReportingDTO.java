package it.gov.pagopa.pu.pagopapayments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaPaymentReportingDTO extends PaForNodeDTO {
    private byte[] paymentReportingBytes;
}
