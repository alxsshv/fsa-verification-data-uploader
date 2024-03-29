package main.service.employee;

import main.dto.MiTypeDto;
import main.dto.MiTypeFullDto;
import main.dto.mappers.MiTypeDtoMapper;
import main.model.MiType;
import main.model.MiTypeInstruction;
import main.repository.MiTypeInstructionRepository;
import main.repository.MiTypeModificationRepository;
import main.repository.MiTypeRepository;
import main.service.mi_type.MiTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MiTypeServiceTest {
    private final MiTypeRepository miTypeRepository =
            Mockito.mock(MiTypeRepository.class);
    private final MiTypeInstructionRepository miTypeInstructionRepository =
            Mockito.mock(MiTypeInstructionRepository.class);
    private final MiTypeModificationRepository miTypeModificationRepository =
            Mockito.mock(MiTypeModificationRepository.class);
    private final MiTypeService miTypeService =
            new MiTypeService(miTypeRepository, miTypeInstructionRepository, miTypeModificationRepository);

    @Test
    @DisplayName("Test save if miType already exists")
    public void testSaveIfMiTypeAlreadyExists(){
        int miTypeId = 5;
        MiTypeFullDto miTypeDto = new MiTypeFullDto();
        miTypeDto.setId(miTypeId);
        miTypeDto.setNumber("12345-12");
        miTypeDto.setTitle("Вольтметры");
        miTypeDto.setNotation("В7-78");
        List<String> modifications = new ArrayList<>();
        modifications.add("В7-78/1");
        modifications.add("В7-78/2");
        miTypeDto.setModifications(modifications);
        MiType miType = new MiType();
        miType.setId(miTypeId);
        when(miTypeRepository.findByNumber(miTypeDto.getNumber())).thenReturn(miType);
        ResponseEntity<?> responseEntity = miTypeService.save(miTypeDto);
        assertEquals("422 UNPROCESSABLE_ENTITY", responseEntity.getStatusCode().toString());
        verify(miTypeRepository,times(1)).findByNumber(miTypeDto.getNumber());
        verify(miTypeInstructionRepository,never()).save(any(MiTypeInstruction.class));
    }

    @Test
    @DisplayName("Test save if number is not corrected")
    public void testSaveIfMiTypeNumberIsNotCorrected() {
        int miTypeId = 5;
        MiTypeFullDto miTypeDto = new MiTypeFullDto();
        miTypeDto.setId(miTypeId);
        miTypeDto.setNumber("12345678");
        miTypeDto.setTitle("Вольтметры");
        miTypeDto.setNotation("В7-78");
        List<String> modifications = new ArrayList<>();
        modifications.add("В7-78/1");
        modifications.add("В7-78/2");
        miTypeDto.setModifications(modifications);
        MiType miType = new MiType();
        miType.setId(miTypeId);
        when(miTypeRepository.findByNumber(miTypeDto.getNumber())).thenReturn(miType);
        ResponseEntity<?> responseEntity = miTypeService.save(miTypeDto);
        assertEquals("422 UNPROCESSABLE_ENTITY", responseEntity.getStatusCode().toString());
        verify(miTypeRepository, never()).findByNumber(miTypeDto.getNumber());
        verify(miTypeInstructionRepository, never()).save(any(MiTypeInstruction.class));
    }

    @Test
    @DisplayName("Test save if title is null")
    public void testSaveIfMiTypeTitleIsNull() {
        int miTypeId = 5;
        MiTypeFullDto miTypeDto = new MiTypeFullDto();
        miTypeDto.setId(miTypeId);
        miTypeDto.setNumber("12345-78");
        miTypeDto.setNotation("В7-78");
        List<String> modifications = new ArrayList<>();
        modifications.add("В7-78/1");
        modifications.add("В7-78/2");
        miTypeDto.setModifications(modifications);
        MiType miType = new MiType();
        miType.setId(miTypeId);
        when(miTypeRepository.findByNumber(miTypeDto.getNumber())).thenReturn(miType);
        ResponseEntity<?> responseEntity = miTypeService.save(miTypeDto);
        assertEquals("422 UNPROCESSABLE_ENTITY", responseEntity.getStatusCode().toString());
        verify(miTypeRepository, never()).findByNumber(miTypeDto.getNumber());
        verify(miTypeInstructionRepository, never()).save(any(MiTypeInstruction.class));
    }

    @Test
    @DisplayName("Test save if modification is null")
    public void testSaveIfMiTypeModificationIsNull() {
        int miTypeId = 5;
        MiTypeFullDto miTypeDto = new MiTypeFullDto();
        miTypeDto.setId(miTypeId);
        miTypeDto.setNumber("12345-78");
        miTypeDto.setTitle("Вольтметры");
        miTypeDto.setNotation("В7-78");
        MiType miType = new MiType();
        miType.setId(miTypeId);
        when(miTypeRepository.findByNumber(miTypeDto.getNumber())).thenReturn(miType);
        ResponseEntity<?> responseEntity = miTypeService.save(miTypeDto);
        assertEquals("422 UNPROCESSABLE_ENTITY", responseEntity.getStatusCode().toString());
        verify(miTypeRepository, never()).findByNumber(miTypeDto.getNumber());
        verify(miTypeInstructionRepository, never()).save(any(MiTypeInstruction.class));
    }

    @Test
    @DisplayName("Test save if created new MiType")
    public void testSaveIfCreatedNewMiType(){
        int miTypeId = 5;
        MiTypeFullDto miTypeDto = new MiTypeFullDto();
        miTypeDto.setId(miTypeId);
        miTypeDto.setNumber("12345-12");
        miTypeDto.setTitle("Вольтметры");
        miTypeDto.setNotation("В7-78");
        List<String> modifications = new ArrayList<>();
        modifications.add("В7-78/1");
        modifications.add("В7-78/2");
        miTypeDto.setModifications(modifications);
        when(miTypeRepository.findByNumber(miTypeDto.getNumber())).thenReturn(null);
        ResponseEntity<?> responseEntity = miTypeService.save(miTypeDto);
        assertEquals("200 OK", responseEntity.getStatusCode().toString());
        verify(miTypeRepository,times(1)).findByNumber(miTypeDto.getNumber());
        verify(miTypeInstructionRepository,times(1)).save(any(MiTypeInstruction.class));
    }

    @Test
    @DisplayName("Test update if miType not found")
    public void testUpdateIfMiTypeNotFound(){
        int miTypeId = 5;
        MiTypeFullDto miTypeDto = new MiTypeFullDto();
        miTypeDto.setId(miTypeId);
        miTypeDto.setNumber("12345-12");
        miTypeDto.setTitle("Вольтметры");
        miTypeDto.setNotation("В7-78");
        List<String> modifications = new ArrayList<>();
        modifications.add("В7-78/1");
        modifications.add("В7-78/2");
        miTypeDto.setModifications(modifications);
        when(miTypeInstructionRepository.findById(miTypeId)).thenReturn(Optional.empty());
        ResponseEntity<?> responseEntity = miTypeService.update(miTypeDto);
        assertEquals("404 NOT_FOUND", responseEntity.getStatusCode().toString());
        verify(miTypeInstructionRepository,times(1)).findById(miTypeId);
        verify(miTypeInstructionRepository,never()).save(any(MiTypeInstruction.class));
    }

    @Test
    @DisplayName("Test update if modification is null")
    public void testUpdateIfMiTypeModificationIsNull() {
        int miTypeId = 5;
        MiTypeFullDto miTypeDto = new MiTypeFullDto();
        miTypeDto.setId(miTypeId);
        miTypeDto.setNumber("12345-78");
        miTypeDto.setTitle("Вольтметры");
        miTypeDto.setNotation("В7-78");
        MiTypeInstruction instruction = MiTypeDtoMapper.mapToEntity(miTypeDto);
        when(miTypeInstructionRepository.findById(miTypeId)).thenReturn(Optional.of(instruction));
        ResponseEntity<?> responseEntity = miTypeService.update(miTypeDto);
        assertEquals("422 UNPROCESSABLE_ENTITY", responseEntity.getStatusCode().toString());
        verify(miTypeInstructionRepository, never()).findById(miTypeId);
        verify(miTypeInstructionRepository, never()).save(any(MiTypeInstruction.class));
    }

    @Test
    @DisplayName("Test delete if miType not found")
    public void testDeleteIfMiTypeNotFound() {
        int miTypeId = 5;
        MiType miType = new MiType();
        miType.setId(miTypeId);
        miType.setNumber("12345-78");
        miType.setTitle("Вольтметры");
        miType.setNotation("В7-78");
        when(miTypeRepository.findById(miTypeId)).thenReturn(Optional.empty());
        ResponseEntity<?> responseEntity = miTypeService.delete(miTypeId);
        assertEquals("404 NOT_FOUND", responseEntity.getStatusCode().toString());
        verify(miTypeRepository,times(1)).findById(miTypeId);
        verify(miTypeRepository,never()).delete(any(MiType.class));
    }

    @Test
    @DisplayName("Test delete if miType found")
    public void testDeleteIfMiTypeFound() {
        int miTypeId = 5;
        MiType miType = new MiType();
        miType.setId(miTypeId);
        miType.setNumber("12345-78");
        miType.setTitle("Вольтметры");
        miType.setNotation("В7-78");
        when(miTypeRepository.findById(miTypeId)).thenReturn(Optional.of(miType));
        ResponseEntity<?> responseEntity = miTypeService.delete(miTypeId);
        assertEquals("200 OK", responseEntity.getStatusCode().toString());
        verify(miTypeRepository,times(1)).findById(miTypeId);
        verify(miTypeRepository,times(1)).delete(miType);
    }

    @Test
    @DisplayName("Test findById if miType found")
    public void testFindByIDIfMiTypeFound() {
        int miTypeId = 5;
        MiTypeInstruction instruction = new MiTypeInstruction();
        MiType miType = new MiType();
        miType.setId(miTypeId);
        miType.setNumber("12345-78");
        miType.setTitle("Вольтметры");
        miType.setNotation("В7-78");
        instruction.setMiType(miType);
        when(miTypeInstructionRepository.findById(miTypeId)).thenReturn(Optional.of(instruction));
        ResponseEntity<?> responseEntity = miTypeService.findById(miTypeId);
        assertEquals("200 OK", responseEntity.getStatusCode().toString());
        verify(miTypeInstructionRepository,times(1)).findById(miTypeId);
    }

    @Test
    @DisplayName("Test findById if miType not found")
    public void testFindByIDIfMiTypeNotFound() {
        int miTypeId = 5;
        when(miTypeRepository.findById(miTypeId)).thenReturn(Optional.empty());
        ResponseEntity<?> responseEntity = miTypeService.findById(miTypeId);
        assertEquals("404 NOT_FOUND", responseEntity.getStatusCode().toString());
    }

    @Test
    @DisplayName("Test findBySearchString if searchString is empty")
    public void testFindBySearchStringIfSearcStringIsEmpty() {
        String searchString = "";
        Pageable pageable = PageRequest.of(0,10, Sort.by(Sort.Direction.ASC,"number"));
        when(miTypeRepository
                .findByNumberContainingOrTitleContainingOrNotationContaining(searchString,searchString,searchString, pageable))
                .thenReturn(null);
        ResponseEntity<?> responseEntity = miTypeService.findBySearchString(searchString, pageable);
        assertEquals("400 BAD_REQUEST", responseEntity.getStatusCode().toString());
    }

    @Test
    @DisplayName("Test findBySearchString if searchString is not empty")
    public void testFindBySearchStringIfMiTypeNotFound() {
        int totalPages = 1;
        String searchString = "В7-78";
        List <MiType> findedMiTypes = new ArrayList<>();
        MiType miType = new MiType();
        miType.setId(1);
        miType.setNumber("12345-78");
        miType.setTitle("Вольтметры");
        miType.setNotation("В7-78");
        findedMiTypes.add(miType);
        Pageable pageable = PageRequest.of(0,10, Sort.by(Sort.Direction.ASC,"number"));
        Page<MiType> page = new PageImpl<>(findedMiTypes,pageable,totalPages);
        when(miTypeRepository
                .findByNumberContainingOrTitleContainingOrNotationContaining(searchString,searchString,searchString, pageable))
                .thenReturn(page);
        ResponseEntity<?> responseEntity = miTypeService.findBySearchString(searchString, pageable);
        assertEquals("200 OK", responseEntity.getStatusCode().toString());
        verify(miTypeRepository,times(1))
                .findByNumberContainingOrTitleContainingOrNotationContaining(searchString,searchString,searchString, pageable);
    }

    @Test
    @DisplayName("Test find All with pageable")
    public void testPageableFindAll(){
        int totalPages = 1;
        List <MiType> miTypes = new ArrayList<>();
        MiType miType = new MiType();
        miType.setId(1);
        miType.setNumber("12345-78");
        miType.setTitle("Вольтметры");
        miType.setNotation("В7-78");
        miTypes.add(miType);
        Pageable pageable = PageRequest.of(0,10, Sort.by(Sort.Direction.ASC,"number"));
        Page<MiType> page = new PageImpl<>(miTypes,pageable,totalPages);
        when(miTypeRepository.findAll(pageable)).thenReturn(page);
        Page<MiTypeDto> miTypeDtos = miTypeService.findAll(pageable);
        assertEquals(miTypes.size(),miTypeDtos.getContent().size());
        verify(miTypeRepository,times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Test find All without pageable")
    public void testFindAllWithoutPageable(){
        List <MiType> miTypes = new ArrayList<>();
        MiType miType1 = new MiType();
        miType1.setId(1);
        miType1.setNumber("12345-78");
        miType1.setTitle("Вольтметры");
        miType1.setNotation("В7-78");
        miTypes.add(miType1);
        MiType miType2 = new MiType();
        miType2.setId(2);
        miType2.setNumber("12345-12");
        miType2.setTitle("Штангенциркуль");
        miType2.setNotation("ШЦ");
        miTypes.add(miType2);
        when(miTypeRepository.findAll()).thenReturn(miTypes);
        List<MiTypeDto> miTypeDtos = miTypeService.findAll();
        assertEquals(miTypes.size(),miTypeDtos.size());
        verify(miTypeRepository,times(1)).findAll();
    }






}
