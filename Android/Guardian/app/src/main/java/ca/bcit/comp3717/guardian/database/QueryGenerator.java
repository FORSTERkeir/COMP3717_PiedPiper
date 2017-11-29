package ca.bcit.comp3717.guardian.database;

public class QueryGenerator {

    public static final String SAVED_LOGIN_TABLE = "SavedLogin";

    public static class SelectQuery {
        public static String LastLoggedInUser = "SELECT * FROM " + SAVED_LOGIN_TABLE +
                " WHERE LoginId = ?;";
    }

    public static class CreateQuery {
        public static String SavedLoginTable = "CREATE TABLE IF NOT EXISTS " +
                SAVED_LOGIN_TABLE + " ( " +
                "LoginId INTEGER PRIMARY KEY, " +
                "Email TEXT NOT NULL, " +
                "Password TEXT NOT NULL" +
                ");";
    }

    public static class UpdateQuery {
        public static String SavedLoginWhereClause = "LoginId = ?";
    }
}
