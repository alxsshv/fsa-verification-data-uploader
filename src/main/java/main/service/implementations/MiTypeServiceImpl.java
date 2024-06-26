package main.service.implementations;

import jakarta.persistence.EntityNotFoundException;
import main.dto.rest.MiTypeDto;
import main.dto.rest.MiTypeFullDto;
import main.dto.rest.mappers.MiTypeDtoMapper;
import main.exception.DtoCompositionException;
import main.exception.EntityAlreadyExistException;
import main.exception.ParameterNotValidException;
import main.model.MiType;
import main.model.MiTypeInstruction;
import main.model.MiTypeModification;
import main.repository.MiTypeInstructionRepository;
import main.repository.MiTypeModificationRepository;
import main.repository.MiTypeRepository;
import main.service.Category;
import main.service.ServiceMessage;
import main.service.interfaces.MiTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MiTypeServiceImpl implements MiTypeService {
    public static final Logger log = LoggerFactory.getLogger(MiTypeServiceImpl.class);
    private final MiTypeRepository miTypeRepository;
    private final MiTypeInstructionRepository miTypeInstructionRepository;
    private final MiTypeModificationRepository miTypeModificationRepository;
    private final FileServiceImpl fileService;

    public MiTypeServiceImpl(MiTypeRepository miTypeRepository,
                             MiTypeInstructionRepository miTypeInstructionRepository,
                             MiTypeModificationRepository miTypeModificationRepository,
                             FileServiceImpl fileService) {
        this.miTypeRepository = miTypeRepository;
        this.miTypeInstructionRepository = miTypeInstructionRepository;
        this.miTypeModificationRepository = miTypeModificationRepository;
        this.fileService = fileService;
    }

    @Override
    public ResponseEntity<?> save(MiTypeFullDto miTypeFullDto, MultipartFile[] files,String[] descriptions) throws IOException {
        try {
            checkMiTypeDtoComposition(miTypeFullDto);
            checkExistenceEntity(miTypeFullDto.getNumber());
            MiTypeInstruction miTypeInstruction = MiTypeDtoMapper.mapToEntity(miTypeFullDto);
            MiTypeInstruction savedInstruction = miTypeInstructionRepository.save(miTypeInstruction);
            uploadFilesIfFilesExist(files, descriptions, savedInstruction.getId());
            String okMessage = "Запись о типе СИ № " + miTypeFullDto.getNumber() + " успешно добавлена";
            log.info(okMessage);
            return ResponseEntity.status(201).body(new ServiceMessage(okMessage));
        } catch (DtoCompositionException | EntityAlreadyExistException ex){
            log.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ServiceMessage(ex.getMessage()));
        }
    }

    private void checkMiTypeDtoComposition(MiTypeFullDto dto) throws DtoCompositionException {
        String miTypeNumberTemplate = "[0-9]{3,5}-[0-9]{2}";
        Pattern pattern = Pattern.compile(miTypeNumberTemplate);
        Matcher matcher = pattern.matcher(dto.getNumber());
        if (dto.getNumber() == null || !matcher.find()) {
            throw new DtoCompositionException("Некорректно указан регистрационный номер типа средства измерений" +
                    " в Федеральном информационном фонде по обеспечению единства измерений");
        }
        if (dto.getTitle() == null || dto.getTitle().isEmpty()){
            throw new DtoCompositionException("Пожалуйста заполните наименование типа средства измерений");
        }
        if (dto.getModifications().isEmpty()){
            throw new DtoCompositionException("Пожалуйста укажите не менее одной модификации типа средства измерений," +
                    " если модификации отсутствуют, введите единственную модифкацию," +
                    " соответствующую обозначению типа СИ или фразу \"Модификация отсутствует\"");
        }
    }

    private void checkExistenceEntity(String miTypeNumber) throws EntityAlreadyExistException {
        MiType miTypeFromDb = miTypeRepository.findByNumber(miTypeNumber);
        if (miTypeFromDb != null){
            throw new EntityAlreadyExistException("Запись о типе СИ № " + miTypeNumber + " уже существует");
        }
    }

    private void uploadFilesIfFilesExist(MultipartFile[] files, String[] descriptions, Long categoryId) throws IOException {
        if (files.length > 0) {
            fileService.uploadAllFiles(files, descriptions, Category.MI_TYPE, categoryId);
        }
    }

    @Override
    public ResponseEntity<?> findById(long id) {
        try {
        MiTypeInstruction instruction = getInstructionById(id);
        MiTypeFullDto miTypeFullDto = MiTypeDtoMapper.mapToFullDto(instruction);
        return ResponseEntity.ok(miTypeFullDto);
        } catch (EntityNotFoundException ex){
            log.error(ex.getMessage());
            return ResponseEntity.status(404).body(new ServiceMessage(ex.getMessage()));
        }
    }

    public MiTypeInstruction getInstructionById(long id){
        Optional<MiTypeInstruction> instructionOpt = miTypeInstructionRepository.findById(id);
        if(instructionOpt.isEmpty()){
            throw new EntityNotFoundException("Запись о типе СИ № " + id + " не найдена");
        }
        return instructionOpt.get();
    }


    @Override
    public ResponseEntity<?> findByNumber(String number) {
        MiType type = miTypeRepository.findByNumber(number);
        if (type != null) {
            MiTypeDto miTypeDto = MiTypeDtoMapper.mapToDto(type);
            return ResponseEntity.ok(miTypeDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<?> findBySearchString(String searchString, Pageable pageable) {
        try{
            validateSearchString(searchString);
            Page<MiTypeDto> page =  miTypeRepository
                    .findByNumberContainingOrTitleIgnoreCaseContainingOrNotationIgnoreCaseContaining(searchString.trim(),searchString.trim(),searchString.trim(), pageable)
                    .map(MiTypeDtoMapper::mapToDto);
            return ResponseEntity.ok(page);
        } catch (ParameterNotValidException ex){
            log.info(ex.getMessage());
            return ResponseEntity.status(400).body(new ServiceMessage(ex.getMessage()));
        }

    }
    @Override
    public ResponseEntity<?> findBySearchString(String searchString) {
        try {
            validateSearchString(searchString);
            List<MiTypeDto> miTypeDtos = miTypeRepository
                    .findByNumberContainingOrTitleIgnoreCaseContainingOrNotationIgnoreCaseContaining(searchString.trim(), searchString.trim(), searchString.trim()).stream()
                    .map(MiTypeDtoMapper::mapToDto).toList();
            return ResponseEntity.ok(miTypeDtos);
        } catch (ParameterNotValidException ex){
            log.info(ex.getMessage());
            return ResponseEntity.status(400).body(new ServiceMessage(ex.getMessage()));
        }
    }

    private void validateSearchString(String searchString) throws ParameterNotValidException {
        if (searchString == null || searchString.isEmpty()){
            throw new ParameterNotValidException("Поле для поиска не может быть пустым");
        }
    }

    @Override
    public ResponseEntity<?> findModifications(long miTypeId){
        Optional<MiType> typeOpt = miTypeRepository.findById(miTypeId);
        if (typeOpt.isPresent()) {
            List<MiTypeModification> modifications = miTypeModificationRepository.findByMiType(typeOpt.get());
            List<String> modificationNotations = modifications.stream().map(MiTypeModification::getNotation).toList();
            return ResponseEntity.ok(modificationNotations);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @Override
    public Page<MiTypeDto> findAll(Pageable pageable) {
        return miTypeRepository.findAll(pageable).map(MiTypeDtoMapper ::mapToDto);
    }

    @Override
    public List<MiTypeDto> findAll() {
        return miTypeRepository.findAll().stream().map(MiTypeDtoMapper ::mapToDto).toList();
    }

    @Override
    public ResponseEntity<?> update(MiTypeFullDto miTypeDto){
        try {
            checkMiTypeDtoComposition(miTypeDto);
            MiTypeInstruction instruction = getInstructionById(miTypeDto.getId());
            MiTypeInstruction updateData = MiTypeDtoMapper.mapToEntity(miTypeDto);
            instruction.updateFrom(updateData);
            miTypeInstructionRepository.save(instruction);
            String okMessage = "Cведения о типе СИ " + miTypeDto.getNumber() + " обновлены";
            log.info(okMessage);
            return ResponseEntity.ok(new ServiceMessage(okMessage));
        } catch (DtoCompositionException | EntityNotFoundException ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ServiceMessage(ex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?>delete(long id) throws IOException {
        try {
            MiTypeInstruction instruction = getInstructionById(id);
            fileService.deleteAllFiles(Category.MI_TYPE, id);
            miTypeRepository.deleteById(id);
            String okMessage = "Запись о типе СИ №" + instruction.getMiType().getNumber() + " успешно удалена";
            log.info(okMessage);
            return ResponseEntity.ok(new ServiceMessage(okMessage));
        } catch (EntityNotFoundException ex){
            log.error(ex.getMessage());
            return ResponseEntity.status(404).body(new ServiceMessage(ex.getMessage()));
        }
    }



}
