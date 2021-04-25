import org.sql2o.Sql2o;

public class DAOclass {
    public static Sql2o sql2o;

    static{
        sql2o = new Sql2o("jdbc:mysql://localhost:3306/sakila", "User", "TrueLVL010492");
    }
}
