package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.imrg.patientapp.entity.JournalEntry;
import ch.uzh.ifi.imrg.patientapp.rest.dto.input.JournalEntryRequestDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.GetAllJournalEntriesDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.JournalEntryOutputDTO;

@Mapper
public interface JournalEntryMapper {

    JournalEntryMapper INSTANCE = Mappers.getMapper(JournalEntryMapper.class);

    JournalEntryOutputDTO convertEntityToJournalEntryOutputDTO(JournalEntry journalEntry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "patient", ignore = true)
    JournalEntry convertJournalEntryRequestDTOToEntity(JournalEntryRequestDTO journalEntryRequestDTO);

    GetAllJournalEntriesDTO convertEntitytoGetAllJournalEntriesDTO(JournalEntry entry);

}
