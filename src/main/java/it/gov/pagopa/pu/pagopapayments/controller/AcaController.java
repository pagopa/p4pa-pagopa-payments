package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.controller.generated.AcaApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class AcaController implements AcaApi {

  private final AcaService acaService;

  public AcaController(AcaService acaService){
    this.acaService = acaService;
  }

  @Override
  public ResponseEntity<List<String>> createAca(DebtPositionDTO debtPositionDTO) {
    log.info("invoking createAca, debtPositionDTO[{}]", debtPositionDTO.getDebtPositionId());
    List<String> iudList = acaService.create(debtPositionDTO, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(iudList);
  }

  @Override
  public ResponseEntity<List<String>> updateAca(DebtPositionDTO debtPositionDTO) {
    log.info("invoking updateAca, debtPositionDTO[{}]", debtPositionDTO.getDebtPositionId());
    List<String> iudList = acaService.update(debtPositionDTO, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(iudList);
  }

  @Override
  public ResponseEntity<List<String>> deleteAca(DebtPositionDTO debtPositionDTO) {
    log.info("invoking deleteAca, debtPositionDTO[{}]", debtPositionDTO.getDebtPositionId());
    List<String> iudList = acaService.delete(debtPositionDTO, SecurityUtils.getAccessToken());
    return ResponseEntity.ok(iudList);
  }

}
