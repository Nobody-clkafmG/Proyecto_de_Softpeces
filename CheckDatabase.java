import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:data/softpeces.db";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("Conexión exitosa a la base de datos.");
            
            // Obtener información de la tabla ESTACION
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "ESTACION", null)) {
                System.out.println("\nColumnas en la tabla ESTACION:");
                System.out.println("-----------------------------");
                System.out.printf("%-20s %-20s %-10s %s%n", "NOMBRE", "TIPO", "TAMAÑO", "NULO");
                System.out.println("-----------------------------");
                
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String columnType = rs.getString("TYPE_NAME");
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    String isNullable = rs.getString("IS_NULLABLE");
                    
                    System.out.printf("%-20s %-20s %-10d %s%n", 
                            columnName, columnType, columnSize, isNullable);
                }
            }
            
            // Verificar si la tabla tiene datos
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM ESTACION")) {
                if (rs.next()) {
                    System.out.println("\nNúmero de registros en ESTACION: " + rs.getInt("count"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos:");
            e.printStackTrace();
        }
    }
}
