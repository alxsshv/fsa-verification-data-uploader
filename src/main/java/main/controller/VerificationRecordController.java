package main.controller;

import main.dto.rest.VerificationRecordDto;
import main.service.implementations.VerificationRecordServiceImpl;
import main.service.interfaces.VerificationRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verifications/records")
public class VerificationRecordController {

    private final VerificationRecordService recordService;

    public VerificationRecordController(VerificationRecordServiceImpl recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVerificationRecord(@PathVariable ("id") String id){
        return recordService.findById(Long.parseLong(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVerificationRecord(@RequestBody VerificationRecordDto recordDto){
        return recordService.update(recordDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVerificationRecord(@PathVariable ("id") String id){
        return recordService.delete(Long.parseLong(id));
    }
}
