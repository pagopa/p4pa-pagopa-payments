package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.controller.generated.GpdApi;
import it.gov.pagopa.pu.pagopapayments.service.acagpdsync.gpd.GpdFacadeService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class GpdController implements GpdApi {

  private final GpdFacadeService gpdFacadeService;

  public GpdController(GpdFacadeService gpdFacadeService){
    this.gpdFacadeService = gpdFacadeService;
  }

  @Override
  public ResponseEntity<Void> syncGpd(String iud, DebtPositionDTO debtPositionDTO) {
    log.info("invoking syncGpd, iud[{}] debtPositionDTO[{}]", iud, debtPositionDTO.getDebtPositionId());
    gpdFacadeService.sync(iud, debtPositionDTO, SecurityUtils.getAccessToken());
    return ResponseEntity.ok().build();
  }
}
