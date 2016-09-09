import java.util.List;

/**
 * Created by gregory on 09/09/16.
 */
public interface IDataAccessor {

    List<DBEntry> readDatabase();
}
