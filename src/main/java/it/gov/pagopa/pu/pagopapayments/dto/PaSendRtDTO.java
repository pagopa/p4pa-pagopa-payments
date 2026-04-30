package it.gov.pagopa.pu.pagopapayments.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptTransferDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaSendRtDTO extends PaForNodeDTO {
    private List<ReceiptTransferDTO> transferList;
    private byte[] receiptBytes;
}
