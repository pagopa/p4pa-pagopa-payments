package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.pagopapayments.dto.generated.GeneratedNoticeMassiveFolderDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.pagopa.nodo.printpaymentnotice.dto.generated.NoticeGenerationMassiveResourceDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeneratedNoticeMassiveFolderMapperTest {

  private final PodamFactory podamFactory;

  public GeneratedNoticeMassiveFolderMapperTest() {
    this.podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidDataWhenToGeneratedNoticeMassiveFolderDTOThenOk() {
    //given
    NoticeGenerationMassiveResourceDTO request = podamFactory.manufacturePojo(NoticeGenerationMassiveResourceDTO.class);

    //when
    GeneratedNoticeMassiveFolderDTO response = GeneratedNoticeMassiveFolderMapper.toGeneratedNoticeMassiveFolderDTO(request);

    //verify
    assertNotNull(response);
    Assertions.assertEquals(request.getFolderId(), response.getFolderId());
    TestUtils.checkNotNullFields(response);
  }
}
