package it.gov.pagopa.pu.pagopapayments.controller;

import it.gov.pagopa.pu.pagopapayments.controller.generated.AcaApi;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AcaController implements AcaApi {

  private final AcaService acaService;

  public AcaController(AcaService acaService){
    this.acaService = acaService;
  }

  @Override
  public ResponseEntity<Void> createAca(DebtPositionDTO debtPositionDTO) {
    log.info("invoking createAca, debtPositionDTO[{}]", debtPositionDTO.getDebtPositionId());
    acaService.create(debtPositionDTO);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<Void> updateAca(DebtPositionDTO debtPositionDTO) {
    log.info("invoking updateAca, debtPositionDTO[{}]", debtPositionDTO.getDebtPositionId());
    acaService.update(debtPositionDTO);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<Void> deleteAca(DebtPositionDTO debtPositionDTO) {
    log.info("invoking deleteAca, debtPositionDTO[{}]", debtPositionDTO.getDebtPositionId());
    acaService.delete(debtPositionDTO);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
