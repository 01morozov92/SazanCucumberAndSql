import org.junit.Test;
import org.sql2o.Sql2o;

public class SqlMethods {

    public Integer getStudentCount() throws ClassNotFoundException {
        String sql = "SELECT actor_id FROM sakila.actor WHERE last_name = 'WAHLBERG'";
        try (org.sql2o.Connection con = DAOclass.sql2o.open()) {
            return con.createQuery(sql).executeScalar(Integer.class);
        }
    }

    @Test
    public void testDb() throws ClassNotFoundException {
        System.out.println(getStudentCount());
    }

}
