package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.controller.generated.AcaApi;
import it.gov.pagopa.pu.pagopapayments.service.acagpdsync.aca.AcaFacadeService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AcaController implements AcaApi {

  private final AcaFacadeService acaFacadeService;

  public AcaController(AcaFacadeService acaFacadeService){
    this.acaFacadeService = acaFacadeService;
  }

  @Override
  public ResponseEntity<Void> syncAca(String iud, DebtPositionDTO debtPositionDTO) {
    log.info("invoking syncAca, iud[{}] debtPositionDTO[{}]", iud, debtPositionDTO.getDebtPositionId());
    acaFacadeService.sync(iud, debtPositionDTO, SecurityUtils.getAccessToken());
    return ResponseEntity.ok().build();
  }
}
