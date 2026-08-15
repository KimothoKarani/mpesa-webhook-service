package io.github.kimothokarani.mpesawebhook.repository;

import io.github.kimothokarani.mpesawebhook.domain.RawEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RawEventRepository extends JpaRepository<RawEvent, UUID> {
    // Find raw events that are stuck in "PENDING" processing status
    List<RawEvent> findAllByProcessingStatus(RawEvent.ProcessingStatus processingStatus);

}
