package advancedweb.com.dao;

import advancedweb.com.Entity.Answerlogs;
import advancedweb.com.Entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AnsLogsRepository extends CrudRepository<Answerlogs,Long> {
    Answerlogs findByUsername(String username);

    @Transactional
    @Modifying
    @Query(value = "update answerlogs set highest = ?2 where id=?1",nativeQuery = true)
    void updatePoint(int id, int point);

    @Transactional
    @Modifying
    @Query(value = "select * from answerlogs order by highest desc", nativeQuery = true)
    List<Answerlogs> getAllByDESC();
}

