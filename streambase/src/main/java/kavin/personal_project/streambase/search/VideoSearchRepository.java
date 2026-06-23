package kavin.personal_project.streambase.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoSearchRepository extends ElasticsearchRepository<VideoSearchDocument, Long> {

    @Query("""
            {
                "multi_match": {
                    "query": "?0",
                    "fields": ["title^3", "description"],
                    "fuzziness": "AUTO"
                }
            }
            """)
    Page<VideoSearchDocument> search(String query, Pageable pageable);

    @Query("""
            {
                "match": {
                    "title.autocomplete": "?0"
                }
            }
            """)
    Page<VideoSearchDocument> autocomplete(String prefix, Pageable pageable);
}
