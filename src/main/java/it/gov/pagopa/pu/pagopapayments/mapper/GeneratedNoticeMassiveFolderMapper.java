package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.pagopapayments.dto.generated.GeneratedNoticeMassiveFolderDTO;
import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.NoticeGenerationMassiveResourceDTO;

public class GeneratedNoticeMassiveFolderMapper {
  private GeneratedNoticeMassiveFolderMapper() {}

  public static GeneratedNoticeMassiveFolderDTO toGeneratedNoticeMassiveFolderDTO(NoticeGenerationMassiveResourceDTO resourceDTO) {
    return GeneratedNoticeMassiveFolderDTO.builder()
      .folderId(resourceDTO.getFolderId())
      .build();
  }
}
