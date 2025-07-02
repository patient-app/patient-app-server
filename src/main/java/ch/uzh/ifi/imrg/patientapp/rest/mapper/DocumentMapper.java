package ch.uzh.ifi.imrg.patientapp.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ch.uzh.ifi.imrg.patientapp.entity.Document.Document;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentDownloadDTO;
import ch.uzh.ifi.imrg.patientapp.rest.dto.output.document.DocumentOverviewDTO;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    DocumentOverviewDTO toOverview(Document doc);

    @Mapping(target = "data", source = "data")
    DocumentDownloadDTO toDownload(Document doc);
}
