/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.breadmoirai.samurai7.core;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.sql.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class Database {

    private static final String PROTOCOL = "jdbc:derby:";
    private static final String DB_NAME = "SamuraiDatabase";

    private static final Database INSTANCE;

    static {
        try {
            INSTANCE = new Database();
        } catch (SQLException e) {
            printSQLException(e);
            throw new ExceptionInInitializerError("Connection could not be opened");
        }
    }

    public static Database get() {
        return INSTANCE;
    }

    private final Jdbi jdbi;

    private Database() throws SQLException {
        connectElseCreate();
        jdbi = Jdbi.create(PROTOCOL + DB_NAME + ";");
    }


    public <T, R> R withExtension(Class<T> extensionType, Function<T, R> function) {
        return jdbi.withExtension(extensionType, function::apply);
    }

    public <T> void useExtension(Class<T> extensionType, Consumer<T> consumer) {
        jdbi.useExtension(extensionType, consumer::accept);
    }

    public <R> R withHandle(Function<Handle, R> callback) {
        return jdbi.withHandle(callback::apply);
    }

    public void useHandle(Consumer<Handle> callback) {
        jdbi.useHandle(callback::accept);
    }

    public boolean tableExists(String tableName) {
        final String s = tableName.toUpperCase();
        return jdbi.withHandle(handle -> {
            try {
                final DatabaseMetaData metaData = handle.getConnection().getMetaData();
                try (ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
                    while (tables.next()) {
                        if (tables.getString("TABLE_NAME").equals(s)) {
                            return true;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(5);
            }
            return false;
        });
    }

    private void connectElseCreate() throws SQLException {
        boolean created = false;
        Connection initialConnection = null;
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
            final String url = PROTOCOL + DB_NAME + ";";
            initialConnection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            if (e.getErrorCode() == 40000
                    && e.getSQLState().equalsIgnoreCase("XJ004")) {
                initialConnection = DriverManager.getConnection(PROTOCOL + DB_NAME + ";create=true");
                System.out.println("New Database created at ./" + DB_NAME);
            } else {
                printSQLException(e);
            }
        } finally {
            if (initialConnection == null) {
                throw new SQLException("Could not connect nor create SamuraiDerbyDatabase");
            } else {
                initialConnection.close();
            }
        }
    }

    private static void printSQLException(SQLException e) {

        while (e != null) {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).filter(s -> s.contains("samurai")).forEach(System.err::println);
            e = e.getNextException();
        }
    }

}
