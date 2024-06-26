package main.repository;

import lombok.NonNull;
import main.model.MeasurementInstrument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeasurementInstrumentRepository extends JpaRepository <MeasurementInstrument, Long> {
    MeasurementInstrument findByModificationAndSerialNum(String modification, String serialNum);
    Page<MeasurementInstrument> findByModificationIgnoreCaseContainingOrSerialNumIgnoreCaseContainingOrInventoryNumIgnoreCaseContaining(
            String modification,
            String serialNum,
            String inventoryNum,
            Pageable pageable);
    List<MeasurementInstrument> findByModificationIgnoreCaseContainingOrSerialNumIgnoreCaseContainingOrInventoryNumIgnoreCaseContaining(
            String modification,
            String serialNum,
            String inventoryNum);
    @Override
    @NonNull
    Page<MeasurementInstrument> findAll(@NonNull Pageable pageable);
}
