package it.gov.pagopa.pu.pagopapayments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDataDTO {
  private byte[] notice;
  private String fileName;
}
