package kavin.personal_project.streambase.controller;

import kavin.personal_project.streambase.search.VideoSearchDocument;
import kavin.personal_project.streambase.search.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final VideoSearchRepository videoSearchRepository;

    @GetMapping
    public Map<String, Object> search(
            @RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {

        long start = Instant.now().toEpochMilli();

        Pageable pageable = PageRequest.of(page, size);
        Page<VideoSearchDocument> results = videoSearchRepository.search(query, pageable);

        return Map.of(
                "query", query,
                "page", page,
                "size", results.getSize(),
                "totalElements", results.getTotalElements(),
                "totalPages", results.getTotalPages(),
                "results", results.getContent(),
                "latency", Instant.now().toEpochMilli() - start
        );
    }

    @GetMapping("/suggest")
    public Map<String, Object> suggest(
            @RequestParam("q") String prefix,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        long start = Instant.now().toEpochMilli();

        Pageable pageable = PageRequest.of(0, size);
        Page<VideoSearchDocument> results = videoSearchRepository.autocomplete(prefix, pageable);

        return Map.of(
                "prefix", prefix,
                "suggestions", results.getContent().stream()
                        .map(VideoSearchDocument::getTitle)
                        .distinct()
                        .toList(),
                "latency", Instant.now().toEpochMilli() - start
        );
    }
}
