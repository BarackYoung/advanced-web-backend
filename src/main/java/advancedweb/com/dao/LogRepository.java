package advancedweb.com.dao;

import advancedweb.com.Entity.Log;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LogRepository extends CrudRepository<Log,Long> {
       List<Log> getAllByUsername(String username);
}
