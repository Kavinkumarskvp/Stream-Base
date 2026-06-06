package kavin.personal_project.streambase.repository;

import kavin.personal_project.streambase.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LinkRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByCode(String code);

    @Modifying
    @Query("UPDATE LinkEntity l SET l.clickCount = l.clickCount + :count WHERE l.code = :code")
    void incrementClickCount(@Param("code") String code, @Param("count") Long count);
}
